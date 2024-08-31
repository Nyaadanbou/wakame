package cc.mewcraft.wakame.util

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.io.IOException
import java.nio.file.*
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class PathChangeWatcher(
    private val directory: Path,
    private val specificFile: Path,
    private val executor: Executor,
    private val onFileChange: (PathChangeWatcher) -> Unit
) {
    private var isStarted = false
    private var isStopped = AtomicBoolean(false)
    private var watchService: WatchService? = null

    fun watch() {
        if (isStarted) {
            throw IllegalStateException("Watcher already started")
        }
        executor.execute { watchInternal() }
        isStarted = true
        isStopped.set(false)
    }

    fun stop() {
        if (!isStarted) {
            throw IllegalStateException("Watcher not started")
        }
        isStopped.set(true)
        try {
            watchService?.close()
        } catch (e: IOException) {
            PathWatcherSupport.LOGGER.error("Error closing watch service", e)
        }
        isStarted = false
    }

    private fun watchInternal() {
        try {
            watchService = FileSystems.getDefault().newWatchService()
            watchService?.use { ws ->
                directory.register(
                    ws,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY
                )

                PathWatcherSupport.LOGGER.info("Watching directory $directory...")

                while (!isStopped.get()) {
                    val watchKey = try {
                        ws.take()
                    } catch (ex: InterruptedException) {
                        PathWatcherSupport.LOGGER.error("Failed to watch $directory, watch service interrupted", ex)
                        return
                    } catch (ex: ClosedWatchServiceException) {
                        PathWatcherSupport.LOGGER.info("Watch service closed, stopping watcher for $directory")
                        return
                    }

                    for (event in watchKey.pollEvents()) {
                        val kind = event.kind()
                        val context = event.context()

                        val fileName = event.context() as Path
                        val resolvedPath: Path = directory.resolve(fileName)

                        PathWatcherSupport.LOGGER.info("$kind: $context")

                        if (resolvedPath == specificFile) {
                            PathWatcherSupport.LOGGER.info("检测到特定文件的事件 (" + kind.name() + "), 执行特定逻辑...");
                            onFileChange.invoke(this)
                        }
                    }

                    val valid = watchKey.reset()
                    if (!valid) {
                        break
                    }
                }
            }
        } catch (e: IOException) {
            PathWatcherSupport.LOGGER.error("Failed to watch $directory", e)
        }
    }
}

private object PathWatcherSupport : KoinComponent {
    val LOGGER: Logger by inject()
}
