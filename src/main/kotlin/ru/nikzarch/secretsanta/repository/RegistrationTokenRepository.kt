package ru.nikzarch.secretsanta.repository

import ru.nikzarch.secretsanta.security.RegistrationToken

interface RegistrationTokenRepository {
    fun insert(registrationToken: RegistrationToken)
    fun findByForUsername(username: String): RegistrationToken?
    fun deleteAllExpiredTokens()
}