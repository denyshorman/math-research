package keccak.arithmetic.zeroone

import io.kotest.core.spec.style.FunSpec
import keccak.math.arithmetic.*
import keccak.math.arithmetic.zeroone.*
import java.math.BigInteger
import java.util.LinkedList
import kotlin.random.Random
import kotlin.test.fail

class EquationSystemTest : FunSpec({
    test("0") {
        val system = arrayOf<ArithmeticNode>(
            x.pow(1) - (x0 + 2*x1 + 4*x2).pow(1),
            x.pow(2) - (x0 + 2*x1 + 4*x2).pow(2),
            x.pow(3) - (x0 + 2*x1 + 4*x2).pow(3),
            x.pow(4) - (x0 + 2*x1 + 4*x2).pow(4),
            x.pow(5) - (x0 + 2*x1 + 4*x2).pow(5),
            x.pow(6) - (x0 + 2*x1 + 4*x2).pow(6),
            x.pow(7) - (x0 + 2*x1 + 4*x2).pow(7),
        )

        var i = 0
        while (i < system.size) {
            system[i] = system[i].expand()
            i++
        }

        val x = arrayOf(
            mapOf(x to IntNumber(0), x0 to IntNumber(0), x1 to IntNumber(0), x2 to IntNumber(0)),
            mapOf(x to IntNumber(1), x0 to IntNumber(1), x1 to IntNumber(0), x2 to IntNumber(0)),
            mapOf(x to IntNumber(2), x0 to IntNumber(0), x1 to IntNumber(1), x2 to IntNumber(0)),
            mapOf(x to IntNumber(3), x0 to IntNumber(1), x1 to IntNumber(1), x2 to IntNumber(0)),
            mapOf(x to IntNumber(4), x0 to IntNumber(0), x1 to IntNumber(0), x2 to IntNumber(1)),
            mapOf(x to IntNumber(5), x0 to IntNumber(1), x1 to IntNumber(0), x2 to IntNumber(1)),
            mapOf(x to IntNumber(6), x0 to IntNumber(0), x1 to IntNumber(1), x2 to IntNumber(1)),
            mapOf(x to IntNumber(7), x0 to IntNumber(1), x1 to IntNumber(1), x2 to IntNumber(1)),
        )

        x.forEach { vars ->
            system.forEach {eq ->
                println(eq.substitute(vars).expand())
            }
            println()
        }


        for (eq in system) {
            println("$eq = 0")
        }
        println()

        val expVars = arrayOf(
            x0,
            x1,
            x2,
            x0 * x1,
            x0 * x2,
            x1 * x2,(x0 * x1 * x2).expand()
        )

        solveLinearSystem(system, expVars)

        for (eq in system) {
            println("$eq = 0")
        }
    }

    test("1") {
        val basicSystem = arrayOf(
            x0 - 2*x9 + x7 + 2*x8 - 1,
            x1 + x5 + 2*x6 - 2*x9 - 1,
            x2 - x5 - x7 - 2*x6 - 2*x8 + 2*x9 + 1,
            x3 - 2*x9 + x5 + x7 + 2,
            x4 - x9 + x8 + x6 - 2,
        )

        val eqs = LinkedList<ArithmeticNode>()
        eqs.addAll(basicSystem)

        val rnd = Random(1)

        for (eq in basicSystem) {
            repeat(10) {
                val mult = Sum((0..9).asSequence().map { i ->
                    val coeff = if (rnd.nextInt(0, 10) <= 3) {
                        IntNumber(0)
                    } else {
                        IntNumber(rnd.nextInt(-4, 4))
                    }

                    coeff*BooleanVariable("x$i", BooleanVariable.Type.ZERO_ONE)
                })

                val eq2 = mult*eq
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

        val varsMap = mapOf(
            x0 to IntNumber(1),
            x1 to IntNumber(1),
            x2 to IntNumber(1),
            x3 to IntNumber(0),
            x4 to IntNumber(1),
            x5 to IntNumber(0),
            x6 to IntNumber(1),
            x7 to IntNumber(0),
            x8 to IntNumber(1),
            x9 to IntNumber(1),
        )

        var i = 0
        while (i < system.size) {
            system[i] = system[i].expand()
            val res = system[i].evaluate(varsMap)
            if (res != BigInteger.ZERO) {
                fail("${system[i]} = $res != 0")
            }
            println("${system[i]} = 0")
            i++
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
