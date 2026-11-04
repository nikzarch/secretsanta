package ru.nikzarch.secretsanta.repository.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.jooq.generated.tables.Users.USERS
import ru.nikzarch.secretsanta.mapping.toCredentials
import ru.nikzarch.secretsanta.repository.Credentials
import ru.nikzarch.secretsanta.repository.CredentialsRepository

@Repository
class CredentialsRepositoryImpl(
    private val create: DSLContext
) : CredentialsRepository {
    override fun findByUsername(username: String): Credentials? {
        return create.selectFrom(USERS)
            .where(USERS.USERNAME.eq(username))
            .fetchOne()?.toCredentials()
    }

    override fun setPassword(
        userId: UserId,
        passwordHash: String,
    ) {
        val updated = create.update(USERS)
            .set(USERS.PASSWORD, passwordHash)
            .where(USERS.ID.eq(userId.value))
            .execute()

        require(updated == 1) {
            "User with id ${userId.value} not found"
        }
    }
}

