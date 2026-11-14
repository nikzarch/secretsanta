package ru.nikzarch.secretsanta.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.service.*

@Tag(name = "Авторизация")
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userService: UserService,
) {
    @Operation(summary = "Регистрация по одноразовому приглашению")
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterUserRequestDto): AuthResponseDto =
        AuthResponseDto.fromDomain(userService.registerWeb(request.toServiceRequest()))

    @Operation(summary = "Вход по логину и паролю")
    @PostMapping("/login")
    fun login(@RequestBody request: LoginUserRequestDto): AuthResponseDto =
        AuthResponseDto.fromDomain(userService.loginWeb(request.toServiceRequest()))

    @PostMapping("/change-password")
    fun changePassword(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: ChangePasswordRequestDto
    ): ResponseEntity<Any> {
        userService.changePassword(request.toServiceRequest(jwt.userId()))
        return ResponseEntity.ok().build()
    }
}

data class RegisterUserRequestDto(val username: String, val password: String, val invitationToken: String) {
    fun toServiceRequest() = RegisterUserRequest(username, password, invitationToken)
}

data class LoginUserRequestDto(val username: String, val password: String) {
    fun toServiceRequest() = LoginUserRequest(username, password)
}

data class ChangePasswordRequestDto(val oldPassword: String, val newPassword: String) {
    fun toServiceRequest(userId: UserId) = ChangePasswordRequest(userId, oldPassword, newPassword)
}

data class AuthResponseDto(val token: String) {
    companion object {
        fun fromDomain(response: AuthUserResponse) = AuthResponseDto(response.token.token)
    }
}


