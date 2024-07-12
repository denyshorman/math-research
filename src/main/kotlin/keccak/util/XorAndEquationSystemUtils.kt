package keccak.util

import keccak.Bit
import keccak.Node
import keccak.NodeEquationSystem
import keccak.XorAndEquationSystem
import java.io.File

fun XorAndEquationSystem.toFile(
    xorSystemFile: File,
    andSystemFile: File,
    xorHumanReadable: Boolean,
    andHumanReadable: Boolean,
) {
    xorSystem.toFile(xorSystemFile, humanReadable = xorHumanReadable)
    andSystem.toFile(andSystemFile, humanReadable = andHumanReadable)
}

fun XorAndEquationSystem.toNodeEquationSystem(varPrefix: String = "x", varOffset: Int = 0): NodeEquationSystem {
    val xorNodeSystem = xorSystem.toNodeEquationSystem(varPrefix, varOffset)
    val andNodeSystem = andSystem.toNodeEquationSystem(varPrefix, varOffset)

    val equations = Array<Node>(xorNodeSystem.rows + andNodeSystem.rows) { Bit() }

    var i = 0
    while (i < xorNodeSystem.rows) {
        equations[i] = xorNodeSystem.equations[i]
        i++
    }

    var j = 0
    while (j < andNodeSystem.rows) {
        equations[i + j] = andNodeSystem.equations[j]
        j++
    }

    return NodeEquationSystem(equations)
}
