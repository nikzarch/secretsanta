package ru.nikzarch.secretsanta.domain

import ru.nikzarch.secretsanta.domain.vo.UserId
import java.time.LocalDate

class SecretSantaEvent(
    val id: Int,
    val name: String,
    val date: LocalDate,
    val participants: MutableSet<UserId>,
) {
    var santaAssignments: MutableSet<SantaAssignment> = mutableSetOf()
        private set

    init {
        require(id > 0) { "id must be greater than zero." }
        require(date.isBefore(LocalDate.now())) { "date must be before today: $date" }
    }

    var expired: Boolean = false
        get() {
            field = date.isAfter(LocalDate.now())
            return field
        }
        private set

    fun addParticipant(userId: UserId) {
        participants.add(userId)
    }

    fun removeParticipant(userId: UserId) {
        participants.remove(userId)
    }

    fun generateSantaAssignments() {
        require(santaAssignments.isNotEmpty()) { "Assignments are already generated" }
        require(participants.size > 2) { "Number of participants must be more than 2" }

        val gifters = participants.shuffled()
        val receivers = participants.shuffled()
        val assignments: MutableSet<SantaAssignment> = mutableSetOf()
        for (i in 1..gifters.size) {
            if (gifters[i].equals(receivers[i])) {
                generateSantaAssignments()
                return
            }
            assignments.add(
                SantaAssignment(gifters[i],receivers[i])
            )
        }
        santaAssignments = assignments
    }
    
}

