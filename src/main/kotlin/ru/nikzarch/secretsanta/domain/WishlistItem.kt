package ru.nikzarch.secretsanta.domain

import ru.nikzarch.secretsanta.domain.vo.WishlistItemId
import java.util.regex.Pattern

class WishlistItem(
    val id: WishlistItemId? = null,
    val name: String,
    val description: String?,
    link: String? = null
) {
    companion object {
        private val LINK_PATTERN = Pattern.compile(
            "^[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,63}\\b(?:[-a-zA-Z0-9()@:%_+.~#?&/=]*)$"
        )
    }

    init {
        require(name.isNotBlank()) { "name must not be blank" }
        validateLink(link)
    }

    var link: String? = link
        set(value) {
            validateLink(value)
            field = value
        }

    private fun validateLink(value: String?) {
        require(value == null || LINK_PATTERN.matcher(value).matches()) {
            "link must be valid"
        }
    }
}