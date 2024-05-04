package keccak.arithmetic.zeroone

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import keccak.*
import keccak.math.arithmetic.*
import keccak.math.arithmetic.zeroone.*
import keccak.util.*
import keccak.util.x0
import keccak.util.x1
import keccak.util.x2
import keccak.util.x3
import keccak.util.x4
import java.math.BigInteger
import java.util.*
import kotlin.random.Random

class UtilFunctionsTest : FunSpec({
    context("toSum") {
        test("1") {
            val xor = x0 + x1 + x2
            val sum = xor.toSum()
            println(sum.toString())
        }
    }

    context("evaluate") {
        test("1") {
            val funcs = arrayOf(
                x0 + x1 + x2 + x3,
                x0 + x1 + x2 + x0 * x1,
                x0 * x1 + x1 * x2 + t,
                x0 + x0 * x1 + x0 * x1 * x2 + t,
            )

            funcs.forEach { xor ->
                val varsCount = xor.countVariables()
                val sum = xor.toSum()

                val iter = CombinationIteratorSimple(varsCount)

                iter.iterateAll {
                    val xorRes = xor.evaluate(iter.combination).toInt()
                    val sumRes = sum.evaluate(iter.combination)
                    sumRes.shouldBe(xorRes)
                }
            }
        }
    }

    context("collectTerms") {
        test("1") {
            val funcs = arrayOf(
                x0 + x1 + x2 + x3,
                x0 + x1 + x2 + x0 * x1,
                x0 * x1 + x1 * x2 + t,
                x0 + x0 * x1 + x0 * x1 * x2 + t,
                x0 + x1 + x2 + (x0 + x1) * x2,
            )

            funcs.forEach { xor ->
                val varsCount = xor.countVariables()
                val sum = xor.toSum()

                val iter = CombinationIteratorSimple(varsCount)

                iter.iterateAll {
                    val xorRes = xor.evaluate(iter.combination).toInt()
                    val sumRes = sum.evaluate(iter.combination)
                    val optimizedSumRes = sum.collectTerms().evaluate(iter.combination)
                    sumRes.shouldBe(xorRes)
                    optimizedSumRes.shouldBe(xorRes)
                }
            }
        }

        test("2") {
            val xor = x0 + x1 + (x0 + x1) * x1
            val sum = xor.toSum()
            val sum2 = sum.collectTerms()

            println(sum)
            println(sum2)

            val iter = CombinationIteratorSimple(xor.countVariables())

            iter.iterateAll {
                val xorRes = xor.evaluate(iter.combination).toInt()
                val sumRes = sum.evaluate(iter.combination)
                val sum2Res = sum2.evaluate(iter.combination)
                sumRes.shouldBe(xorRes)
                sum2Res.shouldBe(xorRes)
            }
        }

        test("3") {
            val sum = BooleanVariable("x0", BooleanVariable.Type.ZERO_ONE) + BooleanVariable("x1", BooleanVariable.Type.ZERO_ONE) +
                    IntNumber(2) * (-BooleanVariable("x0", BooleanVariable.Type.ZERO_ONE)) - BooleanVariable("x1", BooleanVariable.Type.ZERO_ONE) +
                    IntNumber(-1) * (-BooleanVariable("x0", BooleanVariable.Type.ZERO_ONE)) - BooleanVariable("x1", BooleanVariable.Type.ZERO_ONE)

            val sum2 = sum.collectTerms()

            println(sum)
            println(sum2)

            sum2.shouldBe(IntNumber(0))
        }

        test("4") {
            val sum = IntNumber(2) * BooleanVariable("x0", BooleanVariable.Type.ZERO_ONE) * BooleanVariable("x0", BooleanVariable.Type.ZERO_ONE)
            val sum2 = sum.collectTerms()
            val sum3 = IntNumber(2) * BooleanVariable("x0", BooleanVariable.Type.ZERO_ONE)

            sum2.shouldBe(sum3)
        }
    }

    context("expand") {
        test("1") {
            val funcs = arrayOf<Node>(
                (x0 + x1) * (x2 + x3) * (x4 + t),
                (x0 + x1) * ((x2 + x3 + t) * x1 + t),
                (x0 + t) * ((x2 + x3 * (x0 + x1) + t) * x1 + t),
                (x0 + t) * ((x2 + x3 * (x0 + x1) + t) * x1 + t) * (x0 + x1 + x2 + t),
            )

            funcs.forEach { xor ->
                val varsCount = xor.countVariables()
                val sum = xor.toArithmeticNode()
                val sum2 = sum.collectTerms()
                val sum3 = sum.expand()

                println(xor)
                println(sum)
                println(sum2)
                println(sum3)
                println()

                val iter = CombinationIteratorSimple(varsCount)

                iter.iterateAll {
                    val xorRes = xor.evaluate(iter.combination).toInt()
                    val sumRes = sum.evaluate(iter.combination)
                    val sum2Res = sum2.evaluate(iter.combination)
                    val sum3Res = sum3.evaluate(iter.combination)
                    sumRes.shouldBe(xorRes)
                    sum2Res.shouldBe(xorRes)
                    sum3Res.shouldBe(xorRes)
                }
            }
        }

        test("2") {
            val (_, andSystem) = randomAndEquationSystem(
                rows = 5,
                cols = 5,
                allowIncompatibleSystem = true,
                equalToZero = false,
                solutionsCount = 0,
                random = Random(1),
            )

            println(andSystem.toHumanString())
            println()

            val solutions = AndEquationSystem.BruteForceAlgorithm(andSystem).solve().toSet()

            solutions.forEach { solution ->
                println("Solution: ${solution.toString(andSystem.cols)}")
                println()
            }

            val sumXor = andSystem.toSumXor()
            val sumNodes = LinkedList<ArithmeticNode>()

            var i = 0
            while (i < sumXor.rows) {
                val sumNode = Sum(
                    (0..3).asSequence()
                        .map { sumXor.equations[i + it].toNode(sumXor.results[i + it]).toArithmeticNode() }
                )

                sumNodes.add(sumNode)

                (0..3).asSequence().forEach {
                    val eq = sumXor.equations[i + it].toXorString(sumXor.results[i + it])
                    println("($eq)")
                }
                println()

                i += 4
            }

            val multiplyNode = Multiply(sumNodes) + IntNumber(-1)
            val multiplyNodeCompact = multiplyNode.collectTerms()
            val multiplyNodeExpanded = multiplyNodeCompact.expand()
            val multiplyNodeExpandedReduced = multiplyNodeExpanded.reduceBy(IntNumber(2))

            val sumNode = Sum(sumNodes) + IntNumber(-andSystem.rows)
            val sumNodeCompact = sumNode.collectTerms()
            val sumNodeCompactReduced = sumNodeCompact.reduceBy(IntNumber(2)).collectTerms()

            println("multiplyNode:")
            println(multiplyNode)
            println()
            println("multiplyNodeCompact")
            println(multiplyNodeCompact)
            println()
            println("multiplyNodeExpanded")
            println(multiplyNodeExpanded)
            println()
            println("sumNode")
            println(sumNode)
            println()
            println("sumNodeCompact")
            println(sumNodeCompact)
            println()
            println("sumNodeCompactReduced")
            println(sumNodeCompactReduced)
            println()
            println("multiplyNodeExpandedReduced")
            println(multiplyNodeExpandedReduced)
            println()

            solutions.forEach { solution ->
                val sum = multiplyNodeExpanded.evaluate(solution.toBooleanArray(andSystem.cols))
                println("Solution: ${solution.toString(andSystem.cols)} => $sum")
            }
            println()

            val iter = CombinationIteratorSimple(andSystem.cols)

            val actualSolutionsMultiply = HashSet<BitSet>()
            val actualSolutionsSum = HashSet<BitSet>()

            iter.iterateAll {
                val resMul = multiplyNodeExpanded.evaluate(iter.combination)
                val resSum = sumNodeCompactReduced.evaluate(iter.combination)

                if (resMul == BigInteger.ZERO) {
                    actualSolutionsMultiply.add(iter.combination.toBitSet())
                }

                if (resSum == BigInteger.ZERO) {
                    actualSolutionsSum.add(iter.combination.toBitSet())
                }

                println("${iter.combination.toBitSet().toString(andSystem.cols)} | $resMul | $resSum")
            }

            solutions.shouldBe(actualSolutionsMultiply)
            solutions.shouldBe(actualSolutionsSum)
        }
    }
})
