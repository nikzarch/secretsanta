package ru.nikzarch.secretsanta.domain

import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import java.time.LocalDate

class SecretSantaEvent(
    val id: EventId? = null,
    var name: String,
    var description: String?,
    var date: LocalDate,
    var ownerId: UserId,
    participants: Collection<UserId> = emptyList(),
    santaAssignments: Collection<SantaAssignment> = emptySet()
) {
    private val participantsSet: MutableSet<UserId> = participants.toMutableSet()
    private val santaAssignmentsSet: MutableSet<SantaAssignment> = santaAssignments.toMutableSet()

    val participants: Set<UserId>
        get() = participantsSet.toSet()

    val santaAssignments: Set<SantaAssignment>
        get() = santaAssignmentsSet.toSet()

    var deactivated: Boolean = false
        get() = field || expired

    private val expired: Boolean
        get() = date.isBefore(LocalDate.now())

    init {
        require(name.isNotBlank()) { "name must not be blank" }
    }

    fun addParticipant(userId: UserId) {
        check(!areAssignmentsGenerated()) { "event already has assigments with current participants" }
        participantsSet.add(userId)
    }

    fun addParticipants(users: Collection<UserId>) {
        check(!areAssignmentsGenerated()) { "event already has assigments with current participants" }
        participantsSet.addAll(users)
    }

    fun removeParticipant(userId: UserId) {
        check(!areAssignmentsGenerated()) { "event already has assigments with current participants" }
        participantsSet.remove(userId)
    }

    fun generateSantaAssignments() {
        require(santaAssignmentsSet.isEmpty()) { "assignments are already generated" }
        require(participantsSet.size > 2) { "number of participants must be more than 2" }

        val shuffled = participantsSet.shuffled()
        val generated = shuffled.mapIndexed { index, gifterId ->
            SantaAssignment(
                gifterId = gifterId,
                receiverId = shuffled[(index + 1) % shuffled.size]
            )
        }
        santaAssignmentsSet.clear()
        santaAssignmentsSet.addAll(generated)
    }

    fun deactivate() {
        deactivated = true
    }

    fun isActive() = !deactivated

    fun areAssignmentsGenerated() = santaAssignmentsSet.isNotEmpty()

    private fun invalidateAssignments() {
        santaAssignmentsSet.clear()
    }
}