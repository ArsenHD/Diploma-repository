import kotlin.contracts.*

fun isPositive(num: Int) = num > 0

fun inRange(num: Int) = num in -100..100

fun isEven(num: Int) = num % 2 == 0

fun foo(num: Int) contract [
    returns() implies (num satisfies arrayOf(::isPositive, ::inRange, ::isEven))
] {
    require(isPositive(num))
    require(inRange(num))
    require(isEven(num))
}
