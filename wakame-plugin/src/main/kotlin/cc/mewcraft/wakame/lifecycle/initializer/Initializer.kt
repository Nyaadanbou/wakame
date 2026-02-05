package cc.mewcraft.wakame.lifecycle.initializer

import cc.mewcraft.wakame.BootstrapContexts
import cc.mewcraft.wakame.LOGGER
import cc.mewcraft.wakame.adventure.BuiltInMessages
import cc.mewcraft.wakame.api.event.KoishLoadDataEvent
import cc.mewcraft.wakame.config.PermanentStorage
import cc.mewcraft.wakame.lifecycle.LifecycleUtils
import cc.mewcraft.wakame.lifecycle.initializer.Initializer.initialize
import cc.mewcraft.wakame.util.data.JarUtils
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableGraph
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import java.io.File
import java.nio.file.Path

internal object Initializer : Listener {

    private val initializables: HashSet<Initializable> = HashSet()
    private val disableables: HashSet<Disableable> = HashSet()

    private val initBootstrapDependencyGraph: MutableGraph<Initializable> = GraphBuilder.directed().allowsSelfLoops(false).build()
    private val initPreWorldDependencyGraph: MutableGraph<Initializable> = GraphBuilder.directed().allowsSelfLoops(false).build()
    private val initPostWorldDependencyGraph: MutableGraph<Initializable> = GraphBuilder.directed().allowsSelfLoops(false).build()
    private val disableDependencyGraph: MutableGraph<Disableable> = GraphBuilder.directed().allowsSelfLoops(false).build()

    private var bootstrapInitialized = false
    private var preWorldInitialized = false

    var isDone = false
        private set

    /**
     * Finds all initializable tasks and registers them all.
     */
    fun initialize(): Unit = LifecycleUtils.runLifecycle {
        findAndRegisterTasks(BootstrapContexts.PLUGIN_JAR, javaClass.classLoader)
    }

    /**
     * Performs pre-world initialization.
     */
    fun performBootstrap(): Unit {
        LifecycleUtils.runLifecycle(::initBootstrap)
    }

    /**
     * Performs pre-world-late initialization.
     * This is called at the Mixin injection point, after bootstrap but before plugins are loaded.
     */
    fun performPreWorld(): Unit {
        LifecycleUtils.runLifecycle(::initPreWorld)
    }

    /**
     * Performs post-world initialization.
     */
    fun performPostWorld(): Unit = LifecycleUtils.runLifecycle {
        if (bootstrapInitialized && preWorldInitialized) {
            initPostWorld()
        } else {
            LOGGER.error("Skipping post-world initialization because pre-world initialization failed")
        }
    }

    /**
     * Runs all disable functions of [Initializable] in the reverse order that they were initialized in.
     */
    fun performDisable(): Unit = runBlocking {
        if (isDone) {
            coroutineScope {
                LifecycleUtils.launchAll(this, disableDependencyGraph)
            }
        } else {
            LOGGER.warn("Skipping disable phase due to incomplete initialization")
        }
    }

    private fun findAndRegisterTasks(path: Path, classLoader: ClassLoader) {
        val file = path.toFile()
        val (initializables, disableables) = findTasks(file, classLoader)
        registerTasks(initializables, disableables)
    }

    /**
     * Searches [file] and collects classes annotated by [InternalInit] and [Init] and functions annotated by
     * [InitFun] and [DisableFun] as [Initializables][Initializable] and [DisableableFunctions][Disableable].
     */
    private fun findTasks(file: File, classLoader: ClassLoader): Pair<List<Initializable>, List<Disableable>> {
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
            if (clazz == null) throw IllegalStateException("Class $className is missing an ${Init::class} annotation!")
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
     * Adds the given [InitializeFunctions][Initializable] and [DisableFunctions][Disableable] to the initialization process.
     *
     * This method can only be invoked during the pre-world initialization phase or before the [initialize] method is called.
     */
    private fun registerTasks(initializables: List<Initializable>, disableables: List<Disableable>) {
        check(!bootstrapInitialized) { "Cannot add additional callables after pre-world initialization!" }

        // add vertices
        for (initializable in initializables) {
            Initializer.initializables += initializable
            when (initializable.stage) {
                InternalInitStage.BOOTSTRAP -> initBootstrapDependencyGraph.addNode(initializable)
                InternalInitStage.PRE_WORLD -> initPreWorldDependencyGraph.addNode(initializable)
                InternalInitStage.POST_WORLD -> initPostWorldDependencyGraph.addNode(initializable)
            }
        }
        for (disableable in disableables) {
            Initializer.disableables += disableable
            disableDependencyGraph.addNode(disableable)
        }

        // add edges
        for (initializable in initializables) {
            initializable.loadDependencies(
                Initializer.initializables,
                when (initializable.stage) {
                    InternalInitStage.BOOTSTRAP -> initBootstrapDependencyGraph
                    InternalInitStage.PRE_WORLD -> initPreWorldDependencyGraph
                    InternalInitStage.POST_WORLD -> initPostWorldDependencyGraph
                }
            )
        }
        for (disableable in disableables) {
            disableable.loadDependencies(Initializer.disableables, disableDependencyGraph)
        }

        if (BootstrapContexts.IS_DEV_SERVER) {
            dumpGraphs()
        }
    }

    private fun dumpGraphs() {
        // TODO 将图库从 guava 迁移至 jgrapht 以实现 dumpGraphs
        //val dir = File("debug/koish/")
        //val preWorldFile = File(dir, "pre_world.dot")
        //val postWorldFile = File(dir, "post_world.dot")
        //val disableFile = File(dir, "disable.dot")
        //dir.mkdirs()
        //
        //val exporter = DOTExporter<InitializerRunnable<*>, DefaultEdge>()
        //exporter.setVertexAttributeProvider { vertex ->
        //    mapOf(
        //        "label" to DefaultAttribute.createAttribute(vertex.toString()),
        //        "color" to DefaultAttribute.createAttribute(if (vertex.dispatcher != null) "aqua" else "black")
        //    )
        //}
        //exporter.exportGraph(initPreWorld as Graph<InitializerRunnable<*>, DefaultEdge>, preWorldFile)
        //exporter.exportGraph(initPostWorld as Graph<InitializerRunnable<*>, DefaultEdge>, postWorldFile)
        //exporter.exportGraph(disable as Graph<InitializerRunnable<*>, DefaultEdge>, disableFile)
    }

    /**
     * Starts the bootstrap initialization process.
     */
    private fun initBootstrap(): Unit = runBlocking {
        LifecycleUtils.runLifecycle {
            coroutineScope {
                LifecycleUtils.launchAll(this, initBootstrapDependencyGraph)
            }
        }

        bootstrapInitialized = true
    }

    /**
     * Starts the pre-world-late initialization process.
     * This runs at the Mixin injection point, after bootstrap but before plugins are loaded.
     */
    private fun initPreWorld(): Unit = runBlocking {
        LifecycleUtils.runLifecycle {
            coroutineScope {
                LifecycleUtils.launchAll(this, initPreWorldDependencyGraph)
            }
        }

        preWorldInitialized = true
    }

    /**
     * Starts the post-world initialization process.
     */
    private fun initPostWorld(): Unit = runBlocking {
        LifecycleUtils.runLifecycle {
            coroutineScope {
                LifecycleUtils.launchAll(this, initPostWorldDependencyGraph)
            }
        }

        isDone = true
        KoishLoadDataEvent().callEvent()
        PermanentStorage.store("last_version", BootstrapContexts.PLUGIN_VERSION.toString())
        BuiltInMessages.STARTUP_BANNER.send(Bukkit.getServer().consoleSender)
        LOGGER.info(Component.text("Done loading").color(NamedTextColor.AQUA))
    }
}