package io.github.darkest.anthropic.model

import io.circe.*
import io.circe.syntax.*
import io.circe.generic.extras.semiauto.*
import sttp.tapir.Schema

sealed trait StreamEvent

object StreamEvent {
  final case class MessageStart(message: MessagesResponse)                   extends StreamEvent
  final case class ContentBlockStart(index: Int, contentBlock: ContentBlock) extends StreamEvent
  final case class ContentBlockDelta(index: Int, delta: ContentDelta)        extends StreamEvent
  final case class ContentBlockStop(index: Int)                              extends StreamEvent
  final case class MessageDelta(delta: MessageDeltaData, usage: DeltaUsage)  extends StreamEvent
  final case class MessageStop()                                             extends StreamEvent
  final case class Ping()                                                    extends StreamEvent
  final case class Error(error: ErrorDetail)                                 extends StreamEvent

  given Encoder[StreamEvent] = Encoder.instance {
    case MessageStart(message) =>
      Json.obj("type" -> "message_start".asJson, "message" -> message.asJson)
    case ContentBlockStart(index, contentBlock) =>
      Json.obj("type" -> "content_block_start".asJson, "index" -> index.asJson, "content_block" -> contentBlock.asJson)
    case ContentBlockDelta(index, delta) =>
      Json.obj("type" -> "content_block_delta".asJson, "index" -> index.asJson, "delta" -> delta.asJson)
    case ContentBlockStop(index) =>
      Json.obj("type" -> "content_block_stop".asJson, "index" -> index.asJson)
    case MessageDelta(delta, usage) =>
      Json.obj("type" -> "message_delta".asJson, "delta" -> delta.asJson, "usage" -> usage.asJson)
    case MessageStop() =>
      Json.obj("type" -> "message_stop".asJson)
    case Ping() =>
      Json.obj("type" -> "ping".asJson)
    case Error(error) =>
      Json.obj("type" -> "error".asJson, "error" -> error.asJson)
  }

  given Decoder[StreamEvent] = Decoder.instance { c =>
    c.downField("type").as[String].flatMap {
      case "message_start"       => c.downField("message").as[MessagesResponse].map(MessageStart.apply)
      case "content_block_start" =>
        for {
          index        <- c.downField("index").as[Int]
          contentBlock <- c.downField("content_block").as[ContentBlock]
        } yield ContentBlockStart(index, contentBlock)
      case "content_block_delta" =>
        for {
          index <- c.downField("index").as[Int]
          delta <- c.downField("delta").as[ContentDelta]
        } yield ContentBlockDelta(index, delta)
      case "content_block_stop" =>
        c.downField("index").as[Int].map(ContentBlockStop.apply)
      case "message_delta" =>
        for {
          delta <- c.downField("delta").as[MessageDeltaData]
          usage <- c.downField("usage").as[DeltaUsage]
        } yield MessageDelta(delta, usage)
      case "message_stop" => Right(MessageStop())
      case "ping"         => Right(Ping())
      case "error"        => c.downField("error").as[ErrorDetail].map(Error.apply)
      case other          => Left(DecodingFailure(s"Unknown stream event type: $other", c.history))
    }
  }

  given Schema[StreamEvent] = Schema.string
}

sealed trait ContentDelta

object ContentDelta {
  final case class TextDelta(text: String)             extends ContentDelta
  final case class InputJsonDelta(partialJson: String) extends ContentDelta
  final case class ThinkingDelta(thinking: String)     extends ContentDelta
  final case class SignatureDelta(signature: String)   extends ContentDelta

  given Encoder[ContentDelta] = Encoder.instance {
    case TextDelta(text) =>
      Json.obj("type" -> "text_delta".asJson, "text" -> text.asJson)
    case InputJsonDelta(partialJson) =>
      Json.obj("type" -> "input_json_delta".asJson, "partial_json" -> partialJson.asJson)
    case ThinkingDelta(thinking) =>
      Json.obj("type" -> "thinking_delta".asJson, "thinking" -> thinking.asJson)
    case SignatureDelta(signature) =>
      Json.obj("type" -> "signature_delta".asJson, "signature" -> signature.asJson)
  }

  given Decoder[ContentDelta] = Decoder.instance { c =>
    c.downField("type").as[String].flatMap {
      case "text_delta"       => c.downField("text").as[String].map(TextDelta.apply)
      case "input_json_delta" => c.downField("partial_json").as[String].map(InputJsonDelta.apply)
      case "thinking_delta"   => c.downField("thinking").as[String].map(ThinkingDelta.apply)
      case "signature_delta"  => c.downField("signature").as[String].map(SignatureDelta.apply)
      case other              => Left(DecodingFailure(s"Unknown content delta type: $other", c.history))
    }
  }

  given Schema[ContentDelta] = Schema.string
}

final case class MessageDeltaData(
    stopReason: Option[StopReason],
    stopSequence: Option[String]
)

object MessageDeltaData {
  given Codec[MessageDeltaData] = deriveConfiguredCodec
  given Schema[MessageDeltaData] = Schema.derived
}

final case class DeltaUsage(
    outputTokens: Int
)

object DeltaUsage {
  given Codec[DeltaUsage] = deriveConfiguredCodec
  given Schema[DeltaUsage] = Schema.derived
}
