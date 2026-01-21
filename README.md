# anthropic-model

Scala 3 models for the [Anthropic API](https://docs.anthropic.com/en/api) using Tapir and Circe.

## Features

- Complete Messages API models (requests, responses, streaming)
- Token counting and message batches endpoints
- Beta tools support (computer use, text editor, bash)
- Extended thinking support
- Type-safe endpoint definitions with Tapir
- JSON serialization

## Usage

```scala
import anthropic.model.*
import anthropic.Endpoints

// Create a request
val request = MessagesRequest(
  model = Model.Claude35Sonnet,
  maxTokens = 1024,
  messages = List(
    Message(Role.User, MessageContent.Text("Hello!"))
  )
)

// Use with any Tapir-compatible HTTP client
```

## API Version

Based on Anthropic API version `2023-06-01`.

## Dependencies

- Scala 3.3.4
- Tapir 1.11.11
- Circe 0.14.10
