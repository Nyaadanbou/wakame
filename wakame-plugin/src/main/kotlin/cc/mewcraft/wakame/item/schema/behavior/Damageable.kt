package cc.mewcraft.wakame.item.schema.behavior

import cc.mewcraft.commons.provider.Provider
import cc.mewcraft.commons.provider.immutable.map
import cc.mewcraft.commons.provider.immutable.orElse
import cc.mewcraft.wakame.config.ConfigProvider
import cc.mewcraft.wakame.config.optionalEntry
import cc.mewcraft.wakame.event.PlayerSkillPrepareCastEvent
import cc.mewcraft.wakame.item.binary.NekoStack
import cc.mewcraft.wakame.item.binary.PlayNekoStackFactory
import cc.mewcraft.wakame.item.binary.getMetaAccessor
import cc.mewcraft.wakame.item.binary.meta.BDurabilityMeta
import cc.mewcraft.wakame.item.getBehaviorOrNull
import cc.mewcraft.wakame.item.schema.NekoItem
import cc.mewcraft.wakame.item.schema.meta.SDurabilityMeta
import cc.mewcraft.wakame.item.schema.meta.SchemaItemMeta
import cc.mewcraft.wakame.skill.Caster
import cc.mewcraft.wakame.skill.Skill
import cc.mewcraft.wakame.skill.condition.DurabilityCondition
import cc.mewcraft.wakame.skill.condition.DurabilityContext
import cc.mewcraft.wakame.util.Key
import net.kyori.adventure.key.Key
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

/**
 * 可以受损的物品。
 */
interface Damageable : ItemBehavior {
    override val requiredItemMeta: Array<KClass<out SchemaItemMeta<*>>>
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
    ) : Damageable {
        override val repairMaterials: List<Key> by repairMaterials.map { it.map(::Key) }
        override val disappearWhenBroken: Boolean by disappearWhenBroken

        override fun handleBreakBlock(player: Player, itemStack: ItemStack, event: BlockBreakEvent) {
            val nekoStack = PlayNekoStackFactory.require(itemStack)
            nekoStack.decreaseDurabilityNaturally(100)
        }

        //对于原版掉耐久事件的处理，应该写在对应的Item Behavior中
        //例如希望某物品行为不会因为原版的操作掉耐久，则应在其handleDamage中取消原版掉耐久事件
//        override fun handleDamage(player: Player, itemStack: ItemStack, event: PlayerItemDamageEvent) {
//            val damage = event.damage
//            val nekoStack = PlayNekoStackFactory.require(itemStack)
//            event.isCancelled = true
//            nekoStack.decreaseDurabilityNaturally(damage)
//        }

        override fun handleSkillPrepareCast(caster: Caster.Player, itemStack: ItemStack, skill: Skill, event: PlayerSkillPrepareCastEvent) {
            val condition = event.getCondition(DurabilityCondition::class.java) ?: return
            val context = DurabilityContext(itemStack, 1)
            if (condition.test(context)) {
                condition.cost(context)
            } else { //TODO 用isPass缓存条件的判定结果，减少这次test
                event.isAllowCast = false
                caster.bukkitPlayer.sendMessage("物品耐久不足，无法释放技能")
            }
        }
    }


}

fun NekoStack.decreaseDurabilityNaturally(loss: Int) {
    if (loss == 0) return
    require(loss > 0) { "自然减少的耐久不能是负数" }
    val damageable = this.getBehaviorOrNull<Damageable>()
    damageable ?: return
    val durabilityMeta = this.getMetaAccessor<BDurabilityMeta>()
    if (!durabilityMeta.exists) {
        return
    }

    val originDamage = durabilityMeta.damage()
    val threshold = durabilityMeta.threshold()
    if (loss + originDamage >= threshold) {
        if (damageable.disappearWhenBroken) {
            itemStack.amount = 0
        } else {
            durabilityMeta.damage(threshold)
        }
    } else {
        durabilityMeta.damage(loss + originDamage)
    }
}

fun NekoStack.increaseDurabilityNaturally(add: Int) {
    if (add == 0) return
    require(add > 0) { "自然增加的耐久不能是负数" }
    val damageable = this.getBehaviorOrNull<Damageable>()
    damageable ?: return
    val durabilityMeta = this.getMetaAccessor<BDurabilityMeta>()
    if (!durabilityMeta.exists) {
        return
    }

    val originDamage = durabilityMeta.damage()
    durabilityMeta.damage((originDamage - add).coerceAtLeast(0))
}