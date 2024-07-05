package cc.mewcraft.wakame.item.vanilla

import cc.mewcraft.wakame.config.configurate.TypeDeserializer
import cc.mewcraft.wakame.util.EnumLookup
import net.kyori.examination.Examinable
import net.kyori.examination.ExaminableProperty
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.jetbrains.annotations.TestOnly
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type
import java.util.stream.Stream

/**
 * 用于移除物品的原版组件, 例如 `!minecraft:attribute_modifiers`.
 */
interface VanillaComponentRemover : Examinable {
    /**
     * 检查组件是否在要移除的列表内.
     */
    @TestOnly
    fun has(name: String): Boolean

    /**
     * 从指定的物品上移除既定的组件.
     */
    fun applyTo(item: ItemStack)

    companion object {
        /**
         * 获取一个空的实例. 该实例不会进行任何操作.
         */
        fun empty(): VanillaComponentRemover = Empty
    }

    private object Empty : VanillaComponentRemover {
        override fun has(name: String): Boolean = false
        override fun applyTo(item: ItemStack) = Unit
    }
}

/**
 * [VanillaComponentRemover] 的序列化器.
 */
internal object VanillaComponentRemoverSerializer : TypeDeserializer<VanillaComponentRemover> {
    override fun deserialize(type: Type, node: ConfigurationNode): VanillaComponentRemover {
        val removes = node.getList<NaiveVanillaComponentRemover.Supported>(emptyList()).toSet()
        return NaiveVanillaComponentRemover(removes)
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): VanillaComponentRemover {
        return EmptyVanillaComponentRemover // 配置文件缺省时使用空实例
    }
}

/**
 * 一个空的实现. 该实现不会对物品进行任何操作.
 */
private object EmptyVanillaComponentRemover : VanillaComponentRemover {
    override fun has(name: String): Boolean = false
    override fun applyTo(item: ItemStack) = Unit
}

/**
 * 临时的不完整实现, 仅支持少数几个组件的移除.
 */
private class NaiveVanillaComponentRemover(
    private val removes: Set<Supported>,
) : VanillaComponentRemover {
    override fun has(name: String): Boolean {
        return removes.any { EnumLookup.lookup<Supported>(name).getOrNull() != null }
    }

    private fun applyToMeta(meta: ItemMeta) {
        for (supported in removes) {
            supported.applyToMeta(meta)
        }
    }

    override fun applyTo(item: ItemStack) {
        item.editMeta(::applyToMeta)
    }

    override fun examinableProperties(): Stream<out ExaminableProperty> = Stream.of(
        ExaminableProperty.of("removes", removes)
    )

    enum class Supported {
        ATTRIBUTE_MODIFIERS {
            override fun applyToMeta(meta: ItemMeta) {
                meta.attributeModifiers = null
            }
        },
        FOOD {
            override fun applyToMeta(meta: ItemMeta) {
                meta.setFood(null)
            }
        },
        TOOL {
            override fun applyToMeta(meta: ItemMeta) {
                meta.setTool(null)
            }
        },
        ;

        abstract fun applyToMeta(meta: ItemMeta)
    }
}