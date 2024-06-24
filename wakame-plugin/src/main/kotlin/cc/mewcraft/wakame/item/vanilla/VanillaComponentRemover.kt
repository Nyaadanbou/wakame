package cc.mewcraft.wakame.item.vanilla

import cc.mewcraft.wakame.config.configurate.TypeDeserializer
import com.google.common.collect.ImmutableMultimap
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.spongepowered.configurate.ConfigurationNode
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
}

/**
 * 序列化器.
 */
internal object VanillaComponentRemoverSerializer : TypeDeserializer<VanillaComponentRemover> {
    override fun deserialize(type: Type, node: ConfigurationNode): VanillaComponentRemover {
        val toRemove = node.getList<VanillaComponentRemoverImpl.Supported>(emptyList())
        return VanillaComponentRemoverImpl(toRemove.toSet())
    }
}

// 临时的不完整实现, 仅支持少数几个组件的移除
// 等 DataComponent API 出来后再写个完整的实现
private class VanillaComponentRemoverImpl(
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
                meta.attributeModifiers = ImmutableMultimap.of() // bad implementation
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