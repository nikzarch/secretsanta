package ru.nikzarch.secretsanta.domain

import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import java.time.Instant

class Participation(
    val participantId: UserId,
    val eventId: EventId,
    val requestedAt: Instant,
    status: ParticipationStatus = ParticipationStatus.PENDING,
    decidedAt: Instant? = null,
) {
    companion object {
        const val RETRY_DELAY_SECONDS: Long = 4 * 60 * 60 // 4 hours
    }

    var status = status
        private set

    var decidedAt = decidedAt
        private set

    init {
        require((status == ParticipationStatus.PENDING) == (decidedAt == null)) {
            "pending participation must not have a decision date"
        }
    }

    fun accept() {
        check(status == ParticipationStatus.PENDING) { "participation is already decided" }
        status = ParticipationStatus.ACCEPTED
        decidedAt = Instant.now()
    }

    fun reject() {
        check(status == ParticipationStatus.PENDING) { "participation is already decided" }
        status = ParticipationStatus.REJECTED
        decidedAt = Instant.now()
    }

    fun canRetry(now: Instant = Instant.now()): Boolean =
        status == ParticipationStatus.REJECTED &&
                !now.isBefore(requireNotNull(decidedAt).plusSeconds(RETRY_DELAY_SECONDS))
}

enum class ParticipationStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
}
