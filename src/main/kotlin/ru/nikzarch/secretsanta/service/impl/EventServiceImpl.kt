package ru.nikzarch.secretsanta.service.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Service
import ru.nikzarch.secretsanta.domain.SecretSantaEvent
import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.mapping.toDomain
import ru.nikzarch.secretsanta.repository.EventRepository
import ru.nikzarch.secretsanta.service.CreateEventRequest
import ru.nikzarch.secretsanta.service.EventService
import ru.nikzarch.secretsanta.service.UpdateEventRequest

@Service
class EventServiceImpl(
    val eventRepository: EventRepository,
) : EventService {
    override fun createEvent(request: CreateEventRequest): SecretSantaEvent {
        return eventRepository.save(
            SecretSantaEvent(
                name = request.name,
                description = request.description,
                date = request.date,
                ownerId = request.ownerId
            )
        )
    }

    override fun updateEvent(request: UpdateEventRequest): SecretSantaEvent {
        val event = getEventById(request.id)
        val toUpdate = request.toDomain(event)
        return eventRepository.save(request.toDomain(event))
    }

    override fun deleteEvent(id: EventId) {
        if (!eventRepository.deleteById(id)) {
            // TODO: LOGGING
        }
    }

    override fun findById(id: EventId): SecretSantaEvent? {
        return eventRepository.findById(id)
    }

    override fun isOwnerOfEventById(
        eventId: EventId,
        userId: UserId
    ): Boolean {
        val event = getEventById(eventId)
        return event.ownerId.value == userId.value
    }

    override fun addParticipantToEvent(
        eventId: EventId,
        userId: UserId
    ) {
        val event = getEventById(eventId)
        event.addParticipant(userId)
        eventRepository.save(event)
    }

    override fun removeParticipantFromEvent(
        eventId: EventId,
        userId: UserId
    ) {
        val event = getEventById(eventId)
        event.removeParticipant(userId)
        eventRepository.save(event)
    }

    private fun getEventById(id: EventId) =
        eventRepository.findById(id) ?: throw IllegalArgumentException("event not found")
}