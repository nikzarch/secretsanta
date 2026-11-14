package ru.nikzarch.secretsanta.service.impl

import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nikzarch.secretsanta.domain.Wishlist
import ru.nikzarch.secretsanta.domain.WishlistItem
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.domain.vo.WishlistId
import ru.nikzarch.secretsanta.domain.vo.WishlistItemId
import ru.nikzarch.secretsanta.mapping.toDomain
import ru.nikzarch.secretsanta.repository.WishlistRepository
import ru.nikzarch.secretsanta.service.AddWishlistItemRequest
import ru.nikzarch.secretsanta.service.UserService
import ru.nikzarch.secretsanta.service.WishlistService
import ru.nikzarch.secretsanta.service.exception.NotFoundException

private val logger = KotlinLogging.logger {}

@Service
class WishlistServiceImpl(
    private val wishlistRepository: WishlistRepository,
    private val userService: UserService,
) : WishlistService {
    override fun findById(id: WishlistId): Wishlist? = wishlistRepository.findById(id)

    override fun findByUserId(userId: UserId): Wishlist? = wishlistRepository.findByOwnerId(userId)

    override fun findByUserName(username: String): Wishlist? =
        userService.findByUsername(username)?.id?.let { findByUserId(it) }

    @Transactional
    override fun addItemToWishlist(request: AddWishlistItemRequest, userId: UserId): WishlistItem {
        val wishlist = findByUserId(userId) ?: wishlistRepository.save(Wishlist(ownerId = userId))
        require(wishlist.items.size < Wishlist.MAX_ITEMS) { "wishlist must not exceed ${Wishlist.MAX_ITEMS} items" }
        val item = wishlistRepository.addItem(requireNotNull(wishlist.id), request.toDomain())
        logger.info { "Wishlist item ${requireNotNull(item.id).value} added by user ${userId.value}" }
        return item
    }

    @Transactional
    override fun updateItem(itemId: WishlistItemId, request: AddWishlistItemRequest, userId: UserId): WishlistItem {
        val wishlist = findByUserId(userId) ?: throw NotFoundException("wishlist not found")
        if (wishlist.items.none { it.id == itemId }) throw NotFoundException("wishlist item not found")
        val item = request.toDomain()
        val updated = wishlistRepository.updateItem(
            requireNotNull(wishlist.id),
            WishlistItem(itemId, item.name, item.description, item.link)
        )
        logger.info { "Wishlist item ${itemId.value} updated by user ${userId.value}" }
        return updated
    }

    @Transactional
    override fun removeItem(itemId: WishlistItemId, userId: UserId) {
        val wishlist = findByUserId(userId) ?: throw NotFoundException("wishlist not found")
        if (!wishlistRepository.deleteItem(requireNotNull(wishlist.id), itemId)) {
            throw NotFoundException("wishlist item not found")
        }
        logger.info { "Wishlist item ${itemId.value} removed by user ${userId.value}" }
    }
}
