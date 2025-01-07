package cc.mewcraft.wakame.registry

import cc.mewcraft.wakame.initializer2.Init
import cc.mewcraft.wakame.initializer2.InitFun
import cc.mewcraft.wakame.initializer2.InitStage
import cc.mewcraft.wakame.rarity.Rarity
import cc.mewcraft.wakame.reloader.Reload
import cc.mewcraft.wakame.reloader.ReloadFun
import cc.mewcraft.wakame.util.krequire
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.spongepowered.configurate.yaml.YamlConfigurationLoader

@Init(
    stage = InitStage.PRE_WORLD,
)
@Reload
object RarityRegistry : KoinComponent, BiKnot<String, Rarity, Byte> {

    override val INSTANCES: Registry<String, Rarity> = SimpleRegistry()
    override val BI_LOOKUP: BiRegistry<String, Byte> = SimpleBiRegistry()

    /**
     * The default rarity. By design, it should be the most common rarity.
     */
    val DEFAULT: Rarity by lazy { INSTANCES.values.first() }

    @InitFun
    fun init() {
        loadDataIntoRegistry()
    }

    @ReloadFun
    fun reload() {
        loadDataIntoRegistry()
    }

    private fun loadDataIntoRegistry() {
        INSTANCES.clear()
        BI_LOOKUP.clear()

        val root = get<YamlConfigurationLoader>(named(RARITY_GLOBAL_CONFIG_LOADER)).load()
        root.node("rarities").childrenMap().forEach { (_, n) ->
            val rarity = n.krequire<Rarity>()
            INSTANCES.register(rarity.uniqueId, rarity)
            BI_LOOKUP.register(rarity.uniqueId, rarity.binaryId)
        }
    }
}