package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import keccak.util.BitSet
import keccak.util.XorEquationSystem
import keccak.util.randomXorEquationSystem
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.*

class XorEquationSystemTest : FunSpec({
    context("evaluate") {
        test("1") {
            val rows = 2
            val cols = 2
            val eqSystem = XorEquationSystem(rows, cols)
            val vars = BitSet(cols)

            vars[0] = true
            vars[1] = true

            eqSystem.equations[0][0] = true
            eqSystem.equations[0][1] = false
            eqSystem.results[0] = true

            eqSystem.equations[1][0] = false
            eqSystem.equations[1][1] = true
            eqSystem.results[1] = true

            val expectedEqSystem = XorEquationSystem(rows, cols)
            expectedEqSystem.equations[0][0] = false
            expectedEqSystem.equations[0][1] = false
            expectedEqSystem.results[0] = false

            expectedEqSystem.equations[1][0] = false
            expectedEqSystem.equations[1][1] = false
            expectedEqSystem.results[1] = false

            eqSystem.substitute(vars)

            assertEquals(expectedEqSystem, eqSystem)
        }

        test("2") {
            val rows = 2
            val cols = 2
            val eqSystem = XorEquationSystem(rows, cols)
            val vars = BitSet(cols)

            vars[0] = true
            vars[1] = true

            eqSystem.equations[0][0] = false
            eqSystem.equations[0][1] = false
            eqSystem.results[0] = true

            eqSystem.equations[1][0] = true
            eqSystem.equations[1][1] = true
            eqSystem.results[1] = false

            val expectedEqSystem = XorEquationSystem(rows, cols)
            expectedEqSystem.equations[0][0] = false
            expectedEqSystem.equations[0][1] = false
            expectedEqSystem.results[0] = true

            expectedEqSystem.equations[1][0] = false
            expectedEqSystem.equations[1][1] = false
            expectedEqSystem.results[1] = false

            eqSystem.substitute(vars)

            assertEquals(expectedEqSystem, eqSystem)
        }
    }

    context("partiallyEvaluate") {
        test("1") {
            val rows = 2
            val cols = 2

            val varValues = BitGroup(cols)

            varValues[0] = true
            varValues[1] = true

            val availableVars = BitGroup(cols)

            availableVars[0] = true
            availableVars[1] = true

            val eqSystem = XorEquationSystem(rows, cols)

            eqSystem.equations[0][0] = true
            eqSystem.equations[0][1] = false
            eqSystem.results[0] = true

            eqSystem.equations[1][0] = false
            eqSystem.equations[1][1] = true
            eqSystem.results[1] = true

            val expectedEqSystem = XorEquationSystem(rows, cols)

            expectedEqSystem.equations[0][0] = false
            expectedEqSystem.equations[0][1] = false
            expectedEqSystem.results[0] = false

            expectedEqSystem.equations[1][0] = false
            expectedEqSystem.equations[1][1] = false
            expectedEqSystem.results[1] = false

            eqSystem.substitute(varValues.bitSet, availableVars.bitSet)

            assertEquals(expectedEqSystem, eqSystem)
        }

        test("2") {
            val rows = 2
            val cols = 2

            val varValues = BitGroup(cols)

            varValues[0] = true
            varValues[1] = true

            val availableVars = BitGroup(cols)

            availableVars[0] = false
            availableVars[1] = false

            val eqSystem = XorEquationSystem(rows, cols)

            eqSystem.equations[0][0] = true
            eqSystem.equations[0][1] = false
            eqSystem.results[0] = true

            eqSystem.equations[1][0] = false
            eqSystem.equations[1][1] = true
            eqSystem.results[1] = true

            val expectedEqSystem = XorEquationSystem(rows, cols)

            expectedEqSystem.equations[0][0] = true
            expectedEqSystem.equations[0][1] = false
            expectedEqSystem.results[0] = true

            expectedEqSystem.equations[1][0] = false
            expectedEqSystem.equations[1][1] = true
            expectedEqSystem.results[1] = true

            eqSystem.substitute(varValues.bitSet, availableVars.bitSet)

            assertEquals(expectedEqSystem, eqSystem)
        }

        test("3") {
            val rows = 2
            val cols = 2

            val varValues = BitGroup(cols)

            varValues[0] = true
            varValues[1] = true

            val availableVars = BitGroup(cols)

            availableVars[0] = false
            availableVars[1] = true

            val eqSystem = XorEquationSystem(rows, cols)

            eqSystem.equations[0][0] = true
            eqSystem.equations[0][1] = false
            eqSystem.results[0] = true

            eqSystem.equations[1][0] = false
            eqSystem.equations[1][1] = true
            eqSystem.results[1] = true

            val expectedEqSystem = XorEquationSystem(rows, cols)

            expectedEqSystem.equations[0][0] = true
            expectedEqSystem.equations[0][1] = false
            expectedEqSystem.results[0] = true

            expectedEqSystem.equations[1][0] = false
            expectedEqSystem.equations[1][1] = false
            expectedEqSystem.results[1] = false

            eqSystem.substitute(varValues.bitSet, availableVars.bitSet)

            assertEquals(expectedEqSystem, eqSystem)
        }
    }

    context("solve") {
        test("1") {
            val system = XorEquationSystem(
                rows = 10, cols = 15,
                humanReadable = false,
                "110100011101011|1",
                "011011110101100|1",
                "011100000010000|0",
                "101011111000111|1",
                "101011000010000|1",
                "101100011011101|0",
                "100101011100100|1",
                "000011111001100|0",
                "011100101011101|0",
                "101011111000011|1",
            )

            val solved = system.solve(sortEquations = true)
            solved.shouldBeTrue()

            val expected = """
                100001000001010|0
                010001000001011|0
                001001000000001|1
                000100000011010|1
                000011000011011|0
                000000100011011|0
                000000010011010|0
                000000001010010|0
                000000000111000|0
                000000000000100|0
            """.trimIndent()

            system.toString().shouldBe(expected)
        }

        test("2") {
            val system = randomXorEquationSystem(6, 6)

            println(system)
            println()

            val solved = system.solve()

            println(system)
            println()

            println("Solved: $solved")
        }

        test("3") {
            val system = XorEquationSystem(
                rows = 5, cols = 5,
                humanReadable = false,
                "01111|1",
                "10010|0",
                "00100|1",
                "10011|0",
                "00111|1",
            )
            val system2 = system.clone()

            system.solve(activeRows = BitSet("11111"), varPriority = BitSet("11111"))
            system2.solve()

            system.shouldBe(system2)
        }

        test("4") {
            val system = randomXorEquationSystem(6, 6)
            val system2 = system.clone()

            println(system)
            println()

            val solved = system.solve(activeRows = BitSet("110011"), varPriority = BitSet("110011"))
            val solved2 = system2.solve()

            println(system)
            println()

            println(system2)
            println()

            println("Solved: $solved")
            println("Solved2: $solved2")
        }

        test("5") {
            val system = XorEquationSystem(rows = 5, cols = 5,
                humanReadable = true,
                "x1 + x2 + x4 = 1",
                "x2 + x4 = 0",
                "x0 + x2 + x4 = 1",
                "x1 + x4 = 0",
                "x0 + x1 + x4 = 0",
            )

            val solved = system.solve()

            solved.shouldBeFalse()
        }
    }

    context("characteristicSystem") {
        test("1") {
            val system = XorEquationSystem(
                rows = 10,
                cols = 7,
                humanReadable = true,
                "x0 + x2 + x3 + x6 = 1",
                "x0 + x1 + x2 + x5 = 0",
                "x1 + x2 + x4 + x5 + x6 = 1",
                "x0 + x2 + x5 + x6 = 0",
                "x3 + x5 = 1",
                "x1 + x3 = 0",
                "x1 + x3 + x6 = 0",
                "x2 + x3 + x5 + x6 = 0",
                "x1 + x3 + x5 + x6 = 1",
                "x0 + x2 + x3 + x4 + x5 + x6 = 1",
            )

            val charSystem = system.characteristicSystem()

            val charSystemExpected = XorEquationSystem(
                rows = 7 + 1,
                cols = 10,
                humanReadable = true,
                "a0 + a1 + a3 + a9 = 0",
                "a1 + a2 + a5 + a6 + a8 = 0",
                "a0 + a1 + a2 + a3 + a7 + a9 = 0",
                "a0 + a4 + a5 + a6 + a7 + a8 + a9 = 0",
                "a2 + a9 = 0",
                "a1 + a2 + a3 + a4 + a7 + a8 + a9 = 0",
                "a0 + a2 + a3 + a6 + a7 + a8 + a9 = 0",
                "a0 + a2 + a4 + a8 + a9 = 0",
            )

            charSystem.shouldBe(charSystemExpected)
        }
    }

    context("randomXorEquationSystem") {
        test("1") {
            val rnd = kotlin.random.Random(1)

            val randomSystem = randomXorEquationSystem(
                rows = 5,
                cols = 5,
                solutionsCount = 1,
                random = rnd,
            )

            val randomSystemActual = randomSystem.toString()

            val randomSystemExpected = """
                01111|1
                01100|1
                01101|1
                10101|0
                10010|0
            """.trimIndent()

            randomSystemActual.shouldBe(randomSystemExpected)

            randomSystem.solve(sortEquations = true)

            val randomSystemSolvedActual = randomSystem.toString()

            val randomSystemSolvedExpected = """
                10000|0
                01000|1
                00100|0
                00010|0
                00001|0
            """.trimIndent()

            randomSystemSolvedActual.shouldBe(randomSystemSolvedExpected)
        }
    }
})
