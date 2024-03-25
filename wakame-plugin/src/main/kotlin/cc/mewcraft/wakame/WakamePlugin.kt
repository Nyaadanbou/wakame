package cc.mewcraft.wakame

import cc.mewcraft.wakame.ability.abilityModule
import cc.mewcraft.wakame.attack.attackModule
import cc.mewcraft.wakame.attribute.attributeModule
import cc.mewcraft.wakame.crate.crateModule
import cc.mewcraft.wakame.damage.damageModule
import cc.mewcraft.wakame.display.displayModule
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.entity.entityModule
import cc.mewcraft.wakame.initializer.Initializer
import cc.mewcraft.wakame.initializer.initializerModule
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.level.levelModule
import cc.mewcraft.wakame.lookup.lookupModule
import cc.mewcraft.wakame.pack.packModule
import cc.mewcraft.wakame.packet.packetModule
import cc.mewcraft.wakame.user.userModule
import cc.mewcraft.wakame.random.randomModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.reference.referenceModule
import cc.mewcraft.wakame.reforge.reforgeModule
import cc.mewcraft.wakame.registry.registryModule
import cc.mewcraft.wakame.skin.skinModule
import cc.mewcraft.wakame.test.testModule
import me.lucko.helper.plugin.KExtendedJavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin

val NEKO_PLUGIN: WakamePlugin by lazy { GlobalContext.get().get<WakamePlugin>() }

class WakamePlugin : KoinComponent, KExtendedJavaPlugin() {

    override suspend fun load() {
        // Start Koin container
        startKoin {

            // Define modules
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
                entityModule(),
                initializerModule(),
                itemModule(),
                kizamiModule(),
                levelModule(),
                lookupModule(),
                packetModule(),
                packModule(),
                randomModule(),
                rarityModule(),
                referenceModule(),
                reforgeModule(),
                registryModule(),
                skinModule(),
                testModule(),
                userModule(),
            )
        }
    }

    override suspend fun enable() {
        Initializer.start()
    }

    override suspend fun disable() {
        Initializer.disable()
        stopKoin()
    }

}