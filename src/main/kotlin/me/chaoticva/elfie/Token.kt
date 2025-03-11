package me.chaoticva.elfie

class Token(
    val type: TokenType,
    val value: String?,
    val line: Int,
    val col: Int
) {
    override fun toString(): String {
        return "Token($type, $value)"
    }
}