// WITH_RUNTIME
// FULL_JDK

open class BaseFirBuilder<T> {
    fun <T> MutableList<T>.pop(): T? {
        val result = lastOrNull()
        if (result != null) {
            removeAt(size - 1)
        }
        return result
    }
}

open class BaseConverter : BaseFirBuilder<String>()

class ExpressionsConverter : BaseConverter() {
    fun foo(list: MutableList<String>) {
        list.pop()
    }
}

fun box(): String {
    val list = mutableListOf("OK", "FAIL")
    ExpressionsConverter().foo(list)
    return list.last()
}