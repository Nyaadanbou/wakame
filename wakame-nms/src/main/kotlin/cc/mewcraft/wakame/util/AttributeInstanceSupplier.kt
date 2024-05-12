package cc.mewcraft.wakame.util

import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeInstance
import org.bukkit.craftbukkit.attribute.CraftAttribute
import org.bukkit.craftbukkit.attribute.CraftAttributeInstance
import net.minecraft.world.entity.ai.attributes.AttributeInstance as MojangAttributeInstance

object AttributeInstanceSupplier {
    /**
     * 所返回的 [AttributeInstance] 应该仅作为原型对象。
     *
     * @param attribute 所基于的 [Attribute]
     * @return 新的 [AttributeInstance]
     */
    fun createInstance(attribute: Attribute): AttributeInstance {
        val mojangAttribute = CraftAttribute.bukkitToMinecraftHolder(attribute)
        val mojangAttributeInstance = MojangAttributeInstance(mojangAttribute) {}
        return CraftAttributeInstance(mojangAttributeInstance, attribute)
    }
}