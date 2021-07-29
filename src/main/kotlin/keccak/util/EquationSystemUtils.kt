package keccak.util

import keccak.EquationSystem

fun EquationSystem.toLittleEndianBytes(): Array<EquationSystem> {
    return Array(Long.SIZE_BYTES) { byteIndex ->
        val system = EquationSystem(Byte.SIZE_BITS, cols)

        var i = Long.SIZE_BITS - (byteIndex + 1) * Byte.SIZE_BITS
        val limit = i + Byte.SIZE_BITS
        var j = 0
        while (i < limit) {
            system.equations[j] = equations[i]
            system.results[j] = results[i]
            i++
            j++
        }

        system
    }
}

fun Array<EquationSystem>.littleEndianBytesToLong(cols: Int): EquationSystem {
    val bytes = this
    val system = EquationSystem(Long.SIZE_BITS, cols)

    var bitIndex = 0
    while (bitIndex < Long.SIZE_BITS) {
        val byteIndex = Long.SIZE_BYTES - bitIndex / Byte.SIZE_BITS - 1
        val newBitIndex = bitIndex % Byte.SIZE_BITS
        system.equations[bitIndex] = bytes[byteIndex].equations[newBitIndex]
        system.results[bitIndex] = bytes[byteIndex].results[newBitIndex]
        bitIndex++
    }

    return system
}