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
            while (tokens[pos].type == TokenType.IMPORT) {
                nodes.add(importNode())
            }

            when (tokens[pos].type) {
                TokenType.LET -> nodes.add(letStatement())
                TokenType.IDENTIFIER -> {
                    nodes.add(identifier())
                    consume(TokenType.SEMICOLON)
                }
                TokenType.FUN -> nodes.add(functionDefinition())
                else -> {}
            }
        }

        generateAstTree(nodes)

        return Program(nodes)
    }

    private fun importNode(): ASTNode {
        consume(TokenType.IMPORT)
        val path = arrayListOf(tokens[pos].value!!)
        consume(TokenType.IDENTIFIER)

        while (tokens[pos].type == TokenType.COLON) {
            consume(TokenType.COLON)
            path.add(tokens[pos].value!!)
            consume(TokenType.IDENTIFIER)
        }

        consume(TokenType.SEMICOLON)

        return ImportNode(path)
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

    private fun body(): ArrayList<ASTNode> {
        val body = arrayListOf<ASTNode>()

        while (tokens[pos].type != TokenType.CLOSE_BRACE) {
            when (tokens[pos].type) {
                TokenType.LET -> body.add(letStatement())
                TokenType.IF -> body.add(ifStatement())
                else -> {}
            }
        }

        return body
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

    private fun functionDefinition(): ASTNode {
        consume(TokenType.FUN)
        val name = tokens[pos].value!!
        consume(TokenType.IDENTIFIER)
        consume(TokenType.OPEN_PAREN)
        val arguments = arrayListOf<Argument>()

        if (tokens[pos].type != TokenType.CLOSE_PAREN) {
            var paramName = tokens[pos].value!!
            consume(TokenType.IDENTIFIER)
            arguments.add(Argument(paramName))

            while (tokens[pos].type == TokenType.COMMA) {
                consume(TokenType.COMMA)
                paramName = tokens[pos].value!!
                consume(TokenType.IDENTIFIER)
                arguments.add(Argument(paramName))
            }
        }

        consume(TokenType.CLOSE_PAREN)
        consume(TokenType.OPEN_BRACE)
        val body = body()
        consume(TokenType.CLOSE_BRACE)

        return FunctionNode(name, arguments, body)
    }

    private fun bit(): ASTNode {
        var node = factor()

        while (tokens[pos].type in listOf(TokenType.PIPE)) {
            consume(tokens[pos].type)
            val factor = factor()
            node = BinaryOperationNode("|", node, factor)
        }

        return node
    }

    private fun op(): ASTNode {
        var node = bit()

        while (tokens[pos].type in listOf(TokenType.BANG, TokenType.EQUALS, TokenType.OPEN_ANGLE, TokenType.CLOSE_ANGLE)) {
            var operator = tokens[pos].value
            consume(tokens[pos].type)
            if (tokens[pos].type == TokenType.EQUALS) {
                operator += "="
                consume(tokens[pos].type)
            }
            val factor = bit()
            node = BinaryOperationNode(operator!!, node, factor)
        }

        return node
    }


    private fun ifStatement(): ASTNode {
        consume(TokenType.IF);
        val condition = expr();

        consume(TokenType.OPEN_BRACE)
        val body = body()
        consume(TokenType.CLOSE_BRACE)

        var elseIf: IfNode? = null
        var elseBody = arrayListOf<ASTNode>()

        if (tokens[pos].type == TokenType.ELSE) {
            consume(TokenType.ELSE);
                if (tokens[pos].type == TokenType.IF) {
                    elseIf = ifStatement() as IfNode

                } else {
                    consume(TokenType.OPEN_BRACE)
                    elseBody = body()
                    consume(TokenType.CLOSE_BRACE)
                }
        }

        return IfNode(condition, body, elseBody, elseIf)
    }


    //         private fun ifNode(): IfNode {
    //  3         val lineStart = current.lineStart
    //  2         val line = current.line
    //  1         tryConsume(TokenType.IF)
    //  1         val condition = expr()
    //  3         val thenBody = arrayListOf<ASTNode>()
    //  4
    //  5         tryConsume(TokenType.OPEN_BRACE)
    //  6
    //  7         body(thenBody, TokenType.CLOSE_BRACE)
    //  8
    //  9         tryConsume(TokenType.CLOSE_BRACE)
    // 10
    // 11         if (current.type == TokenType.ELSE) {
    // 12             consume()
    // 13             if (current.type == TokenType.IF) {
    // 14                 val ifNode = ifNode()
    // 15                 return IfNode(condition, ScopeNode(thenBody, false, lineStart, thenBody.last().lineEnd, line), null, ifNode, lineStart, ifNode.lineEnd, ifNode.line)
    // 16             } else {
    // 17                 val elseBody = arrayListOf<ASTNode>()
    // 18                 tryConsume(TokenType.OPEN_BRACE)
    // 19
    // 20                 body(elseBody, TokenType.CLOSE_BRACE)
    // 21
    // 22                 val lineEnd = current.lineEnd
    // 23                 tryConsume(TokenType.CLOSE_BRACE)
    // 24                 return IfNode(condition, ScopeNode(thenBody, false, lineStart, thenBody.last().lineEnd, line), ScopeNode(elseBody, false, lineStart, elseBody.last().lineEnd, line), null, lineStart, lineEnd, line)
    // 25             }
    // 26         }
    // 27         val lineEnd = current.lineEnd
    // 28
    // 29         return IfNode(condition, ScopeNode(thenBody, false, lineStart, thenBody.last().lineEnd, line), null, null, lineStart, lineEnd, line)
    // 30     }
    // also the condition is just an expression. i had some extra stuff being called other that expr, term and factor for other prefixes like || && >= <= < > == != easy

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
        var node = op()

        while (tokens[pos].type in listOf(TokenType.ASTERISK, TokenType.F_SLASH)) {
            val operator = tokens[pos].value!!
            consume(tokens[pos].type)

            node = BinaryOperationNode(operator, node, op())
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