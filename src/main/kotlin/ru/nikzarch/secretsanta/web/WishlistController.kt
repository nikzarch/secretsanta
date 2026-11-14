package ru.nikzarch.secretsanta.web

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import ru.nikzarch.secretsanta.domain.Wishlist
import ru.nikzarch.secretsanta.domain.WishlistItem
import ru.nikzarch.secretsanta.domain.vo.WishlistItemId
import ru.nikzarch.secretsanta.service.AddWishlistItemRequest
import ru.nikzarch.secretsanta.service.WishlistService

@Tag(name = "Вишлист")
@RestController
@RequestMapping("/api/v1/me/wishlist")
class WishlistController(private val wishlistService: WishlistService) {

    @GetMapping
    @Operation(summary = "Мой вишлист")
    fun get(@AuthenticationPrincipal jwt: Jwt): GetWishlistResponseDto =
        GetWishlistResponseDto.fromDomain(wishlistService.findByUserId(jwt.userId()))

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@RequestBody request: WishlistItemRequestDto, @AuthenticationPrincipal jwt: Jwt): WishlistItemDto =
        WishlistItemDto.fromDomain(wishlistService.addItemToWishlist(request.toServiceRequest(), jwt.userId()))

    @PutMapping("/items/{itemId}")
    fun update(
        @PathVariable itemId: Int,
        @RequestBody request: WishlistItemRequestDto,
        @AuthenticationPrincipal jwt: Jwt
    ): WishlistItemDto =
        WishlistItemDto.fromDomain(
            wishlistService.updateItem(
                WishlistItemId(itemId), request.toServiceRequest(), jwt.userId()
            )
        )

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable itemId: Int, @AuthenticationPrincipal jwt: Jwt) {
        wishlistService.removeItem(WishlistItemId(itemId), jwt.userId())
    }
}

data class WishlistItemRequestDto(val name: String, val description: String? = null, val link: String? = null) {
    fun toServiceRequest() = AddWishlistItemRequest(name, description, link)
}

data class GetWishlistResponseDto(val items: List<WishlistItemDto>) {
    companion object {
        fun fromDomain(wishlist: Wishlist?) = GetWishlistResponseDto(
            wishlist?.items?.map(WishlistItemDto::fromDomain) ?: emptyList()
        )
    }
}

data class WishlistItemDto(
    val id: Int,
    val name: String,
    val description: String?,
    val link: String?,
) {
    companion object {
        fun fromDomain(item: WishlistItem) =
            WishlistItemDto(requireNotNull(item.id).value, item.name, item.description, item.link)
    }
}
