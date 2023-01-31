package keccak

import keccak.util.pow2
import keccak.util.toInt
import java.util.*

sealed interface ArithmeticNode : Comparable<ArithmeticNode>

@JvmInline
value class IntNumber(val value: Int = 0) : ArithmeticNode {
    override fun toString(): String {
        return if (value >= 0) {
            value.toString()
        } else {
            "($value)"
        }
    }

    override fun compareTo(other: ArithmeticNode): Int {
        return when (other) {
            is IntNumber -> value.compareTo(other.value)
            is BooleanVariable, is Sum, is Multiply -> -1
        }
    }
}

@JvmInline
value class BooleanVariable(val name: String) : ArithmeticNode {
    override fun toString(): String {
        return name
    }

    override fun compareTo(other: ArithmeticNode): Int {
        return when (other) {
            is IntNumber -> 1
            is Sum, is Multiply -> -1
            is BooleanVariable -> {
                val var0 = VariablePattern.matchEntire(name)
                val var1 = VariablePattern.matchEntire(other.name)

                if (var0 != null && var1 != null) {
                    val (name0, id0) = var0.destructured
                    val (name1, id1) = var1.destructured

                    val nameComp = name0.compareTo(name1)

                    if (nameComp == 0) {
                        id0.toLong().compareTo(id1.toLong())
                    } else {
                        nameComp
                    }
                } else {
                    name.compareTo(other.name)
                }
            }
        }
    }

    companion object {
        private val VariablePattern = """^([a-zA-Z]+)(\d+)$""".toRegex()
    }
}

class Sum : ArithmeticNode {
    private val hashCode: Lazy<Int>
    val nodes: List<ArithmeticNode>

    constructor(vararg initNodes: ArithmeticNode) : this(initNodes.asIterable())
    constructor(initNodes: Sequence<ArithmeticNode>) : this(initNodes.asIterable())

    constructor(initNodes: Iterable<ArithmeticNode>) {
        val nodes0 = LinkedList<ArithmeticNode>()
        nodes0.addAll(initNodes)
        nodes = nodes0
        hashCode = lazy(LazyThreadSafetyMode.NONE, nodes::hashCode)
        nodes.sort()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Sum
        return nodes == other.nodes
    }

    override fun hashCode(): Int {
        return this.hashCode.value
    }

    override fun toString(): String {
        return nodes.asSequence().map {
            when (it) {
                is IntNumber, is BooleanVariable, is Multiply -> it.toString()
                else -> "($it)"
            }
        }.joinToString(" + ")
    }

    override fun compareTo(other: ArithmeticNode): Int {
        return when (other) {
            is IntNumber, is BooleanVariable, is Multiply -> 1
            is Sum -> when (val sizeComp = nodes.size.compareTo(other.nodes.size)) {
                -1, 1 -> sizeComp
                else -> hashCode().compareTo(other.hashCode())
            }
        }
    }
}

class Multiply : ArithmeticNode {
    private val hashCode: Lazy<Int>
    val nodes: List<ArithmeticNode>

    constructor(vararg initNodes: ArithmeticNode) : this(initNodes.asIterable())
    constructor(initNodes: Sequence<ArithmeticNode>) : this(initNodes.asIterable())

    constructor(initNodes: Iterable<ArithmeticNode>) {
        val nodes0 = LinkedList<ArithmeticNode>()
        nodes0.addAll(initNodes)
        nodes = nodes0
        hashCode = lazy(LazyThreadSafetyMode.NONE, nodes::hashCode)
        nodes.sort()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Multiply
        return nodes == other.nodes
    }

    override fun hashCode(): Int {
        return this.hashCode.value
    }

    override fun toString(): String {
        return nodes.asSequence().map {
            when (it) {
                is IntNumber, is BooleanVariable -> it.toString()
                else -> "($it)"
            }
        }.joinToString("*")
    }

    override fun compareTo(other: ArithmeticNode): Int {
        return when (other) {
            is IntNumber, is BooleanVariable -> 1
            is Sum -> -1
            is Multiply -> when (val sizeComp = nodes.size.compareTo(other.nodes.size)) {
                -1, 1 -> sizeComp
                else -> hashCode().compareTo(other.hashCode())
            }
        }
    }
}

//#region Extensions
operator fun ArithmeticNode.plus(other: ArithmeticNode) = Sum(this, other)
operator fun ArithmeticNode.minus(other: ArithmeticNode) = Sum(this, -other)
operator fun ArithmeticNode.times(other: ArithmeticNode) = Multiply(this, other)
operator fun ArithmeticNode.unaryMinus() = Multiply(IntNumber(-1), this)

fun Bit.toIntNumber(): IntNumber {
    return IntNumber(if (this.value) 1 else 0)
}

fun Variable.toBooleanVariable(): BooleanVariable {
    return BooleanVariable(name)
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

fun Node.replaceXorWithSum(): ArithmeticNode {
    return when (this) {
        is Bit, is Variable -> toArithmeticNode()
        is And -> Multiply(nodes.asSequence().map { it.replaceXorWithSum() })
        is Xor -> Sum(nodes.asSequence().map { it.replaceXorWithSum() })
    }
}

fun ArithmeticNode.evaluate(variables: Map<BooleanVariable, IntNumber>): Int {
    return when (this) {
        is IntNumber -> value
        is BooleanVariable -> variables[this]?.value ?: throw Exception("Not value defined for the variable $this")
        is Sum -> nodes.sumOf { it.evaluate(variables) }
        is Multiply -> nodes.fold(1) { acc, node -> acc * node.evaluate(variables) }
    }
}

fun ArithmeticNode.evaluate(variables: BooleanArray, variablePrefix: String = "x", indexMap: IntArray? = null): Int {
    val vars = variables.asSequence()
        .mapIndexed { index, value ->
            val varIndex = if (indexMap == null) index else indexMap[index]
            BooleanVariable("$variablePrefix$varIndex") to IntNumber(value.toInt())
        }
        .toMap()

    return evaluate(vars)
}

fun ArithmeticNode.collectTerms(): ArithmeticNode {
    return when (this) {
        is IntNumber, is BooleanVariable -> this
        is Sum -> {
            val modifiedNodes = LinkedList<ArithmeticNode>()

            fun Sum.collectNodes() {
                for (node in nodes) {
                    when (node) {
                        is IntNumber, is BooleanVariable -> modifiedNodes.add(node.collectTerms())
                        is Sum -> node.collectNodes()
                        is Multiply -> when (val newNode = node.collectTerms()) {
                            is BooleanVariable, is IntNumber, is Multiply -> modifiedNodes.add(newNode)
                            is Sum -> newNode.collectNodes()
                        }
                    }
                }
            }

            collectNodes()

            var num = 0
            val varsMap = HashMap<Multiply, Int>()

            for (node in modifiedNodes) {
                when (node) {
                    is IntNumber -> num += node.value
                    is BooleanVariable -> {
                        val multNode = Multiply(node)
                        val count = varsMap.getOrDefault(multNode, 0) + 1
                        varsMap[multNode] = count
                    }

                    is Multiply -> {
                        val mult = Multiply(node.nodes.asSequence().filter { it !is IntNumber })
                        val coeff = (node.nodes.firstOrNull { it is IntNumber } as? IntNumber)?.value ?: 1
                        val count = varsMap.getOrDefault(mult, 0) + coeff
                        varsMap[mult] = count
                    }

                    is Sum -> throw IllegalStateException("Sum node shouldn't appear here")
                }
            }

            val sumNodes = LinkedList<ArithmeticNode>()

            if (num != 0) {
                sumNodes.add(IntNumber(num))
            }

            for ((node, count) in varsMap) {
                if (count == 1) {
                    if (node.nodes.size == 1) {
                        sumNodes.add(node.nodes.first())
                    } else {
                        sumNodes.add(node)
                    }
                } else if (count != 0) {
                    sumNodes.add(Multiply(sequenceOf(IntNumber(count)) + node.nodes.asSequence()))
                }
            }

            when (sumNodes.size) {
                0 -> IntNumber(0)
                1 -> sumNodes.first()
                else -> Sum(sumNodes)
            }
        }

        is Multiply -> {
            var num = 1
            val terms = HashSet<ArithmeticNode>()

            fun Multiply.collectNodes() {
                fun Sum.collectNodes() {
                    when (val sum = collectTerms()) {
                        is IntNumber -> num *= sum.value
                        is Multiply -> sum.collectNodes()
                        is BooleanVariable, is Sum -> terms.add(sum)
                    }
                }

                for (node in nodes) {
                    when (node) {
                        is IntNumber -> num *= node.value
                        is BooleanVariable -> terms.add(node)
                        is Sum -> node.collectNodes()
                        is Multiply -> node.collectNodes()
                    }
                }
            }

            collectNodes()

            if (num == 0 || terms.size == 0) {
                IntNumber(num)
            } else if (num == 1) {
                if (terms.size == 1) {
                    terms.first()
                } else {
                    Multiply(terms)
                }
            } else {
                if (terms.size == 1 && terms.first() is Sum) {
                    val sum = terms.first() as Sum
                    Sum(sum.nodes.asSequence().map { Multiply(IntNumber(num), it) }).collectTerms()
                } else {
                    Multiply(sequenceOf(IntNumber(num)) + terms.asSequence())
                }
            }
        }
    }
}

fun ArithmeticNode.expand(): ArithmeticNode {
    return when (this) {
        is IntNumber, is BooleanVariable -> this
        is Sum -> Sum(nodes.asSequence().map { it.expand() }).collectTerms()
        is Multiply -> when (val mult = collectTerms()) {
            is IntNumber, is BooleanVariable -> mult
            is Sum -> mult.expand()
            is Multiply -> {
                val multTerms = LinkedList<ArithmeticNode>()
                val sumTerms = LinkedList<Sum>()

                for (node in mult.nodes) {
                    when (val expandedNode = node.expand()) {
                        is IntNumber, is BooleanVariable -> multTerms.add(expandedNode)
                        is Sum -> sumTerms.add(expandedNode)
                        is Multiply -> throw IllegalStateException("Multiply node shouldn't appear here")
                    }
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

                if (expandedSum.nodes.size == 1) {
                    expandedSum.nodes.first()
                } else {
                    expandedSum
                }
            }
        }
    }
}

fun ArithmeticNode.substitute(variable: BooleanVariable, expr: ArithmeticNode): ArithmeticNode {
    return when (this) {
        is IntNumber -> this
        is BooleanVariable -> if (this == variable) expr else this
        is Multiply -> Multiply(nodes.asSequence().map { it.substitute(variable, expr) })
        is Sum -> Sum(nodes.asSequence().map { it.substitute(variable, expr) })
    }
}

fun ArithmeticNode.reduceBy(num: IntNumber): ArithmeticNode {
    return when (this) {
        is IntNumber -> if (value % num.value == 0) {
            IntNumber(value / num.value)
        } else {
            throw Exception("Reduction is not possible")
        }

        is BooleanVariable -> this
        is Multiply -> Multiply(nodes.asSequence().map { if (it !is Sum) it.reduceBy(num) else it })
        is Sum -> Sum(nodes.asSequence().map { it.reduceBy(num) })
    }
}

fun ArithmeticNode.moduloBy(num: IntNumber): ArithmeticNode {
    return when (this) {
        is IntNumber -> IntNumber(value.mod(num.value))
        is BooleanVariable -> this
        is Multiply -> Multiply(nodes.asSequence().map { if (it !is Sum) it.moduloBy(num) else it })
        is Sum -> Sum(nodes.asSequence().map { it.moduloBy(num) })
    }
}

fun ArithmeticNode.andBy(num: IntNumber): ArithmeticNode {
    return when (this) {
        is IntNumber -> IntNumber(value.and(num.value))
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
//#endregion