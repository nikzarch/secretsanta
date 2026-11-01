package ru.nikzarch.secretsanta.service

import ru.nikzarch.secretsanta.domain.Wishlist
import ru.nikzarch.secretsanta.domain.vo.UserId

interface WishlistService {
    fun findById(id: Int) : Wishlist?
    fun findByUserId(userId: UserId) : Wishlist?
}