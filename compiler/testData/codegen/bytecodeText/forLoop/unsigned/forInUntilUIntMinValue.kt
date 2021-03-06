// WITH_RUNTIME
const val M = UInt.MIN_VALUE

fun f(a: UInt): Int {
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
// 0 INVOKESTATIC kotlin/UInt.constructor-impl
// 0 INVOKE\w+ kotlin/UInt.(un)?box-impl

// JVM_TEMPLATES
// 1 IF

// JVM_IR_TEMPLATES
// 1 IF
