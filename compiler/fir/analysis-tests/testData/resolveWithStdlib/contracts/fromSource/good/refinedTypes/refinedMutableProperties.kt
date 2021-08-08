import kotlin.contracts.*

typealias PositiveInt = Int satisfies ::isPositive

fun isPositive(num: Int) = num > 0

// this class has smart constructor which allows passing only the PositiveInts
class CustomPair {
    // refined types are forbidden for mutable properties with custom setters
    <!REFINED_VAR_WITH_CUSTOM_SETTER!>var first: PositiveInt
        set(value) {
            field = <!UNSATISFIED_REFINED_TYPE_CONSTRAINTS!>value * 10<!>
        }<!>

    // refined types are forbidden for mutable properties with custom setters
    <!REFINED_VAR_WITH_CUSTOM_SETTER!>var second: PositiveInt
        set(value) {
            field = <!UNSATISFIED_REFINED_TYPE_CONSTRAINTS!>value * 20<!>
        }<!>

    constructor(first: PositiveInt, second: PositiveInt) {
        this.first = first
        this.second = second
    }
}

fun checkPositive(num: Int)
        contract [returns(true) implies (num satisfies ::isPositive)]
        = isPositive(num)
