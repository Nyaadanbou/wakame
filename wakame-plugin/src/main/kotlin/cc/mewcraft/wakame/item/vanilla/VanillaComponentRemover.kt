package cc.mewcraft.wakame.item.vanilla

import cc.mewcraft.wakame.config.configurate.TypeDeserializer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.kotlin.extensions.getList
import java.lang.reflect.Type

/**
 * 用于移除物品的原版组件, 例如 `!minecraft:attribute_modifiers`.
 */
interface VanillaComponentRemover {
    /**
     * 从指定的物品上移除既定的组件.
     */
    fun applyToItem(item: ItemStack)

    companion object {
        /**
         * 获取一个空的实例. 该实例不会进行任何操作.
         */
        fun empty(): VanillaComponentRemover = Empty
    }

    private object Empty : VanillaComponentRemover {
        override fun applyToItem(item: ItemStack) = Unit
    }
}

/**
 * 序列化器.
 */
internal object VanillaComponentRemoverSerializer : TypeDeserializer<VanillaComponentRemover> {
    override fun deserialize(type: Type, node: ConfigurationNode): VanillaComponentRemover {
        val removes = node.getList<NaiveVanillaComponentRemover.Supported>(emptyList()).toSet()
        return NaiveVanillaComponentRemover(removes)
    }

    override fun emptyValue(specificType: Type, options: ConfigurationOptions): VanillaComponentRemover? {
        return EmptyVanillaComponentRemover
    }
}

/**
 * 一个空的实现. 该实现不会对物品进行任何操作.
 */
private object EmptyVanillaComponentRemover : VanillaComponentRemover {
    override fun applyToItem(item: ItemStack) = Unit
}

/**
 * 临时的不完整实现, 仅支持少数几个组件的移除.
 */
private class NaiveVanillaComponentRemover(
    private val removes: Set<Supported>,
) : VanillaComponentRemover {
    private fun applyToMeta(meta: ItemMeta) {
        for (supported in removes) {
            supported.applyToMeta(meta)
        }
    }

    override fun applyToItem(item: ItemStack) {
        item.editMeta(::applyToMeta)
    }

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