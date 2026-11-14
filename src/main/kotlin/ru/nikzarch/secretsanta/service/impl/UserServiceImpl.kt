package ru.nikzarch.secretsanta.service.impl

import mu.KotlinLogging
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
import ru.nikzarch.secretsanta.repository.RegistrationTokenRepository
import ru.nikzarch.secretsanta.repository.UserRepository
import ru.nikzarch.secretsanta.service.*
import ru.nikzarch.secretsanta.service.exception.NotFoundException
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val credentialsRepository: CredentialsRepository,
    private val registrationTokenRepository: RegistrationTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) : UserService, UserDetailsService {

    @Transactional
    override fun registerWeb(request: RegisterUserRequest): AuthUserResponse {
        val persistedUser = userRepository.save(request.toDomain())
        credentialsRepository.setPassword(
            requireNotNull(persistedUser.id),
            passwordEncoder.encode(request.password)!!,
        )
        logger.info { "User ${requireNotNull(persistedUser.id).value} registered" }
        return AuthUserResponse(persistedUser, jwtService.getTokenForUser(persistedUser))
    }

    @Transactional
    override fun loginWeb(request: LoginUserRequest): AuthUserResponse {
        val user = userRepository.findByUsername(request.username)

        return when {
            user != null -> loginExistingUser(user, request.password)
            loginByToken(request.username, request.password) -> registerUser(request)
            else -> {
                logger.warn { "Failed login for ${request.username}" }
                throw BadCredentialsException("Invalid credentials")
            }
        }
    }

    private fun loginExistingUser(
        user: User,
        password: String
    ): AuthUserResponse {
        val credentials = credentialsRepository.findByUsername(user.username)
            ?: throw BadCredentialsException("Invalid credentials")

        if (!passwordEncoder.matches(password, credentials.password)) {
            throw BadCredentialsException("Invalid credentials")
        }

        logger.info { "User ${requireNotNull(user.id).value} logged in" }

        return AuthUserResponse(user, jwtService.getTokenForUser(user))
    }

    private fun registerUser(request: LoginUserRequest): AuthUserResponse {
        val persistedUser = userRepository.save(request.toDomain())
        val userId = requireNotNull(persistedUser.id)

        credentialsRepository.setPassword(
            userId,
            passwordEncoder.encode(request.password)!!
        )

        logger.info { "User ${userId.value} registered" }

        return AuthUserResponse(
            persistedUser,
            jwtService.getTokenForUser(persistedUser)
        )
    }

    private fun loginByToken(username: String, password: String): Boolean {
        val token = registrationTokenRepository.findByForUsername(username)
            ?: return false

        return token.token == password &&
                token.expiresAt.isAfter(Instant.now())
    }

    override fun findByUserId(userId: UserId): User? = userRepository.findById(userId)

    override fun findByUsername(username: String): User? = userRepository.findByUsername(username)

    @Transactional
    override fun registerTelegram(telegramId: TelegramId, telegramUsername: String?): User {
        userRepository.findByTelegramId(telegramId)?.let { return it }
        val username = generateUsername(telegramId, telegramUsername)
        val user = userRepository.save(User(username = username, telegramId = telegramId))
        logger.info { "User ${requireNotNull(user.id).value} registered through Telegram" }
        return user
    }

    override fun changePassword(request: ChangePasswordRequest) {
        val (userId, oldPassword, newPassword) = request
        val credentials = credentialsRepository.findById(userId)
            ?: throw NotFoundException("User with id $userId not found")
        require(oldPassword == credentials.password && oldPassword != newPassword) { "Old password should match current password and doesnt match new one" }
        credentialsRepository.setPassword(userId, newPassword)
    }

    private fun generateUsername(telegramId: TelegramId, telegramUsername: String?): String {
        val candidate = telegramUsername?.removePrefix("@")?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        return if (candidate != null && userRepository.findByUsername(candidate) == null) {
            candidate
        } else {
            "tg_${telegramId.value}"
        }
    }

    override fun loadUserByUsername(username: String): UserDetails {
        val credentials = credentialsRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("Username $username not found")
        val password = credentials.password
        return org.springframework.security.core.userdetails.User(credentials.username, password, emptyList())
    }
}
