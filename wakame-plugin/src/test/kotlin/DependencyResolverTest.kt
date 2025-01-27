import cc.mewcraft.wakame.util.dependency.CircularDependencyException
import cc.mewcraft.wakame.util.dependency.DependencyComponent
import cc.mewcraft.wakame.util.dependency.DependencyResolver
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DependencyResolverTest {

    @Test
    fun testResolveDependenciesWithComplexGraph() {
        val nodes = listOf(
            StringDependencyComponent(
                component = "A",
                dependenciesBefore = listOf(),
                dependenciesAfter = listOf("B", "C")
            ),
            StringDependencyComponent(
                component = "B",
                dependenciesBefore = listOf("A"),
                dependenciesAfter = listOf("D")
            ),
            StringDependencyComponent(
                component = "C",
                dependenciesBefore = listOf("A"),
                dependenciesAfter = listOf("D", "E")
            ),
            StringDependencyComponent(
                component = "D",
                dependenciesBefore = listOf("B", "C"),
                dependenciesAfter = listOf("F")
            ),
            StringDependencyComponent(
                component = "E",
                dependenciesBefore = listOf("C"),
                dependenciesAfter = listOf("F")
            ),
            StringDependencyComponent(
                component = "F",
                dependenciesBefore = listOf("D", "E"),
                dependenciesAfter = listOf()
            )
        )

        val sortedNodeNames = DependencyResolver.resolveDependencies(nodes)

        assertTrue(sortedNodeNames.indexOf("A") < sortedNodeNames.indexOf("B"), "A should come before B.")
        assertTrue(sortedNodeNames.indexOf("A") < sortedNodeNames.indexOf("C"), "A should come before C.")
        assertTrue(sortedNodeNames.indexOf("B") < sortedNodeNames.indexOf("D"), "B should come before D.")
        assertTrue(sortedNodeNames.indexOf("C") < sortedNodeNames.indexOf("D"), "C should come before D.")
        assertTrue(sortedNodeNames.indexOf("C") < sortedNodeNames.indexOf("E"), "C should come before E.")
        assertTrue(sortedNodeNames.indexOf("D") < sortedNodeNames.indexOf("F"), "D should come before F.")
        assertTrue(sortedNodeNames.indexOf("E") < sortedNodeNames.indexOf("F"), "E should come before F.")
    }

    @Test
    fun testDetectCircularDependency() {
        val nodesWithCycle = listOf(
            StringDependencyComponent(
                component = "A",
                dependenciesBefore = listOf(),
                dependenciesAfter = listOf("B")
            ),
            StringDependencyComponent(
                component = "B",
                dependenciesBefore = listOf("A"),
                dependenciesAfter = listOf("C")
            ),
            StringDependencyComponent(
                component = "C",
                dependenciesBefore = listOf("B"),
                dependenciesAfter = listOf("A") // Creates a cycle A -> B -> C -> A
            )
        )

        assertThrows(CircularDependencyException::class.java) {
            DependencyResolver.resolveDependencies(nodesWithCycle)
        }
    }
}

class StringDependencyComponent(
    override val component: String,
    override val dependenciesBefore: List<String>,
    override val dependenciesAfter: List<String>,
) : DependencyComponent<String>