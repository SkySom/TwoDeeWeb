package io.sommers.twodee.web.config

case class AuthConfig(
    secretKey: String,
    google: GoogleOAuthConfig
) {}

case class GoogleOAuthConfig(
    clientId: String
) {}
