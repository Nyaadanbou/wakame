package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.event.SkillCastEvent
import cc.mewcraft.wakame.item.binary.PlayNekoStackFactory
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.BDurabilityMeta
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.meta.SDurabilityMeta
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.registry.SkillRegistry
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.TargetAdapter
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import kotlin.reflect.KClass

/**
 * 可以受损的物品。
 */
interface Damageable : ItemBehavior {
    override val requiredMetaTypes: Array<KClass<out SchemaItemMeta<*>>>
        get() = arrayOf(SDurabilityMeta::class)

    /**
     * 可以用于修复的材料类型。
     */
    val repairMaterials: List<Key>

    /**
     * 耐久归零时，物品是否消失。
     */
    val disappearWhenBroken: Boolean

    companion object Factory : ItemBehaviorFactory<Damageable> {
        override fun create(item: NekoItem, behaviorConfig: ConfigProvider): Damageable {
            val repairMaterials = behaviorConfig.optionalEntry<List<String>>("repair").orElse(emptyList())
            val isLostWhenBreak = behaviorConfig.optionalEntry<Boolean>("will_lost").orElse(true)
            return Default(repairMaterials, isLostWhenBreak)
        }
    }

    /**
     * 默认实现。理论上还可以有其他实现。
     */
    private class Default(
        repairMaterials: Provider<List<String>>,
        disappearWhenBroken: Provider<Boolean>,
    ) : Damageable, KoinComponent {
        private val logger: Logger by inject()
        override val repairMaterials: List<Key> by repairMaterials.map { it.map(::Key) }
        override val disappearWhenBroken: Boolean by disappearWhenBroken

        override fun handleBreakBlock(player: Player, itemStack: ItemStack, event: BlockBreakEvent) {
            itemStack.damage(10, player)
            SkillRegistry.INSTANCE[Key(NekoNamespaces.SKILL, "buff_potion_remove")].castAt(TargetAdapter.adapt(player))
        }

        override fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) {
            val damage = event.damage
            val nekoStack = PlayNekoStackFactory.require(itemStack)
            val durabilityMeta = nekoStack.getMetaAccessor<BDurabilityMeta>()
            if (!durabilityMeta.exists) {
                logger.warn("物品 ${nekoStack.schema.key} 拥有行为 Damageable, 但是没有耐久度元数据")
                return
            }

            val originDamage = durabilityMeta.damage()
            val threshold = durabilityMeta.threshold()
            if (damage + originDamage >= threshold) {
                if (disappearWhenBroken) {
                    player.inventory.removeItem(itemStack)
                } else {
                    durabilityMeta.damage(threshold)
                    event.isCancelled = true
                }
            } else {
                durabilityMeta.damage(damage + originDamage)
            }
        }

        override fun handleSkillCast(caster: Caster, itemStack: ItemStack, skill: Skill, event: SkillCastEvent) {
            val nekoStack = PlayNekoStackFactory.require(itemStack)
            val durabilityMeta = nekoStack.getMetaAccessor<BDurabilityMeta>()
            if (!durabilityMeta.exists) {
                logger.warn("物品 ${nekoStack.schema.key} 拥有行为 Damageable, 但是没有耐久度元数据")
                return
            }

            val damage = durabilityMeta.damage()
            val threshold = durabilityMeta.threshold()
            if (damage >= threshold) {
                if (disappearWhenBroken) {
//                    caster.player.inventory.removeItem(itemStack)
                    logger.warn("物品 ${nekoStack.schema.key} 耐久度已经归零，消失")
                } else {
                    durabilityMeta.damage(threshold)
                    event.result = SkillCastEvent.Result.NO_DURABILITY
                }
            } else {
                durabilityMeta.damage(damage + event.skill.costDurability)
            }
        }
    }
}