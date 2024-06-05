package cc.mewcraft.wakame.pack.model.impl

import cc.mewcraft.wakame.pack.model.Model
import cc.mewcraft.wakame.pack.model.ModelView
import com.ticxo.modelengine.api.model.ModeledEntity
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class ModelEngineModelView(
    private val modeledEntity: ModeledEntity
) : ModelView {
    override val uniqueId: UUID = modeledEntity.base.uuid

    override fun playAnimation(model: Model, animation: String) {
        val handler = modeledEntity.getModel(model.name).getOrNull()?.animationHandler ?: return
        handler.playAnimation(animation, 0.3, 0.3, 1.0, true)
    }

    override fun stopAnimation(model: Model, animation: String) {
        val handler = modeledEntity.getModel(model.name).getOrNull()?.animationHandler ?: return
        handler.stopAnimation(animation)
    }
}