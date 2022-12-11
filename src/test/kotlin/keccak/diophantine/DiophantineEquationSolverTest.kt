package keccak.diophantine

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import keccak.util.gcd
import kotlin.random.Random

class DiophantineEquationSolverTest : FunSpec({
    test("solveSimplestDiophantineEquation") {
        val rnd = Random(3)
        repeat(500) {
            val a = rnd.nextInt(-10000, 10000)
            val b = rnd.nextInt(-10000, 10000)
            if (a == 0 || b == 0) return@repeat
            val gcd = gcd(a, b).toInt()
            val c = rnd.nextInt(-1000, 1000)
            val d = c * gcd
            val eq = DiophantineEquation(a, b, d)
            val solution = solveSimplestDiophantineEquation(eq) ?: fail("Must have solution")

            (-10..10).forEach { k ->
                val x = solution[0][0] + solution[0][1] * k
                val y = solution[1][0] + solution[1][1] * k

                (a * x + b * y).shouldBe(d)
            }
        }
    }

    test("solveGeneralDiophantineEquation") {
        val rnd = Random(1)

        repeat(1000) {
            val coeffsSize = rnd.nextInt(4, 6)

            val coeffs = IntArray(coeffsSize) {
                var value: Int
                while (true) {
                    value = rnd.nextInt(-100, 100)
                    if (value == 0) continue else break
                }
                value
            }

            coeffs[coeffsSize - 1] *= gcd(coeffs, coeffsSize - 1).toInt()

            val eq = DiophantineEquation(*coeffs)
            val solution = solveGeneralDiophantineEquation(eq)

            repeat(100) {
                val k = IntArray(coeffsSize - 1) { rnd.nextInt(-100, 100) }
                var sum = 0

                var i = 0
                while (i < solution.size) {
                    var j = 0
                    var sum0 = 0
                    while (j < solution[i].size) {
                        sum0 += if (j == 0) {
                            solution[i][j]
                        } else {
                            solution[i][j] * k[j - 1]
                        }
                        j++
                    }

                    sum += sum0 * coeffs[i]
                    i++
                }

                sum.shouldBe(coeffs[coeffsSize - 1])
            }
        }
    }

    test("printGeneralDiophantineSolution") {
        val eq = DiophantineEquation(1, 1, 1, 1, 1)
        val solution = specialDiophantineSolver(eq)
        solution.printGeneralDiophantineSolution()

        val rnd = Random(1)

        repeat(100) {
            val k = IntArray(eq.coefficients.size - 1) { rnd.nextInt(-100, 100) }
            var sum = 0

            var i = 0
            while (i < solution.size) {
                var j = 0
                var sum0 = 0
                while (j < solution[i].size) {
                    sum0 += if (j == 0) {
                        solution[i][j]
                    } else {
                        solution[i][j] * k[j - 1]
                    }
                    j++
                }

                sum += sum0 * eq.coefficients[i]
                i++
            }

            sum.shouldBe(eq.coefficients[eq.coefficients.size - 1])
        }
    }
})

private fun IntArray.toDiophantineSolutionString(): String {
    return this.asSequence()
        .mapIndexed { index, value -> index to value }
        .filter { it.second != 0 }
        .joinToString(" + ") { (i, v) ->
            if (i == 0) {
                v.toString()
            } else {
                if (v < 0) {
                    "($v)*k${i - 1}"
                } else if (v == 1) {
                    "k${i - 1}"
                } else {
                    "$v*k${i - 1}"
                }
            }
        }
}

private fun Array<IntArray>.printGeneralDiophantineSolution() {
    indices.forEach { i ->
        val eq = this[i].toDiophantineSolutionString()
        println("x$i = $eq")
    }
}

private fun specialDiophantineSolver(equation: DiophantineEquation): Array<IntArray> {
    val variablesCount = equation.coefficients.size - 1

    val gcds = IntArray(variablesCount) { 0 }
    gcds[0] = equation.coefficients[0]
    var i = 1
    while (i < gcds.size) {
        gcds[i] = gcd(gcds[i - 1], equation.coefficients[i]).toInt()
        i++
    }

    val solution = Array(variablesCount) { IntArray(variablesCount) { 0 } }
    solution[0][0] = equation.coefficients[variablesCount] / gcds[gcds.size - 1]

    // a0*x0 + a1*x1 + a2*x2 + a3*x3 = a4*gcd(a0,a1,a2,a3)

    // a0*x0 + a1*x1 = y1
    // y1 + a2*x2 = y0
    // y0 + a3*x3 = a4

    i = variablesCount - 1
    while (i > 0) {
        //val (_, x0, x1) = gcdExtendedEuclid(gcds[i - 1], equation.coefficients[i])
        val (x0, x1) = 1 to 0

        solution[0].copyInto(solution[i])

        (0 until variablesCount).forEach { j ->
            solution[0][j] *= x0
            solution[i][j] *= x1
        }

        solution[0][i] = equation.coefficients[i]
        solution[i][i] = -gcds[i - 1]

        i--
    }

    return solution
}
