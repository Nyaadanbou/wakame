package cc.mewcraft.wakame.pack

import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import org.bukkit.Material
import java.io.File

interface Model : Keyed {

    /**
     * You can access the custom model data of this model from [CustomModelDataConfiguration.customModelDataMap]
     */
    val modelKey: Key

    // Overrides Java's getter
    override fun key(): Key = modelKey

    val overriddenMaterials: Set<Material>

    /**
     * Gets the model path of this [Model].
     *
     * The model path rules need to be implemented by the implementer.
     *
     * We recommend using [modelKey] to get the path.
     *
     * @return The model file, or null if the model file does not exist.
     */
    val modelFile: File?

    /**
     * Gets the sub-models of this [Model].
     *
     * You can get the sub-models of this model by calling [getSubModelWithSubId].
     */
    val subModels: List<Model>
        get() = listOf(this)

    /**
     * Gets the model key of this [Model].
     *
     * When subId is 0, it returns the same value as [modelKey].
     *
     * @param subId The index of [subModels]
     * @return The model of this [Model].
     */
    fun getSubModelWithSubId(subId: Int = 0): Model? = if (subId == 0) this else subModels.getOrNull(subId)
}

fun Model.bfsTraversal(): List<Model> {
    val result = mutableListOf<Model>()
    val visited = mutableSetOf<Model>()
    val queue = ArrayDeque<Model>()
    queue.add(this)

    while (queue.isNotEmpty()) {
        val currentNode = queue.removeFirst()
        if (currentNode in visited) continue
        visited.add(currentNode)

        result.add(currentNode)
        queue.addAll(currentNode.subModels)
    }

    return result
}