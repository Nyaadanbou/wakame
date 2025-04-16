package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.brew.BrewRecipeManager
import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.serialization.configurate.TypeSerializer2
import cc.mewcraft.wakame.serialization.configurate.serializer.DispatchingSerializer
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.register
import cc.mewcraft.wakame.util.registerExact
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting
import org.spongepowered.configurate.serialize.TypeSerializerCollection

interface MetaBrewRecipe : ItemMetaEntry<String> {

    companion object {

        @JvmField
        val SERIALIZERS: TypeSerializerCollection = TypeSerializerCollection.builder()
            .register(RandomFromAll.SERIALIZER)
            .registerExact(
                DispatchingSerializer.createPartial<String, MetaBrewRecipe>(
                    mapOf(
                        "fixed" to Fixed::class,
                        "random_from_set" to RandomFromSet::class,
                        "random_from_all" to RandomFromAll::class,
                    )
                )
            )
            .build()
    }

    override fun write(value: String, itemstack: MojangStack) {
        itemstack.ensureSetData(ItemDataTypes.BREW_RECIPE, value)
    }

    /**
     * 生成固定配方.
     */
    @ConfigSerializable
    data class Fixed(
        @Setting("value", true)
        val entry: String,
    ) : MetaBrewRecipe {

        override fun make(context: Context): ItemMetaResult<String> {
            return ItemMetaResult.of(entry)
        }
    }

    /**
     * 从给定的配方集合中随机选择一个.
     */
    @ConfigSerializable
    data class RandomFromSet(
        @Setting("value")
        val entries: Set<String>,
    ) : MetaBrewRecipe {

        override fun make(context: Context): ItemMetaResult<String> {
            return if (entries.isEmpty()) {
                ItemMetaResult.empty()
            } else {
                ItemMetaResult.of(entries.random())
            }
        }
    }

    /**
     * 从所有已知的配方中随机选择一个.
     */
    object RandomFromAll : MetaBrewRecipe {

        @JvmField
        val SERIALIZER: TypeSerializer2<RandomFromAll> = TypeSerializer2<RandomFromAll> { type, node ->
            if (node.virtual()) null else RandomFromAll
        }

        override fun make(context: Context): ItemMetaResult<String> {
            val result = BrewRecipeManager.INSTANCE.random()?.id

            return if (result == null) {
                ItemMetaResult.empty()
            } else {
                ItemMetaResult.of(result)
            }
        }
    }
}