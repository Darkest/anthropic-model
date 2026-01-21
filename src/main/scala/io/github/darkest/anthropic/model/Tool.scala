package io.github.darkest.anthropic.model

import io.circe.*
import io.circe.syntax.*
import io.circe.generic.extras.semiauto.*
import sttp.tapir.Schema

final case class Tool(
    name: String,
    description: Option[String] = None,
    inputSchema: Json,
    cacheControl: Option[CacheControl] = None
)

object Tool {
  given Codec[Tool] = deriveConfiguredCodec

  given Schema[Tool] = Schema.string
}

enum ToolChoice {
  case Auto
  case Any
  case None
  case Tool(name: String)
}

object ToolChoice {

  import io.circe.generic.extras.Configuration

  private given Configuration = Configuration.default
    .withDiscriminator("type")
    .copy(transformConstructorNames = _.toLowerCase)

  given Codec[ToolChoice] = deriveConfiguredCodec

  given Schema[ToolChoice] = Schema.string
}

final case class CacheControl(
    `type`: String = "ephemeral"
)

object CacheControl {
  given Codec[CacheControl] = deriveConfiguredCodec

  given Schema[CacheControl] = Schema.derived
}

sealed trait BetaTool

object BetaTool {

  import io.circe.generic.extras.Configuration

  final case class Computer(
      displayWidthPx: Int,
      displayHeightPx: Int,
      displayNumber: Option[Int] = None
  ) extends BetaTool

  object Computer {
    private val derived: Codec[Computer] = deriveConfiguredCodec
    given Encoder[Computer] = derived.mapJson(json => Json.obj("name" -> "computer".asJson).deepMerge(json))
    given Decoder[Computer] = derived
  }

  case object TextEditor extends BetaTool {
    given Encoder[TextEditor.type] = Encoder.instance(_ => Json.obj("name" -> "str_replace_editor".asJson))
    given Decoder[TextEditor.type] = Decoder.const(TextEditor)
  }

  case object Bash extends BetaTool {
    given Encoder[Bash.type] = Encoder.instance(_ => Json.obj("name" -> "bash".asJson))
    given Decoder[Bash.type] = Decoder.const(Bash)
  }

  final case class Standard(
      name: String,
      description: Option[String] = None,
      inputSchema: Json,
      cacheControl: Option[CacheControl] = None
  ) extends BetaTool

  given Codec[Standard] = deriveConfiguredCodec(using config = Configuration.default.withSnakeCaseMemberNames)

  private given Configuration = Configuration.default
    .withSnakeCaseMemberNames
    .withDiscriminator("type")
    .copy(transformConstructorNames = {
      case "Computer"   => "computer_20241022"
      case "TextEditor" => "text_editor_20241022"
      case "Bash"       => "bash_20241022"
      case other        => other
    })

  given Codec[BetaTool] = deriveConfiguredCodec

  given Schema[BetaTool] = Schema.string
}
