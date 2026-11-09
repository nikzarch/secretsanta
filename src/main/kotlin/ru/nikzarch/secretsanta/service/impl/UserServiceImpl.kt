package ru.nikzarch.secretsanta.service.impl

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.domain.vo.TelegramId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.mapping.toDomain
import ru.nikzarch.secretsanta.repository.CredentialsRepository
import ru.nikzarch.secretsanta.repository.UserRepository
import ru.nikzarch.secretsanta.service.JwtService
import ru.nikzarch.secretsanta.service.RegisterUserRequest
import ru.nikzarch.secretsanta.service.AuthUserResponse
import ru.nikzarch.secretsanta.service.LoginUserRequest
import ru.nikzarch.secretsanta.service.UserService

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val credentialsRepository: CredentialsRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) : UserService, UserDetailsService {

    @Transactional
    override fun registerWeb(request: RegisterUserRequest): AuthUserResponse {
        val persistedUser = userRepository.save(request.toDomain())
        val hashedPassword = passwordEncoder.encode(request.password)
        credentialsRepository.setPassword(persistedUser.id!!, hashedPassword!!) // both are guaranteed non-null
        return AuthUserResponse(persistedUser, jwtService.getTokenForUser(persistedUser))
    }

    override fun loginWeb(request: LoginUserRequest): AuthUserResponse {
        val user = userRepository.findByUsername(request.username)
            ?: throw UsernameNotFoundException("Username ${request.username} not found")
        val credentials = credentialsRepository.findByUsername(request.username)
            ?: throw BadCredentialsException("Invalid credentials")
        if (!passwordEncoder.matches(request.password, credentials.password)) {
            throw BadCredentialsException("Invalid credentials")
        }
        return AuthUserResponse(user, jwtService.getTokenForUser(user))
    }

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
            && userRepository.findByUsername(candidate) == null
        ) {
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