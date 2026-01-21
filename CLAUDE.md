# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Scala 3 library providing type-safe models for the Anthropic API using Tapir (endpoint definitions) and Circe (JSON serialization).

## Build Commands

```bash
sbt compile          # Compile main sources
sbt test             # Run tests
sbt Test/compile     # Compile test sources
sbt scalafmt         # Format main sources
sbt Test/scalafmt    # Format test sources
sbt scalafmtAll      # Format all sources
sbt publishLocal     # Publish to local Ivy repository
```

Integration tests require `ANTHROPIC_API_KEY` environment variable.

## Architecture

### JSON Serialization Pattern

The Anthropic API uses snake_case. Global configuration in `CirceConfig.scala`:
```scala
given Configuration = Configuration.default.withSnakeCaseMemberNames
```

All models use `deriveConfiguredCodec` from circe-generic-extras.

### Enum Conventions

Scala enums use UpperCamelCase with Configuration-based transformation:
```scala
enum StopReason { case EndTurn, MaxTokens, StopSequence, ToolUse }
// Serializes to: "end_turn", "max_tokens", etc.
```

### ADT Discriminators

Sealed traits with discriminator use local Configuration:
```scala
private given Configuration = Configuration.default
  .withDiscriminator("type")
  .copy(transformConstructorNames = ...)
```

### Constant Fields Pattern

When a field has a fixed value, don't include it in the case class. Add it at encoder level:
```scala
given Encoder[Foo] = derivedCodec.mapJson { json =>
  Json.obj("type" -> "fixed_value".asJson).deepMerge(json)
}
```

## Code Style

- Use brace-based Scala 3 syntax (no significant indentation)
- 120 character max line width
- Align `=` in val definitions and `=>` in case expressions
