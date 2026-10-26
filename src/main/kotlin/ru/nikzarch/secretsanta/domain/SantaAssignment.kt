package ru.nikzarch.secretsanta.domain

import ru.nikzarch.secretsanta.domain.vo.UserId

class SantaAssignment(
    val gifterId: UserId,
    val receiverId: UserId
)