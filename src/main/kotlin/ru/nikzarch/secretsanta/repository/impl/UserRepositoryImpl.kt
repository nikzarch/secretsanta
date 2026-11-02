package ru.nikzarch.secretsanta.repository.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.jooq.generated.tables.Users.USERS
import ru.nikzarch.secretsanta.mapping.toDomain
import ru.nikzarch.secretsanta.repository.UserRepository

@Repository
class UserRepositoryImpl(
    private val create: DSLContext,
) : UserRepository {
    override fun findById(id: UserId): User? =
        create.selectFrom(USERS)
            .where(USERS.ID.eq(id.value))
            .fetchOne()?.toDomain()


    override fun findByUsername(username: String): User? =
        create.selectFrom(USERS)
            .where(USERS.USERNAME.eq(username))
            .fetchOne()?.toDomain()

}