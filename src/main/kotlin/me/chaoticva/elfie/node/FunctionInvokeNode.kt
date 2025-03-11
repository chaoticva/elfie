package me.chaoticva.elfie.node

class FunctionInvokeNode(val name: String, val arguments: ArrayList<ASTNode>): ASTNode("invoke")