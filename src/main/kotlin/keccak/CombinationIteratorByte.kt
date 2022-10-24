package keccak

class CombinationIteratorByte(
    val varsCount: Int,
    val highestValue: Byte,
    val algorithm: Algorithm = Algorithm.Lexicographical,
) {
    val combination = ByteArray(varsCount) { 0 }

    val next = when (algorithm) {
        Algorithm.Lexicographical -> ::lexicographicalNext
    }

    fun hasNext(): Boolean {
        var i = 0

        while (i < varsCount) {
            if (combination[i] != highestValue) {
                return true
            }

            i++
        }

        return false
    }

    fun reset() {
        combination.fill(0)
    }

    inline fun iterateAll(callback: () -> Unit) {
        while (true) {
            callback()
            if (hasNext()) next() else break
        }
        reset()
    }

    inline fun iterate(callback: () -> Boolean) {
        while (callback() && hasNext()) {
            next()
        }
        reset()
    }

    override fun toString(): String {
        return combination.joinToString(" ")
    }

    private fun lexicographicalNext() {
        var i = varsCount - 1

        while (i >= 0) {
            if (combination[i] == highestValue) {
                combination[i] = 0
                i--
            } else {
                combination[i]++
                break
            }
        }
    }

    enum class Algorithm {
        Lexicographical,
    }
}
