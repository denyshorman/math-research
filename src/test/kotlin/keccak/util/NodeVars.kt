package keccak.util

import keccak.Bit
import keccak.Variable

//region x variable
val x0 = Variable("x0")
val x1 = Variable("x1")
val x2 = Variable("x2")
val x3 = Variable("x3")
val x4 = Variable("x4")
val x5 = Variable("x5")
val x6 = Variable("x6")
val x7 = Variable("x7")
val x8 = Variable("x8")
val x9 = Variable("x9")
val x10 = Variable("x10")
val x11 = Variable("x11")
//endregion

//region y variable
val y0 = Variable("y0")
val y1 = Variable("y1")
val y2 = Variable("y2")
val y3 = Variable("y3")
val y4 = Variable("y4")
val y5 = Variable("y5")
val y6 = Variable("y6")
val y7 = Variable("y7")
val y8 = Variable("y8")
val y9 = Variable("y9")
val y10 = Variable("y10")
//endregion

//region a variable
val a00 = Variable("a00")
val a01 = Variable("a01")
val a02 = Variable("a02")
val a03 = Variable("a03")
val a04 = Variable("a04")
val a05 = Variable("a05")
val a06 = Variable("a06")
val a07 = Variable("a07")
val a08 = Variable("a08")
val a09 = Variable("a09")

val a10 = Variable("a10")
val a11 = Variable("a11")
val a12 = Variable("a12")
val a13 = Variable("a13")
val a14 = Variable("a14")
val a15 = Variable("a15")
val a16 = Variable("a16")
val a17 = Variable("a17")
val a18 = Variable("a18")
val a19 = Variable("a19")

val a20 = Variable("a20")
val a21 = Variable("a21")
val a22 = Variable("a22")
val a23 = Variable("a23")
val a24 = Variable("a24")
val a25 = Variable("a25")
val a26 = Variable("a26")
val a27 = Variable("a27")
val a28 = Variable("a28")
val a29 = Variable("a29")

val a30 = Variable("a30")
val a31 = Variable("a31")
val a32 = Variable("a32")
val a33 = Variable("a33")
val a34 = Variable("a34")
val a35 = Variable("a35")
val a36 = Variable("a36")
val a37 = Variable("a37")
val a38 = Variable("a38")
val a39 = Variable("a39")

val a40 = Variable("a40")
val a41 = Variable("a41")
val a42 = Variable("a42")
val a43 = Variable("a43")
val a44 = Variable("a44")
val a45 = Variable("a45")
val a46 = Variable("a46")
val a47 = Variable("a47")
val a48 = Variable("a48")
val a49 = Variable("a49")

val a50 = Variable("a50")
val a51 = Variable("a51")
val a52 = Variable("a52")
val a53 = Variable("a53")
val a54 = Variable("a54")
val a55 = Variable("a55")
val a56 = Variable("a56")
val a57 = Variable("a57")
val a58 = Variable("a58")
val a59 = Variable("a59")

val a60 = Variable("a60")
val a61 = Variable("a61")
val a62 = Variable("a62")
val a63 = Variable("a63")
val a64 = Variable("a64")
val a65 = Variable("a65")
val a66 = Variable("a66")
val a67 = Variable("a67")
val a68 = Variable("a68")
val a69 = Variable("a69")

val a70 = Variable("a70")
val a71 = Variable("a71")
val a72 = Variable("a72")
val a73 = Variable("a73")
val a74 = Variable("a74")
val a75 = Variable("a75")
val a76 = Variable("a76")
val a77 = Variable("a77")
val a78 = Variable("a78")
val a79 = Variable("a79")
//endregion

//region b variable
val b00 = Variable("b00")
val b01 = Variable("b01")
val b02 = Variable("b02")
val b03 = Variable("b03")
val b04 = Variable("b04")
val b05 = Variable("b05")
val b06 = Variable("b06")
val b07 = Variable("b07")
val b08 = Variable("b08")
val b09 = Variable("b09")

val b10 = Variable("b10")
val b11 = Variable("b11")
val b12 = Variable("b12")
val b13 = Variable("b13")
val b14 = Variable("b14")
val b15 = Variable("b15")
val b16 = Variable("b16")
val b17 = Variable("b17")
val b18 = Variable("b18")
val b19 = Variable("b19")

val b20 = Variable("b20")
val b21 = Variable("b21")
val b22 = Variable("b22")
val b23 = Variable("b23")
val b24 = Variable("b24")
val b25 = Variable("b25")
val b26 = Variable("b26")
val b27 = Variable("b27")
val b28 = Variable("b28")
val b29 = Variable("b29")

val b30 = Variable("b30")
val b31 = Variable("b31")
val b32 = Variable("b32")
val b33 = Variable("b33")
val b34 = Variable("b34")
val b35 = Variable("b35")
val b36 = Variable("b36")
val b37 = Variable("b37")
val b38 = Variable("b38")
val b39 = Variable("b39")

val b40 = Variable("b40")
val b41 = Variable("b41")
val b42 = Variable("b42")
val b43 = Variable("b43")
val b44 = Variable("b44")
val b45 = Variable("b45")
val b46 = Variable("b46")
val b47 = Variable("b47")
val b48 = Variable("b48")
val b49 = Variable("b49")

val b50 = Variable("b50")
val b51 = Variable("b51")
val b52 = Variable("b52")
val b53 = Variable("b53")
val b54 = Variable("b54")
val b55 = Variable("b55")
val b56 = Variable("b56")
val b57 = Variable("b57")
val b58 = Variable("b58")
val b59 = Variable("b59")

val b60 = Variable("b60")
val b61 = Variable("b61")
val b62 = Variable("b62")
val b63 = Variable("b63")
val b64 = Variable("b64")
val b65 = Variable("b65")
val b66 = Variable("b66")
val b67 = Variable("b67")
val b68 = Variable("b68")

val b70 = Variable("b70")
val b71 = Variable("b71")
val b72 = Variable("b72")
val b73 = Variable("b73")
val b74 = Variable("b74")
val b75 = Variable("b75")
val b76 = Variable("b76")
val b77 = Variable("b77")
val b78 = Variable("b78")
//endregion

//region c variable
val c00 = Variable("c00")
val c01 = Variable("c01")
val c02 = Variable("c02")
val c03 = Variable("c03")
val c04 = Variable("c04")
val c05 = Variable("c05")
val c06 = Variable("c06")
val c07 = Variable("c07")
val c08 = Variable("c08")
val c09 = Variable("c09")

val c10 = Variable("c10")
val c11 = Variable("c11")
val c12 = Variable("c12")
val c13 = Variable("c13")
val c14 = Variable("c14")
val c15 = Variable("c15")
val c16 = Variable("c16")
val c17 = Variable("c17")
val c18 = Variable("c18")
val c19 = Variable("c19")

val c20 = Variable("c20")
val c21 = Variable("c21")
val c22 = Variable("c22")
val c23 = Variable("c23")
val c24 = Variable("c24")
val c25 = Variable("c25")
val c26 = Variable("c26")
val c27 = Variable("c27")
val c28 = Variable("c28")
val c29 = Variable("c29")

val c30 = Variable("c30")
val c31 = Variable("c31")
val c32 = Variable("c32")
val c33 = Variable("c33")
val c34 = Variable("c34")
val c35 = Variable("c35")
val c36 = Variable("c36")
val c37 = Variable("c37")
val c38 = Variable("c38")

val c40 = Variable("c40")
val c41 = Variable("c41")
val c42 = Variable("c42")
val c43 = Variable("c43")
val c44 = Variable("c44")
val c45 = Variable("c45")
val c46 = Variable("c46")
val c47 = Variable("c47")
val c48 = Variable("c48")

val c50 = Variable("c50")
val c51 = Variable("c51")
val c52 = Variable("c52")
val c53 = Variable("c53")
val c54 = Variable("c54")
val c55 = Variable("c55")
val c56 = Variable("c56")
val c57 = Variable("c57")
val c58 = Variable("c58")

val c60 = Variable("c60")
val c61 = Variable("c61")
val c62 = Variable("c62")
val c63 = Variable("c63")
val c64 = Variable("c64")
val c65 = Variable("c65")
val c66 = Variable("c66")
val c67 = Variable("c67")
val c68 = Variable("c68")

val c70 = Variable("c70")
val c71 = Variable("c71")
val c72 = Variable("c72")
val c73 = Variable("c73")
val c74 = Variable("c74")
val c75 = Variable("c75")
val c76 = Variable("c76")
val c77 = Variable("c77")
val c78 = Variable("c78")
//endregion

//region t/f variables
val t = Bit(true)
val f = Bit(false)
//endregion
