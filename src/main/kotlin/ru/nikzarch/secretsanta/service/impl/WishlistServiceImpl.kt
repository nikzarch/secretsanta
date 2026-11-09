package ru.nikzarch.secretsanta.service.impl

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import ru.nikzarch.secretsanta.domain.Wishlist
import ru.nikzarch.secretsanta.domain.WishlistItem
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.domain.vo.WishlistId
import ru.nikzarch.secretsanta.mapping.toDomain
import ru.nikzarch.secretsanta.repository.WishlistRepository
import ru.nikzarch.secretsanta.service.AddWishlistItemRequest
import ru.nikzarch.secretsanta.service.UserService
import ru.nikzarch.secretsanta.service.WishlistService

@Service
class WishlistServiceImpl(
    private val wishlistRepository: WishlistRepository,
    private val userService: UserService
) : WishlistService {
    override fun findById(id: WishlistId): Wishlist? = wishlistRepository.findById(id)

    override fun findByUserId(userId: UserId): Wishlist? = wishlistRepository.findByOwnerId(userId)

    override fun findByUserName(username: String): Wishlist? {
        val user = userService.findByUsername(username)
            ?: throw UsernameNotFoundException("User $username not found")
        return findByUserId(user.id!!)
    }


    override fun addItemToWishlist(
        request: AddWishlistItemRequest,
        userId: UserId
    ): WishlistItem {
        val wishlist = findByUserId(userId) ?: wishlistRepository.save(Wishlist(null, userId))
        val item = request.toDomain()
        return wishlistRepository.addItem(wishlist.id!!, item)
    }

}