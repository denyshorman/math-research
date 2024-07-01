package keccak.math

import keccak.util.*
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.sign

private val logger = KotlinLogging.logger {}

fun solveLinearCongruencesSystem(
    a: Array<LongArray>,
    b: LongArray,
    modulo: Long,
    skipValidation: Boolean = false,
    logProgress: Boolean = false,
    progressStep: Int = 1024,
    rowsMask: BitSet? = null,
    colsMask: BitSet? = null,
): Boolean {
    val rows = a.size
    val cols = a[0].size

    fun isEmpty(i: Int): Boolean {
        var c = colsMask?.nextSetBitDefault(0, cols) ?: 0
        while (c < cols) {
            if (a[i][c] != 0L) {
                return false
            }
            c = colsMask?.nextSetBitDefault(c + 1, cols) ?: (c + 1)
        }
        return true
    }

    fun exchange(i: Int, j: Int) {
        val eq = a[i]
        a[i] = a[j]
        a[j] = eq

        val value = b[i]
        b[i] = b[j]
        b[j] = value
    }

    fun substitute(i: Int, j: Int, k: Int) {
        val iV = a[i][k].absoluteValue.toULong()
        val jV = a[j][k].absoluteValue.toULong()

        val gcdValue = gcd(iV, jV)

        val iMult = iV / gcdValue
        val jMult = jV / gcdValue

        val sign = if (a[i][k].sign * a[j][k].sign == 1) -1L else 1L

        var c = colsMask?.nextSetBitDefault(0, cols) ?: 0
        while (c < cols) {
            a[j][c] = (sign * jMult.toLong() * a[i][c] + iMult.toLong() * a[j][c]).mod(modulo)
            c = colsMask?.nextSetBitDefault(c + 1, cols) ?: (c + 1)
        }

        b[j] = (sign * jMult.toLong() * b[i] + iMult.toLong() * b[j]).mod(modulo)
    }

    fun isInvalid(i: Int): Boolean {
        return b[i] != 0L && isEmpty(i)
    }

    fun setColIndex(i: Int): Int {
        var c = colsMask?.nextSetBitDefault(0, cols) ?: 0
        while (c < cols) {
            if (a[i][c] != 0L) {
                return c
            }
            c = colsMask?.nextSetBitDefault(c + 1, cols) ?: (c + 1)
        }
        throw IllegalStateException("Can't find a non-zero column")
    }

    if (logProgress) {
        if (!isPow2(progressStep)) {
            throw IllegalArgumentException("progressStep must be a power of 2")
        }

        logger.info("Starting forward processing")
    }

    var row = rowsMask?.nextSetBitDefault(0, rows) ?: 0
    var col = colsMask?.nextSetBitDefault(0, cols) ?: 0

    while (row < rows && col < cols) {
        var i = row
        var found = false

        while (i < rows) {
            if (isEmpty(i)) {
                if (!skipValidation && b[i] != 0L) {
                    return false
                }
            } else {
                if (a[i][col] != 0L) {
                    found = true
                    break
                }
            }

            i = rowsMask?.nextSetBitDefault(i + 1, rows) ?: (i + 1)
        }

        if (found) {
            if (row != i) {
                exchange(row, i)
            }

            i = rowsMask?.nextSetBitDefault(row + 1, rows) ?: (row + 1)

            while (i < rows) {
                if (a[i][col] != 0L) {
                    substitute(row, i, col)

                    if (!skipValidation && isInvalid(i)) {
                        return false
                    }
                }

                i = rowsMask?.nextSetBitDefault(i + 1, rows) ?: (i + 1)
            }

            row = rowsMask?.nextSetBitDefault(row + 1, rows) ?: (row + 1)

            if (logProgress && modPow2(row, progressStep) == 0) {
                logger.info("Processed $row rows")
            }
        }

        col = colsMask?.nextSetBitDefault(col + 1, cols) ?: (col + 1)
    }

    if (logProgress) {
        logger.info("Forward processing has been completed. Starting backward processing")
    }

    row = rowsMask?.previousSetBit(rows - 1) ?: (rows - 1)

    while (row >= 0) {
        if (isEmpty(row)) {
            if (!skipValidation && b[row] != 0L) {
                return false
            }
        } else {
            var i = rowsMask?.previousSetBit(row - 1) ?: (row - 1)

            if (i >= 0) {
                col = setColIndex(row)

                if (col >= 0) {
                    while (i >= 0) {
                        if (a[i][col] != 0L) {
                            substitute(row, i, col)

                            if (!skipValidation && isInvalid(i)) {
                                return false
                            }
                        }

                        i = rowsMask?.previousSetBit(i - 1) ?: (i - 1)
                    }
                }
            }
        }

        row = rowsMask?.previousSetBit(row - 1) ?: (row - 1)

        if (logProgress && modPow2(row, progressStep) == 0) {
            logger.info("Processed ${rows - row} rows")
        }
    }

    if (logProgress) {
        logger.info("Backward processing has been completed.")
    }

    return true
}
