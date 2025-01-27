package cc.mewcraft.wakame.item.templates.virtual

import cc.mewcraft.wakame.item.component.ItemComponentType
import cc.mewcraft.wakame.item.component.ItemComponentTypes
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.*
import cc.mewcraft.wakame.util.MenuIconLore.LineConfig
import io.leangen.geantyref.TypeToken
import net.kyori.adventure.text.Component
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializerCollection

data class ItemMenuIconLore(
    val delegate: MenuIconLore,
) : ItemTemplate<Nothing> {
    override val componentType: ItemComponentType<Nothing> = ItemComponentTypes.EMPTY

    override fun generate(context: ItemGenerationContext): ItemGenerationResult<Nothing> {
        return ItemGenerationResult.empty()
    }

    /**
     * @see MenuIconLore.resolve
     */
    fun resolve(config: LineConfig): List<Component> {
        return delegate.resolve(config)
    }

    /**
     * @see MenuIconLore.resolve
     */
    fun resolve(dict: MenuIconDictionary = MenuIconDictionary(), dsl: LineConfig.Builder.() -> Unit): List<Component> {
        return delegate.resolve(dict, dsl)
    }

    companion object : ItemTemplateBridge<ItemMenuIconLore> {
        override fun codec(id: String): ItemTemplateType<ItemMenuIconLore> {
            return Codec(id)
        }
    }

    private data class Codec(
        override val id: String,
    ) : ItemTemplateType<ItemMenuIconLore> {
        override val type: TypeToken<ItemMenuIconLore> = typeTokenOf()

        /**
         * ## Node structure
         * ```yaml
         * <node>:
         *   - "line 1 (MiniMessage string)"
         *   - "line 2 (MiniMessage string)"
         * ```
         *
         * @see cc.mewcraft.wakame.util.MenuIconLore
         * @see cc.mewcraft.wakame.util.MenuIconLoreSerializer
         */
        override fun decode(node: ConfigurationNode): ItemMenuIconLore {
            return ItemMenuIconLore(node.require<MenuIconLore>())
        }

        override fun childrenCodecs(): TypeSerializerCollection {
            return TypeSerializerCollection.builder()
                .register<MenuIconLore>(MenuIconLoreSerializer)
                .build()
        }
    }
}
