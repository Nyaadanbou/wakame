package cc.mewcraft.wakame.brew

import cc.mewcraft.wakame.registry2.Registry
import cc.mewcraft.wakame.registry2.WritableRegistry
import cc.mewcraft.wakame.util.Identifiers
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.bukkit.Color

// 我们希望玩家手中的配方物品的 lore 始终能反应最新的配置文件, 因此这里只写一个配方 id.
// 如果要获取配方的具体内容, 如材料, 其他代码需要使用这里的 id 来获取完整的配方对象.
data class BrewRecipe(
    val id: String,
    val name: String,
    val difficulty: Int,
    val cookingTime: Int,
    val distillRuns: Int,
    val distillTime: Int,
    val age: Int,
    val woodType: BarrelWoodType,
    val lore: List<String>,
    val ingredients: Map<String, Int>,
    val potionColor: Color?,
    val customModelData: Int,
    val rarityWeight: Int,
)

/**
 * 酒桶木头类型.
 *
 * 该类型的实例由插件 Brewery 创建.
 *
 * @property formattedName 来自外部的类型名字
 * @property translatedName 经过翻译后的类型名字
 */
class BarrelWoodType(
    val formattedName: String,
    val translatedName: String = formattedName,
) : Keyed {

    override fun key(): Key {
        return REGISTRY.getId(this) ?: Identifiers.of("unregistered")
    }

    override fun toString(): String {
        return "BarrelWoodType(formattedName='$formattedName', translatedName='$translatedName')"
    }

    companion object {

        @JvmField
        val REGISTRY: WritableRegistry<BarrelWoodType> = Registry.of("barrel_wood_type")
    }
}
