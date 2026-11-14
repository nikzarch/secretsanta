package ru.nikzarch.secretsanta.web

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nikzarch.secretsanta.security.RegistrationToken
import ru.nikzarch.secretsanta.service.AdminService
import ru.nikzarch.secretsanta.service.IssueRegistrationTokenRequest
import java.time.Duration

@RestController
@RequestMapping("/admin-api/v1")
class AdminController(
    private val adminService: AdminService,
) {

    @PostMapping("/token")
    fun issueToken(@RequestBody request: IssueTokenRequestDto): IssueRegistrationTokenResponse {
        return IssueRegistrationTokenResponse.fromDomain(
            adminService.issueRegistrationToken(request.toServiceRequest())
        )
    }

}


data class IssueTokenRequestDto(
    val forUsername: String,
    val ttl: Duration = Duration.ofDays(1),
) {
    fun toServiceRequest() = IssueRegistrationTokenRequest(forUsername, ttl)
}

data class IssueRegistrationTokenResponse(
    val token: String,
) {
    companion object {
        fun fromDomain(registrationToken: RegistrationToken) = IssueRegistrationTokenResponse(registrationToken.token)
    }
}