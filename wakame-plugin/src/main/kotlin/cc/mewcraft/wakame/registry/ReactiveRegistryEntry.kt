package cc.mewcraft.wakame.registry

import xyz.xenondevs.commons.provider.Provider

// 为了使 Provider 在 RegistryEntry 中的作用更加直观, 用 typealias 重命名
typealias ReactiveRegistryEntry<T> = Provider<T>