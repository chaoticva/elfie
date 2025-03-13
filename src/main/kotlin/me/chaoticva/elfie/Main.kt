package me.chaoticva.elfie

fun main() {
    val lexer = Lexer("example.elfie")
    val parser = Parser(lexer)
    val compiler = Compiler(parser)
    compiler.compile()
}