package ru.nikzarch.secretsanta.domain

import ru.nikzarch.secretsanta.domain.vo.UserId

class Wishlist(
    val ownerId: UserId,
    items: MutableList<WishlistItem>
){
    var items = items
    private set

    init {
        require(items.size <= 100) { "Wishlist must not be longer than 100 items." }
    }

    fun addItem(item: WishlistItem) {
        items.add(item)
        require(items.size <= 100) { "Wishlist must not be longer than 100 items." }
    }

    fun removeItem(item: WishlistItem) {
        items.remove(item)
    }
}