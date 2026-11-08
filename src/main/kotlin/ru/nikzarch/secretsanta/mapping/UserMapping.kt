package ru.nikzarch.secretsanta.mapping

import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.domain.vo.TelegramId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.jooq.generated.tables.records.UsersRecord
import ru.nikzarch.secretsanta.repository.Credentials

fun UsersRecord.toDomain() = User(UserId(id), username, telegramId?.let(::TelegramId))

fun UsersRecord.toCredentials() = Credentials(username, password)

fun User.toSpringUser(password: String) =
    org.springframework.security.core.userdetails.User(username, password, emptyList())

fun org.springframework.security.core.userdetails.User.toDomain(id: UserId, telegramId: TelegramId? = null) = User(
    id = id,
    username = username,
    telegramId = telegramId
)