package cc.mewcraft.wakame.world.kizami

import cc.mewcraft.wakame.ability.PlayerAbility
import cc.mewcraft.wakame.ability.character.CasterAdapter
import cc.mewcraft.wakame.attribute.Attribute
import cc.mewcraft.wakame.attribute.AttributeModifier
import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.user.User
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type

interface KizamiEffect {
    fun apply(user: User<*>)
    fun remove(user: User<*>)
}

internal object KizamiEffectEmpty : KizamiEffect {
    override fun apply(user: User<*>) = Unit
    override fun remove(user: User<*>) = Unit
}

internal class KizamiEffectPlayerAbility(
    private val ability: PlayerAbility,
) : KizamiEffect {
    override fun apply(user: User<*>) {
        ability.recordBy(CasterAdapter.adapt(user), null, null)
    }

    override fun remove(user: User<*>) {
        // do nothing
    }
}

internal class KizamiEffectAttributeModifier(
    private val attributeModifiers: Map<Attribute, AttributeModifier>,
) : KizamiEffect {
    override fun apply(user: User<*>) {
        attributeModifiers.forEach { (attribute, modifier) -> user.attributeMap.getInstance(attribute)?.addTransientModifier(modifier) }
    }

    override fun remove(user: User<*>) {
        attributeModifiers.forEach { (attribute, modifier) -> user.attributeMap.getInstance(attribute)?.removeModifier(modifier) }
    }
}

internal object KizamiEffectPlayerAbilitySerializer : TypeSerializer<KizamiEffectPlayerAbility> {
    override fun deserialize(type: Type, node: ConfigurationNode): KizamiEffectPlayerAbility {
        TODO("Not yet implemented")
    }
}