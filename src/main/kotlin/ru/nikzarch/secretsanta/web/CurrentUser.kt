package ru.nikzarch.secretsanta.web

import org.springframework.security.oauth2.jwt.Jwt
import ru.nikzarch.secretsanta.domain.vo.UserId

fun Jwt.userId(): UserId = UserId(subject.toInt())
