// !USE_EXPERIMENTAL: kotlin.RequiresOptIn
// FILE: api.kt

package api

@RequiresOptIn
@Retention(AnnotationRetention.BINARY)
annotation class E

open class Base {
    @E
    open fun foo() {}
}

// FILE: usage.kt

package usage

import api.*

class Derived : Base() {
    override fun foo() {}
}

fun test(b: Base) {
    b.<!EXPERIMENTAL_API_USAGE_ERROR!>foo<!>()
}
