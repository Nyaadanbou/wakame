package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationTrigger
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toNamespacedKey
import org.bukkit.Registry

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
     * @param user the player on which the realization is based
     * @return a one-off NekoStack
     */
    fun realize(prototype: NekoItem, user: User<*>): NekoStack

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
    fun realize(prototype: NekoItem, context: GenerationContext): NekoStack

}

internal object VanillaNekoItemRealizer : NekoItemRealizer {
    override fun realize(prototype: NekoItem): VanillaNekoStack {
        // 没 trigger 的 ctx
        val context = GenerationContext(GenerationTrigger.nop(), prototype.key, 0)
        // 获取 物品组件 的构建器
        val componentMapBuilder = ItemComponentMap.builder()

        fun <T, S : ItemTemplate<T>> generate(type: ItemTemplateType<S>) {
            val template = prototype.templates.get(type) ?: return
            val generated = template.generate(context)
            if (!generated.isEmpty()) {
                val value = generated.value
                componentMapBuilder.set(template.componentType, value)
            }
        }

        generate(ItemTemplateTypes.ATTRIBUTABLE)
        generate(ItemTemplateTypes.CASTABLE)
        generate(ItemTemplateTypes.GLOWABLE)

        generate(ItemTemplateTypes.ARROW)
        generate(ItemTemplateTypes.BOW)

        generate(ItemTemplateTypes.LEVEL)
        generate(ItemTemplateTypes.RARITY)
        generate(ItemTemplateTypes.ELEMENTS)
        generate(ItemTemplateTypes.KIZAMIZ)
        generate(ItemTemplateTypes.LORE)
        generate(ItemTemplateTypes.CELLS)

        val components = componentMapBuilder.build()
        val immutableComponents = ItemComponentMap.unmodifiable(components)

        val vanillaNekoStack = VanillaNekoStack(
            key = prototype.key,
            prototype = prototype,
            components = immutableComponents
        )
        return vanillaNekoStack
    }

    override fun realize(prototype: NekoItem, user: User<*>): VanillaNekoStack {
        return realize(prototype)
    }

    override fun realize(prototype: NekoItem, crate: Crate): VanillaNekoStack {
        return realize(prototype)
    }

    override fun realize(prototype: NekoItem, context: GenerationContext): VanillaNekoStack {
        return realize(prototype)
    }
}

internal object CustomNekoItemRealizer : NekoItemRealizer {
    override fun realize(prototype: NekoItem): NekoStack {
        return realizeByTrigger(prototype, GenerationTrigger.nop())
    }

    override fun realize(prototype: NekoItem, user: User<*>): NekoStack {
        return realizeByTrigger(prototype, GenerationTrigger.wrap(user))
    }

    override fun realize(prototype: NekoItem, crate: Crate): NekoStack {
        return realizeByTrigger(prototype, GenerationTrigger.wrap(crate))
    }

    override fun realize(prototype: NekoItem, context: GenerationContext): NekoStack {
        return realizeByContext(prototype, context)
    }

    private fun realizeByTrigger(item: NekoItem, trigger: GenerationTrigger): NekoStack {
        val target = item.key
        val context = GenerationContext(trigger, target)
        val nekoStack = realizeByContext(item, context)
        return nekoStack
    }

    /**
     * Generates a NekoStack with the [context].
     *
     * @param prototype the item blueprint
     * @param context the input context
     * @return a new NekoStack
     */
    private fun realizeByContext(prototype: NekoItem, context: GenerationContext): NekoStack {
        // 获取基础的物品类型
        val itemType = requireNotNull(Registry.MATERIAL.get(prototype.itemType.toNamespacedKey())) {
            "Can't find org.bukkit.Material by '${prototype.itemType}'"
        }
        // 创建空的萌芽物品
        val nekoStack = itemType.createNekoStack()
        // 获取萌芽物品的底层 ItemStack
        val itemStack = nekoStack.unsafe.handle

        // 移除既定的 原版组件
        prototype.removeComponents.applyTo(itemStack)
        // 设置物品的 id 和 variant
        NekoStackSupport.setKey(itemStack, prototype.key)
        NekoStackSupport.setVariant(itemStack, 0)

        // 获取 物品组件 的容器
        val components = nekoStack.components
        // 获取 物品组件模板 的容器
        val templates = nekoStack.templates

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

        generate(ItemTemplateTypes.ATTRIBUTABLE)
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

        generate(ItemTemplateTypes.ATTRIBUTE_MODIFIERS)
        generate(ItemTemplateTypes.FIRE_RESISTANT)
        generate(ItemTemplateTypes.UNBREAKABLE)
        generate(ItemTemplateTypes.TRIM)
        generate(ItemTemplateTypes.HIDE_TOOLTIP)
        generate(ItemTemplateTypes.HIDE_ADDITIONAL_TOOLTIP)
        generate(ItemTemplateTypes.CAN_BREAK)
        generate(ItemTemplateTypes.CAN_PLACE_ON)
        generate(ItemTemplateTypes.DYED_COLOR)
        generate(ItemTemplateTypes.ENCHANTMENTS)
        generate(ItemTemplateTypes.STORED_ENCHANTMENTS)
        generate(ItemTemplateTypes.DAMAGEABLE)
        generate(ItemTemplateTypes.TOOL)
        generate(ItemTemplateTypes.FOOD)

        generate(ItemTemplateTypes.PORTABLE_CORE)
        generate(ItemTemplateTypes.CELLS) // 词条栏最复杂, 并且依赖部分组件, 因此放在最后
        generate(ItemTemplateTypes.CRATE)

        return nekoStack
    }
}