package keccak.math.arithmetic

import keccak.util.exchange
import keccak.util.gcd
import keccak.util.lcm
import java.math.BigInteger

fun solveLinearSystem(
    eqs: Array<ArithmeticNode>,
    expressNodes: Array<ArithmeticNode>,
): Boolean {
    var i = 0
    for (expressNode in expressNodes) {
        var found = false

        for (j in i until eqs.size) {
            if (eqs[j].contains(expressNode)) {
                eqs.exchange(i, j)
                found = true
                break
            }
        }

        if (found) {
            express(eqs, expressNode, i)
            i++
        }
    }

    return true
}

private fun ArithmeticNode.contains(node: ArithmeticNode): Boolean {
    return when (this) {
        is IntNumber, is InverseNumber, is BooleanVariable -> this == node
        is Multiply -> Multiply(nodes.asSequence().filter { it is BooleanVariable }).unwrap() == node
        is Sum -> nodes.any { it.contains(node) }
    }
}

private fun ArithmeticNode.substitute(src: ArithmeticNode, dst: ArithmeticNode): ArithmeticNode {
    return when (this) {
        is IntNumber, is InverseNumber -> this
        is BooleanVariable -> if (this == src) dst else this
        is Sum -> Sum(nodes.asSequence().map { it.substitute(src, dst) })
        is Multiply -> {
            when (src) {
                is BooleanVariable -> {
                    if (contains(src)) {
                        substitute(mapOf(src to dst))
                    } else {
                        this
                    }
                }
                is Multiply -> {
                    if (contains(src)) {
                        Multiply(nodes.asSequence().filter { it !is BooleanVariable } + sequenceOf(dst)).unwrap()
                    } else {
                        this
                    }
                }
                else -> throw IllegalStateException("variables and product are supported only")
            }
        }
    }
}

private fun getRhs(eqs: Array<ArithmeticNode>, expressNode: ArithmeticNode, i: Int): ArithmeticNode {
    return when (val eq = eqs[i]) {
        is IntNumber, is InverseNumber -> throw IllegalStateException("Can't express numbers")
        is BooleanVariable, is Multiply -> throw Exception("$eq can't equal to 0")
        is Sum -> {
            val (l,r) = eq.nodes.partition { it.contains(expressNode) }

            if (l.size != 1) {
                throw IllegalStateException("Can't express $eq")
            }

            val mult = when(val lhs = l.first()) {
                is BooleanVariable -> emptySequence()
                is Multiply -> lhs.nodes.asSequence()
                    .filter { it is IntNumber || it is InverseNumber }
                    .map {
                        when (it) {
                            is IntNumber -> InverseNumber(it.value)
                            is InverseNumber -> IntNumber(it.value)
                            else -> throw IllegalStateException()
                        }
                    }
                else -> throw IllegalStateException()
            }

            Multiply(sequenceOf(IntNumber(-1)) + mult + sequenceOf(Sum(r)))
        }
    }
}

private fun ArithmeticNode.multiplyByLcm(): ArithmeticNode {
    return when (this) {
        is IntNumber -> {
            if (value == BigInteger.ZERO) {
                this
            } else {
                throw IllegalStateException("$this = 0 is incorrect")
            }
        }
        is InverseNumber, is BooleanVariable, is Multiply -> throw IllegalStateException("$this = 0 is incorrect")
        is Sum -> {
            var lcmValue = BigInteger.ONE

            for (node in nodes) {
                when (node) {
                    is InverseNumber -> lcmValue = lcm(lcmValue, node.value)
                    is Multiply -> {
                        var finalInverseNumber = BigInteger.ONE

                        for (multNode in node.nodes) {
                            if (multNode is InverseNumber) {
                                finalInverseNumber *= multNode.value
                            }
                        }

                        if (finalInverseNumber != BigInteger.ONE) {
                            lcmValue = lcm(lcmValue, finalInverseNumber)
                        }
                    }
                    else -> {}
                }
            }

            if (lcmValue == BigInteger.ONE) {
                this
            } else {
                (IntNumber(lcmValue) * this).expand()
            }
        }
    }
}

private fun ArithmeticNode.divideByGcd(): ArithmeticNode {
    return when (this) {
        is IntNumber -> {
            if (value == BigInteger.ZERO) {
                this
            } else {
                throw IllegalStateException("$this = 0 is incorrect")
            }
        }
        is InverseNumber, is BooleanVariable, is Multiply -> throw IllegalStateException("$this = 0 is incorrect")
        is Sum -> {
            var gcdValue = null as BigInteger?

            nodes.asSequence()

            for (node in nodes) {
                when (node) {
                    is IntNumber -> {
                        gcdValue = if (gcdValue == null) {
                            node.value.abs()
                        } else {
                            gcd(gcdValue, node.value)
                        }
                    }
                    is InverseNumber, is Sum -> throw IllegalStateException("Shouldn't be here")
                    is BooleanVariable -> {
                        gcdValue = BigInteger.ONE
                        break
                    }
                    is Multiply -> {
                        var num = BigInteger.ONE

                        for (multNode in node.nodes) {
                            if (multNode is IntNumber) {
                                num *= multNode.value
                            }
                        }

                        if (num == BigInteger.ONE) {
                            gcdValue = BigInteger.ONE
                            break
                        } else {
                            gcdValue = if (gcdValue == null) {
                                num.abs()
                            } else {
                                gcd(gcdValue, num)
                            }
                        }
                    }
                }
            }

            if (gcdValue == null || gcdValue == BigInteger.ONE) {
                this
            } else {
                (InverseNumber(gcdValue) * this).expand()
            }
        }
    }
}

private fun express(eqs: Array<ArithmeticNode>, expressNode: ArithmeticNode, i: Int) {
    val rhs = getRhs(eqs, expressNode, i)

    eqs.forEachIndexed { j, eq ->
        if (i != j && eq.contains(expressNode)) {
            val substituted = eq.substitute(expressNode, rhs)
            val expanded = substituted.expand()
            val multiplied = expanded.multiplyByLcm()
            val divided = multiplied.divideByGcd()
            eqs[j] = divided
        }
    }
}
