package me.chaoticva.elfie.node

class IfNode(val condition: ASTNode, val body: ArrayList<ASTNode>, val elseBody: ArrayList<ASTNode>, val elseIfNode: IfNode? = null) : ASTNode("if")