package ru.nikzarch.secretsanta.domain

import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.domain.vo.WishlistId
import ru.nikzarch.secretsanta.domain.vo.WishlistItemId

class Wishlist(
    val id: WishlistId? = null,
    val ownerId: UserId,
    items: List<WishlistItem> = emptyList()
) {
    companion object {
        const val MAX_ITEMS = 100
    }

    private val itemsList: MutableList<WishlistItem> = items.toMutableList()

    val items: List<WishlistItem>
        get() = itemsList.toList()

    init {
        require(itemsList.size <= MAX_ITEMS) { "wishlist must not be longer than $MAX_ITEMS items" }
    }

    fun addItem(item: WishlistItem) {
        require(itemsList.size < MAX_ITEMS) { "wishlist must not be longer than $MAX_ITEMS items" }
        itemsList.add(item)
    }

    fun removeItem(itemId: WishlistItemId) {
        itemsList.removeIf { it.id == itemId }
    }
}