package keccak

import keccak.util.combinations

class StarsAndBarsCombIterator(val items: Int, val boxes: Int) {
    val combination = IntArray(boxes)
    val count = calcCombinationsCount()
    var index = 0L
        private set

    init {
        combination[0] = items
        for (i in 1 until boxes) {
            combination[i] = 0
        }
    }

    fun hasNext(): Boolean {
        return index < count
    }

    fun next() {
        var i = boxes - 2

        while (i >= 0 && combination[i] == 0) {
            i--
        }

        if (i >= 0) {
            combination[i]--
            combination[i + 1]++

            if (i + 1 != boxes - 1) {
                combination[i + 1] += combination[boxes - 1]
                combination[boxes - 1] = 0
            }
        }

        index++
    }

    inline fun iterateAll(callback: (IntArray) -> Unit) {
        while (hasNext()) {
            callback(combination)
            next()
        }
    }

    private fun calcCombinationsCount(): Long {
        return combinations((items + boxes - 1).toLong(), (boxes - 1).toLong())
    }
}
