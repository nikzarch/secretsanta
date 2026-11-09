package ru.nikzarch.secretsanta.mapping

import ru.nikzarch.secretsanta.domain.*
import ru.nikzarch.secretsanta.domain.vo.*
import ru.nikzarch.secretsanta.jooq.generated.tables.records.WishlistsRecord
import ru.nikzarch.secretsanta.jooq.generated.tables.records.WishlistItemsRecord
import ru.nikzarch.secretsanta.service.AddWishlistItemRequest

fun WishlistItemsRecord.toDomain() = WishlistItem(
    WishlistItemId(requireNotNull(id)), requireNotNull(name), description, link
)

fun WishlistsRecord.toDomain(items: List<WishlistItem> = emptyList()) = Wishlist(
    WishlistId(requireNotNull(id)), UserId(requireNotNull(ownerId)), items
)

fun AddWishlistItemRequest.toDomain() = WishlistItem(null, name, description, link)
