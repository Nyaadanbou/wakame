package random2

import net.kyori.adventure.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeContainerTest {

    @Test
    fun testLocalNodeValues() {
        val globalDataSource = GlobalDataSource<String>()
        val nodeContainer = NodeContainer(globalDataSource)

        val rootNode = CompositeNode<String>(Key.key("root"))
            .addNode(LocalNode(Key.key("foo:1"), value = "Value 1"))
            .addNode(LocalNode(Key.key("foo:2"), value = "Value 2"))

        nodeContainer.root = rootNode

        val values = nodeContainer.values()
        assertEquals(listOf("Value 1", "Value 2"), values)
    }

    @Test
    fun testGlobalNodeValues() {
        val globalDataSource = GlobalDataSource<String>()

        globalDataSource.addEntry("bar1", listOf(LocalNode(Key.key("foo:3"), value = "Global Value 3")))
        globalDataSource.addEntry("bar2", listOf(LocalNode(Key.key("foo:4"), value = "Global Value 4"), LocalNode(Key.key("foo:5"), value = "Global Value 5")))
        globalDataSource.addEntry(
            "bar3", listOf(
                CompositeNode<String>(Key.key("global:bar2"))
                    .addNode(LocalNode(Key.key("foo:6"), value = "Global Value 6"))
                    .addNode(LocalNode(Key.key("foo:7"), value = "Global Value 7"))
            )
        )

        val nodeContainer = NodeContainer(globalDataSource)

        val rootNode = CompositeNode<String>(Key.key("root"))
            .addNode(LocalNode(Key.key("foo:1"), value = "Value 1"))
            .addNode(LocalNode(Key.key("foo:2"), value = "Value 2"))
            .addNode(CompositeNode(Key.key("global:bar1")))
            .addNode(CompositeNode(Key.key("global:bar3")))

        nodeContainer.root = rootNode

        val values = nodeContainer.values()
        assertEquals(listOf("Value 1", "Value 2", "Global Value 3", "Global Value 4", "Global Value 5", "Global Value 6", "Global Value 7"), values)
    }

    @Test
    fun testMixedNodeValues() {
        val globalDataSource = GlobalDataSource<String>()

        globalDataSource.addEntry("bar1", listOf(LocalNode(Key.key("foo:3"), value = "Global Value 3")))
        globalDataSource.addEntry("bar2", listOf(LocalNode(Key.key("foo:4"), value = "Global Value 4"), LocalNode(Key.key("foo:5"), value = "Global Value 5")))

        val nodeContainer = NodeContainer(globalDataSource)

        val rootNode = CompositeNode<String>(Key.key("root"))
            .addNode(LocalNode(Key.key("foo:1"), value = "Value 1"))
            .addNode(LocalNode(Key.key("foo:2"), value = "Value 2"))
            .addNode(CompositeNode(Key.key("global:bar1")))
            .addNode(CompositeNode(Key.key("global:bar2")))

        nodeContainer.root = rootNode

        val values = nodeContainer.values()
        assertEquals(listOf("Value 1", "Value 2", "Global Value 3", "Global Value 4", "Global Value 5"), values)
    }

    @Test
    fun testEmptyContainer() {
        val globalDataSource = GlobalDataSource<String>()
        val nodeContainer = NodeContainer(globalDataSource)

        val values = nodeContainer.values()
        assertTrue(values.isEmpty())
    }
}