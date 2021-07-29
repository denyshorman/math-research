package keccak

import io.kotest.core.spec.style.FunSpec
import kotlin.test.assertEquals

class FixedBitSetTest : FunSpec({
    context("flip") {
        test("1") {
            val actual = FixedBitSet(4)
            actual[0] = true
            actual[1] = true
            actual[2] = true
            actual[3] = true

            actual.invert()

            val expected = FixedBitSet(4)
            expected[0] = false
            expected[1] = false
            expected[2] = false
            expected[3] = false

            assertEquals(expected, actual)
        }

        test("2") {
            val actual = FixedBitSet(4)
            actual[0] = true
            actual[1] = false
            actual[2] = true
            actual[3] = false

            actual.invert()

            val expected = FixedBitSet(4)
            expected[0] = false
            expected[1] = true
            expected[2] = false
            expected[3] = true

            assertEquals(expected, actual)
        }
    }

    context("extensions") {
        context("Byte.toFixedBitSet") {
            test("1") {
                val byte = 0xFA.toByte()
                val bitSet = byte.toFixedBitSet()

                assertEquals(8, bitSet.size)
                assertEquals("11111010", bitSet.toString())
            }
        }

        context("ByteArray.toFixedBitSet()") {
            test("1") {
                val bytes = byteArrayOf(0x1A.toByte(), 0xF1.toByte())
                val bitSet = bytes.toFixedBitSet()

                assertEquals(16, bitSet.size)
                assertEquals("0001101011110001", bitSet.toString())
            }
        }
    }

    test("toString()") {
        val bitSet = FixedBitSet(8)
        bitSet[0] = true
        bitSet[6] = true
        assertEquals("10000010", bitSet.toString())
    }
})
