package keccak.math.arithmetic

import keccak.util.gcd
import keccak.util.lcm
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*

fun ArithmeticNode.unwrap(): ArithmeticNode {
    return when (this) {
        is Sum -> when {
            nodes.size == 1 -> nodes.first().unwrap()
            nodes.size >= 2 -> this
            else -> this
        }
        is Multiply -> when {
            nodes.size == 1 -> nodes.first().unwrap()
            nodes.size >= 2 -> this
            else -> this
        }
        else -> this
    }
}

fun ArithmeticNode.pow(i: Int): ArithmeticNode {
    if (i < 0) {
        throw IllegalArgumentException("i should be non-negative")
    }

    if (i == 0) {
        return IntNumber(BigInteger.ONE)
    }

    return Multiply((0 until i).map { this })
}

fun ArithmeticNode.substitute(variables: Map<BooleanVariable, ArithmeticNode>): ArithmeticNode {
    return when (this) {
        is IntNumber -> this
        is InverseNumber -> this
        is BooleanVariable -> variables[this] ?: this
        is Sum -> Sum(nodes.asSequence().map { it.substitute(variables) })
        is Multiply -> Multiply(nodes.asSequence().map { it.substitute(variables) })
    }
}

fun ArithmeticNode.collectTerms(): ArithmeticNode {
    return when (this) {
        is IntNumber, is InverseNumber, is BooleanVariable -> this
        is Sum -> {
            //region Collect terms
            val terms = LinkedList<ArithmeticNode>()

            fun Sum.collectTerms() {
                for (node in nodes) {
                    when (node) {
                        is IntNumber, is InverseNumber, is BooleanVariable -> terms.add(node)
                        is Sum -> node.collectTerms()
                        is Multiply -> when (val newNode = node.collectTerms()) {
                            is IntNumber, is InverseNumber, is BooleanVariable, is Multiply -> terms.add(newNode)
                            is Sum -> newNode.collectTerms()
                        }
                    }
                }
            }

            collectTerms()
            //endregion

            var numSum = BigInteger.ZERO
            val termsMap = HashMap<Multiply, BigInteger>()
            var lcmValue = BigInteger.ONE

            //region Find LCM
            for (node in terms) {
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
            //endregion

            //region Collect terms
            for (node in terms) {
                when (node) {
                    is IntNumber -> {
                        numSum += node.value * lcmValue
                    }
                    is InverseNumber -> {
                        numSum += lcmValue / node.value
                    }
                    is BooleanVariable -> {
                        val multNode = Multiply(node)
                        val count = termsMap.getOrDefault(multNode, BigInteger.ZERO) + lcmValue
                        termsMap[multNode] = count
                    }
                    is Multiply -> {
                        var nominator = lcmValue
                        var denominator = BigInteger.ONE
                        val multTerms = LinkedList<ArithmeticNode>()

                        for (multTerm in node.nodes) {
                            when (multTerm) {
                                is IntNumber -> {
                                    nominator *= multTerm.value
                                }
                                is InverseNumber -> {
                                    denominator *= multTerm.value
                                }
                                else -> {
                                    multTerms.add(multTerm)
                                }
                            }
                        }

                        val coeff = nominator / denominator

                        if (multTerms.isEmpty()) {
                            numSum += coeff
                        } else {
                            val mult = Multiply(multTerms)
                            val count = termsMap.getOrDefault(mult, BigInteger.ZERO) + coeff
                            termsMap[mult] = count
                        }
                    }
                    is Sum -> throw IllegalStateException("Sum node shouldn't appear here")
                }
            }
            //endregion

            //region Create sum nodes
            var sumNodes = LinkedList<ArithmeticNode>()

            if (numSum != BigInteger.ZERO) {
                sumNodes.add(IntNumber(numSum))
            }

            for ((node, count) in termsMap) {
                if (count == BigInteger.ONE) {
                    sumNodes.add(node)
                } else if (count != BigInteger.ZERO) {
                    sumNodes.add(Multiply(sequenceOf(IntNumber(count)) + node.nodes.asSequence()))
                }
            }
            //endregion

            //region Divide each term to LCM
            if (lcmValue != BigInteger.ONE) {
                val sumNodes2 = LinkedList<ArithmeticNode>()

                for (sumNode in sumNodes) {
                    val newNode = when (sumNode) {
                        is IntNumber -> {
                            val gcdValue = gcd(sumNode.value, lcmValue)

                            if (gcdValue == BigInteger.ONE) {
                                Multiply(sumNode, InverseNumber(lcmValue))
                            } else {
                                val n = sumNode.value / gcdValue
                                val d = lcmValue / gcdValue

                                if (d == BigInteger.ONE) {
                                    IntNumber(n)
                                } else {
                                    Multiply(IntNumber(n), InverseNumber(d))
                                }
                            }
                        }
                        is InverseNumber -> InverseNumber(sumNode.value * lcmValue)
                        is BooleanVariable -> Multiply(InverseNumber(lcmValue), sumNode)
                        is Multiply -> Multiply(sequenceOf(InverseNumber(lcmValue)) + sumNode.nodes.asSequence()).collectTerms()
                        is Sum -> throw IllegalStateException("Sum node shouldn't appear here")
                    }

                    sumNodes2.add(newNode)
                }

                sumNodes = sumNodes2
            }
            //endregion

            when (sumNodes.size) {
                0 -> IntNumber(BigInteger.ZERO)
                1 -> sumNodes.first()
                else -> Sum(sumNodes)
            }
        }
        is Multiply -> {
            var num = BigInteger.ONE
            var inverseNum = BigInteger.ONE
            val terms = LinkedList<ArithmeticNode>()

            fun Multiply.collectNodes() {
                for (node in nodes) {
                    when (node) {
                        is IntNumber -> num *= node.value
                        is InverseNumber -> inverseNum *= node.value
                        is BooleanVariable -> {
                            when (node.type) {
                                BooleanVariable.Type.ZERO_ONE -> {
                                    if (!terms.contains(node)) {
                                        terms.add(node)
                                    }
                                }
                                BooleanVariable.Type.PLUS_MINUS_ONE -> {
                                    if (!terms.remove(node)) {
                                        terms.add(node)
                                    }
                                }
                                BooleanVariable.Type.ANY -> {
                                    terms.add(node)
                                }
                            }
                        }
                        is Sum -> {
                            terms.add(Sum(node.nodes.map { it.collectTerms() }).unwrap())
                        }
                        is Multiply -> node.collectNodes()
                    }
                }
            }

            collectNodes()

            val gcdValue = gcd(num, inverseNum)

            if (gcdValue != BigInteger.ONE) {
                num /= gcdValue
                inverseNum /= gcdValue
            }

            if (num == BigInteger.ZERO || inverseNum == BigInteger.ONE && terms.size == 0) {
                return IntNumber(num)
            }

            val termsSec = sequence {
                if (num != BigInteger.ONE) {
                    yield(IntNumber(num))
                }

                if (inverseNum != BigInteger.ONE) {
                    yield(InverseNumber(inverseNum))
                }

                yieldAll(terms)
            }

            Multiply(termsSec).unwrap()
        }
    }
}

fun ArithmeticNode.expand(): ArithmeticNode {
    return when (val node = collectTerms()) {
        is IntNumber, is InverseNumber, is BooleanVariable -> node
        is Sum -> Sum(node.nodes.asSequence().map { it.expand() }).collectTerms()
        is Multiply -> {
            val multTerms = LinkedList<ArithmeticNode>()
            val sumTerms = LinkedList<Sum>()

            fun Multiply.collectTerms() {
                for (n in nodes) {
                    when (val expandedNode = n.expand()) {
                        is IntNumber, is InverseNumber, is BooleanVariable -> multTerms.add(expandedNode)
                        is Sum -> sumTerms.add(expandedNode)
                        is Multiply -> expandedNode.collectTerms()
                    }
                }
            }

            node.collectTerms()

            if (multTerms.any { it == IntNumber(BigInteger.ZERO) }) {
                return IntNumber(BigInteger.ZERO)
            }

            if (multTerms.size == 1) {
                sumTerms.addFirst(Sum(multTerms))
            } else if (multTerms.size > 1) {
                sumTerms.addFirst(Sum(Multiply(multTerms)))
            }

            val expandedSum = sumTerms.reduceOrNull { l, r ->
                val sumSeq = sequence {
                    for (lNode in l.nodes) {
                        for (rNode in r.nodes) {
                            yield(Multiply(lNode, rNode))
                        }
                    }
                }

                val sum = Sum(sumSeq).collectTerms()

                if (sum is Sum) sum else Sum(sum)
            } ?: throw IllegalStateException("Multiply node is empty")

            expandedSum.unwrap()
        }
    }
}

fun ArithmeticNode.evaluate(variables: Map<BooleanVariable, ArithmeticNode>): ArithmeticNode {
    return substitute(variables).expand()
}

fun ArithmeticNode.evaluateDouble(variables: Map<BooleanVariable, ArithmeticNode>): Double {
    return when (val node = evaluate(variables)) {
        is BooleanVariable -> throw IllegalStateException("BooleanVariable shouldn't appear here")
        is IntNumber -> node.value.toDouble()
        is InverseNumber -> BigDecimal.ONE.divide(node.value.toBigDecimal()).toDouble()
        is Multiply -> node.nodes.map { it.evaluateDouble(variables) }.fold(1.0) { l, r -> l * r }
        is Sum -> node.nodes.sumOf { it.evaluateDouble(variables) }
    }
}
