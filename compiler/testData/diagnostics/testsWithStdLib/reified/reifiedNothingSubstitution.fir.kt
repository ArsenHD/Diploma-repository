// !DIAGNOSTICS: -UNUSED_PARAMETER -UNREACHABLE_CODE -UNUSED_VARIABLE -DEPRECATION

inline fun<reified T> foo(block: () -> T): String = block().toString()

inline fun <reified T: Any> javaClass(): Class<T> = T::class.java

fun box() {
    val a = <!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>arrayOf<!>(null!!)
    val b = Array<<!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>Nothing?<!>>(5) { null!! }
    val c = <!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>foo<!>() { null!! }
    val d = foo<Any> { null!! }
    val e = <!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>foo<!> { "1" as Nothing }
    val e1 = <!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>foo<!> { "1" as Nothing? }

    val f = javaClass<<!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>Nothing<!>>()
}
