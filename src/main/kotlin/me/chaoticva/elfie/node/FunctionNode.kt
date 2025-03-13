package me.chaoticva.elfie.node

class FunctionNode(val name: String, val arguments: ArrayList<Argument>, val body: ArrayList<ASTNode>): ASTNode("fun")