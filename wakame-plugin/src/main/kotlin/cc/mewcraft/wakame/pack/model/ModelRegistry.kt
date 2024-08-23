package cc.mewcraft.wakame.pack.model

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import team.unnamed.hephaestus.Model
import team.unnamed.hephaestus.ModelDataCursor
import team.unnamed.hephaestus.bukkit.ModelView
import team.unnamed.hephaestus.reader.blockbench.BBModelReader
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private const val BBMODELS_DIR = "bbmodels"

object ModelRegistry : Initializable, KoinComponent {
    private val assetsDir: File by inject(named(PLUGIN_ASSETS_DIR))

    private val models: MutableMap<String, Model> = ConcurrentHashMap()
    private val views: MutableMap<UUID, ModelView> = ConcurrentHashMap()

    private fun loadModels(): Result<Unit> {
        models.clear()

        return runCatching { register(loadModel("test.bbmodel")) }
    }

    private fun loadModel(fileName: String): Model {
        val modelFile = assetsDir.resolve(BBMODELS_DIR).resolve(fileName)
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

    override fun onPreWorld() {
        loadModels().onFailure { it.printStackTrace() }
    }

    override fun onReload() {
        loadModels().onFailure { it.printStackTrace() }
    }
}