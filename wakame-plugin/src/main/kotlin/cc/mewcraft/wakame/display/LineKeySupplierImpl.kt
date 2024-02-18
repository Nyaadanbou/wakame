package cc.mewcraft.wakame.display

import cc.mewcraft.wakame.item.binary.core.BinaryAbilityCore
import cc.mewcraft.wakame.item.binary.core.BinaryAttributeCore
import cc.mewcraft.wakame.item.scheme.meta.SchemeMeta
import cc.mewcraft.wakame.item.scheme.meta.SchemeMetaKeys
import net.kyori.adventure.key.Key
import kotlin.reflect.KClass

internal class AbilityLineKeySupplierImpl : AbilityLineKeySupplier {
    override fun getKey(value: BinaryAbilityCore): Key {
        return value.key
    }
}

internal class AttributeLineKeySupplierImpl : AttributeLineKeySupplier {
    override fun getKey(value: BinaryAttributeCore): Key {
        TODO("Not yet implemented")
    }
}

internal class MetaLineKeySupplierImpl : MetaLineKeySupplier {
    override fun getKey(value: KClass<SchemeMeta<*>>): Key {
        return SchemeMetaKeys.get(value)
    }
}