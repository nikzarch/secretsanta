package ru.nikzarch.secretsanta.domain

import ru.nikzarch.secretsanta.domain.vo.EventId
import ru.nikzarch.secretsanta.domain.vo.UserId
import java.time.LocalDate

class SecretSantaEvent(
    val id: EventId? = null,
    var name: String,
    var description: String?,
    var date: LocalDate,
    val ownerId: UserId,
    participants: Collection<UserId> = emptyList(),
    santaAssignments: Collection<SantaAssignment> = emptySet(),
    active: Boolean = true,
) {
    private val participantsSet = participants.toMutableSet()
    private val santaAssignmentsSet = santaAssignments.toMutableSet()

    val participants: Set<UserId>
        get() = participantsSet.toSet()

    val santaAssignments: Set<SantaAssignment>
        get() = santaAssignmentsSet.toSet()

    var active = active
        private set

    init {
        require(name.isNotBlank()) { "name must not be blank" }
    }

    fun addParticipant(userId: UserId) {
        check(isActive()) { "event is not active" }
        check(!areAssignmentsGenerated()) { "assignments are already generated" }
        participantsSet.add(userId)
    }

    fun addParticipants(users: Collection<UserId>) {
        check(isActive()) { "event is not active" }
        check(!areAssignmentsGenerated()) { "assignments are already generated" }
        participantsSet.addAll(users)
    }

    fun removeParticipant(userId: UserId) {
        check(!areAssignmentsGenerated()) { "assignments are already generated" }
        participantsSet.remove(userId)
    }

    fun generateSantaAssignments() {
        check(isActive()) { "event is not active" }
        check(santaAssignmentsSet.isEmpty()) { "assignments are already generated" }
        require(participantsSet.size > 2) { "number of participants must be more than 2" }

        val shuffled = participantsSet.shuffled()
        val generated = shuffled.mapIndexed { index, gifterId ->
            SantaAssignment(gifterId, shuffled[(index + 1) % shuffled.size])
        }
        santaAssignmentsSet.addAll(generated)
    }

    fun deactivate() {
        active = false
    }

    fun isActive() = active && !date.isBefore(LocalDate.now())

    fun areAssignmentsGenerated() = santaAssignmentsSet.isNotEmpty()

    private fun invalidateAssignments() {
        santaAssignmentsSet.clear()
    }
}