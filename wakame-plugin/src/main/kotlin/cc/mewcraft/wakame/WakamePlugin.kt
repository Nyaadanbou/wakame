package cc.mewcraft.wakame

import cc.mewcraft.wakame.ability.abilityModule
import cc.mewcraft.wakame.attack.attackModule
import cc.mewcraft.wakame.attribute.attributeModule
import cc.mewcraft.wakame.crate.crateModule
import cc.mewcraft.wakame.damage.damageModule
import cc.mewcraft.wakame.display.displayModule
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.initializer.initializerModule
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.lookup.lookupModule
import cc.mewcraft.wakame.pack.packModule
import cc.mewcraft.wakame.random.randomModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.reference.referenceModule
import cc.mewcraft.wakame.reforge.reforgeModule
import cc.mewcraft.wakame.registry.registryModule
import cc.mewcraft.wakame.skin.skinModule
import cc.mewcraft.wakame.test.TestListener
import cc.mewcraft.wakame.test.testModule
import me.lucko.helper.plugin.KExtendedJavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin

class WakamePlugin : KoinComponent, KExtendedJavaPlugin() {
    override suspend fun load() {

    }

    override suspend fun enable() {
        // Save default config files
        saveDefaultConfig()
        saveResourceRecursively("crates")
        saveResourceRecursively("items")
        saveResourceRecursively("skills")
        saveResource("attributes.yml")
        saveResource("elements.yml")
        saveResource("categories.yml")
        saveResource("kizami.yml")
        saveResource("levels.yml")
        saveResource("projectiles.yml")
        saveResource("rarities.yml")
        saveResource("renderer.yml")
        saveResource("skins.yml")

        // Start Koin container
        startKoin {
            modules(
                // main module
                wakameModule(this@WakamePlugin),

                // sub modules (by alphabet order)
                abilityModule(),
                attackModule(),
                attributeModule(),
                crateModule(),
                damageModule(),
                displayModule(),
                elementModule(),
                initializerModule(),
                itemModule(),
                kizamiModule(),
                lookupModule(),
                packModule(),
                randomModule(),
                rarityModule(),
                referenceModule(),
                reforgeModule(),
                registryModule(),
                skinModule(),
                testModule()
            )
        }

        // Register listeners
        registerTerminableListener(get<TestListener>()).bindWith(this)
    }

    override suspend fun disable() {
        stopKoin()
    }
}