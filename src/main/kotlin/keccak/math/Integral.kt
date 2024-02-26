package keccak.math

fun integral(
    func: (Double) -> Double,
    a: Double,
    b: Double,
    h: Double,
): Double {
    var x = a
    var sum = 0.0
    while (x < b) {
        sum += func(x)*h
        x += h
    }
    return sum
}

fun integral(
    func: (Double, Double) -> Double,
    x0: Double,
    x1: Double,
    y0: Double,
    y1: Double,
    h: Double,
): Double {
    var y = y0
    var sum = 0.0
    while (y < y1) {
        var x = x0
        while (x < x1) {
            sum += func(x, y) * h * h
            x += h
        }
        y += h
    }
    return sum
}

fun integral(
    func: (Double, Double, Double) -> Double,
    x0: Double,
    x1: Double,
    y0: Double,
    y1: Double,
    z0: Double,
    z1: Double,
    h: Double,
): Double {
    var sum = 0.0
    var z = z0
    while (z < z1) {
        var y = y0
        while (y < y1) {
            var x = x0
            while (x < x1) {
                sum += func(x, y, z) * h * h * h
                x += h
            }
            y += h
        }
        z += h
    }
    return sum
}
