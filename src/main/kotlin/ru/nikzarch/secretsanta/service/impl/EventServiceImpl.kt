package ru.nikzarch.secretsanta.service.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Service
import ru.nikzarch.secretsanta.domain.SecretSantaEvent
import ru.nikzarch.secretsanta.service.CreateEventRequest
import ru.nikzarch.secretsanta.service.EventService
import ru.nikzarch.secretsanta.service.UpdateEventRequest

@Service
class EventServiceImpl(
    val create: DSLContext
) : EventService {
    override fun createEvent(request: CreateEventRequest): SecretSantaEvent {
        TODO("Not yet implemented")
    }

    override fun updateEvent(request: UpdateEventRequest): SecretSantaEvent {
        TODO("Not yet implemented")
    }

    override fun deleteEvent(id: Long) {
        TODO("Not yet implemented")
    }

    override fun findById(id: Long): SecretSantaEvent? {
        TODO("Not yet implemented")
    }
}