package cc.mewcraft.wakame.pack.model.impl

import cc.mewcraft.wakame.pack.model.Model
import cc.mewcraft.wakame.pack.model.ModelView
import cc.mewcraft.wakame.pack.model.WakameModelEngine
import com.ticxo.modelengine.api.ModelEngineAPI
import com.ticxo.modelengine.api.model.ActiveModel
import com.ticxo.modelengine.api.model.ModeledEntity
import org.bukkit.entity.Entity
import java.io.File

object ModelEngine : WakameModelEngine {
    override fun loadModel(file: File, cursor: Int): Model {
        throw UnsupportedOperationException()
    }

    override fun spawn(model: Model, baseEntity: Entity): ModelView {
        val modeledEntity: ModeledEntity = ModelEngineAPI.createModeledEntity(baseEntity)
        val activeModel: ActiveModel = ModelEngineAPI.createActiveModel(model.name)
        modeledEntity.addModel(activeModel, true)
        return ModelEngineModelView(modeledEntity)
    }
}