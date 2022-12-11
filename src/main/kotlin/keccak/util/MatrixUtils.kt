package keccak.util

fun Array<IntArray>.printMatrix(): String {
    val sb = StringBuilder()
    var i = 0
    while (i < size) {
        var j = 0
        while (j < this[i].size) {
            val leftSpace = if (j == 0) "" else " "
            val rightSpace = if (j == this[i].size - 1) "" else " "
            sb.append(String.format("$leftSpace%2d$rightSpace", this[i][j]))
            j++
        }
        sb.append('\n')
        i++
    }
    return sb.toString()
}

