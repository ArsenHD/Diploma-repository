fun isPositive(num: Int) = num > 0

fun isNegative(num: Int) = num < 0

fun inRange(num: Int) = num in -100..100

typealias PositiveInt = Int satisfies ::isPositive

typealias NegativeInt = Int satisfies ::isNegative

typealias MyInt = Int satisfies [::isPositive, ::inRange]