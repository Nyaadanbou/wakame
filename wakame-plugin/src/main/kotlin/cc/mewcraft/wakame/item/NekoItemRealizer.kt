package cc.mewcraft.wakame.item

import cc.mewcraft.nbt.CompoundTag
import cc.mewcraft.wakame.crate.Crate
import cc.mewcraft.wakame.item.component.ItemComponentMap
import cc.mewcraft.wakame.item.template.GenerationContext
import cc.mewcraft.wakame.item.template.GenerationResult
import cc.mewcraft.wakame.item.template.GenerationTrigger
import cc.mewcraft.wakame.item.template.ItemTemplate
import cc.mewcraft.wakame.item.template.ItemTemplateMap
import cc.mewcraft.wakame.item.template.ItemTemplateType
import cc.mewcraft.wakame.item.template.ItemTemplateTypes
import cc.mewcraft.wakame.user.User
import cc.mewcraft.wakame.util.toNamespacedKey
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.Registry
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import java.util.concurrent.ThreadLocalRandom

/**
 * Realizes [NekoItem] into an item which then can be added to the game world.
 */
interface NekoItemRealizer {

    /**
     * Realizes an item template from a custom trigger.
     *
     * @param item the item template
     * @param context the context on which the realization is based
     * @return a one-off NekoStack
     */
    fun realize(item: NekoItem, context: GenerationContext): NekoStack

    /**
     * Realizes an item template from a player.
     *
     * @param item the item template
     * @param user the player on which the realization is based
     * @return a one-off NekoStack
     */
    fun realize(item: NekoItem, user: User<*>): NekoStack

    /**
     * Realizes an item template from a crate.
     *
     * @param item the item template
     * @param crate the crate on which the realization is based
     * @return a one-off NekoStack
     */
    fun realize(item: NekoItem, crate: Crate): NekoStack

}

internal object ServerNekoItemRealizer : NekoItemRealizer {
    override fun realize(item: NekoItem, context: GenerationContext): NekoStack {
        return generateNekoStack(item, context)
    }

    override fun realize(item: NekoItem, user: User<*>): NekoStack {
        return realize0(item, user)
    }

    override fun realize(item: NekoItem, crate: Crate): NekoStack {
        return realize0(item, crate)
    }

    private fun realize0(item: NekoItem, source: Any): NekoStack {
        val target = item.key // 物品模板的标识
        val context = GenerationContext(GenerationTrigger.wrap(source), target, ThreadLocalRandom.current().nextLong())
        val nekoStack = generateNekoStack(item, context)
        return nekoStack
    }

    /**
     * Generates a NekoStack with the [context].
     *
     * @param blueprint the item blueprint
     * @param context the input context
     * @return a new NekoStack
     */
    private fun generateNekoStack(blueprint: NekoItem, context: GenerationContext): NekoStack {
        val itemKey: Key = blueprint.key
        val itemType: Material = requireNotNull(Registry.MATERIAL.get(blueprint.itemType.toNamespacedKey)) {
            "Can't find org.bukkit.Material by '${blueprint.itemType}'"
        }
        val nekoStack: NekoStack = itemType.createNekoStack()
        val wakameTag: CompoundTag = nekoStack.nbt
        NekoStackSupport.setKey(wakameTag, itemKey)
        NekoStackSupport.setVariant(wakameTag, 0)

        // TODO 2024/7/3 这些都可以转移到 ItemComponents 框架里去
        nekoStack.editItemStack {
            val im = this.itemMeta

            if (blueprint.hideTooltip) {
                im.isHideTooltip = true
            }
            if (blueprint.hideAdditionalTooltip) {
                im.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            }
            blueprint.shownInTooltip.applyTo(im)

            this.itemMeta = im
        }

        // 获取 物品组件 的容器
        val components: ItemComponentMap = nekoStack.components
        // 获取 物品组件模板 的容器
        val templates: ItemTemplateMap = nekoStack.templates

        fun <T, S : ItemTemplate<T>> generate(templateType: ItemTemplateType<S>) {
            val template: S = templates.get(templateType) ?: return
            val generated: GenerationResult<T> = template.generate(context)
            if (!generated.isEmpty()) {
                val value: T = generated.value
                components.set(template.componentType, value)
            }
        }

        // 下面按照从上到下的顺序, 实例化每个模板
        // 因此, 如果A依赖B, 应该把A写在B的下面

        generate(ItemTemplateTypes.ATTRIBUTABLE)
        generate(ItemTemplateTypes.CASTABLE)

        generate(ItemTemplateTypes.ARROW)

        generate(ItemTemplateTypes.LEVEL)
        generate(ItemTemplateTypes.RARITY)
        generate(ItemTemplateTypes.ELEMENTS)
        generate(ItemTemplateTypes.KIZAMIZ)
        generate(ItemTemplateTypes.CUSTOM_NAME)
        generate(ItemTemplateTypes.ITEM_NAME)
        generate(ItemTemplateTypes.LORE)

        generate(ItemTemplateTypes.DAMAGEABLE)
        generate(ItemTemplateTypes.FIRE_RESISTANT)
        generate(ItemTemplateTypes.FOOD)
        generate(ItemTemplateTypes.TOOL)
        generate(ItemTemplateTypes.UNBREAKABLE)

        generate(ItemTemplateTypes.CELLS) // 词条栏最复杂, 并且依赖部分组件, 因此放在最后
        generate(ItemTemplateTypes.CRATE)

        return nekoStack
    }

    private fun NekoStack.editItemStack(block: ItemStack.() -> Unit) {
        block(handle)
    }
}