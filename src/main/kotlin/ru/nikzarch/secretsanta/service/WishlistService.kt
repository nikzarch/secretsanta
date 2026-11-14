package ru.nikzarch.secretsanta.service

import ru.nikzarch.secretsanta.domain.Wishlist
import ru.nikzarch.secretsanta.domain.WishlistItem
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.domain.vo.WishlistId
import ru.nikzarch.secretsanta.domain.vo.WishlistItemId

interface WishlistService {
    fun findById(id: WishlistId): Wishlist?
    fun findByUserId(userId: UserId): Wishlist?
    fun findByUserName(username: String): Wishlist?
    fun addItemToWishlist(request: AddWishlistItemRequest, userId: UserId): WishlistItem
    fun updateItem(itemId: WishlistItemId, request: AddWishlistItemRequest, userId: UserId): WishlistItem
    fun removeItem(itemId: WishlistItemId, userId: UserId)
}

data class AddWishlistItemRequest(
    val name: String,
    val description: String? = null,
    val link: String? = null,
)
