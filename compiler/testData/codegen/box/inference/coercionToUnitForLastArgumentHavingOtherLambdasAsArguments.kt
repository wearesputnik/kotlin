fun <T> foo(
    lambda1: () -> T,
    lambda2: (T) -> Boolean
): T = lambda1()

fun test() {
    launch {
        foo(
            lambda1 = { double() },
            lambda2 = { it < 0.5 }
        )
    }
}

fun double(): Double = 42.0
fun launch(f: () -> Unit) {}

fun box(): String {
    return "OK"
}