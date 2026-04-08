package cc.mewcraft.wakame.brewery

import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.registry.WritableRegistry
import cc.mewcraft.wakame.util.KoishKeys
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.bukkit.Color

/**
 * 酒酿的配方.
 *
 * 实例创建:
 * 该类型应该由 Brewery 的钩子逻辑创建.
 *
 * 实现细节:
 * - Koish 内部这边仅直接跟该类型交互, 以应对不确定的 Brewery 实现.
 * - 之所以不直接使用 Brewery 的配方类型是因为有太多不需要的属性和函数.
 */
data class BrewRecipe(
    val id: String,
    val name: String,
    val difficulty: Int,
    val cookingTime: Int,
    val distillRuns: Int,
    val distillTime: Int,
    val age: Int,
    val barrelType: BarrelWoodType,
    val lore: List<String>,
    val ingredients: Map<String, Int>,
    val potionColor: Color?,
) {

    override fun equals(other: Any?): Boolean {
        return other is BrewRecipe && other.id == this.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

/**
 * 酒酿配方的适配器, 用于将外部的 [T] 转换为 Koish 定义的 [BrewRecipe].
 *
 * 该接口的作用仅仅只是为了规范实现, 一般来说只有一个实现.
 * 其实例应该仅在钩子内部使用, Koish 内部不需要使用该实例.
 */
interface BrewRecipeAdapter<T> {
    fun adapt(recipe: T): BrewRecipe
}

/**
 * 酒桶木头的类型.
 *
 * 实例创建:
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
        return REGISTRY.getId(this) ?: KoishKeys.of("unregistered")
    }

    override fun toString(): String {
        return "BarrelWoodType(formattedName='$formattedName', translatedName='$translatedName')"
    }

    // equals/hashCode 不实现, 使用 object identity 判断相等

    companion object {

        @JvmField
        val NONE: BarrelWoodType = BarrelWoodType("None")

        @JvmField
        val REGISTRY: WritableRegistry<BarrelWoodType> = Registry.of("barrel_wood_type")
    }
}
