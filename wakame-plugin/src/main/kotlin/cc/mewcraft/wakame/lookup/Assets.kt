package cc.mewcraft.wakame.lookup

import cc.mewcraft.wakame.adventure.key.Keyed
import cc.mewcraft.wakame.registry.ItemRegistry
import net.kyori.adventure.key.Key
import org.bukkit.Material
import java.io.File


// TODO 重构: Asset 目前仅代表*物品*的模型配置.
//  既然是这样的话, Asset 改名叫做 ItemAsset 更符合实际用途, 顶级接口就不用了.
//  等后面写自定义家具的时候, 再写个 FurnitureAsset 之类的.
//  等后面越写越多, 发现代码有重复的地方, 再抽象顶级接口就行.
//  这里的编码原则就是: 如果不知道如何抽象, 那就不要抽象.
//  初版代码讲究清晰且 bug-free.

sealed interface Assets : Keyed {

    // TODO 重构: variant 实际上只是用来区别物品的. 很难想象这个如何用于家具.
    //  所以这个字段应该仅存于 ItemAsset, 而不应该放在 Assets 这种顶级接口里.

    /**
     * 适用的变体.
     */
    val variant: Int

    // TODO 重构: Collection<File> 所代表的东西含义非常模糊.
    //  试问: 当客户端代码拿到一个 Assets 对象, 然后获取里面的 files,
    //  这些 files 对于客户端代码来说意味着什么? 如果客户端代码无法直接
    //  从这些 files 中获取有用的信息, 那么这个设计就需要重新考虑了.

    /**
     * 所有的模型文件.
     */
    val files: Collection<File>
}

data class ItemAssets(
    override val key: Key,
    override val variant: Int,
    private val fileStrings: List<String>,
) : Assets {
    override val files: Collection<File> = fileStrings.map { AssetUtils.getFileOrThrow(it, "json") }
}

// TODO 重构: Assets 作为顶级接口, 并没有指明它的用途.
//  而这里却写了一个 itemType 的扩展函数. 也就是说 itemType 在设计上是 Assets 的一部分,
//  但是 itemType 又只有在 ItemAssets 的具体实现中才有意义. 这种设计是不合理的.
internal val Assets.itemType: Material
    get() = requireNotNull(ItemRegistry.CUSTOM.getOrNull(key)?.base?.type) {
        "ItemType not found for item id: $key"
    }