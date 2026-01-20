package cc.mewcraft.extracontexts.common.runtime

import org.slf4j.LoggerFactory
import xyz.jpenilla.gremlin.runtime.DependencyCache
import xyz.jpenilla.gremlin.runtime.DependencyResolver
import xyz.jpenilla.gremlin.runtime.DependencySet
import xyz.jpenilla.gremlin.runtime.logging.Slf4jGremlinLogger
import java.nio.file.Path

object ExtraContextsDependencies {

    private const val DEPENDENCIES_RESOURCE = "extracontexts-dependencies.txt"

    @JvmStatic
    fun resolve(cacheDir: Path): Set<Path> {
        val deps = DependencySet.readFromClasspathResource(ExtraContextsDependencies::class.java.classLoader, DEPENDENCIES_RESOURCE)
        val cache = DependencyCache(cacheDir)
        val logger = LoggerFactory.getLogger(ExtraContextsDependencies::class.java.simpleName)
        val files: Set<Path>
        DependencyResolver(Slf4jGremlinLogger(logger)).use { downloader ->
            files = downloader.resolve(deps, cache).jarFiles()
        }
        cache.cleanup()
        return files
    }
}