package cc.mewcraft.wakame.core

data class ResourceLocation(
    val namespace: String,
    val path: String,
) {
    companion object {
        fun defaultNamespace(path: String): ResourceLocation = ResourceLocation("koish", path)
    }
}