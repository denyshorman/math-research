package keccak.util

import io.kotest.core.spec.style.FunSpec
import keccak.Keccak256EqSystemGenerator
import keccak.Variable
import java.io.File
import java.io.FileOutputStream
import java.util.*

class KeccakSystemUtilsKtTest : FunSpec({
    test("sortBySolvableGroups") {
        val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100)

        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)

        val andSystem = hashResult.equationSystem.sortBySolvableGroups()

        val stream = FileOutputStream("test.txt")

        var i = 0
        while (i < andSystem.rows) {
            /*if (andSystem.isEmpty(i)) {
                i++
                continue
            }*/

            //val th = if (i >= (andSystem.rows - 1600)) 4 else 5
            //if (i % 5 == 0) println()
            if (i % 5 == 0) stream.write("\n".toByteArray(Charsets.US_ASCII))
            val l = andSystem.equations[i].andOpLeft.toXorString(andSystem.andOpLeftResults[i])
            val r = andSystem.equations[i].andOpRight.toXorString(andSystem.andOpRightResults[i])
            val x = andSystem.equations[i].rightXor.toXorString(andSystem.rightXorResults[i])
            stream.write("($l)*($r) = $x\n".toByteArray(Charsets.US_ASCII))
            //println("($l)*($r) = $x")
            i++
        }

        stream.close()
    }

    test("buildGroupOffsets") {
        val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100)

        val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)
        val offsets = hashResult.equationSystem.buildGroupOffsets()
        println(offsets)
    }

    context("invertGroup") {
        test("1") {
            val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100)

            val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)
            val andSystem = hashResult.equationSystem.sortBySolvableGroups()

            val andNodeSystem = andSystem.invertGroup(groupIndex = 1)

            val vars = (1088 until 1088 + 1600).map { Variable("x$it") }

            andNodeSystem.solve(
                logProgress = true,
                progressStep = 32,
                priorityNodes = vars
            )

            andNodeSystem.expand()

            val stream = FileOutputStream("test.txt")

            var i = 0
            while (i < andNodeSystem.rows) {
                stream.write((andNodeSystem.equations[i].toString() + "\n").toByteArray(Charsets.US_ASCII))
                i++
            }

            stream.close()

            //println(andNodeSystem)
        }

        test("2") {
            val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100)

            val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)
            val andSystem = hashResult.equationSystem.sortBySolvableGroups()

            val andNodeSystem = andSystem.invertGroup(groupIndex = 23)

            andNodeSystem.expand()

            val vars = ((1088 + 1600*22) until (1088 + 1600*23)).map { Variable("x$it") }

            andNodeSystem.solve(
                logProgress = true,
                progressStep = 32,
                priorityNodes = vars
            )

            val stream = FileOutputStream("test.txt")

            var i = 0
            while (i < andNodeSystem.rows) {
                stream.write((andNodeSystem.equations[i].toString() + "\n").toByteArray(Charsets.US_ASCII))
                i++
            }

            stream.close()
        }

        test("3") {
            val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84, -121, -45, 100)

            val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)
            val andSystem = hashResult.equationSystem.sortBySolvableGroups()

            val andNodeSystem = andSystem.invertAllGroups()

            andNodeSystem.expand()

            val vars = (0 until (1088 + 1600*23)).map { Variable("x$it") }

            andNodeSystem.solve(
                logProgress = true,
                progressStep = 32,
                priorityNodes = vars
            )

            val stream = FileOutputStream("test.txt")

            var i = 0
            while (i < andNodeSystem.rows) {
                stream.write((andNodeSystem.equations[i].toString() + "\n").toByteArray(Charsets.US_ASCII))
                i++
            }

            stream.close()
        }

        test("4") {
            val msg = byteArrayOf(43, -41, 18, -104, -29, 71, -26, -52, -77, 125, -82, 85, -96, 0, 108, -45, 118, -98, 110, 47, -53, -85, 0, -18, 13, 98, 26, 69, -121, -84)

            val hashResult = Keccak256EqSystemGenerator.INSTANCE.hash(msg, replaceRulesInverse = true, replacePadding = false)
            val andSystem = hashResult.equationSystem.sortBySolvableGroups()
            val cols = andSystem.cols

            val normalizedAndSystem = andSystem.normalize()
            System.gc()
            normalizedAndSystem.rotate(hashResult.varValues, left = false, right = false)

            val invertedXorAndSystem = normalizedAndSystem.invertToXorAndSystem()
            System.gc()
            val mask = BitSet(invertedXorAndSystem.andSystem.cols)
            mask.invert(cols)

            var i = 0
            while (i < cols) {
                var j = 0
                while (j < invertedXorAndSystem.xorSystem.rows) {
                    if (invertedXorAndSystem.xorSystem.equations[j][i] && invertedXorAndSystem.xorSystem.eqVarMap[j] >= cols) {
                        invertedXorAndSystem.xorSystem.expressVariable(j, i)
                        break
                    }
                    j++
                }
                i++
            }

            /*invertedXorAndSystem.xorSystem.varEqMap.fill(-1)
            invertedXorAndSystem.xorSystem.eqVarMap.fill(-1)
            invertedXorAndSystem.xorSystem.solve(
                skipValidation = true,
                logProgress = true,
                sortEquations = true,
                varPriority = mask,
            )*/

            invertedXorAndSystem.andSystem.substitute(invertedXorAndSystem.xorSystem)

            invertedXorAndSystem.andSystem.toFile(File("test.txt"), humanReadable = true)
        }
    }
})
