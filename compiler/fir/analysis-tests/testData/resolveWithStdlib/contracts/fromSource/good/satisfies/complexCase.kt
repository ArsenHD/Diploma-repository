import kotlin.contracts.*

fun isPositive(num: Int) = num > 0

fun isEven(num: Int) = num % 2 == 0

contract fun myContract1(num: Int) = [
    returns() implies (num satisfies arrayOf(::isPositive, ::isEven))
]

contract fun myContract2(block: () -> Unit) = [
    callsInPlace(block, InvocationKind.EXACTLY_ONCE)
]

contract fun myContract(a: Int, b: () -> Unit) = [
    myContract1(a),
    myContract2(b)
]

fun foo(x: Int, block: () -> Unit) contract [myContract(x, block)] {
    require(isPositive(x))
    require(isEven(x))
    block()
}