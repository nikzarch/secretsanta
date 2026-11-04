package ru.nikzarch.secretsanta.repository

import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.domain.vo.TelegramId
import ru.nikzarch.secretsanta.domain.vo.UserId

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UserId): User?
    fun findByUsername(username: String): User?
    fun findByTelegramId(telegramId: TelegramId): User?
}

data class CreateUserRequest(
    val username: String,
    val password: String,
    val telegramId: TelegramId?
)