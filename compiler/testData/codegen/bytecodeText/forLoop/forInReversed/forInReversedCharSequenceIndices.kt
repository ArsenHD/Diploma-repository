import kotlin.test.*

fun box(): String {
    val cs = "1111"
    var sum = 0
    for (i in cs.indices.reversed()) {
        sum = sum * 10 + i + cs[i].toInt() - '0'.toInt()
    }
    assertEquals(4321, sum)

    return "OK"
}

// 0 reversed
// 0 iterator
// 0 getStart
// 0 getEnd
// 0 getFirst
// 0 getLast
// 0 getStep

// JVM_TEMPLATES
// 1 IFLT
// 1 IF

// JVM_IR_TEMPLATES
// 1 IF_ICMPGT
// 1 IF_ICMPLE
// 2 IF
