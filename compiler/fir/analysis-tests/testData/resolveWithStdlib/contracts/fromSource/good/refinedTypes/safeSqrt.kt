import kotlin.contracts.*
import kotlin.math.sqrt

typealias PositiveDouble = Double satisfies ::isPositive

fun isPositive(a: Double): Boolean = a > 0

fun checkPositive(num: Double): Boolean
    contract [returns(true) implies (num satisfies ::isPositive)]
        = isPositive(num)

fun foo(num: Double) {
    if (checkPositive(num)) {
        safeSqrt(num)
    }
}

fun bar(num: Double) {
    safeSqrt(<!UNSATISFIED_REFINED_TYPE_CONSTRAINTS!>num<!>)
}

fun safeSqrt(num: PositiveDouble): PositiveDouble {
    return sqrt(num)
}
