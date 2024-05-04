package keccak.experiments

import io.kotest.core.spec.style.FunSpec
import keccak.math.arithmetic.*
import keccak.math.arithmetic.zeroone.*
import kotlin.test.fail

class ZeroOneVarTest : FunSpec({
    test("expand") {
        val ff = (-6*(36960 - 14560*x - 2368*x*x + 1792*x*x*x - 224*x*x*x*x) + 4*x*(36960 - 14560*x - 2368*x*x + 1792*x*x*x - 224*x*x*x*x) - 16*(72*x - 36*x*x - 104*x*x*x + 84*x*x*x*x - 16*x*x*x*x*x))

        println(ff.expand())
    }

    test("validate an equation") {
        val system = arrayOf<ArithmeticNode>(
            x0*(-221760 + 234048*x - 43456*x*x - 18560*x*x*x + 7168*x*x*x*x - 640*x*x*x*x*x) - (-144384*x + 129280*x*x - 24064*x*x*x - 6208*x*x*x*x + 2368*x*x*x*x*x - 192*x*x*x*x*x*x)
        )

        var i = 0
        while (i < system.size) {
            system[i] = system[i].expand()
            println(system[i])
            i++
        }

        val values = arrayOf(
            mapOf(x to IntNumber(0), x0 to IntNumber(0), x1 to IntNumber(0), x2 to IntNumber(0)),
            mapOf(x to IntNumber(1), x0 to IntNumber(1), x1 to IntNumber(0), x2 to IntNumber(0)),
            mapOf(x to IntNumber(2), x0 to IntNumber(0), x1 to IntNumber(1), x2 to IntNumber(0)),
            mapOf(x to IntNumber(3), x0 to IntNumber(1), x1 to IntNumber(1), x2 to IntNumber(0)),
            mapOf(x to IntNumber(4), x0 to IntNumber(0), x1 to IntNumber(0), x2 to IntNumber(1)),
            mapOf(x to IntNumber(5), x0 to IntNumber(1), x1 to IntNumber(0), x2 to IntNumber(1)),
            mapOf(x to IntNumber(6), x0 to IntNumber(0), x1 to IntNumber(1), x2 to IntNumber(1)),
            mapOf(x to IntNumber(7), x0 to IntNumber(1), x1 to IntNumber(1), x2 to IntNumber(1)),
        )

        values.forEach { vars ->
            system.forEach {eq ->
                val expanded = eq.substitute(vars).expand()

                if (expanded != IntNumber(0)) {
                    println("(${vars[x]} ${vars[x0]} ${vars[x1]} ${vars[x2]}) $expanded != 0")
                    fail()
                }
            }
        }
    }
})
