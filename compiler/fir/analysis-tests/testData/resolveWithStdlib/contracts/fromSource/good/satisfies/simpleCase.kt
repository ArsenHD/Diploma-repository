import kotlin.contracts.*

fun isPositive(num: Int) = num > 0

fun foo(num: Int) contract [
    returns() implies (num satisfies ::isPositive)
] {
    require(isPositive(num))
}
