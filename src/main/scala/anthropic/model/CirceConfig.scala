package anthropic.model

import io.circe.generic.extras.Configuration

given Configuration = Configuration.default.withSnakeCaseMemberNames
