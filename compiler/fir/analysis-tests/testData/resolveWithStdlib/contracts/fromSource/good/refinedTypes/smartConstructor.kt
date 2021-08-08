import kotlin.contracts.*

typealias PositiveInt = Int satisfies ::isPositive

fun isPositive(num: Int) = num > 0

// this class has smart constructor which allows passing only the PositiveInts
data class PositivePair(val first: PositiveInt, val second: PositiveInt)

fun checkPositive(num: Int)
    contract [returns(true) implies (num satisfies ::isPositive)]
        = isPositive(num)

fun foo(a: Int, b: Int) {
    val pair1 = PositivePair(<!UNSATISFIED_REFINED_TYPE_CONSTRAINTS!>a<!>, <!UNSATISFIED_REFINED_TYPE_CONSTRAINTS!>b<!>) // predicate not checked for any of arguments
    if (checkPositive(a) && checkPositive(b)) {
        val pair2 = PositivePair(a, b) // predicate checked for both arguments
        // bar() can be called on the properties of the created pair
        // since it is known that these properties are PositiveInts
        bar(pair2.first)
        bar(pair2.second)
    }
}

fun bar(a: PositiveInt) {
    println(a)
}
