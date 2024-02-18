package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.scheme.meta.SchemeMeta
import cc.mewcraft.wakame.item.scheme.meta.SchemeMetaKeys
import net.kyori.adventure.key.Key
import kotlin.reflect.KClass

internal class AbilityLineKeySupplierImpl : AbilityLineKeySupplier {
    override fun getKey(value: BinaryAbilityCore): Key {
        return value.key // 技能的 key 就是它在 renderer 配置文件中的 key
    }
}

internal class AttributeLineKeySupplierImpl : AttributeLineKeySupplier {
    override fun getKey(value: BinaryAttributeCore): Key {
        TODO("Not yet implemented") // 属性的 key 需要根据 operation 和 element 来共同决定
    }
}

internal class MetaLineKeySupplierImpl : MetaLineKeySupplier {
    override fun getKey(value: KClass<out SchemeMeta<*>>): Key {
        return SchemeMetaKeys.get(value) // 元数据的 key 为它在模板中的 key
    }
}