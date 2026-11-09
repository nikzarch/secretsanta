package ru.nikzarch.secretsanta.service

import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.security.JwtToken

interface JwtService {
    fun getTokenForUser(user: User): JwtToken
}
