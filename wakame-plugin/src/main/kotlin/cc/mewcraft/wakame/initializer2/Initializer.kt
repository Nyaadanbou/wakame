@file:Suppress("UnstableApiUsage")

package cc.mewcraft.wakame.initializer2

import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.NEKO
import cc.mewcraft.wakame.api.event.NekoLoadDataEvent
import cc.mewcraft.wakame.dependency.dependencyLoaderSupport
import cc.mewcraft.wakame.initializer2.Initializer.start
import cc.mewcraft.wakame.initializer2.InitializerSupport.tryInit
import cc.mewcraft.wakame.util.data.JarUtils
import cc.mewcraft.wakame.util.registerEvents
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.ServerLoadEvent
import java.io.File

internal object Initializer : Listener {

    private val initializables: HashSet<Initializable> = HashSet<Initializable>()
    private val disableables: HashSet<Disableable> = HashSet<Disableable>()

    private val initPreWorld: MutableGraph<Initializable> = GraphBuilder.directed().allowsSelfLoops(false).build()
    private val initPostWorld: MutableGraph<Initializable> = GraphBuilder.directed().allowsSelfLoops(false).build()
    private val disable: MutableGraph<Disableable> = GraphBuilder.directed().allowsSelfLoops(false).build()

    private lateinit var preWorldScope: CoroutineScope
    private var preWorldInitialized = false

    var isDone = false
        private set

    /**
     * Stats the initialization process.
     */
    fun start() = tryInit {
        registerEvents()

        collectAndRegisterRunnables(NEKO.nekooJar, this.javaClass.classLoader)

        // TODO introduce Addon System
        // for (addon in AddonBootstrapper.addons) {
        //     collectAndRegisterRunnables(addon.file, addon.javaClass.classLoader)
        // }

        initPreWorld()
    }

    private fun collectAndRegisterRunnables(file: File, classLoader: ClassLoader) {
        val (initializables, disableables) = collectRunnables(file, classLoader)
        addRunnables(initializables, disableables)
    }

    /**
     * Searches [file] and collects classes annotated by [InternalInit] and [Init] and functions annotated by
     * [InitFun] and [DisableFun] as [Initializables][Initializable] and [DisableableFunctions][Disableable].
     */
    private fun collectRunnables(file: File, classLoader: ClassLoader): Pair<List<Initializable>, List<Disableable>> {
        val initializables = ArrayList<Initializable>()
        val disableables = ArrayList<Disableable>()
        val initializableClasses = HashMap<String, InitializableClass>()

        val result = JarUtils.findAnnotatedClasses(
            file,
            listOf(InternalInit::class, Init::class),
            listOf(InitFun::class, DisableFun::class)
        )

        val internalInits = result.classes[InternalInit::class] ?: emptyMap()
        val inits = result.classes[Init::class] ?: emptyMap()
        val initFunctions = result.functions[InitFun::class] ?: emptyMap()
        val disableFunctions = result.functions[DisableFun::class] ?: emptyMap()

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

        for ((className, annotatedFuncs) in initFunctions) {
            val clazz = initializableClasses[className]
                ?: throw IllegalStateException("Class $className is missing an init annotation!")

            for ((methodName, annotations) in annotatedFuncs) {
                initializables += InitializableFunction.fromInitAnnotation(clazz, methodName, annotations.first())
            }
        }

        for ((className, annotatedFuncs) in disableFunctions) {
            for ((methodName, annotations) in annotatedFuncs) {
                disableables += Disableable.fromInitAnnotation(classLoader, className, methodName, annotations.first())
            }
        }

        return initializables to disableables
    }

    /**
     * Adds the given [Initializables][Initializable] and [DisableFunctions][Disableable] to the initialization process.
     *
     * This method can only be invoked during the pre-world initialization phase or before the [start] method is called.
     */
    private fun addRunnables(initializables: List<Initializable>, disableables: List<Disableable>) = dependencyLoaderSupport {
        check(!preWorldInitialized) { "Cannot add additional callables after pre-world initialization!" }

        // add vertices
        for (initializable in initializables) {
            this@Initializer.initializables += initializable
            when (initializable.stage) {
                InternalInitStage.PRE_WORLD -> initPreWorld.addNode(initializable)
                else -> initPostWorld.addNode(initializable)
            }
        }
        for (disableable in disableables) {
            this@Initializer.disableables += disableable
            disable.addNode(disableable)
        }

        // add edges
        for (initializable in initializables) {
            initializable.loadDependencies(
                this@Initializer.initializables,
                if (initializable.stage == InternalInitStage.PRE_WORLD) initPreWorld else initPostWorld
            )
        }
        for (disableable in disableables) {
            disableable.loadDependencies(this@Initializer.disableables, disable)
        }

        // launch initialization it if already started
        if (::preWorldScope.isInitialized) {
            for (initializable in initializables) {
                if (initializable.stage != InternalInitStage.PRE_WORLD)
                    continue

                launch(preWorldScope, initializable, initPreWorld)
            }
        }

        // if (IS_DEV_SERVER)
        //     dumpGraphs()
    }

    // @Suppress("UNCHECKED_CAST")
    // private fun dumpGraphs() {
    //     val dir = File("debug/nova/")
    //     val preWorldFile = File(dir, "pre_world.dot")
    //     val postWorldFile = File(dir, "post_world.dot")
    //     val disableFile = File(dir, "disable.dot")
    //     dir.mkdirs()
    //
    //     val exporter = DOTExporter<InitializerRunnable<*>, DefaultEdge>()
    //     exporter.setVertexAttributeProvider { vertex ->
    //         mapOf(
    //             "label" to DefaultAttribute.createAttribute(vertex.toString()),
    //             "color" to DefaultAttribute.createAttribute(if (vertex.dispatcher != null) "aqua" else "black")
    //         )
    //     }
    //     exporter.exportGraph(initPreWorld as Graph<InitializerRunnable<*>, DefaultEdge>, preWorldFile)
    //     exporter.exportGraph(initPostWorld as Graph<InitializerRunnable<*>, DefaultEdge>, postWorldFile)
    //     exporter.exportGraph(disable as Graph<InitializerRunnable<*>, DefaultEdge>, disableFile)
    // }

    /**
     * Stats the pre-world initialization process.
     */
    private fun initPreWorld() = dependencyLoaderSupport {
        runBlocking {
            tryInit {
                coroutineScope {
                    preWorldScope = this
                    launchAll(this, initPreWorld)
                }
            }

            preWorldInitialized = true
        }
    }

    /**
     * Starts the post-world initialization process.
     */
    private fun initPostWorld() = dependencyLoaderSupport {
        runBlocking {
            tryInit {
                coroutineScope {
                    launchAll(this, initPostWorld)
                }
            }

            isDone = true
            NekoLoadDataEvent().callEvent()

            // PermanentStorage.store("last_version", Nova.pluginMeta.version)
            // setGlobalIngredients()
            // setupMetrics()
            LOGGER.info("Done loading")
        }
    }

    /**
     * Disables all [Initializables][Initializable] in the reverse order that they were initialized in.
     */
    fun disable() = dependencyLoaderSupport {
        runBlocking {
            if (isDone) {
                coroutineScope { launchAll(this, disable) }
            } else {
                LOGGER.warn("Skipping disable phase due to incomplete initialization")
            }
        }
    }

    @EventHandler
    private fun handleServerStarted(event: ServerLoadEvent) {
        if (preWorldInitialized) {
            initPostWorld()
        } else {
            LOGGER.error("Skipping post world initialization")
        }
    }
}