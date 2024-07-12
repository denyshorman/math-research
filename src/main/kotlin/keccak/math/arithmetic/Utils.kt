package keccak.math.arithmetic

import keccak.CombinationIteratorSimple
import keccak.util.toBitString
import java.io.OutputStream
import kotlin.random.Random

fun Array<BooleanArray>.contains(current: BooleanArray): Boolean {
    var i = 0
    var contains = false
    while (i < size) {
        if (this[i].contentEquals(current)) {
            contains = true
            break
        }
        i++
    }
    return contains
}

fun prodSum(x: BooleanArray, y: IntArray): Int {
    var sum = 0
    var i = 0
    while (i < x.size) {
        if (x[i]) sum += y[i]
        i++
    }
    return sum
}

fun IntArray.randomize(rndLeft: Int, rndRight: Int, rnd: Random) {
    var i = 0
    while (i < size) {
        this[i] = rnd.nextInt(rndLeft, rndRight)
        i++
    }
}

fun generateRandomEquation(
    solutions: Array<BooleanArray>,
    rhs: Int,
    rndLeft: Int,
    rndRight: Int,
    allowMoreSolutions: Boolean,
    rnd: Random = Random,
): IntArray {
    val varsCount = solutions[0].size

    val iter = CombinationIteratorSimple(varsCount)
    val nums = IntArray(varsCount) { 0 }

    while (true) {
        nums.randomize(rndLeft, rndRight, rnd)
        var compatible = true

        iter.iterate {
            val sum = prodSum(iter.combination, nums)
            val exists = solutions.contains(iter.combination)

            if (sum == rhs) {
                if (!exists && !allowMoreSolutions) {
                    compatible = false
                }
            } else {
                if (exists) {
                    compatible = false
                }
            }

            compatible
        }

        if (compatible) {
            return nums
        }
    }
}

fun generateRandomSystem(
    eqsCount: Int,
    solutions: Array<BooleanArray>,
    rhs: Int,
    rndLeft: Int,
    rndRight: Int,
    allowMoreSolutions: Boolean,
    rnd: Random = Random,
): Array<IntArray> {
    return Array(eqsCount) { generateRandomEquation(solutions, rhs, rndLeft, rndRight, allowMoreSolutions, rnd) }
}

fun IntArray.toArithmeticNode(
    varNamePrefix: String = "x",
    varType: BooleanVariable.Type = BooleanVariable.Type.ZERO_ONE,
): Sum {
    return asSequence()
        .mapIndexed { index, num -> BooleanVariable("$varNamePrefix$index", varType) * IntNumber(num) }
        .let { Sum(it) }
}

fun IntArray.printAllCombinations(outputStream: OutputStream = System.out) {
    val varsCount = size
    val iter = CombinationIteratorSimple(varsCount)

    iter.iterateAll {
        val sol = iter.combination.toBitString()
        val sum = prodSum(iter.combination, this)
        outputStream.write("$sol = $sum\n".toByteArray(Charsets.US_ASCII))
    }
}
