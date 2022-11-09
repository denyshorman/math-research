package keccak.diophantine

import io.kotest.assertions.fail
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import keccak.util.gcd
import kotlin.random.Random

class DiophantineEquationSolverKtTest : FunSpec({
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
})
