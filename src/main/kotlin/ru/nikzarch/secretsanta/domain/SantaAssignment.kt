package ru.nikzarch.secretsanta.domain

import ru.nikzarch.secretsanta.domain.vo.UserId

data class SantaAssignment(
    val gifterId: UserId,
    val receiverId: UserId
) {
    init {
        require(gifterId != receiverId) { "gifter and receiver must be different" }
    }
}