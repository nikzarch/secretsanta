package ru.nikzarch.secretsanta.service.impl

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.domain.vo.TelegramId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.repository.CredentialsRepository
import ru.nikzarch.secretsanta.repository.UserRepository
import ru.nikzarch.secretsanta.service.UserService

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val credentialsRepository: CredentialsRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserService, UserDetailsService {
    override fun findByUserId(userId: UserId): User? = userRepository.findById(userId)

    override fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    @Transactional
    override fun registerTelegram(
        telegramId: TelegramId,
        telegramUsername: String?,
    ): User {
        userRepository.findByTelegramId(telegramId)?.let {
            return it
        }

        val username = generateUsername(
            telegramId = telegramId,
            telegramUsername = telegramUsername,
        )

        return userRepository.save(
            User(
                id = null,
                username = username,
                telegramId = telegramId,
            )
        )
    }

    private fun generateUsername(
        telegramId: TelegramId,
        telegramUsername: String?,
    ): String {
        val candidate = telegramUsername
            ?.removePrefix("@")
            ?.trim()
            ?.lowercase()
            ?.takeIf { it.isNotBlank() }

        return if (candidate != null
            && userRepository.findByUsername(candidate) == null)
        {
            candidate
        } else {
            "tg_${telegramId.value}"
        }
    }

    override fun loadUserByUsername(username: String): UserDetails {
        val credentials = credentialsRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Username $username not found")
        return org.springframework.security.core.userdetails.User(
            credentials.username,
            credentials.password,
            emptyList()
        )
    }
}