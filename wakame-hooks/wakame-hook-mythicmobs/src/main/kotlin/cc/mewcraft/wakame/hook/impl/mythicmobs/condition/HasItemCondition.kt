package cc.mewcraft.wakame.hook.impl.mythicmobs.condition

import cc.mewcraft.wakame.api.Nekoo
import cc.mewcraft.wakame.api.NekooProvider
import io.lumine.mythic.api.adapters.AbstractEntity
import io.lumine.mythic.api.adapters.AbstractLocation
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.conditions.IEntityCondition
import io.lumine.mythic.api.skills.conditions.ILocationCondition
import io.lumine.mythic.bukkit.BukkitAdapter
import io.lumine.mythic.bukkit.utils.numbers.RangedInt
import io.lumine.mythic.core.logging.MythicLogger
import io.lumine.mythic.core.skills.SkillCondition
import net.kyori.adventure.key.Key
import org.bukkit.block.Container
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class HasItemCondition(
    line: String,
    private val mlc: MythicLineConfig,
) : SkillCondition(line), IEntityCondition, ILocationCondition {
    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val nekoo: Nekoo = NekooProvider.get()

    private val itemKey: Key
    private val amount: RangedInt

    init {
        val itemString = mlc.getString(arrayOf("type", "t", "item", "i", "material", "m"), "minecraft:dirt", *arrayOf<String>(this.conditionVar))
        itemKey = try {
            Key.key(itemString)
        } catch (_: Exception) {
            Key.key("minecraft:dirt").also { MythicLogger.errorConditionConfig(this, mlc, "'$itemString' is not a valid key.") }
        }

        amount = RangedInt(mlc.getString(arrayOf("amount", "a"), ">0", *arrayOfNulls<String>(0)))
    }

    override fun check(target: AbstractEntity): Boolean {
        val bukkitEntity = target.bukkitEntity
        if (bukkitEntity !is InventoryHolder) {
            return false
        } else {
            val inventory = bukkitEntity.inventory
            return this.checkInventory(inventory)
        }
    }

    override fun check(target: AbstractLocation): Boolean {
        val bukkitLocation = BukkitAdapter.adapt(target)
        val block = bukkitLocation.block
        val state = block.state
        if (state !is Container) {
            return false
        } else {
            val inventory = state.inventory
            return this.checkInventory(inventory)
        }
    }

    private fun checkInventory(inventory: Inventory): Boolean {
        var count = 0
        val contents = inventory.contents

        for (item in contents) {
            if (item != null && nekoo.isNekoStack(item) && nekoo.getNekoItemId(item) == itemKey) {
                count += item.amount
            }
        }

        return this.amount.equals(count)
    }
}