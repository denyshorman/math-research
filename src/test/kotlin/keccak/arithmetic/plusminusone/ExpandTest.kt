package keccak.arithmetic.plusminusone

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import keccak.math.arithmetic.*
import keccak.math.arithmetic.plusminusone.*

class ArithmeticNodesTest : FunSpec({
    test("1") {
        val f = 3*x0 + 4*x1*x2 + 3*(2*x1 + 3*x2*x4)
        val ff = f.expand()
        ff.shouldBe((3*x0 + 6*x1 + 4*x1*x2 + 9*x2*x4).expand())
    }

    test("2") {
        val f = (2*x0 + 3*x1)*(5*x0 + 7*x1)
        val ff = f.expand()
        ff.shouldBe((31 + 29*x0*x1).expand())
    }

    test("3") {
        val f = (2*(2*x0 + 3*x1/2))/2
        val ff = f.expand()
        ff.shouldBe((2*x0 + 3*x1/2).expand())
    }

    test("4") {
        val f = (2*x0 + 3*x1).pow(3)
        val ff = f.expand()
        ff.shouldBe((62*x0 + 63*x1).expand())
    }

    test("5") {
        val f = (1 + x0 + x2 + (-1)*(6 + x3 + (-4)*x2)/2).expand()
        f.shouldBe((x0 - 2 - x3/2 + 3*x2).expand())
    }

    test("6") {
        val vars = mapOf(
            x0 to (y0 + 1)/2,
            x1 to (y1 + 1)/2,
            x2 to (y2 + 1)/2,
            x3 to (y3 + 1)/2,
            x4 to (y4 + 1)/2,
            x5 to (y5 + 1)/2,
        )

        val ff = (x0 + 2*x1 + 4*x2)*(x3 + 2*x4 + 4*x5)

        val expanded = ff.substitute(vars).expand()

        println(expanded)
    }
})
