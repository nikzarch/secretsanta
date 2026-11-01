package ru.nikzarch.secretsanta.repository.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.nikzarch.secretsanta.domain.*
import ru.nikzarch.secretsanta.domain.vo.*
import ru.nikzarch.secretsanta.jooq.generated.tables.EventParticipants.EVENT_PARTICIPANTS
import ru.nikzarch.secretsanta.jooq.generated.tables.SantaAssignments.SANTA_ASSIGNMENTS
import ru.nikzarch.secretsanta.jooq.generated.tables.SecretSantaEvents.SECRET_SANTA_EVENTS
import ru.nikzarch.secretsanta.jooq.generated.tables.records.SecretSantaEventsRecord
import ru.nikzarch.secretsanta.mapping.toDomain

import ru.nikzarch.secretsanta.repository.EventRepository

@Repository
open class EventRepositoryImpl(private val create: DSLContext) : EventRepository {

    @Transactional
    override fun save(event: SecretSantaEvent): SecretSantaEvent {
        event.id ?: return update(event)
        return insert(event)
    }

    private fun insert(event: SecretSantaEvent): SecretSantaEvent {
        val id = create.insertInto(SECRET_SANTA_EVENTS)
            .set(SECRET_SANTA_EVENTS.NAME, event.name)
            .set(SECRET_SANTA_EVENTS.DESCRIPTION, event.description)
            .set(SECRET_SANTA_EVENTS.EVENT_DATE, event.date).returningResult(SECRET_SANTA_EVENTS.ID).fetchSingle().value1()
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
        if (create.update(SECRET_SANTA_EVENTS)
                .set(SECRET_SANTA_EVENTS.NAME, event.name)
                .set(SECRET_SANTA_EVENTS.DESCRIPTION, event.description)
                .set(SECRET_SANTA_EVENTS.EVENT_DATE, event.date)
                .where(SECRET_SANTA_EVENTS.ID.eq(event.id!!.value))
                .execute() != 1
        )
            throw IllegalArgumentException("Event with ${event.id} doesn't exist")
        create.deleteFrom(SANTA_ASSIGNMENTS)
            .where(SANTA_ASSIGNMENTS.EVENT_ID.eq(event.id.value)).execute()
        create.deleteFrom(EVENT_PARTICIPANTS)
            .where(EVENT_PARTICIPANTS.EVENT_ID.eq(event.id.value)).execute()
        event.participants.forEach {
            create.insertInto(EVENT_PARTICIPANTS)
                .set(EVENT_PARTICIPANTS.EVENT_ID, event.id.value)
                .set(EVENT_PARTICIPANTS.USER_ID, it.value).execute()
        }
        event.santaAssignments.forEach {
            create.insertInto(SANTA_ASSIGNMENTS)
                .set(SANTA_ASSIGNMENTS.EVENT_ID, event.id.value)
                .set(SANTA_ASSIGNMENTS.GIFTER_ID, it.gifterId.value)
                .set(SANTA_ASSIGNMENTS.RECEIVER_ID, it.receiverId.value).execute()
        }
        return event
    }

    override fun findById(id: EventId): SecretSantaEvent? {
        val record = create.selectFrom(SECRET_SANTA_EVENTS)
            .where(SECRET_SANTA_EVENTS.ID.eq(id.value))
            .fetchOne()
        if (record == null) {
            return null
        }
        val participants = create.select(EVENT_PARTICIPANTS.USER_ID)
            .from(EVENT_PARTICIPANTS)
            .where(EVENT_PARTICIPANTS.EVENT_ID.eq(id.value))
            .fetch(EVENT_PARTICIPANTS.USER_ID).map(::UserId)
        val assignments = create.select(SANTA_ASSIGNMENTS.GIFTER_ID, SANTA_ASSIGNMENTS.RECEIVER_ID)
            .from(SANTA_ASSIGNMENTS)
            .where(SANTA_ASSIGNMENTS.EVENT_ID.eq(id.value))
            .fetch { SantaAssignment(UserId(it.value1()), UserId(it.value2())) }
        return record.toDomain(participants,assignments)
    }

    override fun deleteById(id: EventId) = create.deleteFrom(SECRET_SANTA_EVENTS)
            .where(SECRET_SANTA_EVENTS.ID.eq(id.value)).execute() == 1


    override fun countAll(): Int = create.selectFrom(SECRET_SANTA_EVENTS).count()

    override fun findByOwnerId(ownerId: UserId): List<SecretSantaEvent> {
        return create.selectFrom(SECRET_SANTA_EVENTS)
            .where(SECRET_SANTA_EVENTS.OWNER_ID.eq(ownerId.value))
            .fetch()
            .map(SecretSantaEventsRecord::toDomain)
    }

}
