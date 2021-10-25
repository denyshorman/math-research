package keccak

import java.util.*

class NodeContext {
    val variables = mutableMapOf<String, Bit>()
}

sealed interface Node {
    fun evaluate(context: NodeContext): Bit
    fun contains(node: Node): Boolean
}

@JvmInline
value class Bit(val value: Boolean = false) : Node {
    constructor(v: Int) : this(v == 1)

    override fun evaluate(context: NodeContext): Bit {
        return this
    }

    override fun contains(node: Node): Boolean {
        return this == node
    }

    override fun toString(): String {
        return if (value) "1" else "0"
    }
}

@JvmInline
value class Variable(val name: String) : Node {
    override fun evaluate(context: NodeContext): Bit {
        return context.variables[name] ?: throw IllegalStateException("Variable $name not found")
    }

    override fun contains(node: Node): Boolean {
        return this == node
    }

    override fun toString(): String {
        return name
    }
}

class Xor : Node {
    val nodes: Set<Node>

    constructor(vararg initNodes: Node) : this(initNodes.asIterable())
    constructor(initNodes: Sequence<Node>) : this(initNodes.asIterable())

    constructor(initNodes: Iterable<Node>) {
        nodes = HashSet<Node>()
        nodes.addNodes(initNodes)
        nodes.pad()
    }

    override fun evaluate(context: NodeContext): Bit {
        return nodes.fold(Bit()) { bit1, node ->
            val bit2 = node.evaluate(context)
            Bit(bit1 != bit2)
        }
    }

    override fun contains(node: Node): Boolean {
        return when (node) {
            is Bit, is Variable, is And -> nodes.contains(node) || nodes.any { it.contains(node) }
            is Xor -> nodes.containsAll(node.nodes) || nodes.any { it.contains(node) }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Xor

        if (nodes != other.nodes) return false

        return true
    }

    override fun hashCode(): Int {
        return nodes.hashCode()
    }

    override fun toString(): String {
        return nodes.asSequence().map {
            when (it) {
                is Bit, is Variable, is And -> it.toString()
                else -> "($it)"
            }
        }.joinToString(" + ")
    }

    private fun HashSet<Node>.addNodes(nodes: Iterable<Node>) {
        nodes.forEach { node ->
            when (node) {
                is Xor -> addNodes(node.nodes)
                else -> addNode(node)
            }
        }
    }

    private fun HashSet<Node>.addNode(node: Node) {
        if (contains(node)) {
            remove(node)
        } else {
            add(node)
        }
    }

    private fun HashSet<Node>.pad() {
        remove(Bit())
        if (isEmpty()) add(Bit())
    }
}

class And : Node {
    val nodes: Set<Node>

    constructor(vararg initNodes: Node) : this(initNodes.asIterable())
    constructor(initNodes: Sequence<Node>) : this(initNodes.asIterable())

    constructor(initNodes: Iterable<Node>) {
        nodes = try {
            val nodeSet = HashSet<Node>()
            nodeSet.addNodes(initNodes.asIterable())
            nodeSet.pad()
            nodeSet
        } catch (_: Zero) {
            setOf(Bit())
        }
    }

    override fun evaluate(context: NodeContext): Bit {
        return Bit(nodes.all { it.evaluate(context).value })
    }

    override fun contains(node: Node): Boolean {
        return when (node) {
            is Bit, is Variable, is Xor -> nodes.contains(node) || nodes.any { it.contains(node) }
            is And -> nodes.containsAll(node.nodes) || nodes.any { it.contains(node) }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as And

        if (nodes != other.nodes) return false

        return true
    }

    override fun hashCode(): Int {
        return nodes.hashCode()
    }

    override fun toString(): String {
        return nodes.asSequence().map {
            when (it) {
                is Bit, is Variable -> it.toString()
                else -> "($it)"
            }
        }.joinToString("*")
    }

    private fun HashSet<Node>.addNodes(nodes: Iterable<Node>) {
        nodes.forEach { node ->
            when (node) {
                is Bit -> if (node.value) add(node) else throw Zero
                is And -> addNodes(node.nodes)
                else -> add(node)
            }
        }
    }

    private fun HashSet<Node>.pad() {
        if (size > 1) {
            remove(Bit(true))
        } else if (isEmpty()) {
            throw IllegalStateException("And operator accepts at least one argument")
        }
    }

    companion object {
        private object Zero : Throwable(null, null, false, false)
    }
}

//#region Extensions

operator fun Node.contains(other: Node) = this.contains(other)

operator fun Node.plus(other: Node) = Xor(this, other)
operator fun Node.times(other: Node) = And(this, other)
operator fun String.plus(other: Node) = Xor(Variable(this), other)
operator fun String.times(other: Node) = And(Variable(this), other)
operator fun Node.plus(other: String) = Xor(this, Variable(other))
operator fun Node.times(other: String) = And(this, Variable(other))
operator fun String.plus(other: String) = Xor(Variable(this), Variable(other))
operator fun String.times(other: String) = And(Variable(this), Variable(other))

infix fun Node.xor(other: Node) = Xor(this, other)
infix fun Node.and(other: Node) = And(this, other)
infix fun String.xor(other: Node) = Xor(Variable(this), other)
infix fun String.and(other: Node) = And(Variable(this), other)
infix fun Node.xor(other: String) = Xor(this, Variable(other))
infix fun Node.and(other: String) = And(this, Variable(other))
infix fun String.xor(other: String) = Xor(Variable(this), Variable(other))
infix fun String.and(other: String) = And(Variable(this), Variable(other))
fun String.toVar() = Variable(this)

fun Node.simplify(): Node {
    return when (this) {
        is Xor -> when {
            nodes.size == 1 -> nodes.first()
            nodes.size >= 2 -> this
            else -> throw IllegalStateException()
        }
        is And -> when {
            nodes.size == 1 -> nodes.first()
            nodes.size >= 2 -> this
            else -> throw IllegalStateException()
        }
        else -> this
    }
}

fun Node.cleanup(): Node {
    return when (this) {
        is Bit, is Variable -> this
        is And -> And(nodes.asSequence().map { it.cleanup().simplify() })
        is Xor -> Xor(nodes.asSequence().map { it.cleanup().simplify() })
    }
}

fun Node.expand(): Node {
    return when (this) {
        is Bit, is Variable -> this
        is And -> {
            val variables = LinkedList<Variable>()
            val xors = LinkedList<Xor>()

            for (node in nodes) {
                when (node) {
                    is Bit -> if (!node.value) {
                        variables.clear()
                        break
                    }
                    is Variable -> variables.add(node)
                    is Xor -> {
                        when (val flattenedNode = node.expand().simplify()) {
                            is Bit -> {
                                xors.add(Xor(flattenedNode))
                            }
                            is Variable -> {
                                variables.add(flattenedNode)
                            }
                            is And -> {
                                flattenedNode.nodes.forEach { variable ->
                                    require(variable is Variable)
                                    variables.add(variable)
                                }
                            }
                            is Xor -> {
                                xors.add(flattenedNode)
                            }
                        }
                    }
                    else -> throw IllegalStateException()
                }
            }

            if (variables.size == 0 && xors.size == 0) {
                Bit(false)
            } else {
                val initXor = if (variables.size == 0) {
                    Xor(Bit(true))
                } else {
                    Xor(And(variables))
                }

                val flattenXor = xors.fold(initXor) { a, b ->
                    val nodes = LinkedList<Node>()

                    a.nodes.forEach { l ->
                        b.nodes.forEach { r ->
                            nodes.add(And(l, r).simplify())
                        }
                    }

                    Xor(nodes)
                }

                flattenXor.simplify()
            }
        }
        is Xor -> {
            val flattenNodes = this.nodes.asSequence().map { it.expand().simplify() }
            Xor(flattenNodes).simplify()
        }
    }
}

fun Node.groupBy(varPrefix: String): Node {
    return when (this) {
        is Bit, is Variable, is And -> this
        is Xor -> {
            val groups = HashMap<Set<Variable>, LinkedList<Node>>()
            val others = LinkedList<Node>()

            for (xorTerm in nodes) {
                val group = LinkedList<Variable>()
                val other = LinkedList<Node>()

                when (xorTerm) {
                    is Bit -> {
                        others.add(xorTerm)
                    }
                    is Variable -> {
                        if (xorTerm.name.startsWith(varPrefix)) {
                            group.add(xorTerm)
                        } else {
                            others.add(xorTerm)
                        }
                    }
                    is Xor -> {
                        throw IllegalStateException("Xor can't be in Xor")
                    }
                    is And -> {
                        for (andTerm in xorTerm.nodes) {
                            when (andTerm) {
                                is Bit -> {
                                    other.add(andTerm)
                                }
                                is Variable -> {
                                    if (andTerm.name.startsWith(varPrefix)) {
                                        group.add(andTerm)
                                    } else {
                                        other.add(andTerm)
                                    }
                                }
                                is And -> {
                                    throw IllegalStateException("And can't be inside And")
                                }
                                is Xor -> {
                                    other.add(andTerm)
                                }
                            }
                        }
                    }
                }

                if (group.isNotEmpty()) {
                    groups.compute(group.toSet()) { _, oldValues ->
                        val newValues = oldValues ?: LinkedList<Node>()
                        if (other.isNotEmpty()) {
                            newValues.add(And(other).simplify())
                        } else {
                            newValues.add(Bit(true))
                        }
                        newValues
                    }
                } else if (other.isNotEmpty()) {
                    others.add(And(other).simplify())
                }
            }

            val xorTerms = LinkedList<Node>()

            groups.forEach { (group, terms) ->
                val and = And(group.asSequence() + Xor(terms).simplify()).simplify()
                xorTerms.add(and)
            }

            xorTerms.addAll(others)

            Xor(xorTerms).simplify()
        }
    }
}

fun Node.groups(varPrefix: String): Map<Node, Node> {
    return when (this) {
        is Bit -> emptyMap()
        is Variable -> if (name.startsWith(varPrefix)) {
            mapOf(Pair(this, Bit(true)))
        } else {
            emptyMap()
        }
        is And -> {
            val matched = LinkedList<Node>()
            val notMatched = LinkedList<Node>()

            for (node in nodes) {
                when (node) {
                    is Bit -> notMatched.add(node)
                    is Variable -> if (node.name.startsWith(varPrefix)) {
                        matched.add(node)
                    } else {
                        notMatched.add(node)
                    }
                    is And -> throw IllegalStateException("And can't be inside And")
                    is Xor -> notMatched.add(node)
                }
            }

            if (matched.isEmpty()) {
                emptyMap()
            } else {
                val v = if (notMatched.isEmpty()) {
                    Bit(true)
                } else {
                    And(notMatched)
                }

                mapOf(Pair(And(matched).simplify(), v.simplify()))
            }
        }
        is Xor -> {
            val groupValues = HashMap<Node, Node>()

            for (node in nodes) {
                val nodeGroups = node.groups(varPrefix)

                for (nodeGroup in nodeGroups) {
                    groupValues[nodeGroup.key] = nodeGroup.value
                }
            }

            groupValues
        }
    }
}

fun Node.notGroups(varPrefix: String): Node {
    return when(this) {
        is Bit -> this
        is Variable -> {
            if (name.startsWith(varPrefix)) {
                Bit(false)
            } else {
                this
            }
        }
        is And -> {
            var valid = true

            for (node in nodes) {
                when (node) {
                    is Variable -> {
                        if (node.name.startsWith(varPrefix)) {
                            valid = false
                            break
                        }
                    }
                    else -> {/*ignore*/}
                }
            }

            if (valid) {
                this
            } else {
                Bit(false)
            }
        }
        is Xor -> {
            Xor(nodes.asSequence().map { it.notGroups(varPrefix) }).simplify()
        }
    }
}

fun Node.substitute(varName: String, value: Node): Node {
    return when (this) {
        is Bit -> this
        is Variable -> if (this.name == varName) value else this
        is And -> And(nodes.asSequence().map { it.substitute(varName, value) })
        is Xor -> Xor(nodes.asSequence().map { it.substitute(varName, value) })
    }
}

fun Node.keep(termSize: Int): Node {
    return when (this) {
        is Bit -> if (termSize == 0) this else Bit(false)
        is Variable -> if (termSize == 1) this else Bit(false)
        is And -> if (nodes.size == termSize) this else Bit(false)
        is Xor -> Xor(nodes.asSequence().map { it.keep(termSize) }).simplify()
    }
}

fun Node.keep(varPrefix: String, termSize: Int): Node {
    return when (this) {
        is Bit -> Bit(false)
        is Variable -> if (termSize == 1 && name.startsWith(varPrefix)) this else Bit(false)
        is And -> if (nodes.count { it is Variable && it.name.startsWith(varPrefix) } == termSize) this else Bit(false)
        is Xor -> Xor(nodes.asSequence().map { it.keep(varPrefix, termSize) }).simplify()
    }
}

fun Node.termsCount(): Int {
    return when (this) {
        is Bit, is Variable, is And -> 1
        is Xor -> nodes.size
    }
}
//#endregion
