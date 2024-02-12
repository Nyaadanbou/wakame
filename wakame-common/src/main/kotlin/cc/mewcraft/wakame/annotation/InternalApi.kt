package cc.mewcraft.wakame.annotation

@RequiresOptIn(message = "Only to be used by internal implementation")
@Retention(AnnotationRetention.BINARY)
annotation class InternalApi(
    val message: String = "",
)

