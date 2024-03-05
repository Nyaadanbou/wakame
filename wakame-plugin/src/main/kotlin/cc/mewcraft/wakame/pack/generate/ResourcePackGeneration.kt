package cc.mewcraft.wakame.pack.generate

import cc.mewcraft.wakame.PLUGIN_ASSETS_DIR
import cc.mewcraft.wakame.item.scheme.NekoItem
import me.lucko.helper.text3.mini
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import org.jetbrains.annotations.Contract
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import team.unnamed.creative.ResourcePack
import team.unnamed.creative.base.Readable
import team.unnamed.creative.base.Writable
import team.unnamed.creative.metadata.pack.PackMeta
import team.unnamed.creative.serialize.minecraft.model.ModelSerializer
import java.io.File
import kotlin.math.log

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
                22,
                "<rainbow>Nyaadanbou Resource Pack".mini,
            )
            args.resourcePack.packMeta(packMeta)
        }.onFailure { return Result.failure(it) }

        return generateNext()
    }
}

internal class ResourcePackIconGeneration(
    args: GenerationArgs,
) : ResourcePackGeneration(args), KoinComponent{
    private val assetsDir: File by inject(named(PLUGIN_ASSETS_DIR))

    override fun generate(): Result<Unit> {
        runCatching {
            args.resourcePack.icon(Writable.file(assetsDir.resolve("logo.png")))
        }.onFailure { return Result.failure(it) }

        return generateNext()
    }
}

internal class ResourcePackModelGeneration(
    args: GenerationArgs,
) : ResourcePackGeneration(args), KoinComponent {
    private val logger: ComponentLogger by inject(mode = LazyThreadSafetyMode.NONE)

    @Suppress("UnstableApiUsage")
    override fun generate(): Result<Unit> {
        val items = args.allItems
        runCatching {
            for (nekoItem in items) {
                val modelPath = nekoItem.modelPath ?: continue
                logger.info("<aqua>Generating model for ${nekoItem.key}... (path: $modelPath)".mini)
                val model = ModelSerializer.INSTANCE
                    .deserialize(Readable.path(modelPath), nekoItem.key)
                args.resourcePack.model(model).also {
                    logger.info("<green>Model for ${nekoItem.key} generated".mini)
                }
            }
        }.onFailure { return Result.failure(it) }

        return generateNext()
    }
}