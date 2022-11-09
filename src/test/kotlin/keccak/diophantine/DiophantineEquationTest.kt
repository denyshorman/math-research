package keccak.diophantine

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DiophantineEquationTest : FunSpec({
    test("toString") {
        val eq = DiophantineEquation(3, 5, 7, 17)
        eq.toString().shouldBe("3*x0 + 5*x1 + 7*x2 = 17")
    }

    test("hasSolution") {
        DiophantineEquation(3, 5, 7, 17).hasSolution().shouldBe(true)
        DiophantineEquation(3, 5, 7, 17).hasSolution().shouldBe(true)
        DiophantineEquation(7, 14, 21, 49, 35).hasSolution().shouldBe(true)
    }
})
