package keccak.arithmetic.plusminusone

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import keccak.math.arithmetic.*
import keccak.math.arithmetic.plusminusone.*
import java.util.LinkedList

class EquationSystemTest : FunSpec({
    test("1") {
        val system = arrayOf<ArithmeticNode>(
            x0 + x1 + x2 + 1,
            x0 + 2*x1 - 3*x2 + x3 + 7,
        )

        var i = 0
        while (i < system.size) {
            system[i] = system[i].expand()
            i++
        }

        val expVars = arrayOf<ArithmeticNode>(x0, x1)

        solveLinearSystem(system, expVars)

        system[0].shouldBe((x0 + 5*x2 - x3 - 5).expand())
        system[1].shouldBe((x1 - 4*x2 + x3 + 6).expand())
    }

    test("2") {
        val system = arrayOf<ArithmeticNode>(
            x0 + x1 + x2 + 1,
            x0 + 3*x1 - 3*x2 + x3 + 7,
        )

        var i = 0
        while (i < system.size) {
            system[i] = system[i].expand()
            i++
        }

        val expVars = arrayOf<ArithmeticNode>(x0, x1)

        solveLinearSystem(system, expVars)

        for (eq in system) {
            println("$eq = 0")
        }

        system[0].shouldBe((2*x0 + 6*x2 - x3 - 4).expand())
        system[1].shouldBe((2*x1 - 4*x2 + x3 + 6).expand())
    }

    test("3") {
        val system = arrayOf(
            x0 + x1 - x2 - 3,
            x0*(x0 + x1 - x2 - 3),
            x1*(x0 + x1 - x2 - 3),
            x2*(x0 + x1 - x2 - 3),
        )

        var i = 0
        while (i < system.size) {
            system[i] = system[i].expand()
            i++
        }

        for (eq in system) {
            println("$eq = 0")
        }
        println()

        val expVars = arrayOf(x0, x0*x1, x0*x2, x1*x2)

        solveLinearSystem(system, expVars)

        for (eq in system) {
            println("$eq = 0")
        }

        system[0].shouldBe((x0 - x2 + x1 - 3).expand())
        system[1].shouldBe((x0*x1 - 3*x2 - 4).expand())
        system[2].shouldBe((x0*x2 - 3*x1 + 4).expand())
        system[3].shouldBe((x1*x2 - 3*x2 + 3*x1 - 5).expand())
    }

    test("4") {
        val basicSystem = arrayOf(
            x0 - (2*x9 - x7 - 2*x8),
            x1 - (-x5 - 2*x6 + 2*x9),
            x2 - (x5 + x7 + 2*x6 + 2*x8 - 2*x9 + 1),
            x3 - (2*x9 - x5 - x7 - 5),
            x4 - (x9 - x8 - x6 + 2),
        )

        val eqs = LinkedList<ArithmeticNode>()
        eqs.addAll(basicSystem)

        for (eq in basicSystem) {
            for (i in 0..9) {
                val eq2 = eq*BooleanVariable("x$i", BooleanVariable.Type.PLUS_MINUS_ONE)
                eqs.add(eq2)
            }
        }

        var j = 0
        while (j < basicSystem.size) {
            var k = j
            while (k < basicSystem.size) {
                val eq2 = basicSystem[j]*basicSystem[k]
                eqs.add(eq2)
                k++
            }
            j++
        }

        val system = eqs.toTypedArray()

        var i = 0
        while (i < system.size) {
            system[i] = system[i].expand()
            i++
        }

        for (eq in system) {
            println("$eq = 0")
        }
        println()

        val expVars = arrayOf(
            x0,x1,x2,x3,x4,
            x0*x1, x0*x2, x0*x3, x0*x4, x0*x5, x0*x6, x0*x7, x0*x8, x0*x9,
            x1*x2, x1*x3, x1*x4, x1*x5, x1*x6, x1*x7, x1*x8, x1*x9,
            x2*x3, x2*x4, x2*x5, x2*x6, x2*x7, x2*x8, x2*x9,
            x3*x4, x3*x5, x3*x6, x3*x7, x3*x8, x3*x9,
            x4*x5, x4*x6, x4*x7, x4*x8, x4*x9,
            x5*x6, x5*x7, x5*x8, x5*x9,
            x6*x7, x6*x8, x6*x9,
            x7*x8, x7*x9,
            x8*x9,
        )

        solveLinearSystem(system, expVars)

        for (eq in system) {
            println("$eq = 0")
        }
    }
})
