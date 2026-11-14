package ru.nikzarch.secretsanta.repository

import ru.nikzarch.secretsanta.domain.SecretSantaEvent
import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId

interface EventRepository {
    fun save(event: SecretSantaEvent): SecretSantaEvent
    fun findById(id: EventId): SecretSantaEvent?
    fun findByIdForUpdate(id: EventId): SecretSantaEvent?
    fun findByOwnerId(ownerId: UserId): List<SecretSantaEvent>
    fun findByParticipantId(userId: UserId): List<SecretSantaEvent>
    fun deleteById(id: EventId): Boolean
    fun countAll(): Int
}
