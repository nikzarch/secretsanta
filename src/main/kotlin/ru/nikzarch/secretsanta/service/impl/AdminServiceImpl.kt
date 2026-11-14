package ru.nikzarch.secretsanta.service.impl

import org.springframework.stereotype.Service
import ru.nikzarch.secretsanta.repository.RegistrationTokenRepository
import ru.nikzarch.secretsanta.security.RegistrationToken
import ru.nikzarch.secretsanta.service.AdminService
import ru.nikzarch.secretsanta.service.IssueRegistrationTokenRequest
import java.security.SecureRandom
import java.time.Instant
import java.util.*

@Service
class AdminServiceImpl(
    private val registrationTokenRepository: RegistrationTokenRepository,
) : AdminService {
    private val random = SecureRandom()


    override fun issueRegistrationToken(request: IssueRegistrationTokenRequest): RegistrationToken {

        val randomBytes = ByteArray(32)
        random.nextBytes(randomBytes)
        val token = Base64.getEncoder().encodeToString(randomBytes)
        val registrationToken = RegistrationToken(
            token,
            forUsername = request.forUsername,
            expiresAt = Instant.now().plus(request.ttl)
        )
        registrationTokenRepository.insert(registrationToken)
        return registrationToken
    }

}