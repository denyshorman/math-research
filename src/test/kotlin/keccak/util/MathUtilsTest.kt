package keccak.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeLessThan
import io.kotest.matchers.shouldBe
import kotlin.random.Random

class MathUtilsTest : FunSpec({
    test("pow2") {
        pow2(0).shouldBe(1)
        pow2(1).shouldBe(2)
        pow2(2).shouldBe(4)
        pow2(3).shouldBe(8)
        pow2(4).shouldBe(16)
        pow2(5).shouldBe(32)
    }

    test("gcd") {
        gcd(0, 0).shouldBe(0u)
        gcd(0, 1).shouldBe(1u)
        gcd(1, 0).shouldBe(1u)
        gcd(1, 1).shouldBe(1u)
        gcd(2, 2).shouldBe(2u)
        gcd(2, 0).shouldBe(2u)
        gcd(2, 3).shouldBe(1u)
        gcd(7, 11).shouldBe(1u)
        gcd(-7, -11).shouldBe(1u)
        gcd(-12, 21).shouldBe(3u)
        gcd(12, 64).shouldBe(4u)
        gcd(2 * 2 * 2 * 3 * 3 * 5 * 5 * 7, 2 * 2 * 3 * 7 * 7).shouldBe((2 * 2 * 3 * 7).toUInt())
        gcd(intArrayOf(0, 1, 2, 3, 4)).shouldBe(1u)
        gcd(intArrayOf(2, 4, 6, 8, 10, 12, 20, 40, 50)).shouldBe(2u)
        gcd(intArrayOf(4, 8, 12, 16, 20, 1), 5).shouldBe(4u)
    }

    test("gcdExtendedEuclid") {
        gcdExtendedEuclid(7, 13).shouldBe(intArrayOf(1, 2, -1))
        gcdExtendedEuclid(21, -12).shouldBe(intArrayOf(3, -1, -2))
        gcdExtendedEuclid(2 * 2 * 3 * 5 * 7 * 7, -2 * 5 * 5 * 7 * 11).shouldBe(intArrayOf(2 * 5 * 7, -17, -13))

        val rnd = Random(1)

        repeat(100) {
            val a = rnd.nextInt(-10000, 10000)
            val b = rnd.nextInt(-10000, 10000)

            if (a == 0 || b == 0) return@repeat

            val (d, x0, y0) = gcdExtendedEuclid(a, b)

            (a * x0 + b * y0).shouldBe(d)
        }
    }

    test("gcdBlankinship") {
        val rnd = Random(1)

        repeat(500) {
            val numbers = IntArray(rnd.nextInt(2, 16)) { rnd.nextInt(-300, 300) }

            if (numbers.all { it == 0 }) return@repeat

            val solution = gcdBlankinship(numbers)

            var gcdRowIndex = 0
            while (gcdRowIndex < solution.size) {
                if (solution[gcdRowIndex][0] != 0) break
                gcdRowIndex++
            }

            gcdRowIndex.shouldBeLessThan(solution.size)

            val expectedGcd = gcd(numbers).toInt()
            val actualGcd = solution[gcdRowIndex][0]

            actualGcd.shouldBe(expectedGcd)

            var equationSum = 0
            var columnIndex = 0
            while (columnIndex < numbers.size) {
                equationSum += numbers[columnIndex] * solution[gcdRowIndex][columnIndex + 1]
                columnIndex++
            }

            equationSum.shouldBe(expectedGcd)
        }
    }

    test("linearEquationSystemSolve") {
        val a = arrayOf(
            doubleArrayOf(2.0, 3.0, 1.0),
            doubleArrayOf(1.0, 2.0, 3.0),
            doubleArrayOf(3.0, 1.0, 2.0),
        )

        val b = doubleArrayOf(1.0, 2.0, 3.0)

        //solveLinearEquationSystem(a, b)

        println(a.printMatrix())
        println(b.asSequence().joinToString(" "))
    }
})
