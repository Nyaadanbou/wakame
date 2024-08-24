package cc.mewcraft.wakame.pack.entity

import cc.mewcraft.wakame.pack.RESOURCE_NAMESPACE
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType
import org.slf4j.Logger
import team.unnamed.hephaestus.Model
import team.unnamed.hephaestus.bukkit.ModelView
import team.unnamed.hephaestus.bukkit.track.ModelViewPersistenceHandler
import java.util.concurrent.CompletableFuture

private val MODEL_KEY = NamespacedKey(RESOURCE_NAMESPACE, "model")

class ModelViewPersistenceHandlerImpl(
    private val logger: Logger,
) : ModelViewPersistenceHandler {

    override fun determineModel(entity: Entity): CompletableFuture<Model> {
        val data = entity.persistentDataContainer
        val modelName = data.get(MODEL_KEY, PersistentDataType.STRING)
            ?: return CompletableFuture.completedFuture(null) // This entity doesn't specify a model

        val model = ModelRegistry.model(modelName)
        if (model == null) {
            // This entity specifies an unknown model
            logger.error("Entity with UUID '${entity.uniqueId}' specifies an unknown model '$modelName'")
            return CompletableFuture.completedFuture(null)
        }

        logger.info("Found model '${model.name()}' for entity with UUID '${entity.uniqueId}'!")
        return CompletableFuture.completedFuture(model)
    }

    override fun saveModel(entity: Entity, view: ModelView) {
        val model = view.model()
        val data = entity.persistentDataContainer
        data.set(MODEL_KEY, PersistentDataType.STRING, model.name())
        logger.info("Saved model '${model.name()}' for entity with UUID '${entity.uniqueId}'!")
    }
}