package ru.nikzarch.secretsanta.web

import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.service.UserService
import ru.nikzarch.secretsanta.service.exception.NotFoundException

@Tag(name = "Пользователи")
@RestController
@RequestMapping("/api/v1/me")
class UserController(private val userService: UserService) {
    @GetMapping
    fun me(@AuthenticationPrincipal jwt: Jwt): UserResponseDto {
        val user = userService.findByUserId(jwt.userId()) ?: throw NotFoundException("user not found")
        return UserResponseDto.fromDomain(user)
    }
}

data class UserResponseDto(val id: Int, val username: String, val telegramId: Long?) {
    companion object {
        fun fromDomain(user: User) = UserResponseDto(
            id = requireNotNull(user.id).value,
            username = user.username,
            telegramId = user.telegramId?.value,
        )
    }
}
