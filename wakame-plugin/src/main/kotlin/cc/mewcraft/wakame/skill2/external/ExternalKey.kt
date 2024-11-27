package cc.mewcraft.wakame.skill2.external

interface ExternalKey<T : ExternalComponent<*>> {
    val id: String
}

fun <T: ExternalComponent<*>> ExternalKey(id: String): ExternalKey<T> {
    return ExternalKeyImpl(id)
}

private data class ExternalKeyImpl<T : ExternalComponent<*>>(
    override val id: String,
) : ExternalKey<T>