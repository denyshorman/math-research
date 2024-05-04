package keccak.math.arithmetic

operator fun ArithmeticNode.plus(other: ArithmeticNode) = Sum(this, other)
operator fun ArithmeticNode.plus(other: Int) = Sum(this, IntNumber(other))
operator fun ArithmeticNode.minus(other: ArithmeticNode) = Sum(this, -other)
operator fun ArithmeticNode.minus(other: Int) = Sum(this, IntNumber(-other))
operator fun ArithmeticNode.times(other: ArithmeticNode) = Multiply(this, other)
operator fun ArithmeticNode.times(other: Int) = Multiply(this, IntNumber(other))
operator fun ArithmeticNode.times(other: Long) = Multiply(this, IntNumber(other))
operator fun ArithmeticNode.div(other: Int) = Multiply(this, InverseNumber(other))
operator fun ArithmeticNode.unaryMinus() = Multiply(IntNumber(-1), this)

operator fun Int.plus(other: ArithmeticNode) = Sum(IntNumber(this), other)
operator fun Int.minus(other: ArithmeticNode) = Sum(IntNumber(this), -other)
operator fun Int.times(other: ArithmeticNode) = Multiply(IntNumber(this), other)

operator fun Long.plus(other: ArithmeticNode) = Sum(IntNumber(this), other)
operator fun Long.minus(other: ArithmeticNode) = Sum(IntNumber(this), -other)
operator fun Long.times(other: ArithmeticNode) = Multiply(IntNumber(this), other)
