package cc.mewcraft.wakame

import cc.mewcraft.wakame.test.TestListener
import me.lucko.helper.plugin.KExtendedJavaPlugin
import org.koin.core.component.KoinComponent
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin

class WakamePlugin : KoinComponent, KExtendedJavaPlugin() {
    override suspend fun enable() {
        startKoin {
            modules(wakameModule)
        }

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

        // Register listeners
        registerTerminableListener(TestListener()).bindWith(this)
    }

    override suspend fun disable() {
        stopKoin()
    }
}