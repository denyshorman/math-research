package keccak.util

import io.kotest.core.spec.style.FunSpec

class MatrixUtilsTest : FunSpec({
    test("printMatrix") {
        val matrix = Array(3) { IntArray(3) { 1 } }

        println(matrix.printMatrix())
    }
})
