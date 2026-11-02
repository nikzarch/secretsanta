package ru.nikzarch.secretsanta.repository

import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.domain.vo.UserId

interface UserRepository {
    fun findById(id: UserId): User?
    fun findByUsername(username: String): User?
}
