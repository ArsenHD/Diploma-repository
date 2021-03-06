// TARGET_BACKEND: JVM

// IGNORE_BACKEND_FIR: JVM_IR
//  - FIR2IR should generate call to fake override

// WITH_RUNTIME
// FILE: 1.kt

package test2

import test.A

class C : A() {
    fun a(): String {
        return this.s
    }
}

fun box(): String {
    return C().a()
}

// FILE: 2.kt

package test

open class A {
    @JvmField protected val s = "OK";
}
