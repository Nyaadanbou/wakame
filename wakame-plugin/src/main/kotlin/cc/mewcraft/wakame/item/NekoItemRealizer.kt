package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.extension.makeItemModelKey
import cc.mewcraft.wakame.item.template.*
import cc.mewcraft.wakame.util.item.toNMS
import org.bukkit.entity.Player

/**
 * Realizes [NekoItem] into an item which then can be added to the game world.
 */
interface NekoItemRealizer {

    /**
     * Realizes an item template from no context.
     *
     * @param prototype the item template
     * @return a one-off NekoStack
     */
    fun realize(prototype: NekoItem): NekoStack

    /**
     * Realizes an item template from an online player.
     *
     * @param prototype the item template
     * @param player the player on which the realization is based
     * @return a one-off NekoStack
     */
    fun realize(prototype: NekoItem, player: Player): NekoStack

    /**
     * Realizes an item template from a crate.
     *
     * @param prototype the item template
     * @param crate the crate on which the realization is based
     * @return a one-off NekoStack
     */
    fun realize(prototype: NekoItem, crate: Crate): NekoStack

    /**
     * Realizes an item template from a custom context.
     *
     * @param prototype the item template
     * @param context the context on which the realization is based
     * @return a one-off NekoStack
     */
    fun realize(prototype: NekoItem, context: ItemGenerationContext): NekoStack

}

internal object VanillaNekoItemRealizer : NekoItemRealizer {
    override fun realize(prototype: NekoItem, player: Player): ImaginaryKoishStack {
        return realize(prototype)
    }

    override fun realize(prototype: NekoItem, crate: Crate): ImaginaryKoishStack {
        return realize(prototype)
    }

    override fun realize(prototype: NekoItem, context: ItemGenerationContext): ImaginaryKoishStack {
        return realize(prototype)
    }

    override fun realize(prototype: NekoItem): ImaginaryKoishStack {
        val context = ItemGenerationContexts.create(ItemGenerationTriggers.empty(), prototype.id, 0)
        val builder = ItemComponentMap.builder()

        fun <T, S : ItemTemplate<T>> generate(type: ItemTemplateType<S>) {
            val template = prototype.templates.get(type) ?: return
            val generated = template.generate(context)
            if (!generated.isEmpty()) {
                val value = generated.value
                builder.set(template.componentType, value)
            }
        }

        generate(ItemTemplateTypes.CASTABLE)
        generate(ItemTemplateTypes.GLOWABLE)

        generate(ItemTemplateTypes.ARROW)
        generate(ItemTemplateTypes.ATTACK)
        generate(ItemTemplateTypes.ATTACK_SPEED)

        generate(ItemTemplateTypes.LEVEL)
        generate(ItemTemplateTypes.RARITY)
        generate(ItemTemplateTypes.ELEMENTS)
        generate(ItemTemplateTypes.KIZAMIZ)
        generate(ItemTemplateTypes.LORE)
        generate(ItemTemplateTypes.CELLS)

        val components = builder.build()
        val nekoStack = ImaginaryKoishStack(
            prototype = prototype,
            components = ItemComponentMap.immutable(components)
        )

        return nekoStack
    }
}

internal object StandardNekoItemRealizer : NekoItemRealizer {
    override fun realize(prototype: NekoItem): NekoStack {
        return realizeByTrigger(prototype, ItemGenerationTriggers.empty())
    }

    override fun realize(prototype: NekoItem, player: Player): NekoStack {
        return realizeByTrigger(prototype, ItemGenerationTriggers.wrap(player))
    }

    override fun realize(prototype: NekoItem, crate: Crate): NekoStack {
        return realizeByTrigger(prototype, ItemGenerationTriggers.wrap(crate))
    }

    override fun realize(prototype: NekoItem, context: ItemGenerationContext): NekoStack {
        return realizeByContext(prototype, context)
    }

    private fun realizeByTrigger(item: NekoItem, trigger: ItemGenerationTrigger): NekoStack {
        val target = item.id
        val context = ItemGenerationContexts.create(trigger, target)
        val nekoStack = realizeByContext(item, context)
        return nekoStack
    }

    /**
     * Generates a NekoStack with the [context].
     *
     * @param prototype the item archetype
     * @param context the input context
     * @return a new NekoStack
     */
    private fun realizeByContext(prototype: NekoItem, context: ItemGenerationContext): NekoStack {
        // 创建空的萌芽物品
        val itemstack = prototype.base.createItemStack().toNMS()
        if (itemstack.isEmpty) {
            error("The item base cannot be empty")
        }
        val koishstack = CustomKoishStack(itemstack)

        // 设置物品的 id 和 variant
        KoishStackImplementations.setId(itemstack, prototype.id)
        KoishStackImplementations.setVariant(itemstack, 0)
        KoishStackImplementations.setItemModel(itemstack, prototype.makeItemModelKey())

        val components = koishstack.components
        val templates = koishstack.templates

        fun <T, S : ItemTemplate<T>> generate(type: ItemTemplateType<S>) {
            val template = templates.get(type) ?: return
            val generated = template.generate(context)
            if (!generated.isEmpty()) {
                val value = generated.value
                components.set(template.componentType, value)
            }
        }

        // 下面按照从上到下的顺序, 实例化每个模板
        // 因此, 如果A依赖B, 应该把A写在B的下面

        generate(ItemTemplateTypes.CASTABLE)
        generate(ItemTemplateTypes.GLOWABLE)
        generate(ItemTemplateTypes.ARROW)
        generate(ItemTemplateTypes.LEVEL)
        generate(ItemTemplateTypes.RARITY)
        generate(ItemTemplateTypes.ELEMENTS)
        generate(ItemTemplateTypes.KIZAMIZ)
        generate(ItemTemplateTypes.CUSTOM_NAME)
        generate(ItemTemplateTypes.ITEM_NAME)
        generate(ItemTemplateTypes.LORE)
        generate(ItemTemplateTypes.ATTACK)
        generate(ItemTemplateTypes.ATTACK_SPEED)
        generate(ItemTemplateTypes.ATTRIBUTE_MODIFIERS)
        generate(ItemTemplateTypes.DAMAGE_RESISTANT)
        generate(ItemTemplateTypes.UNBREAKABLE)
        generate(ItemTemplateTypes.TRIM)
        generate(ItemTemplateTypes.HIDE_TOOLTIP)
        generate(ItemTemplateTypes.HIDE_ADDITIONAL_TOOLTIP)
        generate(ItemTemplateTypes.CAN_BREAK)
        generate(ItemTemplateTypes.CAN_PLACE_ON)
        generate(ItemTemplateTypes.DYED_COLOR)
        generate(ItemTemplateTypes.ENCHANTMENTS)
        generate(ItemTemplateTypes.STORED_ENCHANTMENTS)
        generate(ItemTemplateTypes.MAX_DAMAGE)
        generate(ItemTemplateTypes.DAMAGE)
        generate(ItemTemplateTypes.TOOL)
        generate(ItemTemplateTypes.FOOD)
        generate(ItemTemplateTypes.PORTABLE_CORE)
        generate(ItemTemplateTypes.CELLS)
        generate(ItemTemplateTypes.CRATE)

        return koishstack
    }
}