package ru.nikzarch.secretsanta.repository.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import ru.nikzarch.secretsanta.domain.ParticipationStatus
import ru.nikzarch.secretsanta.domain.SantaAssignment
import ru.nikzarch.secretsanta.domain.SecretSantaEvent
import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.jooq.generated.tables.EventParticipants.EVENT_PARTICIPANTS
import ru.nikzarch.secretsanta.jooq.generated.tables.SantaAssignments.SANTA_ASSIGNMENTS
import ru.nikzarch.secretsanta.jooq.generated.tables.SecretSantaEvents.SECRET_SANTA_EVENTS
import ru.nikzarch.secretsanta.jooq.generated.tables.records.SecretSantaEventsRecord
import ru.nikzarch.secretsanta.mapping.toDomain
import ru.nikzarch.secretsanta.repository.EventRepository

@Repository
class EventRepositoryImpl(
    private val create: DSLContext,
) : EventRepository {

    @Transactional
    override fun save(event: SecretSantaEvent): SecretSantaEvent =
        if (event.id == null) insert(event) else update(event)

    private fun insert(event: SecretSantaEvent): SecretSantaEvent {
        val id = create.insertInto(SECRET_SANTA_EVENTS)
            .set(SECRET_SANTA_EVENTS.NAME, event.name)
            .set(SECRET_SANTA_EVENTS.DESCRIPTION, event.description)
            .set(SECRET_SANTA_EVENTS.EVENT_DATE, event.date)
            .set(SECRET_SANTA_EVENTS.OWNER_ID, event.ownerId.value)
            .set(SECRET_SANTA_EVENTS.ACTIVE, event.active)
            .returningResult(SECRET_SANTA_EVENTS.ID)
            .fetchSingle().value1()

        return SecretSantaEvent(
            id = EventId(id),
            name = event.name,
            description = event.description,
            date = event.date,
            ownerId = event.ownerId,
            participants = event.participants,
            santaAssignments = event.santaAssignments,
        )
    }

    private fun update(event: SecretSantaEvent): SecretSantaEvent {
        val id = requireNotNull(event.id)
        check(
            create.update(SECRET_SANTA_EVENTS)
                .set(SECRET_SANTA_EVENTS.NAME, event.name)
                .set(SECRET_SANTA_EVENTS.DESCRIPTION, event.description)
                .set(SECRET_SANTA_EVENTS.EVENT_DATE, event.date)
                .set(SECRET_SANTA_EVENTS.ACTIVE, event.active)
                .where(SECRET_SANTA_EVENTS.ID.eq(id.value))
                .execute() == 1
        ) { "event not found" }

        val current = loadAssignments(id).toSet()
        if (current != event.santaAssignments) {
            create.deleteFrom(SANTA_ASSIGNMENTS)
                .where(SANTA_ASSIGNMENTS.EVENT_ID.eq(id.value))
                .execute()
            event.santaAssignments.forEach {
                create.insertInto(SANTA_ASSIGNMENTS)
                    .set(SANTA_ASSIGNMENTS.EVENT_ID, id.value)
                    .set(SANTA_ASSIGNMENTS.GIFTER_ID, it.gifterId.value)
                    .set(SANTA_ASSIGNMENTS.RECEIVER_ID, it.receiverId.value)
                    .execute()
            }
        }
        return event
    }

    override fun findById(id: EventId): SecretSantaEvent? =
        create.selectFrom(SECRET_SANTA_EVENTS)
            .where(SECRET_SANTA_EVENTS.ID.eq(id.value))
            .fetchOne()?.let(::loadEvent)

    @Transactional(propagation = Propagation.MANDATORY)
    override fun findByIdForUpdate(id: EventId): SecretSantaEvent? =
        create.selectFrom(SECRET_SANTA_EVENTS)
            .where(SECRET_SANTA_EVENTS.ID.eq(id.value))
            .forUpdate()
            .fetchOne()?.let(::loadEvent)

    private fun loadEvent(record: SecretSantaEventsRecord): SecretSantaEvent {
        val id = EventId(requireNotNull(record.id))
        val participants = create.select(EVENT_PARTICIPANTS.USER_ID)
            .from(EVENT_PARTICIPANTS)
            .where(EVENT_PARTICIPANTS.EVENT_ID.eq(id.value))
            .and(EVENT_PARTICIPANTS.STATUS.eq(ParticipationStatus.ACCEPTED.name))
            .fetch(EVENT_PARTICIPANTS.USER_ID)
            .map(::UserId)
        return record.toDomain(participants, loadAssignments(id))
    }

    override fun findByOwnerId(ownerId: UserId): List<SecretSantaEvent> =
        create.selectFrom(SECRET_SANTA_EVENTS)
            .where(SECRET_SANTA_EVENTS.OWNER_ID.eq(ownerId.value))
            .orderBy(SECRET_SANTA_EVENTS.EVENT_DATE.asc())
            .fetch { loadEvent(it) }

    override fun findByParticipantId(userId: UserId): List<SecretSantaEvent> =
        create.selectFrom(SECRET_SANTA_EVENTS)
            .where(
                SECRET_SANTA_EVENTS.ID.`in`(
                    create.select(EVENT_PARTICIPANTS.EVENT_ID)
                        .from(EVENT_PARTICIPANTS)
                        .where(EVENT_PARTICIPANTS.USER_ID.eq(userId.value))
                        .and(EVENT_PARTICIPANTS.STATUS.eq(ParticipationStatus.ACCEPTED.name))
                )
            )
            .orderBy(SECRET_SANTA_EVENTS.EVENT_DATE.asc())
            .fetch { loadEvent(it) }

    override fun deleteById(id: EventId): Boolean =
        create.deleteFrom(SECRET_SANTA_EVENTS)
            .where(SECRET_SANTA_EVENTS.ID.eq(id.value))
            .execute() == 1

    override fun countAll(): Int = create.fetchCount(SECRET_SANTA_EVENTS)

    private fun loadAssignments(id: EventId): List<SantaAssignment> =
        create.select(SANTA_ASSIGNMENTS.GIFTER_ID, SANTA_ASSIGNMENTS.RECEIVER_ID)
            .from(SANTA_ASSIGNMENTS)
            .where(SANTA_ASSIGNMENTS.EVENT_ID.eq(id.value))
            .fetch { SantaAssignment(UserId(it.value1()), UserId(it.value2())) }
}
