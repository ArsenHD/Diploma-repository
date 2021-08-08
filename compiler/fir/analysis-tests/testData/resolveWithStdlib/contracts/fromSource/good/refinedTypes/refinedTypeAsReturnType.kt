import kotlin.contracts.*

typealias BoundedBy10 = Int satisfies [::greaterThanMinus10, ::lessThan10]
typealias BoundedBy100 = Int satisfies [::greaterThanMinus100, ::lessThan100]

fun lessThan10(num: Int) = num < 10
fun greaterThanMinus10(num: Int) = num > -10

fun lessThan100(num: Int) = num < 100
fun greaterThanMinus100(num: Int) = num > - 100

fun ensureLessThan10(arg: Int)
    contract [returns() implies (arg satisfies ::lessThan10)]
        = require(lessThan10(arg))

fun ensureGreaterThanMinus10(arg: Int)
    contract [returns() implies (arg satisfies ::greaterThanMinus10)]
        = require(greaterThanMinus10(arg))

fun foo(num: Int) {
    ensureLessThan10(num)
    ensureGreaterThanMinus10(num)
    val increased = multiplyBy10(num)
    // contract of multiplyBy10() implies that its return value is bounded by -100 and 100
    acceptBoundedBy100(increased)
}

fun bar(num: Int) {
    ensureLessThan10(num)
    val increased = multiplyBy10(<!UNSATISFIED_REFINED_TYPE_CONSTRAINTS!>num<!>) // upper bound not guarranteed here
}

fun multiplyBy10(num: BoundedBy10): BoundedBy100 {
    return num * 10
}

fun acceptBoundedBy100(num: BoundedBy100) {
    println(num)
}
