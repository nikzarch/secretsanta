package ru.nikzarch.secretsanta.mapping

import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.jooq.generated.tables.records.UsersRecord

fun UsersRecord.toDomain() = User(UserId(id), username)