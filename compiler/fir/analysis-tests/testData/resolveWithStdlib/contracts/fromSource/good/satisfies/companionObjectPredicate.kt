import kotlin.contracts.*

class MyClass {
    companion object {
        fun myFun(num: Int): Boolean = num == 0 || num == 100
    }
}

contract fun myContract(arg: Int) = [
    returns() implies (arg satisfies MyClass::myFun)
]

fun foo(a: Int) contract [myContract(a)] {
    require(MyClass.myFun(a))
}