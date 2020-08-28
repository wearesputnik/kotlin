open class Generic<T> {
    fun foo(): String = "456"
    companion object {
        fun foo(): String = "123"
    }
}

fun call(f: () -> String) = f()

fun println(s: String) {}

fun main() {
    println(call((Generic)::foo))
    println(call(Generic::foo))
}
