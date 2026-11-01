package ru.nikzarch.secretsanta.domain.vo

@JvmInline
value class UserId(val value: Int) {
    init {
        require(value > 0) { "userId must be positive" }
    }
}

@JvmInline
value class EventId(val value: Int) {
    init {
        require(value > 0) { "eventId must be positive" }
    }
}

@JvmInline
value class WishlistId(val value: Int) {
    init {
        require(value > 0) { "wishlistId must be positive" }
    }
}

@JvmInline
value class WishlistItemId(val value: Int) {
    init {
        require(value > 0) { "wishlistItemId must be positive" }
    }
}