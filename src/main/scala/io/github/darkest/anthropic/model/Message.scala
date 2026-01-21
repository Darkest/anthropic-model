package io.github.darkest.anthropic.model

import io.circe.*
import io.circe.syntax.*
import io.circe.generic.extras.semiauto.*
import sttp.tapir.Schema

final case class Message(
    role: Role,
    content: MessageContent
)

object Message {
  given Codec[Message] = deriveConfiguredCodec
  given Schema[Message] = Schema.string
}

sealed trait MessageContent

object MessageContent {
  case class Text(text: String)                 extends MessageContent
  case class Blocks(blocks: List[ContentBlock]) extends MessageContent

  given Encoder[MessageContent] = Encoder.instance {
    case Text(text)     => text.asJson
    case Blocks(blocks) => blocks.asJson
  }

  given Decoder[MessageContent] = Decoder.instance { c =>
    c.as[String].map(Text.apply).orElse(c.as[List[ContentBlock]].map(Blocks.apply))
  }

  given Schema[MessageContent] = Schema.string
}

sealed trait SystemPrompt

object SystemPrompt {
  case class Text(text: String)                extends SystemPrompt
  case class Blocks(blocks: List[SystemBlock]) extends SystemPrompt

  given Encoder[SystemPrompt] = Encoder.instance {
    case Text(text)     => text.asJson
    case Blocks(blocks) => blocks.asJson
  }

  given Decoder[SystemPrompt] = Decoder.instance { c =>
    c.as[String].map(Text.apply).orElse(c.as[List[SystemBlock]].map(Blocks.apply))
  }

  given Schema[SystemPrompt] = Schema.string
}

final case class SystemBlock(
    text: String,
    cacheControl: Option[CacheControl] = None
)

object SystemBlock {
  private val derivedCodec: Codec[SystemBlock] = deriveConfiguredCodec

  given Encoder[SystemBlock] = derivedCodec.mapJson { json =>
    Json.obj("type" -> "text".asJson).deepMerge(json).dropNullValues
  }
  given Decoder[SystemBlock] = derivedCodec
  given Schema[SystemBlock]  = Schema.derived
}

final case class Metadata(
    userId: Option[String] = None
)

object Metadata {
  given Codec[Metadata] = deriveConfiguredCodec
  given Schema[Metadata] = Schema.derived
}

final case class ThinkingConfig(
    `type`: String,
    budgetTokens: Option[Int] = None
)

object ThinkingConfig {
  val Enabled: ThinkingConfig = ThinkingConfig("enabled", None)
  def enabled(budgetTokens: Int): ThinkingConfig = ThinkingConfig("enabled", Some(budgetTokens))
  val Disabled: ThinkingConfig = ThinkingConfig("disabled", None)

  given Codec[ThinkingConfig] = deriveConfiguredCodec
  given Schema[ThinkingConfig] = Schema.derived
}

final case class MessagesRequest(
    model: Model,
    messages: List[Message],
    maxTokens: Int,
    system: Option[SystemPrompt] = None,
    metadata: Option[Metadata] = None,
    stopSequences: Option[List[String]] = None,
    stream: Option[Boolean] = None,
    temperature: Option[Double] = None,
    topK: Option[Int] = None,
    topP: Option[Double] = None,
    tools: Option[List[Tool]] = None,
    toolChoice: Option[ToolChoice] = None,
    thinking: Option[ThinkingConfig] = None
)

object MessagesRequest {
  given Codec[MessagesRequest] = deriveConfiguredCodec
  given Schema[MessagesRequest] = Schema.string
}

final case class MessagesResponse(
    id: String,
    `type`: String,
    role: Role,
    content: List[ContentBlock],
    model: String,
    stopReason: Option[StopReason],
    stopSequence: Option[String],
    usage: Usage
)

object MessagesResponse {
  given Codec[MessagesResponse] = deriveConfiguredCodec
  given Schema[MessagesResponse] = Schema.derived
}
