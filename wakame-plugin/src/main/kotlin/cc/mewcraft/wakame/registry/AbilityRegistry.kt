package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.ability.PlainAbilityData
import cc.mewcraft.wakame.ability.SchemaAbilityData
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.NbtCoreDataDecoder
import cc.mewcraft.wakame.item.NbtCoreDataEncoder
import cc.mewcraft.wakame.item.SchemaCoreDataBaker
import cc.mewcraft.wakame.item.SchemaCoreDataBuilder
import me.lucko.helper.nbt.ShadowTagType
import me.lucko.helper.shadows.nbt.CompoundShadowTag
import net.kyori.adventure.key.Key
import org.spongepowered.configurate.ConfigurationNode

object AbilityRegistry : Initializable {

    /**
     * The key of the empty ability.
     */
    val EMPTY_KEY: Key = Key.key(NekoNamespaces.ABILITY, "empty")

    val schemaCoreDataBuilder: MutableMap<Key, SkillSchemaCoreDataBuilder> = HashMap()
    val schemaCoreDataBaker: MutableMap<Key, SkillSchemaCoreDataBaker> = HashMap()
    val nbtCoreDataEncoder: MutableMap<Key, SkillNbtCoreDataEncoder> = HashMap()
    val nbtCoreDataDecoder: MutableMap<Key, SkillNbtCoreDataDecoder> = HashMap()

    /**
     * Starts building an ability facade registry.
     */
    fun build(key: String, type: ShadowTagType) {
        // placeholder code
    }

    override fun onPreWorld() {
        // placeholder code
    }
}

fun interface SkillSchemaCoreDataBuilder : SchemaCoreDataBuilder<ConfigurationNode, SchemaAbilityData>
fun interface SkillSchemaCoreDataBaker : SchemaCoreDataBaker<SchemaAbilityData, PlainAbilityData>
fun interface SkillNbtCoreDataEncoder : NbtCoreDataEncoder<PlainAbilityData, CompoundShadowTag>
fun interface SkillNbtCoreDataDecoder : NbtCoreDataDecoder<CompoundShadowTag, PlainAbilityData>
