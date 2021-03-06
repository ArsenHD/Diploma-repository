// DONT_TARGET_EXACT_BACKEND: WASM
// WASM_MUTE_REASON: FAKE_OVERRIDE_ISSUES
// On wasm this will produce conflicting return types, A.g will return Any but we will try to interpret it as String.
// Before wasm native strings this worked by chance because we added unbox intrinsic for strings.

interface I<T> {
    val prop: T

    fun f(x: String = "1"): String

    fun g(x: String = "2"): String

    fun h(x: T = prop): T
}

interface I2<T> : I<T>

open class A<T> {
    open fun f(x: String) = x

    open fun g(x: T) = x

    open fun h(x: String) = x
}

class B : A<String>(), I2<String> {
    override val prop
        get() = "3"
}

fun box(): String {
    val i: I<String> = B()
    var result = i.f() + i.g() + i.h()
    if (result != "123") return "fail1: $result"

    val b = B()
    result = b.f() + b.g() + b.h()
    if (result != "123") return "fail2: $result"

    val a: A<String> = B()
    result = a.f("q") + a.g("w") + a.h("e")
    if (result != "qwe") return "fail3: $result"

    return "OK"
}
