package me.chaoticva.elfie

enum class TokenType(val seq: String?) {
    FUN("fun"),
    LET("let"),
    IF("if"),
    ELSE("else"),
    WHILE("while"),
    FOR("for"),
    ASM("asm"),
    STRUCT("struct"),
    IMPORT("import"),

    PLUS("+"),
    MINUS("-"),
    ASTERISK("*"),
    F_SLASH("/"),
    MOD("%"),
    EQUALS("="),
    OPEN_PAREN("("),
    OPEN_BRACE("{"),
    OPEN_ANGLE("<"),
    CLOSE_PAREN(")"),
    CLOSE_BRACE("}"),
    CLOSE_ANGLE(">"),
    COMMA(","),
    SEMICOLON(";"),
    COLON(":"),
    PIPE("|"),
    BANG("!"),

    IDENTIFIER("IDENTIFIER"),
    EOF(null)
}