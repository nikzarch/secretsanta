package ru.nikzarch.secretsanta.web

import jakarta.validation.constraints.Positive
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.nikzarch.secretsanta.domain.WishlistItem
import ru.nikzarch.secretsanta.domain.vo.UserId
import ru.nikzarch.secretsanta.service.WishlistService


@RestController
@RequestMapping("/api/v1")
class WishlistController(
    private val wishlistService: WishlistService
) {

    @GetMapping("/user/{id}/wishlist")
    fun getWishlistForUser(@PathVariable(name = "id") @Positive id: Int): GetWishlistResponseDto {
        val items = wishlistService.findByUserId(UserId(id))?.items?.map(WishlistItemDto::fromServiceResponse)
        if (!items.isNullOrEmpty()) {
            return GetWishlistResponseDto(items)
        }
        return GetWishlistResponseDto(emptyList())
    }


}

data class GetWishlistResponseDto(
    val items: List<WishlistItemDto>
)

data class WishlistItemDto(
    val id: Int,
    val name: String,
    val description: String?,
    val link: String?,
) {
    companion object {
        fun fromServiceResponse(item: WishlistItem) = WishlistItemDto(
            item.id!!.value,
            item.name,
            item.description,
            item.link
        )
    }

}