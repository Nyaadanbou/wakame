package cc.mewcraft.wakame.entity.attribute

import cc.mewcraft.wakame.attribute.ImaginaryAttributeMapImpl
import cc.mewcraft.wakame.lifecycle.initializer.Init
import cc.mewcraft.wakame.lifecycle.initializer.InitFun
import cc.mewcraft.wakame.lifecycle.initializer.InitStage
import cc.mewcraft.wakame.lifecycle.reloader.Reload
import cc.mewcraft.wakame.lifecycle.reloader.ReloadFun
import cc.mewcraft.wakame.registry2.KoishRegistries
import cc.mewcraft.wakame.registry2.entry.RegistryEntry
import cc.mewcraft.wakame.util.Identifier
import cc.mewcraft.wakame.util.Identifiers
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import org.jetbrains.annotations.ApiStatus

@Init(stage = InitStage.POST_WORLD) // 实际依赖 AttributeSupplierRegistryLoader
@Reload
internal object ImgAttributeMapRegistryLoader {
    // 通过硬编码注册的 id, 但不确定对应的配置文件是否存在
    private val intrusiveRegisteredIds = HashSet<String>()

    @JvmField
    val ARROW: RegistryEntry<ImaginaryAttributeMap> = createEntry("minecraft:arrow")

    @ApiStatus.Internal
    @InitFun
    fun init() {
        // init 只负责添加 intrusive registry entry
        consumeData(KoishRegistries.IMG_ATTRIBUTE_MAP::add)
    }

    @ApiStatus.Internal
    @ReloadFun
    fun reload() {
        consumeData(KoishRegistries.IMG_ATTRIBUTE_MAP::update)
    }

    private fun consumeData(registryAction: (Identifier, ImaginaryAttributeMap) -> Unit) {
        intrusiveRegisteredIds.forEach { registeredId ->
            val entryId = Identifiers.of(registeredId)
            val entryVal = createData(registeredId)
            registryAction(entryId, entryVal)
        }
    }

    private fun createData(id: String): ImaginaryAttributeMap {
        val default = KoishRegistries.ATTRIBUTE_SUPPLIER.getOrThrow(id)
        val data = Reference2ObjectOpenHashMap<Attribute, ImaginaryAttributeInstance>()
        for (attribute in default.attributes) {
            val instance = default.createImaginaryInstance(attribute) ?: continue
            val snapshot = instance.getSnapshot()
            val imaginary = snapshot.toImaginary()
            data[attribute] = imaginary
        }
        return ImaginaryAttributeMapImpl(data)
    }

    private fun createEntry(id: String): RegistryEntry<ImaginaryAttributeMap> {
        val notExisted = intrusiveRegisteredIds.add(id)
        if (!notExisted) throw IllegalArgumentException("The id $id has already been registered!")
        return KoishRegistries.IMG_ATTRIBUTE_MAP.createEntry(id)
    }
}
