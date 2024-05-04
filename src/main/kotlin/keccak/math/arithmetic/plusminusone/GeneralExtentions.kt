package keccak.math.arithmetic.plusminusone

import keccak.math.arithmetic.*
import java.util.*

fun ArithmeticNode.eliminate(variable: BooleanVariable): ArithmeticNode {
    return when (this) {
        is IntNumber, is InverseNumber, is BooleanVariable, is Multiply -> this
        is Sum -> {
            val varTerms = LinkedList<ArithmeticNode>()
            val otherTerms = LinkedList<ArithmeticNode>()

            for (node in nodes) {
                when (node) {
                    is IntNumber, is InverseNumber -> {
                        otherTerms.add(node)
                    }
                    is BooleanVariable -> {
                        if (node == variable) {
                            varTerms.add(node)
                        } else {
                            otherTerms.add(node)
                        }
                    }
                    is Multiply -> {
                        if (node.nodes.any { it == variable }) {
                            varTerms.add(node)
                        } else {
                            otherTerms.add(node)
                        }
                    }
                    is Sum -> throw IllegalStateException("Sum node shouldn't appear here")
                }
            }

            Sum(varTerms).pow(2) + Sum(otherTerms).pow(2)*(-1)
        }
    }
}
