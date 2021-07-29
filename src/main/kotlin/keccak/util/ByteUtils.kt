package keccak.util

import keccak.*

fun Byte.bit(bitIndex: Int): Boolean {
    return ((this.toInt() shr (Byte.SIZE_BITS - bitIndex - 1)) and 1) > 0
}

fun Byte.toBitString(): String {
    return String(CharArray(Byte.SIZE_BITS) { bit(it).toNumChar() })
}

fun Byte.toBitGroup(): BitGroup {
    val byte = this
    val bits = BitGroup(Byte.SIZE_BITS)

    var i = 0
    while (i < Byte.SIZE_BITS) {
        bits[i] = byte.bit(i)
        i++
    }

    return bits
}

fun Byte.toEquationSystem(cols: Int): EquationSystem {
    val byte = this
    val system = EquationSystem(Byte.SIZE_BITS, cols)

    var bitIndex = 0
    while (bitIndex < Byte.SIZE_BITS) {
        system.results[bitIndex] = byte.bit(bitIndex)
        bitIndex++
    }

    return system
}

fun ByteArray.littleEndianBytesToLong(): Long {
    val bytes = this
    var value = 0L

    var i = 0
    while (i < bytes.size) {
        value = value or bytes[i].toUByte().toLong().shl(i * Byte.SIZE_BITS)
        i++
    }

    return value
}

fun ByteArray.toBitGroup(): BitGroup {
    val bytes = this
    val bits = BitGroup(bytes.size * Byte.SIZE_BITS)

    var byteIndex = 0
    var bitIndex = 0

    while (byteIndex < bytes.size) {
        val byte = bytes[byteIndex]
        var bitIndexInByte = 0
        while (bitIndexInByte < Byte.SIZE_BITS) {
            bits[bitIndex] = byte.bit(bitIndexInByte)
            bitIndex++
            bitIndexInByte++
        }
        byteIndex++
    }

    return bits
}

fun Byte.toNodeGroup(): NodeGroup {
    val byte = this

    val bits = Array<Node>(Byte.SIZE_BITS) { bitIndex ->
        Bit(byte.bit(bitIndex))
    }

    return NodeGroup(bits)
}

fun ByteArray.toNodeGroup(): Array<NodeGroup> {
    return Array(size) { get(it).toNodeGroup() }
}
