package cc.mewcraft.wakame.hook.impl.mythicmobs.placeholder

import cc.mewcraft.wakame.entity.attribute.AttributeMapAccess
import cc.mewcraft.wakame.entity.attribute.Attributes
import cc.mewcraft.wakame.integration.playermana.PlayerManaIntegration
import io.lumine.mythic.bukkit.MythicBukkit
import io.lumine.mythic.core.skills.placeholders.Placeholder

object KoishPlaceholders {

    fun register(api: MythicBukkit) {
        // Caster placeholders
        api.placeholderManager.register("caster.mana", Placeholder.meta { meta, arg ->
            meta?.caster?.entity?.uniqueId?.let(PlayerManaIntegration::getMana)?.toString() ?: "0"
        })
        api.placeholderManager.register("caster.attribute", Placeholder.meta { meta, arg ->
            val entity = meta?.caster?.entity?.bukkitEntity ?: return@meta "0"
            val attributeType = arg?.let(Attributes::get) ?: return@meta "0"
            AttributeMapAccess.INSTANCE.get(entity).getOrNull()?.getValue(attributeType)?.toString() ?: return@meta "0"
        })

        // Trigger placeholders
        api.placeholderManager.register("trigger.mana", Placeholder.meta { meta, arg ->
            meta?.trigger?.uniqueId?.let(PlayerManaIntegration::getMana)?.toString() ?: "0"
        })
        api.placeholderManager.register("trigger.attribute", Placeholder.meta { meta, arg ->
            val entity = meta?.trigger?.bukkitEntity ?: return@meta "0"
            val attributeType = arg?.let(Attributes::get) ?: return@meta "0"
            AttributeMapAccess.INSTANCE.get(entity).getOrNull()?.getValue(attributeType)?.toString() ?: return@meta "0"
        })

        // Target placeholders
        api.placeholderManager.register("target.mana", Placeholder.target { meta, target, arg ->
            target?.bukkitEntity?.uniqueId?.let(PlayerManaIntegration::getMana)?.toString() ?: "0"
        })
        api.placeholderManager.register("target.attribute", Placeholder.target { meta, target, arg ->
            val entity = target?.bukkitEntity ?: return@target "0"
            val attributeType = arg?.let(Attributes::get) ?: return@target "0"
            AttributeMapAccess.INSTANCE.get(entity).getOrNull()?.getValue(attributeType)?.toString() ?: return@target "0"
        })
    }
}