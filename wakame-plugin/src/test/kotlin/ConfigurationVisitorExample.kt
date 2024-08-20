import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.ConfigurationVisitor
import org.spongepowered.configurate.kotlin.extensions.typedSet
import kotlin.test.Test

class ConfigurationVisitorExample {
    /**
     * ```yaml
     * a: layer 1
     * b:
     *   a: layer 2
     *   b:
     *     a: layer 3
     * c:
     *   a:
     *     a:
     *       a: layer 4
     * ```
     */
    @Test
    fun example() {
        val root = BasicConfigurationNode.root()

        root.node("a").typedSet("layer 1")
        root.node("b", "a").typedSet("layer 2")
        root.node("b", "b", "a").typedSet("layer 3")
        root.node("c", "a", "a", "a").typedSet("layer 4")
        root.node("d").typedSet(listOf(1, 2, 3))

        root.visit(ConfigurationVisitor.Stateless { node ->
            if (node.rawScalar() != null) {
                println("${node.path()} -> \"${node.rawScalar()}\"")
            }
        })
    }
}