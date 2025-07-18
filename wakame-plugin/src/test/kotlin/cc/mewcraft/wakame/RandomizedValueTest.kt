package cc.mewcraft.wakame

import cc.mewcraft.wakame.util.RandomizedValue
import org.spongepowered.configurate.yaml.YamlConfigurationLoader
import kotlin.test.Test
import kotlin.test.assertEquals

class RandomizedValueTest {
    companion object {
        const val TOLERANCE = 1e-2
    }

    @Test
    fun deserialize_string() {
        RandomizedValue.create("5").calculate().apply { assertEquals(5.0, this.value, TOLERANCE) }
    }

    @Test
    fun deserialize_config_scalar() {
        val loader = YamlConfigurationLoader.builder().build()
        val node = loader.createNode().apply {
            set(5.0)
        }
        RandomizedValue.create(node).calculate().apply {
            assertEquals(5.0, this.value, TOLERANCE)
        }
    }

    @Test
    fun deserialize_config_base() {
        val loader = YamlConfigurationLoader.builder().build()
        val node = loader.createNode().apply {
            node("base").set(5.0)
        }
        RandomizedValue.create(node).calculate().apply {
            assertEquals(5.0, this.value, TOLERANCE)
        }
    }

    @Test
    fun deserialize_config_base_scale() {
        val loader = YamlConfigurationLoader.builder().build()
        val node = loader.createNode().apply {
            node("base").set(5.0)
            node("scale").set(1.2)
        }
        RandomizedValue.create(node).calculate(scalingFactor = 2).apply {
            assertEquals(5.0 + 1.2 * 2, this.value, TOLERANCE)
        }
    }

    @Test
    fun deserialize_config_base_sigma() {
        val loader = YamlConfigurationLoader.builder().build()
        val node = loader.createNode().apply {
            node("base").set(5.0)
            node("sigma").set(0.2)
        }
        RandomizedValue.create(node).apply {
            repeat(10) {
                println("deserialize_config_base_sigma: " + calculate())
            }
        }
    }

    @Test
    fun deserialize_config_base_sigma_threshold() {
        val loader = YamlConfigurationLoader.builder().build()
        val node = loader.createNode().apply {
            node("base").set(5.0)
            node("sigma").set(0.2)
            node("threshold").set(0.3)
        }
        RandomizedValue.create(node).apply {
            repeat(10) {
                println("deserialize_config_base_sigma_threshold: " + calculate())
            }
        }
    }

    @Test
    fun deserialization_from_number() {
        val value = RandomizedValue.create(5.0)
        value.calculate().apply {
            assertEquals(5.0, this.value, TOLERANCE)
        }
        value.calculate(scalingFactor = 2.1).apply {
            assertEquals(5.0, this.value, TOLERANCE)
        }
        value.calculate(randomVariable = 3.8).apply {
            assertEquals(5.0, this.value, TOLERANCE)
        }
        value.calculate(scalingFactor = 2.1, randomVariable = 3.8).apply {
            assertEquals(5.0, this.value, TOLERANCE)
        }
    }
}