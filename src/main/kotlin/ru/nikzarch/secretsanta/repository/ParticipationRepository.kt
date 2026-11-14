package ru.nikzarch.secretsanta.repository

import ru.nikzarch.secretsanta.domain.Participation
import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId

interface ParticipationRepository {
    fun save(participation: Participation): Participation
    fun findByEventAndUser(eventId: EventId, userId: UserId): Participation?
    fun findByEventId(eventId: EventId): List<Participation>
    fun deleteByEventAndUser(eventId: EventId, userId: UserId): Boolean
}
