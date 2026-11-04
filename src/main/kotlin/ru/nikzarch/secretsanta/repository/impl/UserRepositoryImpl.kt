package ru.nikzarch.secretsanta.repository.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.domain.vo.TelegramId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.jooq.generated.tables.Users.USERS
import ru.nikzarch.secretsanta.mapping.toDomain
import ru.nikzarch.secretsanta.repository.UserRepository

@Repository
class UserRepositoryImpl(
    private val create: DSLContext,
) : UserRepository {

    override fun save(user: User): User {
        return if (user.id == null) insert(user) else update(user)
    }

    private fun insert(user: User): User {
        val query = create.insertInto(USERS)
            .set(USERS.USERNAME, user.username)
            .apply {
                user.telegramId?.let {
                    set(USERS.TELEGRAM_ID, user.telegramId.value)
                }
            }

        val id = query.returningResult(USERS.ID)
            .fetchSingle().value1()

        return User(UserId(id), user.username, user.telegramId)
    }

    private fun update(user: User): User {
        create.update(USERS)
            .set(USERS.USERNAME, user.username)
            .apply {
                user.telegramId?.let {
                    set(USERS.TELEGRAM_ID, it.value)
                }
            }
            .where(USERS.ID.eq(requireNotNull(user.id).value))
            .execute()
        return user
    }

    override fun findById(id: UserId): User? =
        create.selectFrom(USERS)
            .where(USERS.ID.eq(id.value))
            .fetchOne()?.toDomain()


    override fun findByUsername(username: String): User? =
        create.selectFrom(USERS)
            .where(USERS.USERNAME.eq(username))
            .fetchOne()?.toDomain()

    override fun findByTelegramId(telegramId: TelegramId): User? =
        create.selectFrom(USERS)
            .where(USERS.TELEGRAM_ID.eq(telegramId.value))
            .fetchOne()?.toDomain()

}