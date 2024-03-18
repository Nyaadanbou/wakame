package cc.mewcraft.wakame.pack

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.initializer.Initializable
import cc.mewcraft.wakame.initializer.ReloadDependency
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import team.unnamed.hephaestus.Model
import team.unnamed.hephaestus.ModelDataCursor
import team.unnamed.hephaestus.reader.blockbench.BBModelReader
import java.io.File

private const val BBMODELS_DIR = "bbmodels"

@ReloadDependency(
    runAfter = [ResourcePackManager::class]
)
object ModelRegistry : Initializable, KoinComponent {
    private val assetsDir: File by inject(named(PLUGIN_ASSETS_DIR))

    private val modelRegistry: MutableMap<String, Model> = hashMapOf()

    private fun loadModels() {
        modelRegistry.clear()

        registerModel("test", loadModel("test.bbmodel"))
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

    fun registerModel(name: String, model: Model) {
        modelRegistry[name] = model
    }

    fun getModel(name: String): Model? {
        return modelRegistry[name]
    }

    fun clear() {
        modelRegistry.clear()
    }

    val values: Collection<Model>
        get() = modelRegistry.values

    override fun onPreWorld() {
        loadModels()
    }

    override fun onReload() {
        loadModels()
    }
}