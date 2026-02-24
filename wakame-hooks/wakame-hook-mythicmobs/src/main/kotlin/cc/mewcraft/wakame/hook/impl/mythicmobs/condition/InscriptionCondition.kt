package cc.mewcraft.wakame.hook.impl.mythicmobs.condition

import cc.mewcraft.wakame.entity.player.kizamiContainer
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.registry.BuiltInRegistries
import cc.mewcraft.wakame.registry.entry.RegistryEntry
import io.lumine.mythic.api.config.MythicLineConfig
import io.lumine.mythic.api.skills.SkillMetadata
import io.lumine.mythic.api.skills.ThreadSafetyLevel
import io.lumine.mythic.api.skills.conditions.ISkillMetaCondition
import io.lumine.mythic.bukkit.utils.numbers.RangedInt
import io.lumine.mythic.core.skills.SkillCondition
import org.bukkit.entity.Player

class InscriptionCondition(
    line: String,
    mlc: MythicLineConfig,
) : SkillCondition(line), ISkillMetaCondition {

    init {
        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY
    }

    private val inscriptionType: RegistryEntry<Kizami> =
        mlc.getString(arrayOf("type", "t"), null)
            ?.let { typeString -> BuiltInRegistries.KIZAMI.getEntry(typeString) }
            ?: throw IllegalStateException("Failed to parse inscription type from line: $line")
    private val inscriptionAmount: RangedInt =
        mlc.getString(arrayOf("amount", "a"), null, *emptyArray())
            ?.let { RangedInt(it) }
            ?: throw IllegalStateException("Failed to parse inscription amount from line: $line")

    override fun check(meta: SkillMetadata): Boolean {
        val player = meta.caster.entity.bukkitEntity as? Player ?: return false
        val map = player.kizamiContainer
        val type = inscriptionType
        val testAmount = inscriptionAmount
        val actualAmount = map.getAmount(type)
        return testAmount.equals(actualAmount)
    }
}