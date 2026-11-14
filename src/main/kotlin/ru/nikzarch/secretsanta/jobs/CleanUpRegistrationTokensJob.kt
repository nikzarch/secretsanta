package ru.nikzarch.secretsanta.jobs

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import ru.nikzarch.secretsanta.repository.RegistrationTokenRepository


@Component
class CleanUpRegistrationTokensJob(
    private val registrationTokenRepository: RegistrationTokenRepository,
) {
    @Scheduled(cron = "0 * * * *")
    fun run() {
        registrationTokenRepository.deleteAllExpiredTokens()
    }
}