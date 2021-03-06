// FIR_IDENTICAL
// !USE_EXPERIMENTAL: kotlin.RequiresOptIn
// FILE: api.kt

package api

@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
annotation class ExperimentalAPI

@ExperimentalAPI
class C {
    @ExperimentalAPI
    fun function() {}

    @ExperimentalAPI
    val property: String = ""

    @ExperimentalAPI
    class Nested {
        @ExperimentalAPI
        fun nestedFunction() {}
    }
}

// FILE: usage.kt

package usage

import api.*

fun use() {
    val c: <!EXPERIMENTAL_API_USAGE!>C<!> = <!EXPERIMENTAL_API_USAGE!>C<!>()
    <!EXPERIMENTAL_API_USAGE!>c<!>.<!EXPERIMENTAL_API_USAGE!>function<!>()
    <!EXPERIMENTAL_API_USAGE!>c<!>.<!EXPERIMENTAL_API_USAGE!>property<!>
    <!EXPERIMENTAL_API_USAGE!>C<!>.<!EXPERIMENTAL_API_USAGE!>Nested<!>().<!EXPERIMENTAL_API_USAGE!>nestedFunction<!>()
}
