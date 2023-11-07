package keccak.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import keccak.XorEquation
import keccak.plus
import keccak.times
import kotlin.test.assertEquals

class UtilsTest : FunSpec({
    context("calcCombinationIndex") {
        test("1") {
            assertEquals(0, calcCombinationIndex(0, 0, 4))
            assertEquals(1, calcCombinationIndex(0, 1, 4))
            assertEquals(2, calcCombinationIndex(0, 2, 4))
            assertEquals(3, calcCombinationIndex(0, 3, 4))
            assertEquals(4, calcCombinationIndex(1, 1, 4))
            assertEquals(5, calcCombinationIndex(1, 2, 4))
            assertEquals(6, calcCombinationIndex(1, 3, 4))
            assertEquals(7, calcCombinationIndex(2, 2, 4))
            assertEquals(8, calcCombinationIndex(2, 3, 4))
            assertEquals(9, calcCombinationIndex(3, 3, 4))
        }

        test("2") {
            assertEquals(0, calcCombinationIndex(0, 4))
            assertEquals(4, calcCombinationIndex(1, 4))
            assertEquals(7, calcCombinationIndex(2, 4))
            assertEquals(9, calcCombinationIndex(3, 4))
        }
    }

    context("additionalEqToBitSystem") {
        test("1") {
            val varsCount = 3

            val eq0 = XorEquation(varsCount)
            val eq1 = XorEquation(varsCount)

            eq0.setVariable(0)
            eq0.setVariable(1)
            eq0.setBit(true)

            eq1.setVariable(1)
            eq1.setVariable(2)
            eq1.setBit(true)

            val eqSystem = listOf(Pair(eq0, eq1))

            val newSystem = eqSystem.additionalEqToBitSystem(varsCount, 0)

            assertEquals("111010|0", newSystem.toString())
        }

        test("2") {
            val varsCount = 3

            val eq0 = XorEquation(varsCount)
            val eq1 = XorEquation(varsCount)

            eq0.setVariable(0)
            eq0.setVariable(1)
            eq0.setBit(false)

            eq1.setVariable(0)
            eq1.setVariable(1)
            eq1.setBit(true)

            val eqSystem = listOf(Pair(eq0, eq1))

            val newSystem = eqSystem.additionalEqToBitSystem(varsCount, 0)
            assertEquals("100100|1", newSystem.toString())
        }
    }

    context("nodeExpressVariable") {
        test("1") {
            x0.express(x0).shouldBe(x0)
            ((x0*x1 + t)*(x1 + t)).express(x1).shouldBe(x1 + t)
            ((x0*x1 + t)*(x1 + t)).express(x0).shouldBe(null)

            (
                (x5 + (x3)*(x4 + t) + x0 + t)*
                (x6 + (x4)*(x0 + t) + x1 + t)*
                (x7 + (x0)*(x1 + t) + x2 + t)*
                (x8 + (x1)*(x2 + t) + x3 + t)*
                (x9 + (x2)*(x3 + t) + x4 + t)
            )
                .express(x0, setOf(x0,x5,x6,x7,x8,x9))
                .shouldBe(x0 + x5 + x6 + x8 + x6*x7 + x6*x9 + x8*x9 + x6*x7*x9 + t)
        }
    }
})
