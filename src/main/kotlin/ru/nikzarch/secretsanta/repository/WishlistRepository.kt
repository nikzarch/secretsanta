package ru.nikzarch.secretsanta.repository

import ru.nikzarch.secretsanta.domain.Wishlist
import ru.nikzarch.secretsanta.domain.WishlistItem
import ru.nikzarch.secretsanta.domain.vo.*

interface WishlistRepository {
    fun addItem(wishlistId: WishlistId, item: WishlistItem): Boolean
    fun findById(id: WishlistId): Wishlist?
    fun findByOwnerId(ownerId: UserId): Wishlist?
    fun save(wishlist: Wishlist): Wishlist
    fun deleteById(id: WishlistId): Boolean
}
