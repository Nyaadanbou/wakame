import cc.mewcraft.wakame.attribute.AttributeLegacyMappings
import net.kyori.adventure.key.Key
import kotlin.test.Test
import kotlin.test.assertEquals

class AttributeLegacyMappingsTest {

    @Test
    fun testByKey() {
        val key = Key.key("test:foo")
        val uuid = AttributeLegacyMappings.byKey(key)
        assertEquals(uuid, AttributeLegacyMappings.byKey(key))
    }

    @Test
    fun testById() {
        val key = Key.key("test:bar")
        val uuid = AttributeLegacyMappings.byKey(key)
        assertEquals(key, AttributeLegacyMappings.byId(uuid))
    }

    @Test
    fun testBidirectionalMapping() {
        val key = Key.key("test:baz")
        val uuid = AttributeLegacyMappings.byKey(key)
        // Ensure that the reverse mapping exists
        assertEquals(key, AttributeLegacyMappings.byId(uuid))
        // Ensure that the UUID obtained from the Key can be used to retrieve the same Key
        assertEquals(uuid, AttributeLegacyMappings.byKey(key))
    }
}