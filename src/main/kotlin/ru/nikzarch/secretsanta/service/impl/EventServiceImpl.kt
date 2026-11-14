package ru.nikzarch.secretsanta.service.impl

import mu.KotlinLogging
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nikzarch.secretsanta.domain.Participation
import ru.nikzarch.secretsanta.domain.ParticipationStatus
import ru.nikzarch.secretsanta.domain.SecretSantaEvent
import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.repository.EventRepository
import ru.nikzarch.secretsanta.repository.ParticipationRepository
import ru.nikzarch.secretsanta.repository.UserRepository
import ru.nikzarch.secretsanta.repository.WishlistRepository
import ru.nikzarch.secretsanta.service.CreateEventRequest
import ru.nikzarch.secretsanta.service.EventService
import ru.nikzarch.secretsanta.service.GiftRecipient
import ru.nikzarch.secretsanta.service.UpdateEventRequest
import ru.nikzarch.secretsanta.service.exception.NotFoundException
import java.time.Instant
import java.time.LocalDate

private val logger = KotlinLogging.logger {}

@Service
class EventServiceImpl(
    private val eventRepository: EventRepository,
    private val participationRepository: ParticipationRepository,
    private val userRepository: UserRepository,
    private val wishlistRepository: WishlistRepository,
) : EventService {

    @Transactional
    override fun createEvent(request: CreateEventRequest): SecretSantaEvent {
        require(request.name.isNotBlank()) { "event name must not be blank" }
        require(!request.date.isBefore(LocalDate.now())) { "event date must not be in the past" }
        requireNotNull(userRepository.findById(request.ownerId)) { "owner not found" }
        val event = eventRepository.save(
            SecretSantaEvent(
                name = request.name,
                description = request.description,
                date = request.date,
                ownerId = request.ownerId,
            )
        )
        val id = requireNotNull(event.id)
        val owner = Participation(request.ownerId, id, Instant.now())
        owner.accept()
        participationRepository.save(owner)
        logger.info { "Event ${id.value} created by user ${request.ownerId.value}" }
        return getEvent(id)
    }

    @Transactional
    override fun updateEvent(request: UpdateEventRequest, actorId: UserId): SecretSantaEvent {
        val event = lockEvent(request.id)
        checkOwner(event, actorId)
        check(event.isActive()) { "event is not active" }
        request.name?.let {
            require(it.isNotBlank()) { "event name must not be blank" }
            event.name = it
        }
        request.date?.let {
            require(!it.isBefore(LocalDate.now())) { "event date must not be in the past" }
            event.date = it
        }
        request.description?.let { event.description = it }
        val updated = eventRepository.save(event)
        logger.info { "Event ${request.id.value} updated by user ${actorId.value}" }
        return updated
    }

    @Transactional
    override fun deleteEvent(id: EventId, actorId: UserId) {
        val event = lockEvent(id)
        checkOwner(event, actorId)
        check(eventRepository.deleteById(id)) { "event not found" }
        logger.info { "Event ${id.value} deleted by user ${actorId.value}" }
    }

    @Transactional
    override fun deactivateEvent(id: EventId, actorId: UserId): SecretSantaEvent {
        val event = lockEvent(id)
        checkOwner(event, actorId)
        event.deactivate()
        val updated = eventRepository.save(event)
        logger.info { "Event ${id.value} deactivated by user ${actorId.value}" }
        return updated
    }

    override fun findById(id: EventId): SecretSantaEvent? = eventRepository.findById(id)

    override fun findByOwnerId(userId: UserId) = eventRepository.findByOwnerId(userId)

    override fun findByParticipantId(userId: UserId) = eventRepository.findByParticipantId(userId)

    override fun isOwnerOfEventById(eventId: EventId, userId: UserId): Boolean =
        getEvent(eventId).ownerId == userId

    @Transactional
    override fun requestParticipation(eventId: EventId, userId: UserId): Participation {
        val event = lockEvent(eventId)
        check(event.isActive()) { "event is not active" }
        check(!event.areAssignmentsGenerated()) { "assignments are already generated" }
        requireNotNull(userRepository.findById(userId)) { "user not found" }

        val existing = participationRepository.findByEventAndUser(eventId, userId)
        if (existing != null) {
            check(existing.canRetry()) {
                "participation already exists or retry delay has not passed"
            }
        }
        val participation = participationRepository.save(Participation(userId, eventId, Instant.now()))
        logger.info { "User ${userId.value} requested participation in event ${eventId.value}" }
        return participation
    }

    @Transactional
    override fun acceptParticipation(eventId: EventId, userId: UserId, actorId: UserId): Participation {
        val event = lockEvent(eventId)
        checkOwner(event, actorId)
        check(event.isActive()) { "event is not active" }
        check(!event.areAssignmentsGenerated()) { "assignments are already generated" }
        val participation = getParticipation(eventId, userId)
        participation.accept()
        val saved = participationRepository.save(participation)
        logger.info { "User ${userId.value} accepted into event ${eventId.value} by owner ${actorId.value}" }
        return saved
    }

    @Transactional
    override fun rejectParticipation(eventId: EventId, userId: UserId, actorId: UserId): Participation {
        val event = lockEvent(eventId)
        checkOwner(event, actorId)
        check(event.isActive()) { "event is not active" }
        check(!event.areAssignmentsGenerated()) { "assignments are already generated" }
        check(userId != event.ownerId) { "owner cannot be rejected" }
        val participation = getParticipation(eventId, userId)
        participation.reject()
        val saved = participationRepository.save(participation)
        logger.info { "User ${userId.value} rejected from event ${eventId.value} by owner ${actorId.value}" }
        return saved
    }

    @Transactional
    override fun leaveEvent(eventId: EventId, userId: UserId) {
        val event = lockEvent(eventId)
        check(userId != event.ownerId) { "owner cannot leave the event" }
        check(!event.areAssignmentsGenerated()) { "assignments are already generated" }
        val participation = getParticipation(eventId, userId)
        check(participation.status != ParticipationStatus.REJECTED) {
            "rejected participation cannot be withdrawn before retry delay"
        }
        participationRepository.deleteByEventAndUser(eventId, userId)
        logger.info { "User ${userId.value} left event ${eventId.value}" }
    }

    override fun findParticipation(eventId: EventId, userId: UserId): Participation? {
        getEvent(eventId)
        return participationRepository.findByEventAndUser(eventId, userId)
    }

    override fun findParticipations(eventId: EventId, actorId: UserId): List<Participation> {
        checkOwner(getEvent(eventId), actorId)
        return participationRepository.findByEventId(eventId)
    }

    @Transactional
    override fun generateSantaAssignments(eventId: EventId, actorId: UserId): SecretSantaEvent {
        val event = lockEvent(eventId)
        checkOwner(event, actorId)
        event.generateSantaAssignments()
        val updated = eventRepository.save(event)
        logger.info { "Assignments generated for event ${eventId.value} (${event.participants.size} participants)" }
        return updated
    }

    override fun findRecipient(eventId: EventId, gifterId: UserId): GiftRecipient {
        val event = getEvent(eventId)
        val receiverId = event.santaAssignments
            .find { it.gifterId == gifterId }?.receiverId
            ?: throw AccessDeniedException("recipient not assigned")
        val receiver = userRepository.findById(receiverId)
            ?: throw NotFoundException("recipient not found")
        return GiftRecipient(receiver, wishlistRepository.findByOwnerId(receiverId))
    }

    private fun getParticipation(eventId: EventId, userId: UserId) =
        participationRepository.findByEventAndUser(eventId, userId)
            ?: throw NotFoundException("participation not found")

    private fun getEvent(id: EventId) =
        eventRepository.findById(id) ?: throw NotFoundException("event not found")

    private fun lockEvent(id: EventId) =
        eventRepository.findByIdForUpdate(id) ?: throw NotFoundException("event not found")

    private fun checkOwner(event: SecretSantaEvent, actorId: UserId) {
        if (event.ownerId != actorId) throw AccessDeniedException("only event owner can do this")
    }
}
