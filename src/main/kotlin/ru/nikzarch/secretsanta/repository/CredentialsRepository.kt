package ru.nikzarch.secretsanta.repository

import ru.nikzarch.secretsanta.domain.vo.UserId


interface CredentialsRepository {
    fun findByUsername(username: String): Credentials?
    fun setPassword(
        userId: UserId,
        passwordHash: String,
    )
}

data class Credentials(
    val username: String, val password: String
)