package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.item.binary.PlayNekoStackFactory
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.CasterAdapter
import cc.mewcraft.wakame.skill.TargetAdapter
import cc.mewcraft.wakame.skill.condition.DurabilityCondition
import cc.mewcraft.wakame.util.Key
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

/**
 * 可以施放技能的物品。
 *
 * 读取物品的技能信息，然后施放技能。
 */
interface Castable : ItemBehavior {
    companion object Factory : ItemBehaviorFactory<Castable> {
        override fun create(item: NekoItem, behaviorConfig: ConfigProvider): Castable {
            return Default()
        }
    }

    private class Default : Castable {
        override val requiredItemMeta: Array<KClass<out SchemaItemMeta<*>>> = emptyArray()

        override fun handleInteract(player: Player, itemStack: ItemStack, action: Action, wrappedEvent: PlayerInteractEvent) {
            if (action != Action.RIGHT_CLICK_AIR)
                return
            val nekoStack = PlayNekoStackFactory.require(itemStack)
//            nekoStack.getMetaAccessor<Skill>()
            val skill = SkillRegistry.INSTANCE[Key(NekoNamespaces.SKILL, "buff_potion_remove")]
            val event = PlayerSkillPrepareCastEvent(
                skill,
                CasterAdapter.adapt(player),
                itemStack,
                DurabilityCondition::class.java
            )
            event.callEvent()

            if (event.isAllowCast) {
                skill.castAt(TargetAdapter.adapt(player))
                player.sendMessage("施放移除效果技能")
            } else {
                player.sendMessage("无法施放移除效果技能")
            }
        }
    }
}