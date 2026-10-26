package ru.nikzarch.secretsanta.domain

import java.util.regex.Pattern

class WishlistItem(
    val id: Int,
    val name: String,
    val description: String?,
    link: String? = null,
) {
    companion object {
        private val LINK_PATTERN = Pattern.compile(
            "^[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,63}\\b(?:[-a-zA-Z0-9()@:%_+.~#?&/=]*)$"
        )
    }

    var link: String? = link
        set(value) {
            require(value == null || LINK_PATTERN.matcher(value).matches()) {
                "Link must be valid"
            }
            field = value
        }
}