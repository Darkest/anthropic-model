package io.github.darkest.anthropic.model

import io.circe.*
import io.circe.generic.semiauto.*
import sttp.tapir.Schema

enum ErrorType {
  case invalid_request_error
  case authentication_error
  case permission_error
  case not_found_error
  case request_too_large
  case rate_limit_error
  case api_error
  case overloaded_error
}

object ErrorType {
  given Encoder[ErrorType] = Encoder.encodeString.contramap(_.toString)
  given Decoder[ErrorType] = Decoder.decodeString.emap { s =>
    ErrorType.values.find(_.toString == s).toRight(s"Invalid error type: $s")
  }
  given Schema[ErrorType] = Schema.derivedEnumeration.defaultStringBased
}

final case class ErrorDetail(
    `type`: ErrorType,
    message: String
)

object ErrorDetail {
  given Codec[ErrorDetail] = deriveCodec
  given Schema[ErrorDetail] = Schema.derived
}

final case class ErrorResponse(
    `type`: String,
    error: ErrorDetail
)

object ErrorResponse {
  given Codec[ErrorResponse] = deriveCodec
  given Schema[ErrorResponse] = Schema.derived
}
