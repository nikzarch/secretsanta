package ru.nikzarch.secretsanta.repository.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import ru.nikzarch.secretsanta.domain.Participation
import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.jooq.generated.tables.EventParticipants.EVENT_PARTICIPANTS
import ru.nikzarch.secretsanta.mapping.toDomain
import ru.nikzarch.secretsanta.repository.ParticipationRepository
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Repository
class ParticipationRepositoryImpl(
    private val create: DSLContext,
) : ParticipationRepository {


    override fun save(participation: Participation): Participation {
        val eventId = participation.eventId.value
        val userId = participation.participantId.value
        val requestedAt = OffsetDateTime.ofInstant(participation.requestedAt, ZoneOffset.UTC)
        val decidedAt = participation.decidedAt?.let { OffsetDateTime.ofInstant(it, ZoneOffset.UTC) }

        create.insertInto(EVENT_PARTICIPANTS)
            .set(EVENT_PARTICIPANTS.EVENT_ID, eventId)
            .set(EVENT_PARTICIPANTS.USER_ID, userId)
            .set(EVENT_PARTICIPANTS.STATUS, participation.status.name)
            .set(EVENT_PARTICIPANTS.REQUESTED_AT, requestedAt)
            .set(EVENT_PARTICIPANTS.DECIDED_AT, decidedAt)
            .onConflict(EVENT_PARTICIPANTS.EVENT_ID, EVENT_PARTICIPANTS.USER_ID)
            .doUpdate()
            .set(EVENT_PARTICIPANTS.STATUS, participation.status.name)
            .set(EVENT_PARTICIPANTS.REQUESTED_AT, requestedAt)
            .set(EVENT_PARTICIPANTS.DECIDED_AT, decidedAt)
            .execute()

        return participation
    }

    override fun findByEventAndUser(eventId: EventId, userId: UserId): Participation? =
        create.selectFrom(EVENT_PARTICIPANTS)
            .where(EVENT_PARTICIPANTS.EVENT_ID.eq(eventId.value))
            .and(EVENT_PARTICIPANTS.USER_ID.eq(userId.value))
            .fetchOne()?.toDomain()

    override fun findByEventId(eventId: EventId): List<Participation> =
        create.selectFrom(EVENT_PARTICIPANTS)
            .where(EVENT_PARTICIPANTS.EVENT_ID.eq(eventId.value))
            .orderBy(EVENT_PARTICIPANTS.REQUESTED_AT.asc())
            .fetch { it.toDomain() }

    override fun deleteByEventAndUser(eventId: EventId, userId: UserId): Boolean =
        create.deleteFrom(EVENT_PARTICIPANTS)
            .where(EVENT_PARTICIPANTS.EVENT_ID.eq(eventId.value))
            .and(EVENT_PARTICIPANTS.USER_ID.eq(userId.value))
            .execute() == 1
}
