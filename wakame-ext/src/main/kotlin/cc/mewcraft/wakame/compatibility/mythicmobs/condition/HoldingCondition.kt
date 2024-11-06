package cc.mewcraft.wakame.compatibility.mythicmobs.condition

import cc.mewcraft.wakame.Nekoo
import cc.mewcraft.wakame.NekooProvider
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.core.logging.MythicLogger
import io.lumine.mythic.core.skills.SkillCondition
import net.kyori.adventure.key.Key
import org.bukkit.entity.LivingEntity

class HoldingCondition(
    line: String,
    mlc: MythicLineConfig,
) : SkillCondition(line), IEntityCondition {
    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val nekoo: Nekoo = NekooProvider.get()
    private val comparisons: Collection<Key>

    init {
        val itemsString = mlc.getString(arrayOf("type", "t", "item", "i", "material", "m"), "minecraft:dirt", *arrayOf(this.conditionVar))
        this.comparisons = buildList {
            for (itemString in itemsString.split(",")) {
                val key = try {
                    Key.key(itemString)
                } catch (e: Exception) {
                    Key.key("minecraft:dirt").also { MythicLogger.errorConditionConfig(this@HoldingCondition, mlc, "'$itemString' is not a valid key.") }
                }

                add(key)
            }
        }
    }

    override fun check(target: AbstractEntity): Boolean {
        val bukkitEntity = target.bukkitEntity
        if (bukkitEntity !is LivingEntity) {
            return false
        } else {
            val entityEquipment = bukkitEntity.equipment
            if (entityEquipment != null) {
                val holding = entityEquipment.itemInMainHand
                for (itemKey in this.comparisons) {
                    if (nekoo.getNekoItemId(holding) == itemKey) {
                        return true
                    }
                }
            }

            return false
        }
    }
}