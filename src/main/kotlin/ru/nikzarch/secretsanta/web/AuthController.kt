package ru.nikzarch.secretsanta.web

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nikzarch.secretsanta.service.RegisterUserRequest
import ru.nikzarch.secretsanta.service.AuthUserResponse
import ru.nikzarch.secretsanta.service.LoginUserRequest
import ru.nikzarch.secretsanta.service.UserService

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val userService: UserService
) {

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterUserRequestDto): RegisterUserResponseDto {
        return RegisterUserResponseDto.fromServiceResponse(userService.registerWeb(request.toServiceRequest()))
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginUserRequestDto): LoginUserResponseDto {
        return LoginUserResponseDto.fromServiceResponse(userService.loginWeb(request.toServiceRequest()))
    }

}

data class RegisterUserRequestDto(val username: String, val password: String) {
    fun toServiceRequest() = RegisterUserRequest(username, password, null)
}

data class RegisterUserResponseDto(val token: String) {
    companion object {
        fun fromServiceResponse(response: AuthUserResponse) = RegisterUserResponseDto(response.token.token)
    }
}

data class LoginUserRequestDto(val username: String, val password: String) {
    fun toServiceRequest() = LoginUserRequest(username, password)
}

data class LoginUserResponseDto(val token: String) {
    companion object {
        fun fromServiceResponse(response: AuthUserResponse) = LoginUserResponseDto(response.token.token)
    }
}