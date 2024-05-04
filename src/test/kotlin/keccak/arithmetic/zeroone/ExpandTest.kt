package keccak.arithmetic.zeroone

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import keccak.math.arithmetic.*
import keccak.math.arithmetic.zeroone.*

class ArithmeticNodesTest : FunSpec({
    test("1") {
        var ff: ArithmeticNode = Sum(
            IntNumber("12"),
            Multiply(IntNumber("-3"), x9),
            Multiply(IntNumber("-2"), x8),
            Multiply(IntNumber("6"), x5),
            Multiply(IntNumber("6"), x7),
            Multiply(
                IntNumber("3"),
                Multiply(
                    IntNumber("-1"),
                    InverseNumber("654364627785"),
                    Sum(
                        IntNumber("1749419910810"),
                        Multiply(IntNumber("-3095351690670"), x6),
                        Multiply(IntNumber("-3881958040622"), x8),
                        Multiply(IntNumber("-4305768686106"), x9),
                        Multiply(IntNumber("-3058149166380"), x7),
                        Multiply(IntNumber("868038600330"), x5),
                        Multiply(IntNumber("-1308729255570"), x3, x6),
                        Multiply(IntNumber("-1308729255570"), x3, x7),
                        Multiply(IntNumber("-1308729255570"), x4, x9),
                        Multiply(IntNumber("-3472154401320"), x6, x8),
                        Multiply(IntNumber("-3119211504810"), x5, x6),
                        Multiply(IntNumber("-2176767855900"), x5, x7),
                        Multiply(IntNumber("8076119153858"), x8, x9),
                        Multiply(IntNumber("6238423009620"), x6, x9),
                        Multiply(IntNumber("-1526850798165"), x3, x8),
                        Multiply(IntNumber("-3044806456230"), x6, x7),
                        Multiply(IntNumber("-3262927998825"), x5, x8),
                        Multiply(IntNumber("6437396545474"), x7, x9),
                        Multiply(IntNumber("-4038059576929"), x7, x8),
                        Multiply(IntNumber("654364627785"), x4, x5),
                        Multiply(IntNumber("654364627785"), x4, x7),
                        Multiply(IntNumber("3119211504810"), x5, x9),
                        Multiply(IntNumber("1308729255570"), x3, x9)
                    )
                )
            ),
            Multiply(IntNumber("-5"), x8, x9),
            Multiply(IntNumber("-3"), x3, x9),
            Multiply(IntNumber("-3"), x4, x9),
            Multiply(IntNumber("-3"), x6, x9),
            Multiply(IntNumber("2"), x4, x8),
            Multiply(IntNumber("2"), x6, x8),
            Multiply(IntNumber("3"), x3, x6),
            Multiply(IntNumber("3"), x3, x8)
        )

        ff = ff.expand()

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

        val r0 = ff.expand().substitute(varsMap).expand()
        val r1 = ff.substitute(varsMap).expand()

        r0.shouldBe(IntNumber(0))
        r0.shouldBe(r1)
    }
})
