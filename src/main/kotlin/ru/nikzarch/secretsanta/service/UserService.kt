package ru.nikzarch.secretsanta.service

import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.domain.vo.UserId

interface UserService {
    fun findByUserId(userId: UserId) : User?
    fun findByName(name: String) : User?
}