package cc.mewcraft.wakame.skill

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.registry.Registry
import cc.mewcraft.wakame.registry.SimpleRegistry
import cc.mewcraft.wakame.skill.type.RemovePotionEffect
import kotlin.reflect.KType
import kotlin.reflect.typeOf

object SkillTypes : Initializable {
    val INSTANCE: Registry<String, KType> = SimpleRegistry()

    override fun onPreWorld() {
        INSTANCE += "remove_potion_effect" to typeOf<RemovePotionEffect>()
    }
}