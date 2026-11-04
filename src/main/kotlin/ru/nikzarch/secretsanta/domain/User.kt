package ru.nikzarch.secretsanta.domain

import ru.nikzarch.secretsanta.domain.vo.TelegramId
import ru.nikzarch.secretsanta.domain.vo.UserId

class User(
    val id: UserId? = null,
    val username: String,
    val telegramId: TelegramId? = null,
)