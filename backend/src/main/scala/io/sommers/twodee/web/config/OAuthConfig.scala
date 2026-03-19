package io.sommers.twodee.web.config

import cats.effect.IO
import ciris.{ConfigError, ConfigValue}

import java.nio.file.Path

case class OAuthConfig(
    google: GoogleOAuthConfig
) {}

case class GoogleOAuthConfig(
    clientId: String
) {}
