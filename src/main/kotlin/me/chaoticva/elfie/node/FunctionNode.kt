package me.chaoticva.elfie.node

class FunctionNode(val name: String, val parameters: ArrayList<Parameter>, val body: ArrayList<ASTNode>): ASTNode("fun")