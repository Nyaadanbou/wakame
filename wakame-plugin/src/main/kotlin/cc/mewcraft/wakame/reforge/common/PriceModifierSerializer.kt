package cc.mewcraft.wakame.reforge.common

import cc.mewcraft.wakame.config.configurate.TypeSerializer
import cc.mewcraft.wakame.util.require
import org.spongepowered.configurate.ConfigurationNode
import java.lang.reflect.Type
import kotlin.reflect.typeOf

object PriceModifierSerializer : TypeSerializer<PriceModifier> {
    private val TYPE_MAPPINGS = mapOf(
        DamagePriceModifier.NAME to typeOf<DamagePriceModifier>(),
        LevelPriceModifier.NAME to typeOf<LevelPriceModifier>(),
        RarityPriceModifier.NAME to typeOf<RarityPriceModifier>(),
        MergingPenaltyPriceModifier.NAME to typeOf<MergingPenaltyPriceModifier>(),
        ModdingPenaltyPriceModifier.NAME to typeOf<ModdingPenaltyPriceModifier>(),
        RerollingPenaltyPriceModifier.NAME to typeOf<RerollingPenaltyPriceModifier>(),
    )

    override fun deserialize(type: Type, node: ConfigurationNode): PriceModifier {
        val nodeKey = node.key().toString()
        val kType = TYPE_MAPPINGS[nodeKey] ?: throw IllegalArgumentException("Unknown price modifier type: $nodeKey")
        return node.require(kType)
    }
}