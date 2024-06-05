package cc.mewcraft.wakame.pack.model

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.initializer.Initializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.io.File
import java.util.concurrent.ConcurrentHashMap

private const val BBMODELS_DIR = "bbmodels"

object ModelRegistry : Initializable, KoinComponent {
    private val engine: WakameModelEngine by inject()
    private val assetsDir: File by inject(named(PLUGIN_ASSETS_DIR))

    private val models: MutableMap<String, Model> = ConcurrentHashMap()

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

        val model = engine.loadModel(modelFile, 10000)
        return model
    }

    private fun register(model: Model) {
        models[model.name] = model
    }

    fun model(name: String): Model? {
        return models[name]
    }

    fun models(): Collection<Model> {
        return models.values
    }

    override fun onPrePack() {
//        loadModels().onFailure { it.printStackTrace() }
    }

    override fun onReload() {
//        loadModels().onFailure { it.printStackTrace() }
    }
}