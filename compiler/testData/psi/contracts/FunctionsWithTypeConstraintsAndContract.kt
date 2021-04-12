// the following functions have type constraints and contracts written in different order
// any order is correct

fun someFunctionWithTypeConstraints<T, E>(arg: E?, block: () -> T): String
    contract [
        returns() implies (arg != null),
        callsInPlace(block, InvocationKind.EXACTLY_ONCE),
    ]
    where T : MyClass,
          E : MyOtherClass
{
    block()
    arg ?: throw NullArgumentException()
    return "some string"
}

fun anotherFunctionWithTypeConstraints<T, E>(arg: E?, block: () -> T): String
    where T : MyClass,
          E : MyOtherClass
    contract [
        returns() implies (arg != null),
        callsInPlace(block, InvocationKind.EXACTLY_ONCE),
        someComplexContract(arg, block)
    ]
{
    block()
    arg ?: throw NullArgumentException()
    return "some string"
}