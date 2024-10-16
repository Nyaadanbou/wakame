package cc.mewcraft.wakame.random3

import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.nio.file.Path

// 文件说明:
// 包含 Filter 关于 random3.Node 的实现

// 开发日记 2024/7/16
// 一个 FilterNodeReader 不仅用来构建 SharedStorage
// 中的 random3.Node, 还要被用来构建 Pool 中的 Node.
// 这对于 SampleNodeReader 来说也是一样的.
/**
 * 封装了类型 [Filter] 所需要的所有 [Node] 相关的实现.
 */
abstract class FilterNodeFacade<C : RandomSelectorContext> : NodeFacade<Filter<C>>() {
    abstract override val dataDir: Path
    abstract override val serializers: TypeSerializerCollection
    abstract override val repository: NodeRepository<Filter<C>>
}