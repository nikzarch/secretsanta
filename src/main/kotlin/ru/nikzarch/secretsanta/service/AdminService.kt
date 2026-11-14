package ru.nikzarch.secretsanta.service

import ru.nikzarch.secretsanta.security.RegistrationToken
import java.time.Duration

interface AdminService {
    fun issueRegistrationToken(request: IssueRegistrationTokenRequest): RegistrationToken
}


data class IssueRegistrationTokenRequest(
    val forUsername: String,
    val ttl: Duration
)