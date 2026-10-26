package ru.nikzarch.secretsanta.domain

class User(
    val id: Int,
    val username: String,
    val passwordHash: String
)