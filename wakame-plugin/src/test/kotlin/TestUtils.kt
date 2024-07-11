import kotlin.test.fail

fun assertAny(vararg assertions: () -> Unit) {
    val errors = mutableListOf<Throwable>()
    for (assertion in assertions) {
        try {
            assertion()
            return // If any assertion succeeds, return without error.
        } catch (e: Throwable) {
            errors.add(e)
        }
    }
    // If all assertions fail, throw an exception containing all error messages.
    val message = errors.joinToString(separator = "\n") { it.message ?: "Unknown error" }
    fail("All assertions failed:\n$message")
}