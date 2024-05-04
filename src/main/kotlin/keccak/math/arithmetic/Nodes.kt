package keccak.math.arithmetic

import okhttp3.internal.toLongOrDefault
import java.math.BigInteger
import java.util.*

sealed interface ArithmeticNode : Comparable<ArithmeticNode> {
    fun clone(): ArithmeticNode
}

@JvmInline
value class IntNumber(val value: BigInteger = BigInteger.ZERO) : ArithmeticNode {
    constructor(value: Int) : this(value.toBigInteger())
    constructor(value: Long) : this(value.toBigInteger())
    constructor(value: String) : this(value.toBigInteger())

    override fun toString(): String {
        return if (value >= BigInteger.ZERO) {
            value.toString()
        } else {
            "($value)"
        }
    }

    override fun compareTo(other: ArithmeticNode): Int {
        return when (other) {
            is IntNumber -> value.compareTo(other.value)
            is InverseNumber, is BooleanVariable, is Sum, is Multiply -> -1
        }
    }

    override fun clone(): ArithmeticNode {
        return IntNumber(value)
    }
}

@JvmInline
value class InverseNumber(val value: BigInteger = BigInteger.ONE) : ArithmeticNode {
    constructor(value: Int) : this(value.toBigInteger())
    constructor(value: Long) : this(value.toBigInteger())
    constructor(value: String) : this(value.toBigInteger())

    override fun toString(): String {
        return "(1/$value)"
    }

    override fun compareTo(other: ArithmeticNode): Int {
        return when (other) {
            is InverseNumber -> value.compareTo(other.value)
            is IntNumber -> 1
            is BooleanVariable, is Sum, is Multiply -> -1
        }
    }

    override fun clone(): ArithmeticNode {
        return InverseNumber(value)
    }
}

class BooleanVariable(val name: String, val type: Type) : ArithmeticNode {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BooleanVariable

        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun toString(): String {
        return name
    }

    override fun compareTo(other: ArithmeticNode): Int {
        return when (other) {
            is IntNumber, is InverseNumber -> 1
            is Sum, is Multiply -> -1
            is BooleanVariable -> {
                val var0 = VariablePattern.matchEntire(name)
                val var1 = VariablePattern.matchEntire(other.name)

                if (var0 != null && var1 != null) {
                    val (name0, id0) = var0.destructured
                    val (name1, id1) = var1.destructured

                    val nameComp = name0.compareTo(name1)

                    if (nameComp == 0) {
                        id0.toLongOrDefault(-1).compareTo(id1.toLongOrDefault(-1))
                    } else {
                        nameComp
                    }
                } else {
                    name.compareTo(other.name)
                }
            }
        }
    }

    override fun clone(): ArithmeticNode {
        return BooleanVariable(name, type)
    }

    companion object {
        private val VariablePattern = """^([a-zA-Z]+)(\d*)$""".toRegex()
    }

    enum class Type {
        ZERO_ONE,
        PLUS_MINUS_ONE,
        ANY,
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
            is IntNumber, is InverseNumber, is BooleanVariable, is Multiply -> 1
            is Sum -> when (val sizeComp = nodes.size.compareTo(other.nodes.size)) {
                -1, 1 -> sizeComp
                else -> hashCode().compareTo(other.hashCode())
            }
        }
    }

    override fun clone(): ArithmeticNode {
        return nodes.asSequence().map { it.clone() }.let { Sum(it) }
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
                is IntNumber, is InverseNumber, is BooleanVariable -> it.toString()
                else -> "($it)"
            }
        }.joinToString("*")
    }

    override fun compareTo(other: ArithmeticNode): Int {
        return when (other) {
            is IntNumber, is InverseNumber, is BooleanVariable -> 1
            is Sum -> -1
            is Multiply -> when (val sizeComp = nodes.size.compareTo(other.nodes.size)) {
                -1, 1 -> sizeComp
                else -> hashCode().compareTo(other.hashCode())
            }
        }
    }

    override fun clone(): ArithmeticNode {
        return nodes.asSequence().map { it.clone() }.let { Multiply(it) }
    }
}
