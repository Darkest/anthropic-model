import io.github.darkest.anthropic.model.*
import io.circe.syntax.*
import io.circe.parser.*
import io.circe.Json

// Test Computer encoding
val computer: BetaTool = BetaTool.Computer(displayWidthPx = 1920, displayHeightPx = 1080)
val computerJson = computer.asJson
println("Computer:")
println(computerJson.spaces2)
// Expected: {"name":"computer","display_width_px":1920,"display_height_px":1080,"type":"computer_20241022"}

// Test TextEditor encoding
val editor: BetaTool = BetaTool.TextEditor
val editorJson = editor.asJson
println("\nTextEditor:")
println(editorJson.spaces2)
// Expected: {"name":"str_replace_editor","type":"text_editor_20241022"}

// Test Bash encoding
val bash: BetaTool = BetaTool.Bash
val bashJson = bash.asJson
println("\nBash:")
println(bashJson.spaces2)
// Expected: {"name":"bash","type":"bash_20241022"}

// Test Standard encoding
val standard: BetaTool = BetaTool.Standard(
  name = "get_weather",
  description = Some("Get weather for a location"),
  inputSchema = Json.obj(
    "type" -> Json.fromString("object"),
    "properties" -> Json.obj(
      "location" -> Json.obj("type" -> Json.fromString("string"))
    )
  )
)
val standardJson = standard.asJson
println("\nStandard:")
println(standardJson.spaces2)
// Expected: {"name":"get_weather","description":"Get weather for a location","input_schema":{...}}
// Note: Should NOT have "type" field

// Test decoding
println("\n--- Decoding Tests ---")

val computerDecoded = decode[BetaTool]("""{"name":"computer","display_width_px":1920,"display_height_px":1080,"type":"computer_20241022"}""")
println(s"Computer decoded: $computerDecoded")

val editorDecoded = decode[BetaTool]("""{"name":"str_replace_editor","type":"text_editor_20241022"}""")
println(s"TextEditor decoded: $editorDecoded")

val bashDecoded = decode[BetaTool]("""{"name":"bash","type":"bash_20241022"}""")
println(s"Bash decoded: $bashDecoded")

val standardDecoded = decode[BetaTool]("""{"name":"get_weather","input_schema":{}}""")
println(s"Standard decoded: $standardDecoded")

// Roundtrip tests
println("\n--- Roundtrip Tests ---")

def roundtrip(tool: BetaTool): Boolean = {
  val json = tool.asJson
  val decoded = json.as[BetaTool]
  decoded.contains(tool)
}

println(s"Computer roundtrip: ${roundtrip(computer)}")
println(s"TextEditor roundtrip: ${roundtrip(editor)}")
println(s"Bash roundtrip: ${roundtrip(bash)}")
println(s"Standard roundtrip: ${roundtrip(standard)}")
