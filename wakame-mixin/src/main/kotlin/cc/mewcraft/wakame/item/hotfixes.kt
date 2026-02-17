// 临时解决方案
// 未来将被更好的设计替代

package cc.mewcraft.wakame.item

import cc.mewcraft.wakame.item.extension.rarity2
import cc.mewcraft.wakame.item.property.ItemPropTypes
import cc.mewcraft.wakame.util.MojangStack
import cc.mewcraft.wakame.util.item.toNMS
import io.papermc.paper.adventure.PaperAdventure
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.UseCooldown
import org.bukkit.inventory.ItemStack
import java.util.*


/**
 * 通过**物品发包**向物品堆叠上添加对应的 minecraft:item_model 组件.
 *
 * 此修复动机:
 * 物品堆叠上的 minecraft:item_model 是一个纯控制视觉效果的组件.
 * 几乎每一个自定义物品, 都有自己的 [Item Model Definition](https://minecraft.wiki/w/Items_model_definition).
 * 与其在配置文件里手动写 `item_model`, 不如在发包时自动添加上.
 *
 * 更好的方案:
 * 支持直接修改发送到客户端的物品封包, 在那里, 我们添加 `minecraft:item_model`.
 */
@Deprecated("Hotfix")
object HotfixItemModel {

    fun transform(itemstack: ItemStack) {
        transform(itemstack.toNMS())
    }

    fun transform(itemstack: MojangStack) {
        val itemId = itemstack.koishTypeId
        if (itemId == null) {
            // 我们不处理**纯原版**物品, 因为:
            // - 原版物品都有对应的模型和材质, 不需要我们这里再设置一遍
            // - 如果要修改纯原版物品模型, 应该直接用资源包 override
            return
        }
        val clientboundItemModel = itemstack.getProp(ItemPropTypes.CLIENTBOUND_ITEM_MODEL)
        if (clientboundItemModel != null) {
            // 如果指定了 clientbound_item_model, 则优先采用这里指定的 minecraft:item_model
            itemstack.set(DataComponents.ITEM_MODEL, PaperAdventure.asVanilla(clientboundItemModel))
            return
        }
        if (!itemstack.hasNonDefault(DataComponents.ITEM_MODEL)) {
            // 如果物品堆叠上没有重写的 minecraft:item_model 组件, 则自动加上对应物品 id 的 minecraft:item_model 组件
            itemstack.set(DataComponents.ITEM_MODEL, PaperAdventure.asVanilla(itemId))
            return
        }
        // 否则, 物品堆叠上已经重写了 minecraft:item_model 组件 - 以物品堆叠上的 minecraft:item_model 为准
    }
}


/**
 * 通过**物品发包**向物品堆叠上添加对应的 minecraft:item_name 组件.
 *
 * 此修复动机:
 * 物品堆叠上的 minecraft:item_name 是一个纯控制视觉效果的组件.
 * 当物品配置文件中定义了 `item_name` 元数据时, 我们需要将其作为 minecraft:item_name 数据组件发送到客户端.
 * 这样可以在不修改物品的显示名称(display_name)的情况下自定义物品在客户端的显示名称.
 *
 * 更好的方案:
 * 支持直接修改发送到客户端的物品封包, 在那里, 我们添加 `minecraft:item_name`.
 */
@Deprecated("Hotfix")
object HotfixItemName {

    fun transform(itemstack: ItemStack) {
        transform(itemstack.toNMS())
    }

    fun transform(itemstack: MojangStack) {
        val clientboundItemName = itemstack.getProp(ItemPropTypes.CLIENTBOUND_ITEM_NAME)
        if (clientboundItemName != null) {
            val rarity = itemstack.rarity2
            if (rarity != null) {
                itemstack.set(DataComponents.ITEM_NAME, MiniMessage.miniMessage().deserialize(clientboundItemName, Placeholder.styling("rarity_style", *rarity.unwrap().displayStyles)).let(PaperAdventure::asVanilla))
            } else {
                itemstack.set(DataComponents.ITEM_NAME, MiniMessage.miniMessage().deserialize(clientboundItemName).let(PaperAdventure::asVanilla))
            }
        }
        return
    }
}


/**
 * 通过**物品发包**修复武器冷却不显示末影珍珠的效果.
 *
 * 此修复动机:
 * 武器行为这边, 确实会向客户端发送特定武器 id 的 [Set Cooldown](https://minecraft.wiki/w/Java_Edition_protocol/Packets#Set_Cooldown) 封包.
 * 但客户端收到封包后, 发现物品上并没有指定武器 id 的 [UseCooldown.cooldownGroup].
 * 于是客户端那边就会静默的忽略这个封包, 最终导致无法看到攻击冷却的效果.
 *
 * 更好的方案:
 * 支持直接修改发送到客户端的物品封包, 在那里, 我们添加 `minecraft:use_cooldown`.
 */
@Deprecated("Hotfix")
object HotfixWeaponCooldownDisplay {

    // 冷却时间不由 minecraft:use_cooldown 组件决定, 而由实际发送到客户端的 Set Cooldown 封包决定
    private const val PLACEHOLDER_USE_COOLDOWN = 1f

    fun transform(itemstack: ItemStack) {
        transform(itemstack.toNMS())
    }

    /**
     * 如果 [itemstack] 上有带冷却的武器行为, 则添加对应的 cooldown_group 属性.
     */
    fun transform(itemstack: MojangStack) {
        val itemId = itemstack.typeId
        if (!itemstack.hasProp(ItemPropTypes.MINECRAFT_MELEE) &&
            !itemstack.hasProp(ItemPropTypes.MINECRAFT_TRIDENT) &&
            !itemstack.hasProp(ItemPropTypes.MINECRAFT_MACE)
        ) {
            // 这么实现也意味着策划给物品写多个 AttackCooldownLike 行为, 只会取第一个
            // 但我们不应该在这里"解决"这个问题, 应该在加载配置文件的时候就做检查并给出警告
            return
        }
        val useCooldown = UseCooldown(
            PLACEHOLDER_USE_COOLDOWN,
            Optional.of(PaperAdventure.asVanilla(itemId)) // cooldown_group 才是客户端真的需要的数据
        )
        itemstack.set(DataComponents.USE_COOLDOWN, useCooldown)
    }
}