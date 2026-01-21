package io.github.darkest.anthropic.model

import io.circe.*
import io.circe.generic.extras.semiauto.*
import sttp.tapir.Schema

enum Role {
  case User, Assistant
}

object Role {
  import io.circe.generic.extras.Configuration
  private given Configuration = Configuration.default.copy(transformConstructorNames = _.toLowerCase)

  given Encoder[Role] = deriveEnumerationEncoder
  given Decoder[Role] = deriveEnumerationDecoder
  given Schema[Role]  = Schema.derivedEnumeration[Role](encode = Some(_.toString.toLowerCase))
}

enum Model(val value: String) {
  case Claude4Opus    extends Model("claude-opus-4-20250514")
  case Claude4Sonnet  extends Model("claude-sonnet-4-20250514")
  case ClaudeOpus45   extends Model("claude-opus-4-5-20251101")
  case Claude37Sonnet extends Model("claude-3-7-sonnet-20250219")
  case Claude35Sonnet extends Model("claude-3-5-sonnet-20241022")
  case Claude35Haiku  extends Model("claude-3-5-haiku-20241022")
  case Claude3Opus    extends Model("claude-3-opus-20240229")
  case Claude3Sonnet  extends Model("claude-3-sonnet-20240229")
  case Claude3Haiku   extends Model("claude-3-haiku-20240307")
}

object Model {
  given Encoder[Model] = Encoder.encodeString.contramap(_.value)
  given Decoder[Model] = Decoder.decodeString.emap { s =>
    Model.values.find(_.value == s).toRight(s"Unknown model: $s")
  }
  given Schema[Model] = Schema.derivedEnumeration[Model](encode = Some(_.value))
}

enum StopReason {
  case EndTurn, MaxTokens, StopSequence, ToolUse
}

object StopReason {
  import io.circe.generic.extras.Configuration

  private def toSnakeCase(s: String): String =
    s.replaceAll("([A-Z])", "_$1").toLowerCase.stripPrefix("_")

  private given Configuration = Configuration.default.copy(transformConstructorNames = toSnakeCase)

  given Encoder[StopReason] = deriveEnumerationEncoder
  given Decoder[StopReason] = deriveEnumerationDecoder
  given Schema[StopReason]  = Schema.derivedEnumeration[StopReason](encode = Some(s => toSnakeCase(s.toString)))
}

final case class Usage(
    inputTokens: Int,
    outputTokens: Int,
    cacheCreationInputTokens: Option[Int] = None,
    cacheReadInputTokens: Option[Int] = None
)

object Usage {
  given Codec[Usage] = deriveConfiguredCodec
  given Schema[Usage] = Schema.derived
}
