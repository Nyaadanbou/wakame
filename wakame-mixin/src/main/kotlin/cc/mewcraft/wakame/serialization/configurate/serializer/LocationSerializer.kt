package cc.mewcraft.wakame.serialization.configurate.serializer

import cc.mewcraft.lazyconfig.configurate.require
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.Location
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.serialize.TypeSerializer
import java.lang.reflect.Type

object LocationSerializer : TypeSerializer<Location> {
    override fun deserialize(type: Type, node: ConfigurationNode): Location? {
        val x = node.node("x").require<Double>()
        val y = node.node("y").require<Double>()
        val z = node.node("z").require<Double>()
        val yaw = node.node("yaw").getFloat(0f)
        val pitch = node.node("pitch").getFloat(0f)
        val worldName = node.node("world").require<Key>()
        val world = Bukkit.getWorld(worldName)
        return Location(world, x, y, z, yaw, pitch)
    }

    override fun serialize(type: Type, obj: Location?, node: ConfigurationNode) {
        if (obj == null) return
        node.node("x").set(obj.x)
        node.node("y").set(obj.y)
        node.node("z").set(obj.z)
        node.node("yaw").set(obj.yaw.takeIf { it != 0f })
        node.node("pitch").set(obj.pitch.takeIf { it != 0f })
        node.node("world").set(obj.world?.key()?.toString() ?: "")
    }
}