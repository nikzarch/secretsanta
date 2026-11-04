package ru.nikzarch.secretsanta.service

import ru.nikzarch.secretsanta.domain.SecretSantaEvent
import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import java.time.LocalDate

interface EventService {
    fun createEvent(request: CreateEventRequest): SecretSantaEvent
    fun updateEvent(request: UpdateEventRequest): SecretSantaEvent
    fun deleteEvent(id: EventId)
    fun findById(id: EventId): SecretSantaEvent?
    fun isOwnerOfEventById(eventId: EventId, userId: UserId): Boolean
    fun addParticipantToEvent(eventId: EventId, userId: UserId)
    fun removeParticipantFromEvent(eventId: EventId, userId: UserId)
}

data class CreateEventRequest(
    val name: String,
    val description: String?,
    val date: LocalDate,
    val ownerId: UserId,
)

data class UpdateEventRequest(
    val id: EventId,
    val name: String?,
    val description: String?,
    val date: LocalDate?,
    val ownerId: UserId?,
)