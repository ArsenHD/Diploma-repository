// WITH_RUNTIME
const val M = ULong.MAX_VALUE

fun f(a: ULong): Int {
    var n = 0
    for (i in a until M) {
        n++
    }
    return n
}

// JVM non-IR uses while.
// JVM IR uses if + do-while.

// 0 iterator
// 0 getStart
// 0 getEnd
// 0 getFirst
// 0 getLast
// 0 getStep
// 0 INVOKESTATIC kotlin/ULong.constructor-impl
// 0 INVOKE\w+ kotlin/ULong.(un)?box-impl

// JVM_TEMPLATES
// 1 INVOKESTATIC kotlin/UnsignedKt.ulongCompare
// 1 IFGE
// 1 IF

// JVM_IR_TEMPLATES
// 1 INVOKESTATIC kotlin/UnsignedKt.ulongCompare
// 1 IFGE
// 1 IF