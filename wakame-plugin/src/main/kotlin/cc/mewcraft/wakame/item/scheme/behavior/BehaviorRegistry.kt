package cc.mewcraft.wakame.item.scheme.behavior

object BehaviorRegistry {
    operator fun get(key: String): ItemBehaviorHolder? {
        return when (key) {
            Damageable.KEY -> Damageable
            else -> null
        }
    }
}