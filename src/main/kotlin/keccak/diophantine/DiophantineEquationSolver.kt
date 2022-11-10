package keccak.diophantine

import keccak.util.gcd
import keccak.util.gcdExtendedEuclid
import keccak.util.modFast

/**
 * ```
 * Given equation
 * a*x + b*y = c*gcd(a,b) = d
 *
 * Find a solution
 * x = c*x0 + b*k
 * y = c*y0 - a*k
 * ```
 */
fun solveSimplestDiophantineEquation(
    equation: DiophantineEquation,
    solutionMatrix: Array<IntArray>? = null,
): Array<IntArray>? {
    val (a, b, d) = equation.coefficients
    val (gcd, x0, y0) = gcdExtendedEuclid(a, b)

    if (modFast(d, gcd) != 0) {
        return null
    }

    val c = d / gcd

    val solution = solutionMatrix ?: Array(2) { IntArray(2) { 0 } }

    solution[0][0] = c * x0
    solution[0][1] = b
    solution[1][0] = c * y0
    solution[1][1] = -a

    return solution
}

fun solveGeneralDiophantineEquation(
    equation: DiophantineEquation,
): Array<IntArray> {
    val variablesCount = equation.coefficients.size - 1

    val gcds = IntArray(variablesCount) { 0 }
    gcds[0] = equation.coefficients[0]
    var i = 1
    while (i < gcds.size) {
        gcds[i] = gcd(gcds[i - 1], equation.coefficients[i]).toInt()
        i++
    }

    // a0*x0 + a1*x1 + a2*x2 + a3*x3 = a4*gcd(a0,a1,a2,a3)

    // a0*x0 + a1*x1 = y1*gcd(a0,a1)
    // gcd(a0,a1)*y1 + a2*x2 = y0*gcd(a0,a1,a2)
    // gcd(a0,a1,a2)*y0 + a3*x3 = a4*gcd(a0,a1,a2,a3)

    // x0 = y1*x0' + a1*k0
    // x1 = y1*x1' - gcd(a0)*k0
    // y1 = y0*y1' + a2*k1
    // x2 = y0*x2' - gcd(a0,a1)*k1
    // y0 = a4*y0' + a3*k2
    // x3 = a4*x3' - gcd(a0,a1,a2)*k2

    // x0 = a4*x0'*y0'*y1' + a1*k0 + a2*x0'*k1 + a3*x0'*y1'*k2
    // x1 = a4*x1'*y0'*y1' - gcd(a0)*k0 + a2*x1'*k1 + a3*x1'*y1'*k2
    // x2 = a4*y0'*x2' - gcd(a0,a1)*k1 + a3*x2'*k2
    // x3 = a4*x3' - gcd(a0,a1,a2)*k2

    val solution = Array(variablesCount) { IntArray(variablesCount) { 0 } }
    solution[0][0] = equation.coefficients[variablesCount] / gcds[gcds.size - 1]

    i = variablesCount - 1
    while (i > 0) {
        val (_, x0, x1) = gcdExtendedEuclid(gcds[i - 1], equation.coefficients[i])

        solution[0].copyInto(solution[i])

        (0 until variablesCount).forEach { j ->
            solution[0][j] *= x0
            solution[i][j] *= x1
        }

        solution[0][i] = equation.coefficients[i]
        solution[i][i] = -gcds[i - 1]

        i--
    }

    return solution
}