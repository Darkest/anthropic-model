package io.github.darkest.anthropic

import io.github.darkest.anthropic.model.*
import sttp.tapir.*
import sttp.tapir.json.circe.*
import sttp.tapir.generic.auto.*

object Endpoints {

  val ApiVersion = "2023-06-01"
  val BetaHeader = "anthropic-beta"

  private val baseEndpoint = endpoint
    .in("v1")
    .errorOut(jsonBody[ErrorResponse])

  private val authHeaders = header[String]("x-api-key").and(header[String]("anthropic-version"))

  val messages = baseEndpoint
    .name("messages")
    .description("Create a message using the Messages API")
    .post
    .in("messages")
    .in(authHeaders)
    .in(jsonBody[MessagesRequest])
    .out(jsonBody[MessagesResponse])

  val messagesWithBeta = baseEndpoint
    .name("messagesWithBeta")
    .description("Create a message using the Messages API with beta features")
    .post
    .in("messages")
    .in(authHeaders)
    .in(header[Option[String]](BetaHeader))
    .in(jsonBody[MessagesRequest])
    .out(jsonBody[MessagesResponse])

  val messagesStream = baseEndpoint
    .name("messagesStream")
    .description("Create a streaming message using the Messages API")
    .post
    .in("messages")
    .in(authHeaders)
    .in(jsonBody[MessagesRequest])
    .out(stringBody)

  object TokenCounting {
    final case class TokenCountRequest(
        model: Model,
        messages: List[Message],
        system: Option[SystemPrompt] = None,
        tools: Option[List[Tool]] = None
    )

    object TokenCountRequest {
      import io.circe.*
      import io.circe.syntax.*

      given Encoder[TokenCountRequest] = Encoder.instance { r =>
        Json.obj(
          "model"    -> r.model.asJson,
          "messages" -> r.messages.asJson,
          "system"   -> r.system.asJson,
          "tools"    -> r.tools.asJson
        ).dropNullValues
      }

      given Decoder[TokenCountRequest] = Decoder.instance { c =>
        for {
          model    <- c.downField("model").as[Model]
          messages <- c.downField("messages").as[List[Message]]
          system   <- c.downField("system").as[Option[SystemPrompt]]
          tools    <- c.downField("tools").as[Option[List[Tool]]]
        } yield TokenCountRequest(model, messages, system, tools)
      }

      given Schema[TokenCountRequest] = Schema.string
    }

    final case class TokenCountResponse(
        inputTokens: Int
    )

    object TokenCountResponse {
      import io.circe.*
      given Codec[TokenCountResponse] = Codec.forProduct1("input_tokens")(TokenCountResponse.apply)(_.inputTokens)
      given Schema[TokenCountResponse] = Schema.derived
    }

    val countTokens = baseEndpoint
      .name("countTokens")
      .description("Count tokens for a message")
      .post
      .in("messages" / "count_tokens")
      .in(authHeaders)
      .in(jsonBody[TokenCountRequest])
      .out(jsonBody[TokenCountResponse])
  }

  object MessageBatches {
    final case class BatchRequest(
        customId: String,
        params: MessagesRequest
    )

    object BatchRequest {
      import io.circe.*
      import io.circe.syntax.*

      given Encoder[BatchRequest] = Encoder.instance { r =>
        Json.obj(
          "custom_id" -> r.customId.asJson,
          "params"    -> r.params.asJson
        )
      }

      given Decoder[BatchRequest] = Decoder.instance { c =>
        for {
          customId <- c.downField("custom_id").as[String]
          params   <- c.downField("params").as[MessagesRequest]
        } yield BatchRequest(customId, params)
      }

      given Schema[BatchRequest] = Schema.string
    }

    final case class CreateBatchRequest(
        requests: List[BatchRequest]
    )

    object CreateBatchRequest {
      import io.circe.*
      import io.circe.generic.semiauto.*
      given Codec[CreateBatchRequest] = deriveCodec
      given Schema[CreateBatchRequest] = Schema.derived
    }

    enum BatchStatus {
      case in_progress, canceling, ended
    }

    object BatchStatus {
      import io.circe.*
      import sttp.tapir.Schema

      given Encoder[BatchStatus] = Encoder.encodeString.contramap(_.toString)
      given Decoder[BatchStatus] = Decoder.decodeString.emap { s =>
        BatchStatus.values.find(_.toString == s).toRight(s"Invalid batch status: $s")
      }
      given Schema[BatchStatus] = Schema.derivedEnumeration.defaultStringBased
    }

    final case class RequestCounts(
        processing: Int,
        succeeded: Int,
        errored: Int,
        canceled: Int,
        expired: Int
    )

    object RequestCounts {
      import io.circe.*
      import io.circe.generic.semiauto.*
      given Codec[RequestCounts] = deriveCodec
      given Schema[RequestCounts] = Schema.derived
    }

    final case class BatchResponse(
        id: String,
        `type`: String,
        processingStatus: BatchStatus,
        requestCounts: RequestCounts,
        endedAt: Option[String],
        createdAt: String,
        expiresAt: String,
        archivedAt: Option[String],
        cancelInitiatedAt: Option[String],
        resultsUrl: Option[String]
    )

    object BatchResponse {
      import io.circe.*
      import io.circe.syntax.*

      given Encoder[BatchResponse] = Encoder.instance { r =>
        Json.obj(
          "id"                  -> r.id.asJson,
          "type"                -> r.`type`.asJson,
          "processing_status"   -> r.processingStatus.asJson,
          "request_counts"      -> r.requestCounts.asJson,
          "ended_at"            -> r.endedAt.asJson,
          "created_at"          -> r.createdAt.asJson,
          "expires_at"          -> r.expiresAt.asJson,
          "archived_at"         -> r.archivedAt.asJson,
          "cancel_initiated_at" -> r.cancelInitiatedAt.asJson,
          "results_url"         -> r.resultsUrl.asJson
        ).dropNullValues
      }

      given Decoder[BatchResponse] = Decoder.instance { c =>
        for {
          id                <- c.downField("id").as[String]
          t                 <- c.downField("type").as[String]
          processingStatus  <- c.downField("processing_status").as[BatchStatus]
          requestCounts     <- c.downField("request_counts").as[RequestCounts]
          endedAt           <- c.downField("ended_at").as[Option[String]]
          createdAt         <- c.downField("created_at").as[String]
          expiresAt         <- c.downField("expires_at").as[String]
          archivedAt        <- c.downField("archived_at").as[Option[String]]
          cancelInitiatedAt <- c.downField("cancel_initiated_at").as[Option[String]]
          resultsUrl        <- c.downField("results_url").as[Option[String]]
        } yield BatchResponse(
          id,
          t,
          processingStatus,
          requestCounts,
          endedAt,
          createdAt,
          expiresAt,
          archivedAt,
          cancelInitiatedAt,
          resultsUrl
        )
      }

      given Schema[BatchResponse] = Schema.string
    }

    final case class ListBatchesResponse(
        data: List[BatchResponse],
        hasMore: Boolean,
        firstId: Option[String],
        lastId: Option[String]
    )

    object ListBatchesResponse {
      import io.circe.*
      import io.circe.syntax.*

      given Encoder[ListBatchesResponse] = Encoder.instance { r =>
        Json.obj(
          "data"     -> r.data.asJson,
          "has_more" -> r.hasMore.asJson,
          "first_id" -> r.firstId.asJson,
          "last_id"  -> r.lastId.asJson
        ).dropNullValues
      }

      given Decoder[ListBatchesResponse] = Decoder.instance { c =>
        for {
          data    <- c.downField("data").as[List[BatchResponse]]
          hasMore <- c.downField("has_more").as[Boolean]
          firstId <- c.downField("first_id").as[Option[String]]
          lastId  <- c.downField("last_id").as[Option[String]]
        } yield ListBatchesResponse(data, hasMore, firstId, lastId)
      }

      given Schema[ListBatchesResponse] = Schema.string
    }

    val createBatch = baseEndpoint
      .name("createBatch")
      .description("Create a message batch")
      .post
      .in("messages" / "batches")
      .in(authHeaders)
      .in(jsonBody[CreateBatchRequest])
      .out(jsonBody[BatchResponse])

    val getBatch = baseEndpoint
      .name("getBatch")
      .description("Get a message batch by ID")
      .get
      .in("messages" / "batches" / path[String]("batch_id"))
      .in(authHeaders)
      .out(jsonBody[BatchResponse])

    val listBatches = baseEndpoint
      .name("listBatches")
      .description("List message batches")
      .get
      .in("messages" / "batches")
      .in(authHeaders)
      .in(query[Option[String]]("before_id"))
      .in(query[Option[String]]("after_id"))
      .in(query[Option[Int]]("limit"))
      .out(jsonBody[ListBatchesResponse])

    val cancelBatch = baseEndpoint
      .name("cancelBatch")
      .description("Cancel a message batch")
      .post
      .in("messages" / "batches" / path[String]("batch_id") / "cancel")
      .in(authHeaders)
      .out(jsonBody[BatchResponse])

    val getBatchResults = baseEndpoint
      .name("getBatchResults")
      .description("Get batch results as JSONL")
      .get
      .in("messages" / "batches" / path[String]("batch_id") / "results")
      .in(authHeaders)
      .out(stringBody)
  }

  val all: List[AnyEndpoint] = List(
    messages,
    messagesWithBeta,
    messagesStream,
    TokenCounting.countTokens,
    MessageBatches.createBatch,
    MessageBatches.getBatch,
    MessageBatches.listBatches,
    MessageBatches.cancelBatch,
    MessageBatches.getBatchResults
  )
}
