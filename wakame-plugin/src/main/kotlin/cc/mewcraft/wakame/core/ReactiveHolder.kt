package cc.mewcraft.wakame.core

import xyz.xenondevs.commons.provider.Provider

// 为了使 Provider 在 Holder 中的作用更加直观, 用 typealias 重命名
typealias ReactiveRegistryEntry<T> = Provider<T>