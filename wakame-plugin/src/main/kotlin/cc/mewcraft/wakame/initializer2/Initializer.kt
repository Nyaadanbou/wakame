@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer2

import cc.mewcraft.wakame.NEKO
import cc.mewcraft.wakame.api.event.NekoLoadDataEvent
import cc.mewcraft.wakame.initializer2.Initializer.start
import cc.mewcraft.wakame.util.data.JarUtils
import cc.mewcraft.wakame.util.registerSuspendingEvents
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.*
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.slf4j.Logger
import java.io.File
import java.lang.reflect.InvocationTargetException

internal object Initializer : Listener, KoinComponent {
    private val logger: Logger by inject()

    private val initializables: HashSet<Initializable> = HashSet<Initializable>()
    private val disableables: HashSet<DisableableFunction> = HashSet<DisableableFunction>()

    private val initPreWorld: MutableGraph<Initializable> = GraphBuilder.directed().allowsSelfLoops(false).build()
    private val initPostWorld: MutableGraph<Initializable> = GraphBuilder.directed().allowsSelfLoops(false).build()
    private val disable: MutableGraph<DisableableFunction> = GraphBuilder.directed().allowsSelfLoops(false).build()

    private lateinit var preWorldScope: CoroutineScope
    private var preWorldInitialized = false
    var isDone = false
        private set

    /**
     * Stats the initialization process.
     */
    fun start() = tryInit {
        collectAndRegisterRunnables(NEKO.nekooJar, this.javaClass.classLoader)

        initPreWorld()
    }

    private fun collectAndRegisterRunnables(file: File, classLoader: ClassLoader) {
        val (initializables, disableables) = collectRunnables(file, classLoader)
        addRunnables(initializables, disableables)
    }

    /**
     * Searches [file] and collects classes annotated by [InternalInit] and [Init] and functions
     * annotated by [InitFun] and [DisableFun] as [Initializables][Initializable] and [DisableableFunctions][DisableableFunction].
     */
    private fun collectRunnables(file: File, classLoader: ClassLoader): Pair<List<Initializable>, List<DisableableFunction>> {
        val initializables = ArrayList<Initializable>()
        val disableables = ArrayList<DisableableFunction>()
        val initializableClasses = HashMap<String, InitializableClass>()

        val result = JarUtils.findAnnotatedClasses(
            file,
            listOf(InternalInit::class, Init::class),
            listOf(InitFun::class, DisableFun::class)
        )

        val internalInits = result.classes[InternalInit::class] ?: emptyMap()
        val inits = result.classes[Init::class] ?: emptyMap()
        val initFuncs = result.functions[InitFun::class] ?: emptyMap()
        val disableFuncs = result.functions[DisableFun::class] ?: emptyMap()

        for ((className, annotations) in internalInits) {
            val clazz = InitializableClass.fromInternalAnnotation(classLoader, className, annotations.first())
            initializables += clazz
            initializableClasses[className] = clazz
        }
        for ((className, annotations) in inits) {
            val clazz = InitializableClass.fromAddonAnnotation(classLoader, className, annotations.first())
            initializables += clazz
            initializableClasses[className] = clazz
        }

        for ((className, annotatedFuncs) in initFuncs) {
            val clazz = initializableClasses[className]
                ?: throw IllegalStateException("Class $className is missing an init annotation!")

            for ((methodName, annotations) in annotatedFuncs) {
                initializables += InitializableFunction.fromInitAnnotation(clazz, methodName, annotations.first())
            }
        }

        for ((className, annotatedFuncs) in disableFuncs) {
            for ((methodName, annotations) in annotatedFuncs) {
                disableables += DisableableFunction.fromInitAnnotation(classLoader, className, methodName, annotations.first())
            }
        }

        return initializables to disableables
    }

    /**
     * Adds the given [Initializables][Initializable] and [DisableableFunctions][DisableableFunction] to the initialization process.
     *
     * This method can only be invoked during the pre-world initialization phase or before the [start] method is called.
     */
    private fun addRunnables(initializables: List<Initializable>, disableables: List<DisableableFunction>) {
        check(!preWorldInitialized) { "Cannot add additional callables after pre-world initialization!" }

        // add vertices
        for (initializable in initializables) {
            this.initializables += initializable
            when (initializable.stage) {
                InternalInitStage.PRE_WORLD -> initPreWorld.addNode(initializable)
                else -> initPostWorld.addNode(initializable)
            }
        }
        for (disableable in disableables) {
            this.disableables += disableable
            disable.addNode(disableable)
        }

        // add edges
        for (initializable in initializables) {
            initializable.loadDependencies(
                this.initializables,
                if (initializable.stage == InternalInitStage.PRE_WORLD) initPreWorld else initPostWorld
            )
        }
        for (disableable in disableables) {
            disableable.loadDependencies(this.disableables, disable)
        }

        // launch initialization it if already started
        if (::preWorldScope.isInitialized) {
            for (initializable in initializables) {
                if (initializable.stage != InternalInitStage.PRE_WORLD)
                    continue

                launch(preWorldScope, initializable, initPreWorld)
            }
        }
    }

    /**
     * Stats the pre-world initialization process.
     */
    private fun initPreWorld() = runBlocking {
        registerSuspendingEvents() // register `this` listener

        tryInit {
            coroutineScope {
                preWorldScope = this
                launchAll(this, initPreWorld)
            }
        }

        preWorldInitialized = true
    }

    /**
     * Starts the post-world initialization process.
     */
    private fun initPostWorld() = runBlocking {
        tryInit {
            coroutineScope {
                launchAll(this, initPostWorld)
            }
        }

        isDone = true
        NekoLoadDataEvent().callEvent()

        logger.info("Done loading")
    }

    /**
     * Launches all vertices of [graph] in the given [scope].
     */
    private fun <T : InitializerRunnable<T>> launchAll(scope: CoroutineScope, graph: Graph<T>) {
        for (initializable in graph.nodes()) {
            launch(scope, initializable, graph)
        }
    }

    /**
     * Launches [runnable] of [graph] in the given [scope].
     */
    private fun <T : InitializerRunnable<T>> launch(
        scope: CoroutineScope,
        runnable: T,
        graph: Graph<T>,
    ) {
        scope.launch {
            // await dependencies, which may increase during wait
            var prevDepsSize = 0
            var deps: List<Deferred<*>> = emptyList()

            fun findDependencies(): List<Deferred<*>> {
                deps = graph.predecessors(runnable)
                    .map { it.completion }
                return deps
            }

            while (prevDepsSize != findDependencies().size) {
                prevDepsSize = deps.size
                deps.awaitAll()
            }

            // run in preferred context
            withContext(runnable.dispatcher ?: scope.coroutineContext) {
                logger.info(runnable.toString())

                runnable.run()
            }
        }
    }

    /**
     * Wraps [run] in a try-catch block with error logging specific to initialization.
     * Returns whether the initialization was successful, and also shuts down the server if it wasn't.
     */
    private inline fun tryInit(run: () -> Unit) {
        try {
            run()
        } catch (t: Throwable) {
            val cause = if (t is InvocationTargetException) t.targetException else t
            if (cause is InitializationException) {
                logger.error(cause.message)
            } else {
                logger.error("An exception occurred during initialization", cause)
            }

            logger.error("Initialization failure")
            (LogManager.getContext(false) as LoggerContext).stop() // flush log messages
            Runtime.getRuntime().halt(-1) // force-quit process to prevent further errors
        }
    }

    /**
     * Disables all [Initializables][Initializable] in the reverse order that they were initialized in.
     */
    fun disable() = runBlocking {
        if (isDone) {
            coroutineScope { launchAll(this, disable) }
        } else {
            logger.warn("Skipping disable phase due to incomplete initialization")
        }
    }

    @EventHandler
    private fun handleServerStarted(event: ServerLoadEvent) {
        if (preWorldInitialized) {
            initPostWorld()
        } else logger.warn("Skipping post world initialization")
    }

}