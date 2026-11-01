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

    val expired: Boolean
        get() = date.isBefore(LocalDate.now())

    init {
        require(name.isNotBlank()) { "name must not be blank" }
    }

    fun addParticipant(userId: UserId) {
        if (participantsSet.add(userId)) invalidateAssignments()
    }

    fun addParticipants(users: Collection<UserId>) {
        if (participantsSet.addAll(users)) invalidateAssignments()
    }

    fun removeParticipant(userId: UserId) {
        if (participantsSet.remove(userId)) invalidateAssignments()
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

    private fun invalidateAssignments() {
        santaAssignmentsSet.clear()
    }
}