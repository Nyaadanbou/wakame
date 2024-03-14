package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.PreWorldDependency
import cc.mewcraft.wakame.initializer.ReloadDependency
import cc.mewcraft.wakame.kizami.Kizami
import cc.mewcraft.wakame.kizami.KizamiEffect
import cc.mewcraft.wakame.kizami.KizamiInstance
import cc.mewcraft.wakame.util.NekoConfigurationLoader
import cc.mewcraft.wakame.util.NekoConfigurationNode
import cc.mewcraft.wakame.util.requireKt
import com.google.common.collect.Table
import com.google.common.collect.Tables
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named

@PreWorldDependency(runBefore = [AbilityRegistry::class, AttributeRegistry::class])
@ReloadDependency(runBefore = [AbilityRegistry::class, AttributeRegistry::class])
object KizamiRegistry : KoinComponent, Initializable,
    Registry<String, Kizami> by HashMapRegistry(),
    BiMapRegistry<String, Byte> by HashBiMapRegistry() {

    /**
     * Root configuration node.
     */
    private val root: NekoConfigurationNode
        get() = get<NekoConfigurationLoader>(named(KIZAMI_CONFIG_LOADER)).load()

    /**
     * Table used to query the kizami effect, given kizami and amount.
     */
    private val table: Table<Kizami, Int, KizamiEffect> = Tables.newCustomTable(Reference2ObjectOpenHashMap<Kizami, Map<Int, KizamiEffect>>()) { Int2ObjectOpenHashMap() }

    private fun loadConfiguration() {
        clearBoth()

        root.node("kizami").childrenMap().forEach { (_, childNode) ->
            val instance = childNode.requireKt<KizamiInstance>()
            val reference = instance.kizami

            // register kizami reference
            registerBothMapping(reference.key, reference)

            // register kizami instance
            instance.effect.forEach { (amount, effect) ->
                table.put(reference, amount, effect)
            }
        }
    }

    override fun onPreWorld() {
        loadConfiguration()
    }

    override fun onReload() {
        loadConfiguration()
    }
}