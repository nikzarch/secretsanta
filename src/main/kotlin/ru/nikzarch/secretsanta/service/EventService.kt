package ru.nikzarch.secretsanta.service

import ru.nikzarch.secretsanta.domain.SecretSantaEvent
import ru.nikzarch.secretsanta.domain.vo.UserId
import java.time.LocalDate

interface EventService {
    fun createEvent(request: CreateEventRequest) : SecretSantaEvent
    fun updateEvent(request: UpdateEventRequest) : SecretSantaEvent
    fun deleteEvent(id: Long)
    fun findById(id: Long) : SecretSantaEvent?
}

data class CreateEventRequest(
    val name: String,
    val description: String?,
    val date: LocalDate,
    val ownerId: UserId,
)

data class UpdateEventRequest(
    val id: Long,
    val name: String?,
    val description: String?,
    val date: LocalDate?,
    val ownerId: UserId?,
)