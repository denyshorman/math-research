package keccak.util

import keccak.*
import java.math.BigInteger
import kotlin.math.log2
import kotlin.math.min

fun Xor.toBitEquation(varsCount: Int): XorEquation {
    val eq = XorEquation(varsCount)

    this.nodes.forEach { node ->
        when (node) {
            is Variable -> {
                val varPos = node.name.mapToOnlyDigits().toInt()
                eq.setVariable(varPos)
            }
            is Bit -> {
                eq.setBit(node.value)
            }
            else -> throw IllegalStateException()
        }
    }

    return eq
}

fun List<Pair<XorEquation, XorEquation>>.substitute(eqSystem: XorEquationSystem) {
    forEach {
        it.first.substitute(eqSystem)
        it.second.substitute(eqSystem)
    }
}

fun XorEquation.substitute(eqSystem: XorEquationSystem) {
    var i = 0
    val size = min(eqSystem.rows, eqSystem.cols)

    while (i < size) {
        if (eqSystem.equations[i][i] && bitGroup[i]) {
            bitGroup.xor(eqSystem.equations[i])
            result = result xor eqSystem.results[i]
        }

        i++
    }
}

fun List<Pair<XorEquation, XorEquation>>.allSetBits(varsCount: Int): BitGroup {
    val mask = BitGroup(varsCount)

    forEach {
        mask.or(it.first.bitGroup)
        mask.or(it.second.bitGroup)
    }

    return mask
}

fun List<Pair<XorEquation, XorEquation>>.varCountAndOffset(varsCount: Int): Pair<Int, Int> {
    val mask = allSetBits(varsCount)
    val newVarsCount = mask.setBitsCount()
    val offset = mask.nextSetBit(0)
    return Pair(newVarsCount, offset)
}

fun List<Pair<XorEquation, XorEquation>>.additionalEqToBitSystem(varsCount: Int, offset: Int): XorEquationSystem {
    val varCombinationsCount = (varsCount * (varsCount + 1)) / 2
    val system = XorEquationSystem(size, varCombinationsCount)

    forEachIndexed { eqIndex, eq ->
        var leftSetBitIndex = eq.first.bitGroup.nextSetBit(0)
        while (leftSetBitIndex >= 0) {
            if (leftSetBitIndex == Integer.MAX_VALUE) break
            var rightSetBitIndex = eq.second.bitGroup.nextSetBit(0)
            while (rightSetBitIndex >= 0) {
                if (rightSetBitIndex == Integer.MAX_VALUE) break
                val varCombinationIndex = calcCombinationIndex(leftSetBitIndex - offset, rightSetBitIndex - offset, varsCount)
                system.equations[eqIndex].xor(varCombinationIndex, true)
                rightSetBitIndex = eq.second.bitGroup.nextSetBit(rightSetBitIndex + 1)
            }
            leftSetBitIndex = eq.first.bitGroup.nextSetBit(leftSetBitIndex + 1)
        }

        if (eq.first.result) {
            var bitIndex = eq.second.bitGroup.nextSetBit(0)
            while (bitIndex >= 0) {
                if (bitIndex == Integer.MAX_VALUE) break
                val varCombinationIndex = calcCombinationIndex(bitIndex - offset, varsCount)
                system.equations[eqIndex].xor(varCombinationIndex, true)
                bitIndex = eq.second.bitGroup.nextSetBit(bitIndex + 1)
            }
        }

        if (eq.second.result) {
            var bitIndex = eq.first.bitGroup.nextSetBit(0)
            while (bitIndex >= 0) {
                if (bitIndex == Integer.MAX_VALUE) break
                val varCombinationIndex = calcCombinationIndex(bitIndex - offset, varsCount)
                system.equations[eqIndex].xor(varCombinationIndex, true)
                bitIndex = eq.first.bitGroup.nextSetBit(bitIndex + 1)
            }
        }

        if (eq.first.result && eq.second.result) {
            system.results.xor(eqIndex, true)
        }

        var bitIndex = eq.second.bitGroup.nextSetBit(0)
        while (bitIndex >= 0) {
            if (bitIndex == Integer.MAX_VALUE) break
            val varCombinationIndex = calcCombinationIndex(bitIndex - offset, varsCount)
            system.equations[eqIndex].xor(varCombinationIndex, true)
            bitIndex = eq.second.bitGroup.nextSetBit(bitIndex + 1)
        }

        if (eq.second.result) {
            system.results.xor(eqIndex, true)
        }
    }

    return system
}

fun calcCombinationIndex(i: Int, j: Int, varsCount: Int): Int {
    val left: Int
    val right: Int

    if (i >= j) {
        left = j
        right = i
    } else {
        left = i
        right = j
    }

    return right + (left * (2 * varsCount - left - 1)) / 2
}

fun calcCombinationIndex(i: Int, varsCount: Int): Int {
    return (i * (2 * varsCount - i + 1)) / 2
}

fun calcCombinationPartialIndex(targetIndex: Int, varsCount: Int): Pair<Int, Int> {
    var i = 0
    var j = 0
    var k = 0

    while (i < varsCount && j < varsCount) {
        if (targetIndex == k) break

        k++
        j++
        if (j == varsCount) {
            i++
            j = i
        }
    }

    return Pair(i, j)
}

fun <T> Array<T>.exchange(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

fun IntArray.exchange(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

fun factorial(n: Long): Long {
    var x = 1L
    var i = n
    while (i >= 2) {
        x *= i--
    }
    return x
}

fun factorial(n: BigInteger): BigInteger {
    var x = BigInteger.ONE
    var i = n
    while (i >= BigInteger.TWO) {
        x *= i--
    }
    return x
}

fun placement(n: Long, k: Long): Long {
    var x = 1L
    var i = n
    var j = 0
    while (j < k) {
        x *= i
        i--
        j++
    }
    return x
}

fun combinations(n: Long, k: Long): Long {
    return placement(n, k) / factorial(k)
}

fun combinationsWithRepetition(n: Long, k: Long): Long {
    return combinations(n + k - 1, k)
}

fun estimateFactorial(n: Long): Double {
    var i = n
    var estimate = 0.0

    while (i > 1) {
        estimate += log2(i.toDouble())
        i--
    }

    return estimate
}

fun estimatePartialPermutation(n: Long, k: Long): Double {
    return estimateFactorial(n) - estimateFactorial(n - k)
}

fun estimateCombinations(n: Long, k: Long): Double {
    return estimatePartialPermutation(n, k) - estimateFactorial(k)
}

fun estimateCombinationsWithRepetition(n: Long, k: Long): Double {
    return estimateCombinations(n + k - 1, k)
}
