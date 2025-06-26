package cc.mewcraft.wakame.util.test

import java.nio.file.Path

@TestOnly
enum class TestPath(
    val testRootPath: Path,
) {
    MAIN(Path.of("src/main/resources")),
    TEST(Path.of("src/test/resources")),
}