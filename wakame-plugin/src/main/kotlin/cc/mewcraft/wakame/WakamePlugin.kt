package cc.mewcraft.wakame

import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.api.NekooApiProvider
import cc.mewcraft.wakame.attribute.attributeModule
import cc.mewcraft.wakame.compatibility.compatibilityModule
import cc.mewcraft.wakame.core.ItemXBootstrap
import cc.mewcraft.wakame.core.coreModule
import cc.mewcraft.wakame.crate.crateModule
import cc.mewcraft.wakame.damage.damageModule
import cc.mewcraft.wakame.display2.display2Module
import cc.mewcraft.wakame.economy.economyModule
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.enchantment.enchantmentModule
import cc.mewcraft.wakame.entity.entityModule
import cc.mewcraft.wakame.gui.guiModule
import cc.mewcraft.wakame.initializer.Initializer
import cc.mewcraft.wakame.initializer.initializerModule
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.lang.langModule
import cc.mewcraft.wakame.level.levelModule
import cc.mewcraft.wakame.lookup.lookupModule
import cc.mewcraft.wakame.molang.molangModule
import cc.mewcraft.wakame.pack.packModule
import cc.mewcraft.wakame.packet.packetModule
import cc.mewcraft.wakame.player.playerModule
import cc.mewcraft.wakame.random3.randomModule
import cc.mewcraft.wakame.rarity.rarityModule
import cc.mewcraft.wakame.recipe.recipeModule
import cc.mewcraft.wakame.reforge.reforgeModule
import cc.mewcraft.wakame.registry.registryModule
import cc.mewcraft.wakame.resource.resourceModule
import cc.mewcraft.wakame.skill.skillModule
import cc.mewcraft.wakame.skin.skinModule
import cc.mewcraft.wakame.station.stationModule
import cc.mewcraft.wakame.test.testModule
import cc.mewcraft.wakame.tick.tickModule
import cc.mewcraft.wakame.user.userModule
import cc.mewcraft.wakame.world.worldModule
import me.lucko.helper.plugin.KExtendedJavaPlugin
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin

/**
 * 直接访问 [WakamePlugin] 的实例.
 */
val NEKO_PLUGIN: WakamePlugin
    get() = requireNotNull(WakamePlugin.instance) { "WakamePlugin is not initialized yet" }

class WakamePlugin : KExtendedJavaPlugin() {
    companion object {
        var instance: WakamePlugin? = null
    }

    override suspend fun load() {
        instance = this

        // Start Koin container
        startKoin {

            // Define modules
            modules(
                // main module
                wakameModule(this@WakamePlugin),

                // sub modules (by alphabet order)
                adventureModule(),
                attributeModule(),
                compatibilityModule(),
                coreModule(),
                crateModule(),
                damageModule(),
                display2Module(),
                economyModule(),
                elementModule(),
                enchantmentModule(),
                entityModule(),
                guiModule(),
                initializerModule(),
                itemModule(),
                kizamiModule(),
                langModule(),
                levelModule(),
                lookupModule(),
                molangModule(),
                packetModule(),
                packModule(),
                playerModule(),
                randomModule(),
                rarityModule(),
                recipeModule(),
                reforgeModule(),
                registryModule(),
                resourceModule(),
                skinModule(),
                skillModule(),
                stationModule(),
                testModule(),
                tickModule(),
                userModule(),
                worldModule(),
            )
        }

        // Initialize ItemX
        ItemXBootstrap.init()
    }

    override suspend fun enable() {
        // 初始化插件
        Initializer.start()

        // 注册 Nekoo API
        NekooProvider.register(NekooApiProvider())
    }

    override suspend fun disable() {
        Initializer.disable()
        stopKoin()
        instance = null
        NekooProvider.unregister()
    }
}