package cc.mewcraft.wakame.item.scheme.behavior

object BehaviorRegistry {
    private val keyToBehavior: Map<String, ItemBehaviorHolder> = mapOf(
        Damageable.KEY to Damageable
    )

    operator fun get(key: String): ItemBehaviorHolder? {
        return keyToBehavior[key]
    }

    fun getOrThrow(key: String): ItemBehaviorHolder {
        return get(key) ?: throw IllegalArgumentException("No such behavior: $key")
    }
}