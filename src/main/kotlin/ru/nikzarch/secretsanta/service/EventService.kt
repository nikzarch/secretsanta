package ru.nikzarch.secretsanta.service

import ru.nikzarch.secretsanta.domain.Participation
import ru.nikzarch.secretsanta.domain.SecretSantaEvent
import ru.nikzarch.secretsanta.domain.User
import ru.nikzarch.secretsanta.domain.Wishlist
import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import java.time.LocalDate

interface EventService {
    fun createEvent(request: CreateEventRequest): SecretSantaEvent
    fun updateEvent(request: UpdateEventRequest, actorId: UserId): SecretSantaEvent
    fun deleteEvent(id: EventId, actorId: UserId)
    fun deactivateEvent(id: EventId, actorId: UserId): SecretSantaEvent
    fun findById(id: EventId): SecretSantaEvent?
    fun findByOwnerId(userId: UserId): List<SecretSantaEvent>
    fun findByParticipantId(userId: UserId): List<SecretSantaEvent>
    fun isOwnerOfEventById(eventId: EventId, userId: UserId): Boolean

    fun requestParticipation(eventId: EventId, userId: UserId): Participation
    fun acceptParticipation(eventId: EventId, userId: UserId, actorId: UserId): Participation
    fun rejectParticipation(eventId: EventId, userId: UserId, actorId: UserId): Participation
    fun leaveEvent(eventId: EventId, userId: UserId)
    fun findParticipation(eventId: EventId, userId: UserId): Participation?
    fun findParticipations(eventId: EventId, actorId: UserId): List<Participation>

    fun generateSantaAssignments(eventId: EventId, actorId: UserId): SecretSantaEvent
    fun findRecipient(eventId: EventId, gifterId: UserId): GiftRecipient
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
    val ownerId: UserId? = null,
)

data class GiftRecipient(
    val user: User,
    val wishlist: Wishlist?,
)
