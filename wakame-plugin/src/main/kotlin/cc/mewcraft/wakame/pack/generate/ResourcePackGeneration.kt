package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.item.scheme.NekoItem
import me.lucko.helper.text3.mini
import org.jetbrains.annotations.Contract
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.metadata.pack.PackMeta

data class GenerationArgs(
    val resourcePack: ResourcePack,
    val allItems: Set<NekoItem>,
)

sealed class ResourcePackGeneration(
    protected val args: GenerationArgs,
) {
    companion object {
        fun chain(vararg generations: ResourcePackGeneration): ResourcePackGeneration {
            generations.reduce { acc, generation ->
                acc.next = generation
                generation
            }
            return generations.first()
        }
    }

    protected var next: ResourcePackGeneration? = null

    /**
     * Generates the resource pack.
     *
     * The original resource pack will be changed.
     *
     * @return a result encapsulating whether the generation succeeds or not
     */
    @Contract(pure = false)
    abstract fun generate(): Result<Unit>

    protected fun generateNext(): Result<Unit> {
        return next?.generate() ?: Result.success(Unit)
    }
}

internal class ResourcePackMetaGeneration(
    args: GenerationArgs,
) : ResourcePackGeneration(args) {
    override fun generate(): Result<Unit> {
        runCatching {
            val packMeta = PackMeta.of(
                26,
                "<rainbow>Nyaadanbou Resource Pack".mini,
            )
            args.resourcePack.packMeta(packMeta)
        }.onFailure { return Result.failure(it) }

        return generateNext()
    }
}