package keccak

import keccak.util.setIfTrue
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*

class XorAndEquationSystem(
    val xorSystem: XorEquationSystem,
    val andSystem: AndEquationSystem,
) {
    val cols: Int get() = xorSystem.cols

    fun substituteAndWithXor(mask: BitSet? = null) {
        andSystem.substitute(xorSystem, mask)
    }

    fun substitute(index: Int, value: Boolean) {
        xorSystem.substitute(index, value)
        andSystem.substitute(index, value)
    }

    fun substitute(values: Iterable<Pair<Int, Boolean>>) {
        xorSystem.substitute(values)
        andSystem.substitute(values)
    }

    fun isValid(solution: BitSet): Boolean {
        return xorSystem.isValid(solution) && andSystem.isValid(solution)
    }

    fun invert(): XorAndEquationSystem {
        val extVarsCount = andSystem.rows * 2
        val newCols = cols + extVarsCount

        val xorSystem0 = XorEquationSystem(xorSystem.rows + extVarsCount, newCols)
        val andSystem0 = AndEquationSystem(andSystem.rows, newCols)

        var i = 0
        while (i < xorSystem.rows) {
            xorSystem0.equations[i] = xorSystem.equations[i].clone() as BitSet
            xorSystem0.results.setIfTrue(i, xorSystem.results[i])
            i++
        }

        var j = 0
        var k = cols
        while (j < andSystem.rows) {
            xorSystem0.equations[i] = andSystem.equations[j].andOpLeft.clone() as BitSet
            xorSystem0.equations[i].set(k)
            xorSystem0.results.setIfTrue(i, andSystem.andOpLeftResults[j])

            i++
            k++

            xorSystem0.equations[i] = andSystem.equations[j].andOpRight.clone() as BitSet
            xorSystem0.equations[i].set(k)
            xorSystem0.results.setIfTrue(i, andSystem.andOpRightResults[j])

            i++
            k++
            j++
        }

        j = 0
        k = cols
        while (j < andSystem.rows) {
            andSystem0.equations[j].andOpLeft.set(k++)
            andSystem0.equations[j].andOpRight.set(k++)

            andSystem0.equations[j].rightXor = andSystem.equations[j].rightXor.clone() as BitSet
            andSystem0.rightXorResults.setIfTrue(j, andSystem.rightXorResults[j])

            j++
        }

        return XorAndEquationSystem(xorSystem0, andSystem0)
    }

    fun invertToXorSystem(): XorEquationSystem {
        val system = XorEquationSystem(
            rows = xorSystem.rows + 3*andSystem.rows,
            cols = cols + 3*andSystem.rows,
        )

        var i = 0
        while (i < xorSystem.rows) {
            system.equations[i] = xorSystem.equations[i].clone() as BitSet
            system.results.setIfTrue(i, xorSystem.results[i])
            i++
        }

        var j = 0
        var k = cols
        while (j < andSystem.rows) {
            system.equations[i] = andSystem.equations[j].andOpLeft.clone() as BitSet
            system.equations[i].set(k)
            system.results.setIfTrue(i, andSystem.andOpLeftResults[j])

            i++
            k++

            system.equations[i] = andSystem.equations[j].andOpRight.clone() as BitSet
            system.equations[i].set(k)
            system.results.setIfTrue(i, andSystem.andOpRightResults[j])

            i++
            k++
            j++
        }

        j = 0
        while (j < andSystem.rows) {
            system.equations[i] = andSystem.equations[j].rightXor.clone() as BitSet
            system.equations[i].set(k)
            system.results.setIfTrue(i, andSystem.rightXorResults[j])

            i++
            k++
            j++
        }

        return system
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
