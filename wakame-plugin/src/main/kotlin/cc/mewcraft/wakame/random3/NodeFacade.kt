package cc.mewcraft.wakame.random3

import cc.mewcraft.wakame.PLUGIN_DATA_DIR
import cc.mewcraft.wakame.util.javaTypeOf
import cc.mewcraft.wakame.util.yamlConfig
import net.kyori.adventure.key.Key
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.slf4j.Logger
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.SerializationException
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import java.io.File
import java.nio.file.Path

/**
 * 封装了类型 [T] 所需要的所有 [Node] 相关的实现.
 *
 * **注意!** 该实现使用了独立的 config loader, 因此需要单独指定 [serializers].
 */
abstract class NodeFacade<T> : KoinComponent {
    /**
     * 指向存放 [NodeRepository.Entry] 的数据文件夹.
     *
     * 文件夹里面的每一个文件都是一个 [NodeRepository.Entry], 其文件名(不含扩展名)将充当索引.
     * 文件内容必须有一个键为 "nodes" 的根节点, 值为 List, 其中的每一项都是一个 [Node].
     *
     * *必须为相对路径*, 根目录为插件的数据文件夹.
     */
    abstract val dataDir: Path

    /**
     * 读取 [dataDir] 中的数据所需要的序列化器.
     */
    abstract val serializers: TypeSerializerCollection

    /**
     * 储存共享的 [Node<T>][Node].
     */
    abstract val repository: NodeRepository<T>

    /**
     * 将一个 [ConfigurationNode] 转换成 [Node] 所包含的 [T].
     *
     * 将由 [decodeNode] 调用, 用于构建 [Node] 中所包含的具体数据.
     *
     * ## Node structure
     * ```yaml
     * <node>:
     *   # 下面只是随便写点数据演示.
     *   # 具体结构取决于泛型 [T].
     *   foo: bar
     *   bar: foo
     * ```
     */
    protected abstract fun decodeNodeData(node: ConfigurationNode): T

    /**
     * 将一个 [ConfigurationNode] 转换成 [Node].
     *
     * ## Node structure
     * ```yaml
     * <node>:
     *   # type 是必须要有的, 类型为 Key
     *   type: <key>
     *   # 其余都是根据泛型 [T] 来决定的,
     *   # 这些会由 readValue() 来处理.
     *   foo: bar
     *   bar: foo
     * ```
     */
    fun decodeNode(node: ConfigurationNode): Node<T> {
        // 从 config.Node 中读取 "type"
        // ("type" 的类型为 Key)
        // "type" 的命名空间将决定 random3.Node 的解析结果
        // (具体的结果, 参考 random3.Node 的实现)
        val rawType = node.node("type").string ?: throw SerializationException(
            node, javaTypeOf<String>(), "The 'type' of this node is not specified"
        )
        val type = runCatching { Key.key(rawType) }.getOrElse {
            throw SerializationException(node, javaTypeOf<Key>(), it)
        }

        // 首先看看是不是 CompositeNode; 如果是的话就直接构建一个 CompositeNode, 然后返回.
        if (type.namespace() == Node.NAMESPACE_GLOBAL) {
            // 这里只需要直接构建一个 CompositeNode, 并不需要去读取 CompositeNode 所指向的数据.
            // 这是因为 CompositeNode 的数据是懒加载的 - 只有需要的时候才会从 SharedStorage 获取.
            return CompositeNode(type, mutableListOf())
        }

        // 否则就是 LocalNode
        val value = decodeNodeData(node)
        return LocalNode(type, value)
    }

    /**
     * 从配置文件读取所有的数据, 并载入到 [NodeRepository].
     */
    fun populate() {
        if (dataDir.isAbsolute) {
            throw IllegalArgumentException("'dataDir' must be a relative path to the plugin data directory")
        }

        val loadBuilder = yamlConfig {
            withDefaults()
            serializers {
                registerAll(serializers)
            }
        }

        // 清空 repository
        repository.clear()

        // 填充 repository
        //
        // 遍历 entriesDataDir 中的每一个文件,
        // 并将其读取为一个 random.Node,
        // 最后将其添加到 repository 中.
        forEachEntryFile { file ->
            val entryRef = getEntryRef(file)
            validateEntryRef(entryRef)
            val fileText = file.readText(Charsets.UTF_8)
            val rootNode = loadBuilder.buildAndLoadString(fileText)
            repository.addEntry(entryRef) {
                // "nodes" 装的是一个 List, 其中的每一项都是一个 LocalNode 或 CompositeNode
                rootNode.node("nodes")
                    .childrenList()
                    .map { listChild ->
                        decodeNode(listChild)
                    }
                    .forEach {
                        node(it)
                    }
            }
        }
    }

    private fun getEntryRef(file: File): String {
        return file.toRelativeString(pluginDataDir.resolve(dataDir.toFile())).substringBeforeLast(".")
    }

    private fun validateEntryRef(entryRef: String) {
        if (entryRef.isBlank() || !Key.parseableValue(entryRef)) {
            throw IllegalArgumentException("The filename '$entryRef' is not valid to be an entry reference. Valid filename pattern: [a-z0-9/._-]+")
        }
        if (repository.hasEntry(entryRef)) {
            logger.warn("The entry with name '$entryRef' already exists")
        }
    }

    private fun forEachEntryFile(block: (File) -> Unit) {
        val pluginDataDir = get<File>(named(PLUGIN_DATA_DIR))
        val entriesDataDir = pluginDataDir.resolve(dataDir.toFile())
        entriesDataDir
            .walk()
            .drop(1) // exclude the directory itself
            .filter { it.isFile && it.extension == "yml" }
            .forEach(block)
    }

    private val logger: Logger by inject()
    private val pluginDataDir by inject<File>(named(PLUGIN_DATA_DIR))
}