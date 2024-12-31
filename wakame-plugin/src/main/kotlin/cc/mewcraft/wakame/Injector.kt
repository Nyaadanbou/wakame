package cc.mewcraft.wakame

import org.koin.core.component.KoinComponent
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.mp.KoinPlatformTools

internal object Injector : KoinComponent {

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