package ru.nikzarch.secretsanta.service

import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.domain.vo.TelegramId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.security.JwtToken

interface UserService {
    fun registerWeb(request: RegisterUserRequest): AuthUserResponse
    fun loginWeb(request: LoginUserRequest): AuthUserResponse
    fun findByUserId(userId: UserId): User?
    fun findByUsername(username: String): User?
    fun registerTelegram(
        telegramId: TelegramId,
        telegramUsername: String?,
    ): User

}

data class RegisterUserRequest(
    val username: String,
    val password: String,
    val telegramId: TelegramId?
)

data class AuthUserResponse(
    val user: User,
    val token: JwtToken
)

data class LoginUserRequest(
    val username: String,
    val password: String
)