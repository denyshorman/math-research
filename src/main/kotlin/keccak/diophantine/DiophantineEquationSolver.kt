package keccak.diophantine

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
