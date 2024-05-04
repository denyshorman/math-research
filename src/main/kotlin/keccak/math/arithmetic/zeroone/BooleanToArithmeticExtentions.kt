package keccak.math.arithmetic.zeroone

import keccak.*
import keccak.math.arithmetic.ArithmeticNode
import keccak.math.arithmetic.BooleanVariable
import keccak.math.arithmetic.IntNumber
import keccak.math.arithmetic.Multiply
import keccak.math.arithmetic.Sum
import keccak.util.pow2
import java.util.*

fun Bit.toIntNumber(): IntNumber {
    return IntNumber(if (this.value) 1 else 0)
}

fun Variable.toBooleanVariable(): BooleanVariable {
    return BooleanVariable(name, BooleanVariable.Type.ZERO_ONE)
}

fun Xor.toSum(): Sum {
    val nodesArray = nodes.toTypedArray()
    val sumNodes = LinkedList<ArithmeticNode>()

    var k = 0
    while (k < nodesArray.size) {
        val iter = CombWithoutRepetitionIterator(nodesArray.size, k + 1)

        val mult = run {
            var v = pow2(k)
            if (k % 2 == 1) v = -v
            v.toInt()
        }

        iter.iterateAll {
            val multNodes = LinkedList<ArithmeticNode>()
            multNodes.add(IntNumber(mult))

            var j = 0
            while (j < iter.combination.size) {
                val node = nodesArray[iter.combination[j]].toArithmeticNode()
                multNodes.add(node)
                j++
            }

            sumNodes.add(Multiply(multNodes))
        }

        k++
    }

    return Sum(sumNodes)
}

fun And.toMultiplication(): Multiply {
    return Multiply(nodes.asSequence().map { it.toArithmeticNode() })
}

fun Node.toArithmeticNode(): ArithmeticNode {
    return when (this) {
        is Bit -> toIntNumber()
        is Variable -> toBooleanVariable()
        is Xor -> toSum()
        is And -> toMultiplication()
    }
}
