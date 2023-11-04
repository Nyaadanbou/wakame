package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.generic.*

object AttributeRegistry {
    private val registry: MutableMap<String, () -> Attribute> = HashMap()

    @Suppress("UNCHECKED_CAST")
    fun <T : Attribute> getDefault(key: String): T {
        val supplier = checkNotNull(registry[key]) { "Cannot find attribute with $key" }
        return supplier.invoke() as T // Return a new instance
    }

    fun <T : Attribute> addDefault(supplier: () -> T) {
        supplier().apply { registry[this.key().value()] = supplier }
    }

    init {
        // Add all base attributes with default value
        addDefault { AttackDamage(0, 0) } // directly replacement
        addDefault { AttackSpeed(0) }
        addDefault { CriticalStrikeChance(0) }
        addDefault { CriticalStrikeRate(100) }
        addDefault { Defense(0) }
        addDefault { ElementAttackDamage("water", 0, 0) }
        addDefault { ElementAttackDamage("wind", 0, 0) }
        addDefault { ElementAttackRate("water", 0) }
        addDefault { ElementAttackRate("wind", 0) }
        addDefault { ElementEffectChance("water", 0) }
        addDefault { ElementEffectChance("wind", 0) }
        addDefault { DamageReductionRate(0) }
        addDefault { Lifesteal(0) }
        addDefault { ManaReduction(0) }
        addDefault { ManaRegeneration(1) } // directly replacement
        addDefault { MaximumHealth(20) } // directly replacement
        addDefault { MaximumMana(100) }
        addDefault { MovementSpeed(0) }
    }
}