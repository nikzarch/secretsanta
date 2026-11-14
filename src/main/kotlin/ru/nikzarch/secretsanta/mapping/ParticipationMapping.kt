package ru.nikzarch.secretsanta.mapping

import ru.nikzarch.secretsanta.domain.Participation
import ru.nikzarch.secretsanta.domain.ParticipationStatus
import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.jooq.generated.tables.records.EventParticipantsRecord

fun EventParticipantsRecord.toDomain() = Participation(
    participantId = UserId(requireNotNull(userId)),
    eventId = EventId(requireNotNull(eventId)),
    requestedAt = requireNotNull(requestedAt).toInstant(),
    status = ParticipationStatus.valueOf(requireNotNull(status)),
    decidedAt = decidedAt?.toInstant(),
)
