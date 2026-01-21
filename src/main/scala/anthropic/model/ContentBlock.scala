package anthropic.model

import io.circe.*
import io.circe.syntax.*
import sttp.tapir.Schema

enum ImageMediaType(val value: String) {
  case Jpeg extends ImageMediaType("image/jpeg")
  case Png  extends ImageMediaType("image/png")
  case Gif  extends ImageMediaType("image/gif")
  case Webp extends ImageMediaType("image/webp")
}

object ImageMediaType {
  given Encoder[ImageMediaType] = Encoder.encodeString.contramap(_.value)
  given Decoder[ImageMediaType] = Decoder.decodeString.emap { s =>
    ImageMediaType.values.find(_.value == s).toRight(s"Invalid media type: $s")
  }
  given Schema[ImageMediaType] = Schema.derivedEnumeration[ImageMediaType](encode = Some(_.value))
}

sealed trait ImageSource

object ImageSource {
  final case class Base64(
      mediaType: ImageMediaType,
      data: String
  ) extends ImageSource

  final case class Url(
      url: String
  ) extends ImageSource

  given Encoder[Base64] = Encoder.instance { s =>
    Json.obj(
      "type"       -> "base64".asJson,
      "media_type" -> s.mediaType.asJson,
      "data"       -> s.data.asJson
    )
  }

  given Decoder[Base64] = Decoder.instance { c =>
    for {
      mediaType <- c.downField("media_type").as[ImageMediaType]
      data      <- c.downField("data").as[String]
    } yield Base64(mediaType, data)
  }

  given Encoder[Url] = Encoder.instance { s =>
    Json.obj(
      "type" -> "url".asJson,
      "url"  -> s.url.asJson
    )
  }

  given Decoder[Url] = Decoder.instance { c => c.downField("url").as[String].map(Url.apply) }

  given Encoder[ImageSource] = Encoder.instance {
    case b: Base64 => b.asJson
    case u: Url    => u.asJson
  }

  given Decoder[ImageSource] = Decoder.instance { c =>
    c.downField("type").as[String].flatMap {
      case "base64" => c.as[Base64]
      case "url"    => c.as[Url]
      case other    => Left(DecodingFailure(s"Unknown image source type: $other", c.history))
    }
  }

  given Schema[ImageSource] = Schema.string
}

sealed trait ContentBlock

object ContentBlock {
  final case class Text(text: String) extends ContentBlock

  final case class Image(source: ImageSource) extends ContentBlock

  final case class ToolUse(
      id: String,
      name: String,
      input: Json
  ) extends ContentBlock

  final case class ToolResult(
      toolUseId: String,
      content: Option[String] = None,
      isError: Option[Boolean] = None
  ) extends ContentBlock

  final case class Document(
      source: DocumentSource,
      title: Option[String] = None,
      context: Option[String] = None
  ) extends ContentBlock

  final case class Thinking(
      thinking: String,
      signature: String
  ) extends ContentBlock

  final case class RedactedThinking(
      data: String
  ) extends ContentBlock

  given Encoder[ContentBlock] = Encoder.instance {
    case Text(text) =>
      Json.obj("type" -> "text".asJson, "text" -> text.asJson)

    case Image(source) =>
      Json.obj("type" -> "image".asJson, "source" -> source.asJson)

    case ToolUse(id, name, input) =>
      Json.obj(
        "type"  -> "tool_use".asJson,
        "id"    -> id.asJson,
        "name"  -> name.asJson,
        "input" -> input
      )

    case ToolResult(toolUseId, content, isError) =>
      Json.obj(
        "type"        -> "tool_result".asJson,
        "tool_use_id" -> toolUseId.asJson,
        "content"     -> content.asJson,
        "is_error"    -> isError.asJson
      ).dropNullValues

    case Document(source, title, context) =>
      Json.obj(
        "type"    -> "document".asJson,
        "source"  -> source.asJson,
        "title"   -> title.asJson,
        "context" -> context.asJson
      ).dropNullValues

    case Thinking(thinking, signature) =>
      Json.obj(
        "type"      -> "thinking".asJson,
        "thinking"  -> thinking.asJson,
        "signature" -> signature.asJson
      )

    case RedactedThinking(data) =>
      Json.obj(
        "type" -> "redacted_thinking".asJson,
        "data" -> data.asJson
      )
  }

  given Decoder[ContentBlock] = Decoder.instance { c =>
    c.downField("type").as[String].flatMap {
      case "text" =>
        c.downField("text").as[String].map(Text.apply)

      case "image" =>
        c.downField("source").as[ImageSource].map(Image.apply)

      case "tool_use" =>
        for {
          id    <- c.downField("id").as[String]
          name  <- c.downField("name").as[String]
          input <- c.downField("input").as[Json]
        } yield ToolUse(id, name, input)

      case "tool_result" =>
        for {
          toolUseId <- c.downField("tool_use_id").as[String]
          content   <- c.downField("content").as[Option[String]]
          isError   <- c.downField("is_error").as[Option[Boolean]]
        } yield ToolResult(toolUseId, content, isError)

      case "document" =>
        for {
          source  <- c.downField("source").as[DocumentSource]
          title   <- c.downField("title").as[Option[String]]
          context <- c.downField("context").as[Option[String]]
        } yield Document(source, title, context)

      case "thinking" =>
        for {
          thinking  <- c.downField("thinking").as[String]
          signature <- c.downField("signature").as[String]
        } yield Thinking(thinking, signature)

      case "redacted_thinking" =>
        c.downField("data").as[String].map(RedactedThinking.apply)

      case other =>
        Left(DecodingFailure(s"Unknown content block type: $other", c.history))
    }
  }

  given Schema[ContentBlock] = Schema.string
}

sealed trait DocumentSource

object DocumentSource {
  final case class Base64(
      mediaType: String,
      data: String
  ) extends DocumentSource

  final case class Url(
      url: String
  ) extends DocumentSource

  final case class PlainText(
      text: String
  ) extends DocumentSource

  final case class ContentReference(
      id: String
  ) extends DocumentSource

  given Encoder[DocumentSource] = Encoder.instance {
    case Base64(mediaType, data) =>
      Json.obj(
        "type"       -> "base64".asJson,
        "media_type" -> mediaType.asJson,
        "data"       -> data.asJson
      )
    case Url(url) =>
      Json.obj(
        "type" -> "url".asJson,
        "url"  -> url.asJson
      )
    case PlainText(text) =>
      Json.obj(
        "type" -> "text".asJson,
        "text" -> text.asJson
      )
    case ContentReference(id) =>
      Json.obj(
        "type" -> "content".asJson,
        "id"   -> id.asJson
      )
  }

  given Decoder[DocumentSource] = Decoder.instance { c =>
    c.downField("type").as[String].flatMap {
      case "base64" =>
        for {
          mediaType <- c.downField("media_type").as[String]
          data      <- c.downField("data").as[String]
        } yield Base64(mediaType, data)
      case "url" =>
        c.downField("url").as[String].map(Url.apply)
      case "text" =>
        c.downField("text").as[String].map(PlainText.apply)
      case "content" =>
        c.downField("id").as[String].map(ContentReference.apply)
      case other =>
        Left(DecodingFailure(s"Unknown document source type: $other", c.history))
    }
  }

  given Schema[DocumentSource] = Schema.string
}
