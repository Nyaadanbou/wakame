package cc.mewcraft.extracontexts.paper

import cc.mewcraft.extracontexts.common.runtime.ExtraContextsDependencies
import io.papermc.paper.plugin.loader.PluginClasspathBuilder
import io.papermc.paper.plugin.loader.PluginLoader
import xyz.jpenilla.gremlin.runtime.platformsupport.PaperClasspathAppender

class ExtraContextsPaperLoader : PluginLoader {

    override fun classloader(classpath: PluginClasspathBuilder) {
        PaperClasspathAppender(classpath).append(
            ExtraContextsDependencies.resolve(classpath.context.dataDirectory.resolve("libs"))
        )
    }
}