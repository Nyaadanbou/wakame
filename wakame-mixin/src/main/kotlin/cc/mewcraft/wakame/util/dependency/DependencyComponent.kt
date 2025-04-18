package cc.mewcraft.wakame.util.dependency

interface DependencyComponent<T : Any> {
    val component: T
    val dependenciesBefore: List<T>
    val dependenciesAfter: List<T>
}