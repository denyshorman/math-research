package keccak.util

import kotlin.math.absoluteValue

fun pow(a: Int, b: Int): Long {
    var count = 1L
    var i = 0
    while (i < b) {
        count *= a
        i++
    }
    return count
}

fun pow2(n: Int): Long {
    return 1L shl n
}

fun pow2(n: Long): Long {
    return 1L shl n.toInt()
}

fun isPow2(n: Int): Boolean = ((n - 1) and n) == 0

fun isMod2(n: Int): Boolean = (n and 1) == 0

fun modPow2(n: Int, p2: Int): Int = n and (p2 - 1)

fun modPow2(n: Long, p2: Long): Long = n and (p2 - 1)

fun modFast(n: Int, b: Int): Int = if (isPow2(b)) modPow2(n, b) else n % b

fun gcd(a: UInt, b: UInt): UInt {
    var x = a
    var y = b

    if (x == 1u || y == 1u) return 1u
    if (x == 0u) return y
    if (y == 0u) return x

    var i = 0
    while ((x or y) and 1u == 0u) {
        x = x shr 1
        y = y shr 1
        i++
    }

    while (x and 1u == 0u) {
        x = x shr 1
    }

    do {
        while (y and 1u == 0u) {
            y = y shr 1
        }

        if (x > y) {
            val tmp = x
            x = y
            y = tmp
        }

        y -= x
    } while (y != 0u)

    return x shl i
}

fun gcd(a: Int, b: Int): UInt {
    return gcd(
        a.absoluteValue.toUInt(),
        b.absoluteValue.toUInt(),
    )
}

fun gcd(values: IntArray, length: Int = values.size): UInt {
    var i = 0
    while (i < length) {
        if (values[i] == 1 || values[i] == -1) return 1u
        i++
    }

    var gcdValue = values[0].absoluteValue.toUInt()

    i = 1
    while (i < length) {
        gcdValue = gcd(gcdValue, values[i].absoluteValue.toUInt())
        if (gcdValue == 1u) return gcdValue
        i++
    }

    return gcdValue
}

/**
 * @return an array `[gcd, x0, y0]` that satisfies the equation `a*x0 + b*y0 = gcd(a,b)`
 */
fun gcdExtendedEuclid(
    a: Int,
    b: Int,
    solution: IntArray = IntArray(3) { 0 },
): IntArray {
    var a0 = a
    var b0 = b

    var x0 = 1
    var x1 = 0
    var y0 = 0
    var y1 = 1

    while (b0 != 0) {
        val q = a0 / b0
        val r = a0 % b0

        val x = x0 - q * x1
        val y = y0 - q * y1

        x0 = x1
        x1 = x

        y0 = y1
        y1 = y

        a0 = b0
        b0 = r
    }

    val sign = if (a0 < 0) -1 else 1

    solution[0] = a0 * sign
    solution[1] = x0 * sign
    solution[2] = y0 * sign

    return solution
}

/**
 * Having a set of numbers `(a,b,c)` that can be represented as equation `a*x + b*y + c*z = gcd(a,b,c)`,
 * find a solution for this equation and `gcd(a,b,c)`.
 *
 * To find a solution and gcd, the equation is rewritten as
 * ```
 * a = 1*a + 0*b + 0*c
 * b = 0*a + 1*b + 0*c
 * c = 0*a + 0*b + 1*c
 * ```
 * and simple row transformations are used until the system reaches the form
 * ```
 * d = x0*a + y0*b + z0*c
 * 0 = x1*a + y1*b + z1*c
 * 0 = x2*a + y2*b + z2*c
 * ```
 */
fun gcdBlankinship(
    numbers: IntArray,
): Array<IntArray> {
    val solution = Array(numbers.size) { i ->
        IntArray(numbers.size + 1) { j ->
            if (j == 0) {
                numbers[i]
            } else if (j - 1 == i) {
                1
            } else {
                0
            }
        }
    }

    while (true) {
        var rowIndex0 = -1
        var rowIndex1 = -1
        var i = 0

        while (i < solution.size) {
            if (solution[i][0] != 0) {
                if (rowIndex0 == -1) {
                    rowIndex0 = i
                } else {
                    rowIndex1 = i
                    break
                }
            }

            i++
        }

        if (rowIndex1 == -1) {
            // GCD found
            if (solution[rowIndex0][0] < 0) {
                // Fix GCD sign
                i = 0
                while (i < solution[rowIndex0].size) {
                    solution[rowIndex0][i] = -solution[rowIndex0][i]
                    i++
                }
            }

            break
        }

        if (solution[rowIndex0][0].absoluteValue < solution[rowIndex1][0].absoluteValue) {
            // swap to have high value at the top
            rowIndex0 = rowIndex1.apply { rowIndex1 = rowIndex0 }
        }

        // a = b*k + r
        val k = -solution[rowIndex0][0] / solution[rowIndex1][0]

        // modify top row: r = a + b*(-k)
        i = 0
        while (i < solution[rowIndex0].size) {
            solution[rowIndex0][i] += solution[rowIndex1][i]*k
            i++
        }
    }

    return solution
}