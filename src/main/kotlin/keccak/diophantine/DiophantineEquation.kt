package keccak.diophantine

import keccak.util.gcd
import keccak.util.modFast

class DiophantineEquation(
    vararg val coefficients: Int,
) {
    fun hasSolution(): Boolean {
        val rhsValueIndex = coefficients.size - 1
        val rhsValue = coefficients[rhsValueIndex]
        val gcd = gcd(coefficients, rhsValueIndex).toInt()
        return modFast(rhsValue, gcd) == 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as DiophantineEquation
        return coefficients.contentEquals(other.coefficients)
    }

    override fun hashCode(): Int {
        return coefficients.contentHashCode()
    }

    override fun toString(): String {
        val sb = StringBuilder()
        val resultIndex = coefficients.size - 1
        var i = 0
        while (i < resultIndex) {
            if (coefficients[i] < 0) {
                sb.append('(')
                sb.append(coefficients[i])
                sb.append(')')
            } else {
                sb.append(coefficients[i])
            }
            sb.append("*x")
            sb.append(i)
            if (i != resultIndex - 1) sb.append(" + ")
            i++
        }
        sb.append(" = ")
        sb.append(coefficients[resultIndex])
        return sb.toString()
    }
}