import kotlin.contracts.*

typealias MyInt = Int satisfies [::isPositive, ::isEven, ::isBounded]

fun isPositive(a: Int) = a > 0
fun isEven(a: Int) = a % 2 == 0
fun isBounded(a: Int) = -100 < a && a < 100

contract fun myIntIfTrue(num: Int) = [
    returns(true) implies (num satisfies arrayOf(::isPositive, ::isEven, ::isBounded))
]

fun checkMyInt(num: Int): Boolean contract [myIntIfTrue(num)]
        = isPositive(num) && isEven(num) && isBounded(num)

fun checkPositive(num: Int)
    contract [returns(true) implies (num satisfies ::isPositive)]
        = isPositive(num)

fun checkEven(num: Int)
    contract [returns(true) implies (num satisfies ::isEven)]
        = isEven(num)

fun checkBounded(num: Int)
    contract [returns(true) implies (num satisfies ::isBounded)]
        = isBounded(num)

fun foo(num: Int) {
    if (checkMyInt(num)) {
        bar(num)
    }
    if (checkPositive(num) && checkEven(num)) {
        bar(<!UNSATISFIED_REFINED_TYPE_CONSTRAINTS!>num<!>) // did not check for isBounded() yet
        if (checkBounded(num)) {
            bar(num) // this is OK, all predicates checked
        }
    }
}

fun bar(a: MyInt) {
    println(a)
}
