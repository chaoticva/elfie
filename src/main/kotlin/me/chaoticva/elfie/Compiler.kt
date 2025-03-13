package me.chaoticva.elfie

import me.chaoticva.elfie.node.ASTNode
import me.chaoticva.elfie.node.LetNode

class Compiler(private val parser: Parser) {
    private val asm = StringBuilder()
    private val variables = HashMap<String, Int>() // name, pointer

    fun compile() {
        val program = parser.parse()

        program.children.forEach { compileNode(it) }
    }

    private fun compileNode(node: ASTNode) {
        when (node) {
            is LetNode -> compileLetNode(node)
        }
    }

    private fun compileLetNode(node: LetNode) {
        push()
    }

    private fun push(register: String, value: Any) {
        asm.appendLine()
    }
}