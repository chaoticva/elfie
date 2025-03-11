package me.chaoticva.elfie

fun main() {
    val lexer = Lexer("example.elf")
    val parser = Parser(lexer)
    parser.parse()
}