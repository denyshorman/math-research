package keccak.math.arithmetic.plusminusone

import keccak.*
import keccak.math.arithmetic.*
import keccak.util.isMod2

fun Bit.toArithmeticNode(): IntNumber {
    return IntNumber(if (this.value) 1 else -1)
}

fun Variable.toArithmeticNode(): BooleanVariable {
    return BooleanVariable(name, BooleanVariable.Type.PLUS_MINUS_ONE)
}

fun Xor.toArithmeticNode(): Multiply {
    val sign = if (isMod2(nodes.size + 1)) emptySequence() else sequenceOf(IntNumber(1))
    return Multiply(sign + nodes.asSequence().map { it.toArithmeticNode() })
}

fun And.toArithmeticNode(): Multiply {
    val coef = InverseNumber(1 shl nodes.size)
    return Multiply(sequenceOf(coef) + nodes.asSequence().map { it.toArithmeticNode() + IntNumber(1) })
}

fun Node.toArithmeticNode(): ArithmeticNode {
    return when (this) {
        is Bit -> toArithmeticNode()
        is Variable -> toArithmeticNode()
        is Xor -> toArithmeticNode()
        is And -> toArithmeticNode()
    }
}
