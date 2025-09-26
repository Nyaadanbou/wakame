package cc.mewcraft.wakame.item.datagen.impl

import cc.mewcraft.wakame.item.datagen.ItemGenerationContext
import cc.mewcraft.wakame.item.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item.datagen.ItemMetaResult
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.util.MojangStack
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.key.Key
import net.minecraft.core.component.DataComponents
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

sealed interface MetaItemModel : ItemMetaEntry<Key> {

    companion object {

        @JvmField
        val SERIALIZER: TypeSerializer2<MetaItemModel> = DispatchingSerializer.createPartial<String, MetaItemModel>(
            mapOf(
                "none" to None::class, // 其实完全不写 item_model 的话和这个效果一样
                "auto" to Auto::class, // 自动使用物品类型的唯一标识作为 `minecraft:item_model`
                "custom" to Custom::class, // 使用自定义的 `minecraft:item_model`
            )
        )

    }

    @ConfigSerializable
    data object None : MetaItemModel {
        override fun make(context: ItemGenerationContext): ItemMetaResult<Key> {
            return ItemMetaResult.empty()
        }

        override fun write(value: Key, itemstack: MojangStack) {
            // do nothing
        }
    }

    @ConfigSerializable
    data object Auto : MetaItemModel {
        override fun make(context: ItemGenerationContext): ItemMetaResult<Key> {
            val itemModel = context.koishItem.id // 获取当前正在生成的 Koish 物品类型的唯一标识
            return ItemMetaResult.of(itemModel)
        }

        override fun write(value: Key, itemstack: MojangStack) {
            itemstack.set(DataComponents.ITEM_MODEL, PaperAdventure.asVanilla(value))
        }
    }

    @ConfigSerializable
    data class Custom(
        @Setting("value")
        val value: Key,
    ) : MetaItemModel {
        override fun make(context: ItemGenerationContext): ItemMetaResult<Key> {
            return ItemMetaResult.of(value)
        }

        override fun write(value: Key, itemstack: MojangStack) {
            itemstack.set(DataComponents.ITEM_MODEL, PaperAdventure.asVanilla(value))
        }
    }
}