package ru.nikzarch.secretsanta.repository.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import ru.nikzarch.secretsanta.domain.Wishlist
import ru.nikzarch.secretsanta.domain.WishlistItem
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.domain.vo.WishlistId
import ru.nikzarch.secretsanta.domain.vo.WishlistItemId
import ru.nikzarch.secretsanta.jooq.generated.tables.WishlistItems.WISHLIST_ITEMS
import ru.nikzarch.secretsanta.jooq.generated.tables.Wishlists.WISHLISTS
import ru.nikzarch.secretsanta.mapping.toDomain
import ru.nikzarch.secretsanta.repository.WishlistRepository

@Repository
class WishlistRepositoryImpl(private val create: DSLContext) : WishlistRepository {

    override fun addItem(wishlistId: WishlistId, item: WishlistItem): WishlistItem {
        val id = create.insertInto(WISHLIST_ITEMS)
            .set(WISHLIST_ITEMS.WISHLIST_ID, wishlistId.value)
            .set(WISHLIST_ITEMS.NAME, item.name)
            .set(WISHLIST_ITEMS.DESCRIPTION, item.description)
            .set(WISHLIST_ITEMS.LINK, item.link)
            .returningResult(WISHLIST_ITEMS.ID)
            .fetchSingle().value1()
        return WishlistItem(WishlistItemId(id), item.name, item.description, item.link)
    }

    override fun updateItem(wishlistId: WishlistId, item: WishlistItem): WishlistItem {
        val itemId = requireNotNull(item.id)
        check(
            create.update(WISHLIST_ITEMS)
                .set(WISHLIST_ITEMS.NAME, item.name)
                .set(WISHLIST_ITEMS.DESCRIPTION, item.description)
                .set(WISHLIST_ITEMS.LINK, item.link)
                .where(WISHLIST_ITEMS.ID.eq(itemId.value))
                .and(WISHLIST_ITEMS.WISHLIST_ID.eq(wishlistId.value))
                .execute() == 1
        ) { "wishlist item not found" }
        return item
    }

    override fun deleteItem(wishlistId: WishlistId, itemId: WishlistItemId): Boolean =
        create.deleteFrom(WISHLIST_ITEMS)
            .where(WISHLIST_ITEMS.ID.eq(itemId.value))
            .and(WISHLIST_ITEMS.WISHLIST_ID.eq(wishlistId.value))
            .execute() == 1

    override fun findById(id: WishlistId): Wishlist? =
        create.selectFrom(WISHLISTS)
            .where(WISHLISTS.ID.eq(id.value))
            .fetchOne()?.let { it.toDomain(getItems(id)) }

    override fun findByOwnerId(ownerId: UserId): Wishlist? =
        create.selectFrom(WISHLISTS)
            .where(WISHLISTS.OWNER_ID.eq(ownerId.value))
            .fetchOne()?.let { record ->
                record.toDomain(getItems(WishlistId(requireNotNull(record.id))))
            }

    @Transactional
    override fun save(wishlist: Wishlist): Wishlist =
        if (wishlist.id == null) insert(wishlist) else update(wishlist)

    private fun insert(wishlist: Wishlist): Wishlist {
        val wishlistId = WishlistId(
            create.insertInto(WISHLISTS)
                .set(WISHLISTS.OWNER_ID, wishlist.ownerId.value)
                .returningResult(WISHLISTS.ID)
                .fetchSingle().value1()
        )
        wishlist.items.forEach { addItem(wishlistId, it) }
        return requireNotNull(findById(wishlistId))
    }

    private fun update(wishlist: Wishlist): Wishlist {
        val id = requireNotNull(wishlist.id)
        val old = getItems(id)
        val oldIds = old.mapNotNull { it.id }.toSet()
        val newIds = wishlist.items.mapNotNull { it.id }.toSet()
        require(newIds.all { it in oldIds }) { "wishlist contains an item from another wishlist" }
        old.filter { it.id !in newIds }.forEach { deleteItem(id, requireNotNull(it.id)) }
        wishlist.items.forEach { item ->
            if (item.id == null) {
                addItem(id, item)
            } else {
                val previous = old.first { it.id == item.id }
                if (previous.name != item.name || previous.description != item.description || previous.link != item.link) {
                    updateItem(id, item)
                }
            }
        }
        return requireNotNull(findById(id))
    }

    override fun deleteById(id: WishlistId): Boolean =
        create.deleteFrom(WISHLISTS).where(WISHLISTS.ID.eq(id.value)).execute() == 1

    private fun getItems(wishlistId: WishlistId): List<WishlistItem> =
        create.selectFrom(WISHLIST_ITEMS)
            .where(WISHLIST_ITEMS.WISHLIST_ID.eq(wishlistId.value))
            .orderBy(WISHLIST_ITEMS.ID.asc())
            .fetch { it.toDomain() }
}
