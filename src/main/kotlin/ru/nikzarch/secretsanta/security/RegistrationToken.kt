package ru.nikzarch.secretsanta.security

import java.time.Instant

data class RegistrationToken(
    val token: String,
    val forUsername: String,
    val expiresAt: Instant
)
