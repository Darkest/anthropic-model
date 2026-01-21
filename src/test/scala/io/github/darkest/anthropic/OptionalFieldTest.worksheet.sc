import io.circe.*
import io.circe.syntax.*
import io.circe.generic.semiauto.*

// Simple case class with optional field
final case class Person(
    name: String,
    age: Option[Int] = None,
    email: Option[String] = None
)

object Person {
  given Codec[Person] = deriveCodec
}

// Test with all fields
val personWithAll = Person("Alice", Some(30), Some("alice@example.com"))
println("With all fields:")
println(personWithAll.asJson.spaces2)

// Test with optional fields as None
val personWithNone = Person("Bob")
println("\nWith None fields:")
println(personWithNone.asJson.spaces2)

// Note: By default circe serializes None as null
// To drop null values, use .dropNullValues:
println("\nWith None fields (dropNullValues):")
println(personWithNone.asJson.dropNullValues.spaces2)
