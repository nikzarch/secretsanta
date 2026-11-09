package ru.nikzarch.secretsanta.domain

import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import java.time.Instant

class Participation(
    val participantId: UserId,
    val eventId: EventId,
    val requestedAt: Instant,
) {
    var decidedAt: Instant? = null
        private set

    companion object {
        const val RETRY_DELAY_SECONDS: Long = 4 * 60 * 60 // 4 hours
    }

    var status = ParticipationStatus.PENDING
        private set

    fun accept() {
        status = ParticipationStatus.ACCEPTED
        decidedAt = Instant.now()
    }

    fun reject() {
        status = ParticipationStatus.REJECTED
        decidedAt = Instant.now()
    }

}

enum class ParticipationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}