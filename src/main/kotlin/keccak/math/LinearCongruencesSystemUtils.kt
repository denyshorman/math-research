package keccak.math

fun printLinearCongruencesSystem(
    a: Array<LongArray>,
    b: LongArray,
    modulo: Long,
) {
    var row = 0
    while (row < a.size) {
        var col = 0
        var printed = false

        while (col < a[row].size) {
            if (a[row][col] != 0L) {
                if (printed) {
                    print(" + ")
                } else {
                    printed = true
                }

                if (a[row][col] < 0) {
                    print("(${a[row][col]})")
                } else {
                    print("${a[row][col]}")
                }
                print("*r$col")
            }
            col++
        }

        if (!printed) {
            print("0")
        }

        println(" = ${b[row]} (mod $modulo)")

        row++
    }
}
