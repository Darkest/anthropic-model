package anthropic

import anthropic.model.*
import io.circe.syntax.*
import io.circe.parser.*
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.*
import sttp.client3.circe.*

class MessagesApiSpec extends AnyFlatSpec with Matchers with AnthropicApiIntegration {

  "Messages API" should "create a simple message" in {
    val key = apiKey

    val request = MessagesRequest(
      model = Model.Claude35Haiku,
      maxTokens = 10,
      messages = List(
        Message(Role.User, MessageContent.Text("Say 'hello' and nothing else"))
      )
    )

    val response = basicRequest
      .post(uri"$baseUri/v1/messages")
      .header("x-api-key", key)
      .header("anthropic-version", apiVersion)
      .header("content-type", "application/json")
      .body(request.asJson.noSpaces)
      .response(asJson[MessagesResponse])
      .send(backend)

    response.code.code shouldBe 200
    response.body match {
      case Right(msg) =>
        msg.id should startWith("msg_")
        msg.role shouldBe Role.Assistant
        msg.content should not be empty
        msg.usage.inputTokens should be > 0
        msg.usage.outputTokens should be > 0
      case Left(err) =>
        fail(s"Failed to parse response: $err")
    }
  }

  it should "create a message with system prompt" in {
    val key = apiKey

    val request = MessagesRequest(
      model = Model.Claude35Haiku,
      maxTokens = 10,
      system = Some(SystemPrompt.Text("You are a helpful assistant. Always respond in lowercase.")),
      messages = List(
        Message(Role.User, MessageContent.Text("Say HI"))
      )
    )

    val response = basicRequest
      .post(uri"$baseUri/v1/messages")
      .header("x-api-key", key)
      .header("anthropic-version", apiVersion)
      .header("content-type", "application/json")
      .body(request.asJson.noSpaces)
      .response(asJson[MessagesResponse])
      .send(backend)

    response.code.code shouldBe 200
    response.body.isRight shouldBe true
  }

  it should "handle multi-turn conversation" in {
    val key = apiKey

    val request = MessagesRequest(
      model = Model.Claude35Haiku,
      maxTokens = 10,
      messages = List(
        Message(Role.User, MessageContent.Text("My name is Alice")),
        Message(Role.Assistant, MessageContent.Text("Hello Alice!")),
        Message(Role.User, MessageContent.Text("What is my name?"))
      )
    )

    val response = basicRequest
      .post(uri"$baseUri/v1/messages")
      .header("x-api-key", key)
      .header("anthropic-version", apiVersion)
      .header("content-type", "application/json")
      .body(request.asJson.noSpaces)
      .response(asJson[MessagesResponse])
      .send(backend)

    response.code.code shouldBe 200
    response.body match {
      case Right(msg) =>
        msg.content should not be empty
      case Left(err) =>
        fail(s"Failed to parse response: $err")
    }
  }

  it should "serialize request with snake_case fields" in {
    val request = MessagesRequest(
      model = Model.Claude35Haiku,
      maxTokens = 100,
      topK = Some(10),
      topP = Some(0.9),
      stopSequences = Some(List("END")),
      messages = List(
        Message(Role.User, MessageContent.Text("test"))
      )
    )

    val json = request.asJson.noSpaces

    json should include("max_tokens")
    json should include("top_k")
    json should include("top_p")
    json should include("stop_sequences")
    json should not include ("maxTokens")
    json should not include ("topK")
    json should not include ("topP")
    json should not include ("stopSequences")
  }

  it should "deserialize response with snake_case fields" in {
    val jsonResponse = """{
      "id": "msg_123",
      "type": "message",
      "role": "assistant",
      "content": [{"type": "text", "text": "Hello!"}],
      "model": "claude-3-5-haiku-20241022",
      "stop_reason": "end_turn",
      "stop_sequence": null,
      "usage": {
        "input_tokens": 10,
        "output_tokens": 5
      }
    }"""

    val result = decode[MessagesResponse](jsonResponse)

    result match {
      case Right(msg) =>
        msg.id shouldBe "msg_123"
        msg.stopReason shouldBe Some(StopReason.EndTurn)
        msg.usage.inputTokens shouldBe 10
        msg.usage.outputTokens shouldBe 5
      case Left(err) =>
        fail(s"Failed to decode: $err")
    }
  }
}
