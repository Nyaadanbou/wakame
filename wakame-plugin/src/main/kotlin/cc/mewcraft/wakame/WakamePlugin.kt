@file:Suppress(
    "UnstableApiUsage", "HasPlatformType"
)

package cc.mewcraft.wakame

import cc.mewcraft.wakame.ability.abilityModule
import cc.mewcraft.wakame.adventure.adventureModule
import cc.mewcraft.wakame.api.ApiNekooProvider
import cc.mewcraft.wakame.api.NekooProvider
import cc.mewcraft.wakame.attribute.attributeModule
import cc.mewcraft.wakame.core.ItemXBootstrap
import cc.mewcraft.wakame.core.coreModule
import cc.mewcraft.wakame.craftingstation.stationModule
import cc.mewcraft.wakame.crate.crateModule
import cc.mewcraft.wakame.damage.damageModule
import cc.mewcraft.wakame.display2.display2Module
import cc.mewcraft.wakame.ecs.ecsModule
import cc.mewcraft.wakame.element.elementModule
import cc.mewcraft.wakame.enchantment.enchantmentModule
import cc.mewcraft.wakame.entity.entityModule
import cc.mewcraft.wakame.gui.guiModule
import cc.mewcraft.wakame.initializer2.Initializer
import cc.mewcraft.wakame.initializer2.initializer2Module
import cc.mewcraft.wakame.integration.integrationModule
import cc.mewcraft.wakame.item.itemModule
import cc.mewcraft.wakame.kizami.kizamiModule
import cc.mewcraft.wakame.lang.langModule
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
import cc.mewcraft.wakame.skin.skinModule
import cc.mewcraft.wakame.test.testModule
import cc.mewcraft.wakame.user.userModule
import cc.mewcraft.wakame.util.RunningEnvironment
import cc.mewcraft.wakame.world.worldModule
import me.lucko.helper.plugin.KExtendedJavaPlugin
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.bukkit.Bukkit
import org.bukkit.Server
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin

val NEKO: WakamePlugin
    get() = requireNotNull(WakamePlugin.instance) { "plugin is not initialized yet" }

val LOGGER: ComponentLogger
    get() = if (RunningEnvironment.PRODUCTION.isRunning()) {
        NEKO.componentLogger
    } else {
        ComponentLogger.logger("Test")
    }

val SERVER: Server
    get() = Bukkit.getServer()

class WakamePlugin : KExtendedJavaPlugin() {
    companion object {
        var instance: WakamePlugin? = null
    }

    val version = pluginMeta.version
    val nekooJar = file

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
                coreModule(),
                crateModule(),
                damageModule(),
                display2Module(),
                ecsModule(),
                elementModule(),
                enchantmentModule(),
                entityModule(),
                guiModule(),
                initializer2Module(),
                integrationModule(),
                itemModule(),
                kizamiModule(),
                langModule(),
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
                abilityModule(),
                stationModule(),
                testModule(),
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
        NekooProvider.register(ApiNekooProvider())
    }

    override suspend fun disable() {
        Initializer.disable()
        stopKoin()
        instance = null
        NekooProvider.unregister()
    }
}