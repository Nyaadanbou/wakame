package cc.mewcraft.wakame

import cc.mewcraft.wakame.adventure.adventureModule
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
import cc.mewcraft.wakame.molang.molangModule
import cc.mewcraft.wakame.pack.packModule
import cc.mewcraft.wakame.packet.packetModule
import cc.mewcraft.wakame.random2.randomModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.reforge.reforgeModule
import cc.mewcraft.wakame.registry.registryModule
import cc.mewcraft.wakame.skill.skillModule
import cc.mewcraft.wakame.skin.skinModule
import cc.mewcraft.wakame.test.testModule
import cc.mewcraft.wakame.user.userModule
import me.lucko.helper.plugin.KExtendedJavaPlugin
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin

/**
 * 直接访问 [WakamePlugin] 的实例.
 */
val NEKO_PLUGIN: WakamePlugin
    get() = WakamePluginHolder.INSTANCE

class WakamePlugin : KExtendedJavaPlugin() {

    override suspend fun load() {
        WakamePluginHolder.INSTANCE = this

        // Start Koin container
        startKoin {

            // Define modules
            modules(
                // main module
                wakameModule(this@WakamePlugin),

                // sub modules (by alphabet order)
                adventureModule(),
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
                molangModule(),
                packetModule(),
                packModule(),
                randomModule(),
                rarityModule(),
                reforgeModule(),
                registryModule(),
                skinModule(),
                skillModule(),
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

private object WakamePluginHolder {
    lateinit var INSTANCE: WakamePlugin
}
