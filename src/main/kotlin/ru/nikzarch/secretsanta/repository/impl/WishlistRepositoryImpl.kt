package ru.nikzarch.secretsanta.repository.impl

import org.jooq.DSLContext
import org.springframework.stereotype.Repository
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
class WishlistRepositoryImpl(
    private val create: DSLContext
) : WishlistRepository {

    override fun addItem(
        wishlistId: WishlistId,
        item: WishlistItem
    ): Boolean {
        val query = create.insertInto(WISHLIST_ITEMS)
            .set(WISHLIST_ITEMS.WISHLIST_ID, wishlistId.value)
            .set(WISHLIST_ITEMS.NAME, item.name)
            .set(WISHLIST_ITEMS.DESCRIPTION, item.description)
            .set(WISHLIST_ITEMS.LINK, item.link)
        item.id.let {
            query.set(WISHLIST_ITEMS.ID, item.id!!.value)
        }
        return query.execute() == 1
    }


    override fun findById(id: WishlistId): Wishlist? = create.selectFrom(WISHLISTS)
        .where(WISHLISTS.ID.eq(id.value))
        .fetchOne()?.toDomain()


    override fun findByOwnerId(ownerId: UserId): Wishlist? =  create.selectFrom(WISHLISTS)
            .where(WISHLISTS.OWNER_ID.eq(ownerId.value))
            .fetchOne()?.toDomain()


    override fun save(wishlist: Wishlist): Wishlist {
        return if (wishlist.id == null) insert(wishlist) else update(wishlist)
    }

    private fun update(wishlist: Wishlist): Wishlist {
        if (create.update(WISHLISTS)
                .set(WISHLISTS.OWNER_ID, wishlist.ownerId.value)
                .execute() != 1
        ) throw IllegalStateException("Wishlist with ${wishlist.id} doesnt exist")
        create.deleteFrom(WISHLIST_ITEMS)
            .where(WISHLIST_ITEMS.WISHLIST_ID.eq(wishlist.id!!.value))
            .execute()
        val addedItems = mutableListOf<WishlistItem>()
        wishlist.items.forEach {
            val id = create.insertInto(WISHLIST_ITEMS)
                .set(WISHLIST_ITEMS.NAME, it.name)
                .set(WISHLIST_ITEMS.DESCRIPTION, it.description)
                .set(WISHLIST_ITEMS.LINK, it.link)
                .set(WISHLIST_ITEMS.WISHLIST_ID, wishlist.id.value)
                .returningResult(WISHLIST_ITEMS.ID)
                .fetchSingle().value1()
            addedItems.add(
                WishlistItem(
                    id = WishlistItemId(id),
                    name = it.name,
                    description = it.description,
                    link = it.link
                )
            )
        }
        return Wishlist(
            id = wishlist.id,
            ownerId = wishlist.ownerId,
            items = addedItems,
        )
    }

    private fun insert(wishlist: Wishlist): Wishlist {
        val id = create.insertInto(WISHLISTS)
            .set(WISHLISTS.OWNER_ID, wishlist.ownerId.value)
            .returningResult(WISHLISTS.ID)
            .fetchSingle()
            .value1()
        val addedItems = mutableListOf<WishlistItem>()
        wishlist.items.forEach {
            val id = create.insertInto(WISHLIST_ITEMS)
                .set(WISHLIST_ITEMS.NAME, it.name)
                .set(WISHLIST_ITEMS.DESCRIPTION, it.description)
                .set(WISHLIST_ITEMS.LINK, it.link)
                .set(WISHLIST_ITEMS.WISHLIST_ID, id)
                .returningResult(WISHLIST_ITEMS.ID)
                .fetchSingle().value1()
            addedItems.add(
                WishlistItem(
                    id = WishlistItemId(id),
                    name = it.name,
                    description = it.description,
                    link = it.link
                )
            )
        }
        return Wishlist(
            id = WishlistId(id),
            ownerId = wishlist.ownerId,
            items = addedItems,
        )
    }

    override fun deleteById(id: WishlistId): Boolean = create
        .deleteFrom(WISHLISTS)
        .where(WISHLISTS.ID.eq(id.value))
        .execute() == 1
}