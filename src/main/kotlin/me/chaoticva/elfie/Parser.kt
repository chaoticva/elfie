package me.chaoticva.elfie

import com.google.gson.GsonBuilder
import me.chaoticva.elfie.node.*
import java.io.File

class Parser(private val lexer: Lexer) {
    private val tokens = lexer.tokenize()
    private var pos = 0

    private fun consume(type: TokenType) {
        if (tokens[pos].type != type) {
            val current = tokens[pos]
            throw IllegalStateException("Unexpected token type ${current.type}. Expected $type at ${current.col} in line ${current.line}")
        }

        pos++
    }

    fun parse(): Program {
        val nodes = arrayListOf<ASTNode>()

        while (tokens[pos].type != TokenType.EOF) {
            when (tokens[pos].type) {
                TokenType.LET -> nodes.add(letStatement())
                TokenType.IDENTIFIER -> {
                    nodes.add(identifier())
                    consume(TokenType.SEMICOLON)
                }
//                TokenType.FUN -> nodes.add(functionDefinition())
                else -> {}
            }
        }

        generateAstTree(nodes)

        return Program(nodes)
    }

    private fun identifier(): ASTNode {
        val token = tokens[pos]
        consume(TokenType.IDENTIFIER)

        if (tokens[pos].type == TokenType.EQUALS) {
            return reassignmentStatement(token)
        }
        if (tokens[pos].type == TokenType.OPEN_PAREN) {
            return functionInvoke(token)
        }

        return ASTNode("N/A")
    }

    private fun functionInvoke(nameToken: Token): ASTNode {
        consume(TokenType.OPEN_PAREN)
        val arguments = arrayListOf<ASTNode>()

        if (tokens[pos].type != TokenType.CLOSE_PAREN) {
            var argument = expr()

            arguments.add(argument)

            while (tokens[pos].type == TokenType.COMMA) {
                consume(TokenType.COMMA)
                argument = expr()
                arguments.add(argument)
            }
        }

        consume(TokenType.CLOSE_PAREN)

        return FunctionInvokeNode(nameToken.value!!, arguments)
    }

    private fun functionDefinition(nameToken: Token): ASTNode {
        consume(TokenType.OPEN_PAREN)
        val parameters = arrayListOf<Parameter>()

        if (tokens[pos].type != TokenType.CLOSE_PAREN) {
            var paramName = tokens[pos].value!!
            consume(TokenType.IDENTIFIER)
            parameters.add(Parameter(paramName))

            while (tokens[pos].type == TokenType.COMMA) {
                consume(TokenType.COMMA)
                paramName = tokens[pos].value!!
                consume(TokenType.IDENTIFIER)
                parameters.add(Parameter(paramName))
            }
        }

        consume(TokenType.CLOSE_PAREN)
        consume(TokenType.OPEN_BRACE)
        val body = arrayListOf<ASTNode>()

        while (tokens[pos].type != TokenType.CLOSE_BRACE) {
            when (tokens[pos].type) {
                TokenType.LET -> body.add(letStatement())
                // TODO: add if and stuffs later
                else -> {}
            }
        }

        consume(TokenType.CLOSE_BRACE)

        return FunctionNode(nameToken.value!!, parameters, body)
    }

    private fun reassignmentStatement(nameToken: Token): ASTNode {
        consume(TokenType.EQUALS)
        val value = expr()

        return ReassignmentNode(nameToken.value!!, value)
    }

    private fun generateAstTree(nodes: ArrayList<ASTNode>) {
        val gson = GsonBuilder().setPrettyPrinting().create()

        File("${lexer.path}.ast.json").writeText(
            gson.toJson(nodes)
        )
    }

    private fun letStatement(): ASTNode {
        consume(TokenType.LET)
        val name = tokens[pos].value!!
        consume(TokenType.IDENTIFIER)
        consume(TokenType.EQUALS)
        val value = expr()
        consume(TokenType.SEMICOLON)

        return LetNode(name, value)
    }

    private fun factor(): ASTNode {
        val current = tokens[pos]

        when (current.type) {
            TokenType.STRING -> {
                consume(TokenType.STRING)
                return StringNode(current.value!!)
            }
            TokenType.NUMBER -> {
                consume(TokenType.NUMBER)
                return NumberNode(current.value!!.toInt())
            }
            TokenType.BOOLEAN -> {
                consume(TokenType.BOOLEAN)
                return BooleanNode(current.value!!.toBoolean())
            }
            else -> {}
        }

        return ASTNode("N/A")
    }

    private fun term(): ASTNode {
        var node = factor()

        while (tokens[pos].type in listOf(TokenType.ASTERISK, TokenType.F_SLASH)) {
            val operator = tokens[pos].value!!
            consume(tokens[pos].type)

            node = BinaryOperationNode(operator, node, factor())
        }

        return node
    }

    private fun expr(): ASTNode {
        var node = term()

        while (tokens[pos].type in listOf(TokenType.PLUS, TokenType.MINUS)) {
            val operator = tokens[pos].value!!
            consume(tokens[pos].type)

            node = BinaryOperationNode(operator, node, term())
        }

        return node
    }
}