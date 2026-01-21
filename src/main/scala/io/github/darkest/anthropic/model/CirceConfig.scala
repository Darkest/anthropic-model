package io.github.darkest.anthropic.model

import io.circe.generic.extras.Configuration

given Configuration = Configuration.default.withSnakeCaseMemberNames
