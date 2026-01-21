package io.github.darkest.anthropic

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.*

trait AnthropicApiIntegration { self: AnyFlatSpec with Matchers =>

  val apiKey: String = sys.env.get("ANTHROPIC_API_KEY") match {
    case Some(key) => key
    case None      =>
      assume(false, "ANTHROPIC_API_KEY environment variable not set, skipping integration test")
      ""
  }
  val apiVersion: String = Endpoints.ApiVersion
  val baseUri: String    = "https://api.anthropic.com"

  lazy val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

}
