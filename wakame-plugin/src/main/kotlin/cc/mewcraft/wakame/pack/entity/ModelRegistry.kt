package cc.mewcraft.wakame.pack.entity

import cc.mewcraft.wakame.InjectionQualifier
import cc.mewcraft.wakame.Injector
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import team.unnamed.hephaestus.Model
import team.unnamed.hephaestus.ModelDataCursor
import team.unnamed.hephaestus.bukkit.ModelView
import team.unnamed.hephaestus.reader.blockbench.BBModelReader
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Init(
    stage = InitStage.PRE_WORLD,
)
@Reload
object ModelRegistry {
    private const val BBMODELS_DIR_PATH = "bbmodels"
    private val assetsDir: File by Injector.inject(InjectionQualifier.ASSETS_FOLDER)

    private val models: MutableMap<String, Model> = ConcurrentHashMap()
    private val views: MutableMap<UUID, ModelView> = ConcurrentHashMap()

    @InitFun
    fun init() {
        loadModels().onFailure { it.printStackTrace() }
    }

    @ReloadFun
    fun reload() {
        loadModels().onFailure { it.printStackTrace() }
    }

    private fun loadModels(): Result<Unit> {
        models.clear()
        return runCatching { register(loadModel("test.bbmodel")) }
    }

    private fun loadModel(fileName: String): Model {
        val modelFile = assetsDir.resolve(BBMODELS_DIR_PATH).resolve(fileName)
        if (!modelFile.exists()) {
            throw IllegalArgumentException("BBModel file $fileName not found")
        }
        if (modelFile.extension != "bbmodel") {
            throw IllegalArgumentException("BBModel file $fileName is not a bbmodel file")
        }

        val modelDataCursor = ModelDataCursor(10000)
        val reader = BBModelReader.blockbench(modelDataCursor)
        val model = reader.read(modelFile)
        return model
    }

    private fun register(model: Model) {
        models[model.name()] = model
    }

    fun model(name: String): Model? {
        return models[name]
    }

    fun view(uuid: UUID): ModelView? {
        return views[uuid]
    }

    fun view(view: ModelView) {
        views[view.uniqueId] = view
    }

    fun views(): Collection<ModelView> {
        return views.values
    }

    fun models(): Collection<Model> {
        return models.values
    }
}