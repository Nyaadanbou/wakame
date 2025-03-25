package cc.mewcraft.wakame.item2.config.datagen.impl

import cc.mewcraft.wakame.ability2.AbilityObject
import cc.mewcraft.wakame.ability2.meta.AbilityMeta
import cc.mewcraft.wakame.ability2.trigger.AbilityTrigger
import cc.mewcraft.wakame.ability2.trigger.AbilityTriggerVariant
import cc.mewcraft.wakame.item2.config.datagen.Context
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaEntry
import cc.mewcraft.wakame.item2.config.datagen.ItemMetaResult
import cc.mewcraft.wakame.item2.data.ItemDataTypes
import cc.mewcraft.wakame.molang.Expression
import cc.mewcraft.wakame.util.MojangStack
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.objectmapping.meta.Setting

@ConfigSerializable
data class MetaAbilityObject(
    @Setting("id")
    val meta: AbilityMeta,
    val trigger: AbilityTrigger?,
    val variant: AbilityTriggerVariant,
    val manaCost: Expression?,
) : ItemMetaEntry<AbilityObject> {

    override fun make(context: Context): ItemMetaResult<AbilityObject> {
        val abilityObject = AbilityObject(
            meta = meta,
            trigger = trigger,
            variant = variant,
            manaCost = manaCost,
        )
        return ItemMetaResult.of(abilityObject)
    }

    override fun write(value: AbilityObject, itemstack: MojangStack) {
        itemstack.ensureSetData(ItemDataTypes.ABILITY_OBJECT, value)
    }
}
