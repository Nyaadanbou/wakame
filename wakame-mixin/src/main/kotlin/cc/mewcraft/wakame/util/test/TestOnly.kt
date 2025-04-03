package cc.mewcraft.wakame.util.test

@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is only for testing purposes, do not use it in production code."
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
annotation class TestOnly