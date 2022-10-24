package keccak

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class CombinationIteratorByteTest : FunSpec({
    context("algorithm") {
        context("lexicographical") {
            test("1") {
                val iter = CombinationIteratorByte(
                    varsCount = 3,
                    highestValue = 2,
                    CombinationIteratorByte.Algorithm.Lexicographical,
                )

                val combinations = arrayOf(
                    "0 0 0",
                    "0 0 1",
                    "0 0 2",
                    "0 1 0",
                    "0 1 1",
                    "0 1 2",
                    "0 2 0",
                    "0 2 1",
                    "0 2 2",
                    "1 0 0",
                    "1 0 1",
                    "1 0 2",
                    "1 1 0",
                    "1 1 1",
                    "1 1 2",
                    "1 2 0",
                    "1 2 1",
                    "1 2 2",
                    "2 0 0",
                    "2 0 1",
                    "2 0 2",
                    "2 1 0",
                    "2 1 1",
                    "2 1 2",
                    "2 2 0",
                    "2 2 1",
                    "2 2 2",
                )

                combinations.forEach { combination ->
                    iter.toString().shouldBe(combination)
                    iter.next()
                }

                iter.toString().shouldBe(combinations[0])
            }
        }
    }
})