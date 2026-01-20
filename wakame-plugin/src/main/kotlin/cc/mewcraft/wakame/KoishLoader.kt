package cc.mewcraft.wakame

import cc.mewcraft.wakame.KoishDependencies.resolve
import io.papermc.paper.plugin.loader.PluginClasspathBuilder
import io.papermc.paper.plugin.loader.PluginLoader
import xyz.jpenilla.gremlin.runtime.platformsupport.PaperClasspathAppender

internal class KoishLoader : PluginLoader {

    override fun classloader(classpath: PluginClasspathBuilder) {
        PaperClasspathAppender(classpath).append(
            resolve(classpath.getContext().getDataDirectory().resolve("libs"))
        )
    }
}
