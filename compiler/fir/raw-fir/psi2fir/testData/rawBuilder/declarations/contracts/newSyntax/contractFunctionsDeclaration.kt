contract fun <T> notNullIfReturns(arg: T?) = [returns() implies (arg != null)]

contract fun callsInPlaceOnce(block: () -> Unit) = [callsInPlace(block, InvocationKind.EXACTLY_ONCE)]

contract fun <T> complexContract(arg: T?, block: () -> Unit) = [
    notNullIfReturns(arg),
    callsInPlaceOnce(block),
    returnsNotNull()
]