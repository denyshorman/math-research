package keccak

object BitReplacementSubsystem {
    sealed interface ReplaceRule
    data class ByteRule(val index: Int) : ReplaceRule
    data class ByteRangeRule(val fromIndex: Int, val toIndex: Int) : ReplaceRule
    data class BitRule(val byteIndex: Int, val bitIndex: Int) : ReplaceRule

    fun getBlocks(
        message: ByteArray,
        replaceRules: List<ReplaceRule> = emptyList(),
        replaceRulesInverse: Boolean = false,
        replacePadding: Boolean = false,
        blockSizeBytes: Int,
    ): List<Array<BitGroup>> {
        val padding = buildPadding(message.size, blockSizeBytes)

        val messageMask = buildMessageMask(message.size, replaceRules, replaceRulesInverse)
        val paddingMask = buildPaddingMask(padding.size, replacePadding)

        val paddedMessage = byteArrayOf(*message, *padding)
        val paddedMessageMask = arrayOf(*messageMask, *paddingMask)

        val preparedMessage = paddedMessage.setVariables(paddedMessageMask)

        return preparedMessage.blocks(blockSizeBytes)
    }

    private fun buildMessageMask(
        messageSize: Int,
        replaceRules: List<ReplaceRule>,
        replaceRulesInverse: Boolean,
    ): Array<BooleanArray>{
        val mask = Array(messageSize) { BooleanArray(Byte.SIZE_BITS) { false } }

        replaceRules.forEach { rule ->
            when (rule) {
                is ByteRule -> {
                    val byte = mask[rule.index]
                    var i = 0
                    while (i < byte.size) {
                        byte[i++] = true
                    }
                }
                is ByteRangeRule -> {
                    var i = rule.fromIndex
                    while (i <= rule.toIndex) {
                        val byte = mask[i++]
                        var j = 0
                        while (j < byte.size) {
                            byte[j++] = true
                        }
                    }
                }
                is BitRule -> {
                    mask[rule.byteIndex][rule.bitIndex] = true
                }
            }
        }

        if (replaceRulesInverse) {
            var i = 0
            while (i < mask.size) {
                val byte = mask[i++]
                var j = 0
                while (j < byte.size) {
                    byte[j] = byte[j].not()
                    j++
                }
            }
        }

        return mask
    }

    private fun buildPaddingMask(
        paddingSize: Int,
        replacePadding: Boolean,
    ): Array<BooleanArray>{
        return Array(paddingSize) { BooleanArray(Byte.SIZE_BITS) { replacePadding } }
    }

    private fun buildPadding(messageSize: Int, blockSizeBytes: Int): ByteArray {
        val paddingSize = blockSizeBytes - messageSize % blockSizeBytes
        val padding = ByteArray(paddingSize) {0}

        if (paddingSize == 1) {
            padding[0] = 0x81.toByte()
        } else if (paddingSize >= 2) {
            padding[0] = 0x01.toByte()

            var i = 1
            val j = paddingSize - 2

            while (i < j) {
                padding[i++] = 0x00
            }

            padding[paddingSize - 1] = 0x80.toByte()
        }

        return padding
    }

    private fun ByteArray.setVariables(mask: Array<BooleanArray>): Array<BitGroup> {
        val bytes = this
        var globalBitIndex = 0

        return Array(bytes.size) { byteIndex ->
            val bitGroup = bytes[byteIndex].toBitGroup()
            var bitIndex = 0

            while (bitIndex < bitGroup.bits.size) {
                if (mask[byteIndex][bitIndex]) {
                    val variable = Variable("a$globalBitIndex")
                    bitGroup.bits[bitIndex] = variable
                }

                bitIndex++
                globalBitIndex++
            }

            bitGroup
        }
    }

    private fun Array<BitGroup>.blocks(blockSizeBytes: Int): List<Array<BitGroup>> {
        return asSequence()
            .chunked(blockSizeBytes)
            .map { block ->
                block.asSequence()
                    .chunked(Long.SIZE_BYTES)
                    .map { it.toTypedArray().littleEndianBytesToLong() }
                    .toList()
                    .toTypedArray()
            }
            .toList()
    }
}