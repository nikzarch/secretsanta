package ru.nikzarch.secretsanta.mapping

import ru.nikzarch.secretsanta.domain.SantaAssignment
import ru.nikzarch.secretsanta.domain.SecretSantaEvent
import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.jooq.generated.tables.records.SecretSantaEventsRecord
import ru.nikzarch.secretsanta.service.CreateEventRequest
import ru.nikzarch.secretsanta.service.UpdateEventRequest

fun CreateEventRequest.toDomain(id: EventId) =
    SecretSantaEvent(
        id = id,
        name = name,
        description = description,
        date = date,
        ownerId = ownerId
    )

fun UpdateEventRequest.toDomain(event: SecretSantaEvent) =
    SecretSantaEvent(
        id = event.id,
        name = name ?: event.name,
        description = description ?: event.description,
        date = date ?: event.date,
        ownerId = event.ownerId
    )


fun SecretSantaEventsRecord.toDomain(
    participants: Collection<UserId> = emptyList(),
    assignments: Collection<SantaAssignment> = emptyList()
) = SecretSantaEvent(
    id = EventId(requireNotNull(id)),
    name = requireNotNull(name),
    description = description,
    date = requireNotNull(eventDate),
    ownerId = UserId(requireNotNull(ownerId)),
    participants = participants,
    santaAssignments = assignments,
    active = requireNotNull(active),
)



