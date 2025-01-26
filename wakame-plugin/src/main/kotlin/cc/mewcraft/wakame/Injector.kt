package cc.mewcraft.wakame

import org.koin.core.component.KoinComponent
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.QualifierValue
import org.koin.mp.KoinPlatformTools

// 用于配合 by 快速实现基于 enum 的 Qualifier
class NamedQualifier : Qualifier {
    override val value: QualifierValue
        get() = this.toString()
}

/**
 * I(injection)Q(qualifier).
 *
 * 包含整个 Koish 项目的依赖坐标, 用于区分类型相同但实例不同的依赖.
 */
enum class InjectionQualifier : Qualifier by NamedQualifier() {
    DATA_FOLDER, // Plugin#dataFolder
    CONFIGS_FOLDER, // Plugin#dataFolder/configs
    LANG_FOLDER, // Plugin#dataFolder/lang
    ASSETS_FOLDER // Plugin#dataFolder/assets
}

object Injector : KoinComponent {

    /**
     * @see org.koin.core.Koin.get
     */
    inline fun <reified T : Any> get(
        qualifier: Qualifier? = null,
        noinline parameters: ParametersDefinition? = null,
    ): T {
        return getKoin().get(qualifier, parameters)
    }

    /**
     * @see org.koin.core.Koin.inject
     */
    inline fun <reified T : Any> inject(
        qualifier: Qualifier? = null,
        mode: LazyThreadSafetyMode = KoinPlatformTools.defaultLazyMode(),
        noinline parameters: ParametersDefinition? = null,
    ): Lazy<T> {
        return getKoin().inject(qualifier, mode, parameters)
    }

}