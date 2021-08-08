import kotlin.contracts.*

typealias PositiveInt = Int satisfies ::isPositive

fun isPositive(num: Int) = num > 0

// this class has smart constructor which allows passing only the PositiveInts
data class PositiveMutablePair(var first: PositiveInt, var second: PositiveInt)

fun checkPositive(num: Int) contract [returns(true) implies (num satisfies ::isPositive)] = isPositive(num)

fun foo(a: Int, b: Int) {
    if (checkPositive(a) && checkPositive(b)) {
        val pair2 = PositiveMutablePair(a, b) // predicate checked for both arguments
        // bar() can be called on the properties of the created pair
        // since it is known that these properties are PositiveInts
        bar(pair2.first)
        bar(pair2.second)
        // cannot assign an unchecked value to the PositiveInt property
        pair2.first = <!UNSATISFIED_REFINED_TYPE_CONSTRAINTS!>-10<!>
    }
}

fun bar(a: PositiveInt) {
    println(a)
}
