package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.NekoNamespaces
import cc.mewcraft.wakame.annotation.InternalApi
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.item.SchemeBaker
import cc.mewcraft.wakame.item.SchemeBuilder
import cc.mewcraft.wakame.item.ShadowTagDecoder
import cc.mewcraft.wakame.item.ShadowTagEncoder
import me.lucko.helper.nbt.ShadowTagType
import net.kyori.adventure.key.Key

object AbilityRegistry : Initializable {

    /**
     * The key of the empty ability.
     */
    val EMPTY_KEY: Key = Key.key(NekoNamespaces.ABILITY, "empty")

    @InternalApi
    val schemeBuilderRegistry: MutableMap<Key, SchemeBuilder> = hashMapOf()

    @InternalApi
    val schemeBakerRegistry: MutableMap<Key, SchemeBaker> = hashMapOf()

    @InternalApi
    val shadowTagEncoder: MutableMap<Key, ShadowTagEncoder> = hashMapOf()

    @InternalApi
    val shadowTagDecoder: MutableMap<Key, ShadowTagDecoder> = hashMapOf()

    /**
     * Starts building an ability facade registry.
     */
    fun build(key: String, type: ShadowTagType) {
    }

    override fun onPreWorld() {
    }
}