package cc.mewcraft.wakame.damage

import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import net.kyori.adventure.key.Key
import org.bukkit.damage.DamageType

/**
 * 使用这里的 [DamageType] 需要先在数据包注册. 否则返回的伤害类型均为 `minecraft:generic`.
 */
object DamageTypes {

    // 由于 DamageTags 的初衷是为了区分攻击方式 (回顾: 元素是为了区分伤害类型),
    // 所以这里参考物品的 Attack 行为来定义的 DamageType.

    @JvmField
    val SWORD = get("koish:sword")

    @JvmField
    val AXE = get("koish:axe")

    @JvmField
    val BOW = get("koish:bow")

    @JvmField
    val CROSSBOW = get("koish:crossbow")

    //@JvmField
    //val TRIDENT = get("koish:trident") // in favor of "minecraft:trident"

    //@JvmField
    //val HAMMER = get("koish:hammer") // in favor of "minecraft:mace_smash"

    @JvmField
    val SPEAR = get("koish:spear")

    @JvmField
    val CUDGEL = get("koish:cudgel")

    @JvmField
    val SICKLE = get("koish:sickle")

    @JvmField
    val WAND = get("koish:wand")

    private fun get(id: String): DamageType {
        val registryKey = RegistryKey.DAMAGE_TYPE
        val registry = RegistryAccess.registryAccess().getRegistry(registryKey)
        return registry.get(Key.key(id)) ?: DamageType.GENERIC
    }

}