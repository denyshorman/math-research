package keccak.math.arithmetic.zeroone

import keccak.*
import keccak.math.arithmetic.*
import keccak.util.toInt
import java.math.BigInteger

fun Node.replaceXorWithSum(): ArithmeticNode {
    return when (this) {
        is Bit, is Variable -> toArithmeticNode()
        is And -> Multiply(nodes.asSequence().map { it.replaceXorWithSum() })
        is Xor -> Sum(nodes.asSequence().map { it.replaceXorWithSum() })
    }
}

fun ArithmeticNode.evaluate(variables: Map<BooleanVariable, IntNumber>): BigInteger {
    return when (this) {
        is IntNumber -> value
        is InverseNumber -> throw IllegalArgumentException("Inverse numbers are not supported")
        is BooleanVariable -> variables[this]?.value ?: throw Exception("Not value defined for the variable $this")
        is Sum -> nodes.sumOf { it.evaluate(variables) }
        is Multiply -> nodes.fold(BigInteger.ONE) { acc, node -> acc * node.evaluate(variables) }
    }
}

fun ArithmeticNode.evaluate(variables: BooleanArray, variablePrefix: String = "x", indexMap: IntArray? = null): BigInteger {
    val vars = variables.asSequence()
        .mapIndexed { index, value ->
            val varIndex = if (indexMap == null) index else indexMap[index]
            BooleanVariable("$variablePrefix$varIndex", BooleanVariable.Type.ZERO_ONE) to IntNumber(value.toInt())
        }
        .toMap()

    return evaluate(vars)
}

fun ArithmeticNode.reduceBy(num: IntNumber): ArithmeticNode {
    return when (this) {
        is IntNumber -> if (value % num.value == BigInteger.ZERO) {
            IntNumber(value / num.value)
        } else {
            throw Exception("Reduction is not possible")
        }
        is InverseNumber -> throw IllegalArgumentException("Inverse numbers are not supported")
        is BooleanVariable -> this
        is Multiply -> Multiply(nodes.asSequence().map { if (it !is Sum) it.reduceBy(num) else it })
        is Sum -> Sum(nodes.asSequence().map { it.reduceBy(num) })
    }
}

fun ArithmeticNode.moduloBy(num: IntNumber): ArithmeticNode {
    return when (this) {
        is IntNumber -> IntNumber(value.mod(num.value))
        is InverseNumber -> throw IllegalArgumentException("Inverse numbers are not supported")
        is BooleanVariable -> this
        is Multiply -> Multiply(nodes.asSequence().map { if (it !is Sum) it.moduloBy(num) else it })
        is Sum -> Sum(nodes.asSequence().map { it.moduloBy(num) })
    }
}

fun ArithmeticNode.andBy(num: IntNumber): ArithmeticNode {
    return when (this) {
        is IntNumber -> IntNumber(value.and(num.value))
        is InverseNumber -> throw IllegalArgumentException("Inverse numbers are not supported")
        is BooleanVariable -> this
        is Multiply -> Multiply(nodes.asSequence().map { if (it !is Sum) it.andBy(num) else it })
        is Sum -> Sum(nodes.asSequence().map { it.andBy(num) })
    }
}

fun areEqual(l: ArithmeticNode, r: ArithmeticNode, varsCount: Int): Boolean {
    val iter = CombinationIteratorSimple(varsCount)

    iter.iterateAll {
        val lRes = l.evaluate(iter.combination)
        val rRes = r.evaluate(iter.combination)
        if (lRes != rRes) return false
    }

    return true
}
