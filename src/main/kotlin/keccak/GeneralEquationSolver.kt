package keccak

import java.util.*

object GeneralEquationSolver {
    fun solve(equations: Array<Equation>, variables: Array<Variable>) {
        bottom(equations, variables)
        top(equations, variables)
    }

    private fun bottom(equations: Array<Equation>, variables: Array<Variable>) {
        var rowId = 0
        var varId = 0

        while (rowId < equations.size && varId < variables.size) {
            var i = rowId
            var found = false

            while (i < equations.size) {
                if (equations[i].contains(variables[varId])) {
                    found = true
                    break
                }

                i++
            }

            if (found) {
                if (rowId != i) {
                    equations.exchange(rowId, i)
                }

                equations[rowId] = equations[rowId].extract(variables[varId])

                if (equations[rowId].contains(variables[varId])) {
                    i = rowId + 1

                    while (i < equations.size) {
                        if (equations[i].contains(variables[varId])) {
                            equations[i] = equations[i].substitute(equations[rowId], variables[varId])
                        }

                        i++
                    }
                }
            }

            rowId++
            varId++
        }
    }

    private fun top(equations: Array<Equation>, variables: Array<Variable>) {
        var rowId = equations.size - 1
        var varId = equations.size - 1

        while (rowId >= 0 && varId >= 0) {
            if (equations[rowId].contains(variables[varId])) {
                var i = rowId - 1

                while (i >= 0) {
                    if (equations[i].contains(variables[varId])) {
                        equations[i] = equations[i].substitute(equations[rowId], variables[varId])
                    }

                    i--
                }
            }

            rowId--
            varId--
        }
    }
}

class Equation(val left: Node, val right: Node) {
    fun extract(variable: Variable): Equation {
        val node = Xor(left, right).simplify()

        if (!node.contains(variable)) {
            return Equation(node, Bit())
        }

        return when (val extracted = node.extract(variable)) {
            is Bit -> Equation(extracted, extracted)
            is Variable, is And -> Equation(extracted, Bit())
            is Xor -> {
                var l: Node = Bit()
                val r = LinkedList<Node>()

                extracted.nodes.forEach { extractedNode ->
                    if (extractedNode.contains(variable)) {
                        if (l is Bit) {
                            l = extractedNode
                        } else {
                            throw IllegalStateException()
                        }
                    } else {
                        r.add(extractedNode)
                    }
                }

                Equation(l, Xor(r).simplify())
            }
        }
    }

    fun substitute(equation: Equation, variable: Variable): Equation {
        if (!(equation.left.contains(variable) && this.contains(variable))) return this

        return when (equation.left) {
            is Variable -> {
                this.replace(equation.left, equation.right)
            }
            is And -> {
                val mul = And(equation.left.nodes.asSequence().filter { it != variable })
                this.extract(variable).multiply(mul).replace(equation.left, equation.right)
            }
            else -> throw IllegalStateException()
        }
    }

    fun replace(what: Node, with: Node): Equation {
        return Equation(left.replace(what, with), right.replace(what, with))
    }

    fun multiply(mul: And): Equation {
        return Equation(left.multiply(mul), right.multiply(mul))
    }

    fun contains(variable: Variable): Boolean {
        return left.contains(variable) || right.contains(variable)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Equation

        return left == other.left && right == other.right || left == other.right && right == other.left
    }

    override fun hashCode(): Int {
        return left.hashCode() + right.hashCode()
    }

    override fun toString(): String {
        return "$left = $right"
    }

    private fun Node.extract(variable: Variable): Node {
        return when (this) {
            is Bit, is Variable -> this
            is Xor -> {
                val (varNodes, notVarNodes) = nodes.partition(variable)
                val varNodesExtracted = LinkedList<Node>()

                varNodes.forEach { varNode ->
                    when (val extracted = varNode.extract(variable)) {
                        is Bit -> {
                            notVarNodes.add(extracted)
                        }
                        is Variable -> {
                            if (variable == extracted) {
                                varNodesExtracted.add(extracted)
                            } else {
                                notVarNodes.add(extracted)
                            }
                        }
                        is Xor -> {
                            extracted.nodes.forEach { node ->
                                if (node.contains(variable)) {
                                    varNodesExtracted.add(node)
                                } else {
                                    notVarNodes.add(node)
                                }
                            }
                        }
                        is And -> {
                            if (extracted.contains(variable)) {
                                varNodesExtracted.add(extracted)
                            } else {
                                notVarNodes.add(extracted)
                            }
                        }
                    }
                }

                val extractedNodes = varNodesExtracted.asSequence().map { node ->
                    when (node) {
                        is Variable -> Bit(true)
                        is And -> And(node.nodes.asSequence().filter { it != variable }).simplify()
                        else -> throw IllegalStateException()
                    }
                }

                val processedNode = And(variable, Xor(extractedNodes).simplify()).simplify()

                notVarNodes.add(processedNode)

                Xor(notVarNodes).simplify()
            }
            is And -> {
                val (varNodes, notVarNodes) = nodes.partition(variable)
                val varNodesExtracted = LinkedList<Node>()

                varNodes.forEach { varNode ->
                    when (val extracted = varNode.extract(variable)) {
                        is Bit -> {
                            notVarNodes.add(extracted)
                        }
                        is Variable -> {
                            if (variable == extracted) {
                                varNodesExtracted.add(extracted)
                            } else {
                                notVarNodes.add(extracted)
                            }
                        }
                        is Xor -> {
                            if (extracted.contains(variable)) {
                                varNodesExtracted.add(extracted)
                            } else {
                                notVarNodes.add(extracted)
                            }
                        }
                        is And -> {
                            extracted.nodes.forEach { node ->
                                if (node.contains(variable)) {
                                    varNodesExtracted.add(node)
                                } else {
                                    notVarNodes.add(node)
                                }
                            }
                        }
                    }
                }

                val varNodesAnd = And(varNodesExtracted)
                val xorNode = varNodesAnd.nodes.find { it is Xor } as Xor?

                if (xorNode != null) {
                    var extractedVar: Node = Bit()
                    val remainingNodes = LinkedList<Node>()

                    xorNode.nodes.forEach { node ->
                        if (node.contains(variable)) {
                            if (extractedVar is Bit) {
                                extractedVar = node
                            } else {
                                throw IllegalStateException()
                            }
                        } else {
                            remainingNodes.add(node)
                        }
                    }

                    val processedExceptCurrent = varNodesAnd.nodes.asSequence().filter { it != xorNode }

                    Xor(
                        And(sequenceOf(extractedVar) + processedExceptCurrent + notVarNodes).simplify(),
                        And(sequenceOf(Xor(remainingNodes.asSequence()).simplify()) + processedExceptCurrent + notVarNodes).simplify(),
                    )
                        .simplify()
                        .extract(variable)
                } else {
                    And(varNodesAnd.nodes.asSequence() + notVarNodes.asSequence()).simplify()
                }
            }
        }
    }

    private fun Iterable<Node>.partition(variable: Variable): Pair<LinkedList<Node>, LinkedList<Node>> {
        val varNodes = LinkedList<Node>()
        val notVarNodes = LinkedList<Node>()

        forEach { node ->
            if (node.contains(variable)) {
                varNodes.add(node)
            } else {
                notVarNodes.add(node)
            }
        }

        return Pair(varNodes, notVarNodes)
    }

    private fun Node.multiply(mul: And): Node {
        return when (this) {
            is And, is Variable, is Bit -> And(sequenceOf(this, mul)).simplify()
            is Xor -> Xor(this.nodes.asSequence().map { it.multiply(mul) }).simplify()
        }
    }

    private fun Node.replace(what: Node, with: Node): Node {
        return when (this) {
            is Bit, is Variable -> {
                if (this == what) {
                    with
                } else {
                    this
                }
            }
            is And -> {
                when (what) {
                    is Bit -> this
                    is Variable, is Xor -> {
                        if (this.contains(what)) {
                            val newNodes = this.nodes.asSequence().map { node ->
                                if (node.contains(what)) {
                                    node.replace(what, with)
                                } else {
                                    node
                                }
                            }
                            And(newNodes).simplify()
                        } else {
                            this
                        }
                    }
                    is And -> {
                        val containsAll = this.nodes.containsAll(what.nodes)
                        val containsNested = this.nodes.any { it.contains(what) }

                        if (containsAll || containsNested) {
                            val newNodes = this.nodes.asSequence().filter {
                                if (containsAll) {
                                    !what.nodes.contains(it)
                                } else {
                                    true
                                }
                            }.map {
                                it.replace(what, with)
                            }

                            And(newNodes + if (containsAll) sequenceOf(with) else emptySequence()).simplify()
                        } else {
                            this
                        }
                    }
                }
            }
            is Xor -> {
                if (what is Xor) {
                    val containsAll = this.nodes.containsAll(what.nodes)
                    val containsNested = this.nodes.any { it.contains(what) }

                    if (containsAll || containsNested) {
                        val newNodes = this.nodes.asSequence().filter {
                            if (containsAll) {
                                !what.nodes.contains(it)
                            } else {
                                true
                            }
                        }.map {
                            it.replace(what, with)
                        }

                        Xor(newNodes + if (containsAll) sequenceOf(with) else emptySequence()).simplify()
                    } else {
                        this
                    }
                } else {
                    val newNodes = this.nodes.asSequence().map {
                        if (it == what) {
                            with
                        } else {
                            it.replace(what, with)
                        }
                    }

                    Xor(newNodes).simplify()
                }
            }
        }
    }
}
