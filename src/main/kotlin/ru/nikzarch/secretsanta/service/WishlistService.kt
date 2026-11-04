package ru.nikzarch.secretsanta.service

import ru.nikzarch.secretsanta.domain.Wishlist
import ru.nikzarch.secretsanta.domain.WishlistItem
import ru.nikzarch.secretsanta.domain.vo.UserId

interface WishlistService {
    fun findById(id: Int) : Wishlist?
    fun findByUserId(userId: UserId) : Wishlist?
    fun addItemToWishlist(request: AddWishlistItemRequest, userId: UserId) : WishlistItem
}

data class AddWishlistItemRequest(
    val name: String,
    val description: String? = null,
    val link: String? = null,
)