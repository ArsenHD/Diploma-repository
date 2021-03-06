// IGNORE_BACKEND_FIR: JVM_IR
val xs = listOf<Any>().asSequence()

fun box(): String {
    val s = StringBuilder()
    for ((index, x) in xs.withIndex()) {
        return "Loop over empty list should not be executed"
    }
    return "OK"
}

// 0 withIndex
// 1 iterator
// 1 hasNext
// 1 next
// 0 component1
// 0 component2

// - Initializing the index in the lowered for-loop.
// 1 ICONST_0
