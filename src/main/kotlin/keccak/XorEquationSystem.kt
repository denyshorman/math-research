package keccak

import keccak.util.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*

class XorEquationSystem {
    val rows: Int get() = equations.size
    var cols: Int
    var equations: Array<BitSet>
    var results: BitSet

    var eqVarMap: IntArray
    var varEqMap: IntArray

    constructor(
        rows: Int,
        cols: Int,
    ) {
        this.cols = cols
        this.equations = Array(rows) { BitSet(cols) }
        this.results = BitSet(rows)

        this.eqVarMap = IntArray(rows) { -1 }
        this.varEqMap = IntArray(cols) { -1 }
    }

    constructor(
        cols: Int,
        equations: Array<BitSet>,
        results: BitSet,
        eqVarMap: IntArray = IntArray(equations.size) { -1 },
        varEqMap: IntArray = IntArray(cols) { -1 },
    ) {
        this.cols = cols
        this.equations = equations
        this.results = results

        this.eqVarMap = eqVarMap
        this.varEqMap = varEqMap
    }

    fun isValid(solution: BitSet): Boolean {
        var i = 0
        while (i < rows) {
            if (equations[i].evaluate(solution) != results[i]) {
                return false
            }
            i++
        }
        return true
    }

    fun isValid(): Boolean {
        var i = 0
        while (i < rows) {
            if (isInvalid(i)) return false
            i++
        }
        return true
    }

    fun isValid(eqIndex: Int): Boolean {
        return !isInvalid(eqIndex)
    }

    fun isInvalid(eqIndex: Int): Boolean {
        return equations[eqIndex].isEmpty && results[eqIndex]
    }

    fun isPartiallyEmpty(): Boolean {
        var i = 0
        while (i < rows) {
            if (equations[i++].isEmpty) return true
        }
        return false
    }

    fun exchange(i: Int, j: Int) {
        if (i == j) return

        equations.exchange(i, j)
        results.exchange(i, j)

        eqVarMap.exchange(i, j)

        val varIndex0 = eqVarMap[i]
        val varIndex1 = eqVarMap[j]

        if (varIndex0 != -1) {
            varEqMap[varIndex0] = i
        }

        if (varIndex1 != -1) {
            varEqMap[varIndex1] = j
        }
    }

    fun xor(i: Int, j: Int) {
        equations[i].xor(equations[j])
        results[i] = results[i] xor results[j]
    }

    fun xor(vararg systems: XorEquationSystem) {
        var i = 0
        var j: Int
        while (i < systems.size) {
            j = 0
            while (j < systems[i].rows) {
                equations[j].xor(systems[i].equations[j])
                j++
            }
            results.xor(systems[i].results)
            i++
        }
    }

    fun xor(eqIndex: Int, vararg xorEquations: XorEquation) {
        var bitEqIndex = 0
        while (bitEqIndex < xorEquations.size) {
            equations[eqIndex].xor(xorEquations[bitEqIndex].bitGroup.bitSet)
            results.xor(eqIndex, xorEquations[bitEqIndex].result)
            bitEqIndex++
        }
    }

    fun rotateEquationsLeft(count: Int) {
        var roundIndex = 0
        var eqIndex: Int
        var firstEquation: BitSet
        var firstResultBit: Boolean
        val lastBitIndex = rows - 1

        while (roundIndex < count) {
            eqIndex = 0
            firstEquation = equations[0]
            firstResultBit = results[0]
            while (eqIndex < lastBitIndex) {
                equations[eqIndex] = equations[eqIndex + 1]
                results[eqIndex] = results[eqIndex + 1]
                eqIndex++
            }
            equations[lastBitIndex] = firstEquation
            results[lastBitIndex] = firstResultBit
            roundIndex++
        }
    }

    fun sortEquations() {
        var eqIndex = 0
        var varIndex = 0

        while (eqIndex < rows) {
            while (varIndex < cols && varEqMap[varIndex] == -1) varIndex++

            if (varIndex < cols) {
                exchange(eqIndex++, varEqMap[varIndex++])
            } else {
                break
            }
        }
    }

    fun simplifyEquation(index: Int) {
        val eqClone = equations[index].clone() as BitSet

        eqClone.iterateOverAllSetBits { bitIndex ->
            val eqIndex = varEqMap[bitIndex]

            if (eqIndex != -1) {
                equations[index].xor(equations[eqIndex])
                results.xor(index, results[eqIndex])
            }
        }
    }

    fun expressVariable(
        fromEqIndex: Int,
        varIndex: Int,
        activeRows: BitSet? = null,
        varSubstituted: ((Int) -> Boolean)? = null,
    ): Boolean {
        if (!equations[fromEqIndex][varIndex]) {
            return false
        }

        val oldVarIndex = eqVarMap[fromEqIndex]
        eqVarMap[fromEqIndex] = varIndex
        varEqMap[varIndex] = fromEqIndex

        if (oldVarIndex != -1) {
            varEqMap[oldVarIndex] = -1
        }

        val currEqFreeBit = results[fromEqIndex]

        var eqIndex = 0
        while (eqIndex < rows) {
            if (
                (activeRows == null || activeRows[eqIndex]) &&
                eqIndex != fromEqIndex &&
                equations[eqIndex][varIndex]
            ) {
                equations[eqIndex].xor(equations[fromEqIndex])
                results.xor(eqIndex, currEqFreeBit)

                if (varSubstituted != null && !varSubstituted(eqIndex)) {
                    return false
                }
            }

            eqIndex++
        }

        return true
    }

    fun solve(
        skipValidation: Boolean = false,
        logProgress: Boolean = false,
        sortEquations: Boolean = false,
        progressStep: Int = 1024,
        activeRows: BitSet? = null,
        varPriority: BitSet? = null,
    ): Boolean {
        if (logProgress) {
            if (!isPow2(progressStep)) {
                throw IllegalArgumentException("progressStep must be a power of 2")
            }

            if (varPriority == null) {
                logger.info("Start variables expression")
            } else {
                logger.info("Expressing priority variables")
            }
        }

        val validateEquation: ((Int) -> Boolean)? = if (skipValidation) null else ::isValid

        activeRows.iterateOverAllSetBits(fromIndex = 0, rows) { eqIndex ->
            if (equations[eqIndex].isEmpty || eqVarMap[eqIndex] != -1) {
                return@iterateOverAllSetBits
            }

            val varIndex = if (varPriority == null) {
                equations[eqIndex].nextSetBit(0)
            } else {
                equations[eqIndex].firstMatchIndex(varPriority)
            }

            if (varIndex != -1) {
                val expressed = expressVariable(eqIndex, varIndex, activeRows, validateEquation)
                if (!expressed) return false
            }

            if (logProgress && modPow2(eqIndex, progressStep) == 0) {
                logger.info("Processed $eqIndex rows")
            }
        }

        if (logProgress) {
            if (varPriority == null) {
                logger.info("All variables have been expressed")
            } else {
                logger.info("All priority variables have been expressed")
            }
        }

        if (varPriority != null) {
            if (logProgress) {
                logger.info("Expressing non-priority variables")
            }

            activeRows.iterateOverAllSetBits(fromIndex = 0, rows) { eqIndex ->
                if (logProgress && modPow2(eqIndex, progressStep) == 0) {
                    logger.info("Processed $eqIndex rows")
                }

                if (equations[eqIndex].isEmpty || eqVarMap[eqIndex] != -1) {
                    return@iterateOverAllSetBits
                }

                val varIndex = equations[eqIndex].nextSetBit(0)
                val expressed = expressVariable(eqIndex, varIndex, activeRows, validateEquation)
                if (!expressed) return false
            }

            if (logProgress) {
                logger.info("All non-priority variables have been expressed")
            }
        }

        if (sortEquations) {
            if (logProgress) {
                logger.info("Sorting equations")
            }

            sortEquations()

            if (logProgress) {
                logger.info("Equations have been sorted")
            }
        }

        return true
    }

    fun substitute(values: BitSet) {
        var i = 0
        while (i < rows) {
            equations[i].and(values)
            if (equations[i].setBitsCount() and 1 != 0) {
                results.invertValue(i)
            }
            equations[i].clear()
            i++
        }
    }

    fun substitute(values: BitSet, mask: BitSet) {
        val maskInverted = mask.clone() as BitSet
        maskInverted.invert(cols)

        var i = 0
        while (i < rows) {
            val res = equations[i].clone() as BitSet
            res.and(values)
            res.and(mask)

            equations[i].and(maskInverted)

            if (res.setBitsCount() and 1 != 0) {
                results.invertValue(i)
            }

            i++
        }
    }

    fun substitute(index: Int, value: Boolean) {
        var i = 0
        while (i < rows) {
            if (equations[i][index]) {
                equations[i].clear(index)
                results.xor(i, value)
            }
            i++
        }
    }

    fun substitute(values: Iterable<Pair<Int, Boolean>>) {
        for ((index, value) in values) {
            substitute(index, value)
        }
    }

    fun substitute(other: XorEquationSystem) {
        var i = 0
        while (i < other.eqVarMap.size) {
            val expressedVarIndex = other.eqVarMap[i]
            if (expressedVarIndex != -1) {
                var j = 0
                while (j < this.rows) {
                    if (!this.equations[j].isEmpty && this.equations[j][expressedVarIndex]) {
                        this.equations[j].xor(other.equations[i])
                        this.results.xor(j, other.results[i])
                    }
                    j++
                }
            }
            i++
        }
    }

    fun extend(size: Int) {
        equations = Array(equations.size + size) { i ->
            if (i < equations.size) {
                equations[i]
            } else {
                BitSet(cols)
            }
        }

        results = run {
            val bits = BitSet(equations.size)
            bits.xor(results)
            bits
        }
    }

    fun append(
        vars: BitSet,
        res: Boolean,
        appendFromIndex: Int = 0,
        extendSize: Int = 128,
    ): Int {
        var i = appendFromIndex

        while (i < equations.size) {
            if (equations[i].isEmpty && !results[i]) {
                equations[i].xor(vars)
                results.setIfTrue(i, res)
                return i
            }

            i++
        }

        extend(extendSize)

        equations[i].xor(vars)
        results.setIfTrue(i, res)

        return i
    }

    fun characteristicSystem(multiplier: XorEquationSystem? = null): XorEquationSystem {
        val system = XorEquationSystem(
            rows = cols + 1,
            cols = multiplier?.cols ?: rows,
        )

        if (multiplier == null) {
            var row = 0
            while (row < rows) {
                equations[row].iterateOverAllSetBits { col ->
                    system.equations[col].set(row)
                }
                row++
            }

            results.iterateOverAllSetBits { rowIndex ->
                system.equations[cols].set(rowIndex)
            }
        } else {
            var row = 0
            while (row < rows) {
                equations[row].iterateOverAllSetBits { col0 ->
                    multiplier.equations[row].iterateOverAllSetBits { col1 ->
                        system.equations[col0].invertValue(col1)
                    }
                }

                if (results[row] && multiplier.results[row]) {
                    equations[row].iterateOverAllSetBits { col ->
                        system.results.invertValue(col)
                    }

                    system.equations[cols].xor(multiplier.equations[row])
                    system.results.invertValue(cols)
                } else if (results[row]) {
                    system.equations[cols].xor(multiplier.equations[row])
                } else if (multiplier.results[row]) {
                    equations[row].iterateOverAllSetBits { col ->
                        system.results.invertValue(col)
                    }
                }

                row++
            }
        }

        return system
    }

    fun solutions(): Sequence<BitSet> {
        val solution = BitSet(cols)
        val notExpressedVarsCount = varEqMap.count { it == -1 }
        val iter = CombinationIteratorSimple(notExpressedVarsCount)

        return sequence {
            iter.iterateAll {
                var i = 0
                var j = 0
                while (i < varEqMap.size) {
                    if (varEqMap[i] == -1) {
                        solution.setIfTrue(i, iter.combination[j++])
                    }
                    i++
                }

                i = 0
                while (i < varEqMap.size) {
                    if (varEqMap[i] != -1) {
                        val eqIndex = varEqMap[i]
                        val res = equations[eqIndex].evaluate(solution) xor results[eqIndex]
                        solution.setIfTrue(i, res)
                    }
                    i++
                }

                yield(solution)

                solution.clear()
            }
        }
    }

    fun clear() {
        var i = 0
        while (i < equations.size) {
            equations[i].clear()
            i++
        }
        results.clear()

        varEqMap.fill(-1)
        eqVarMap.fill(-1)
    }

    fun clone(): XorEquationSystem {
        val newEquations = Array(rows) { equations[it].clone() as BitSet }
        val newResults = results.clone() as BitSet
        val newEqVarMap = eqVarMap.clone()
        val newVarEqMap = varEqMap.clone()
        return XorEquationSystem(cols, newEquations, newResults, newEqVarMap, newVarEqMap)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as XorEquationSystem

        if (rows != other.rows) return false
        if (cols != other.cols) return false
        if (results != other.results) return false
        if (!equations.contentEquals(other.equations)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rows
        result = 31 * result + cols
        result = 31 * result + equations.contentHashCode()
        result = 31 * result + results.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder(rows * (cols + 3) - 1)
        var eqIndex = 0

        while (eqIndex < rows) {
            var bitIndex = 0
            while (bitIndex < cols) {
                sb.append(equations[eqIndex][bitIndex].toNumChar())
                bitIndex++
            }
            sb.append('|')
            sb.append(results[eqIndex].toNumChar())

            eqIndex++

            if (eqIndex != rows) {
                sb.append('\n')
            }
        }

        return sb.toString()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
