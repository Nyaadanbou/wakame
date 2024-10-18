import cc.mewcraft.wakame.attribute.AttributeLegacyMappings
import net.kyori.adventure.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals

class AttributeLegacyMappingsTest {

    @Test
    fun testByName() {
        val name = Key.key("test:foo")
        val uuid = AttributeLegacyMappings.byName(name)
        assertEquals(uuid, AttributeLegacyMappings.byName(name))
    }

    @Test
    fun testById() {
        val name = Key.key("test:bar")
        val uuid = AttributeLegacyMappings.byName(name)
        assertEquals(name, AttributeLegacyMappings.byId(uuid))
    }

    @Test
    fun testBidirectionalMapping() {
        val name = Key.key("test:baz")
        val uuid = AttributeLegacyMappings.byName(name)
        // Ensure that the reverse mapping exists
        assertEquals(name, AttributeLegacyMappings.byId(uuid))
        // Ensure that the UUID obtained from the name can be used to retrieve the same name
        assertEquals(uuid, AttributeLegacyMappings.byName(name))
    }
}