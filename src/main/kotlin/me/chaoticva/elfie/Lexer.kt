package me.chaoticva.elfie

import com.google.gson.GsonBuilder
import java.io.File
import java.util.regex.Pattern
import kotlin.collections.ArrayList

class Lexer(val path: String) {
    private var pos = 0
    private val content = File(path).readText()
    private var current: Char? = null
    private var column = 0
    private var line = 1

    init {
        if (pos < content.length) {
            current = content[pos]
        }
    }

    private fun advance() {
        pos++

        current = if (pos >= content.length) {
            null
        } else {
            content[pos]
        }
    }

    private fun peek(): Char? {
        return if (pos + 1 < content.length) {
            content[pos + 1]
        } else null
    }

    fun tokenize(): ArrayList<Token> {
        val tokens = ArrayList<Token>()

        while (current != null) {
            while (current != null && current!!.isWhitespace()) {
                column++
                if (current in listOf('\n', '\r')) {
                    column = 0
                    line++
                }
                advance()
            }

            if (current == '-' && peek() == '-') {
                while (current != null && current !in listOf('\n', '\r')) {
                    advance()
                }
            }
            if (current != null && current == '"')
                tokens.add(string())
            if (current != null && current!!.isDigit())
                tokens.add(number())
            if (current != null && current!!.isLetter())
                tokens.add(identifier())


            val tokenType = TokenType.entries.find {
                it.seq != null && it.seq == current.toString() // lets add sth
            }

            if (tokenType != null) {
                val symbol = current.toString()
                column++
                advance()
                tokens.add(Token(tokenType, symbol, line, column))
            }
        }

        tokens.add(Token(TokenType.EOF, null, line, column))

        generateTokenTree(tokens)

        return tokens
    }

    private fun generateTokenTree(tokens: ArrayList<Token>) {
        val gson = GsonBuilder().setPrettyPrinting().create()

        File("$path.tokens.json").writeText(
            gson.toJson(tokens)
        )
    }

    private fun string(): Token {
        advance()
        var value = ""
        column++

        while (current != null && current != '"') {
            if (current == '\\') {
                advance()
                value += current
                advance()
                column++
                column++
                continue
            }
            column++
            value += current
            advance()
        }

        column++
        advance()

        return Token(TokenType.STRING, value, line, column)
    }

    private fun identifier(): Token {
        var value = ""

        while (current != null && (current!!.isLetterOrDigit() || current!! == '_')) {
            value += current.toString();
            column++
            this.advance()
        }

        val tokenType = TokenType.entries.find {
            it.seq != null &&
                    Pattern.matches("[a-z]+", it.seq) &&
                    it.seq == value
        }

        if (tokenType != null) {
            return Token(tokenType, value, line, column)
        }

        return Token(TokenType.IDENTIFIER, value, line, column)
    }

    private fun number(): Token {
        var value = ""

        while (current != null && (current!!.isLetterOrDigit() || current in listOf('.', '_'))) {
            value += current.toString();
            column++
            this.advance()
        }

        if (!Pattern.matches("(([0-9]+)([0-9._]*))|([0-9]x[0-9]{0,8})", value)) {
            throw RuntimeException("Illegal number format")
        }

        return Token(TokenType.NUMBER, value, line, column)
    }
}