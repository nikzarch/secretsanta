package ru.nikzarch.secretsanta.repository.impl

import org.jooq.Comparator
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import ru.nikzarch.secretsanta.jooq.generated.tables.RegistrationTokens.REGISTRATION_TOKENS
import ru.nikzarch.secretsanta.repository.RegistrationTokenRepository
import ru.nikzarch.secretsanta.security.RegistrationToken
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class RegistrationTokenRepositoryImpl(
    private val create: DSLContext
) : RegistrationTokenRepository {
    override fun insert(registrationToken: RegistrationToken) {
        create.insertInto(REGISTRATION_TOKENS)
            .set(REGISTRATION_TOKENS.TOKEN, registrationToken.token)
            .set(REGISTRATION_TOKENS.FOR_USERNAME, registrationToken.forUsername)
            .set(REGISTRATION_TOKENS.EXPIRES_AT, registrationToken.expiresAt.atOffset(ZoneOffset.UTC))
            .execute()
    }

    override fun findByForUsername(username: String): RegistrationToken? =
        create.selectFrom(REGISTRATION_TOKENS)
            .where(REGISTRATION_TOKENS.FOR_USERNAME.eq(username))
            .fetch { RegistrationToken(it.token, it.forUsername, it.expiresAt.toInstant()) }
            .firstOrNull()

    override fun deleteAllExpiredTokens() {
        create.deleteFrom(REGISTRATION_TOKENS)
            .where(
                REGISTRATION_TOKENS.EXPIRES_AT.compare(
                    Comparator.GREATER,
                    OffsetDateTime.now()
                )
            )
            .execute()
    }

}