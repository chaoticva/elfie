package me.chaoticva.elfie.node

class BinaryOperationNode(val operator: String, val left: ASTNode, val right: ASTNode): ASTNode("bin_op")