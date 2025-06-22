import com.diffplug.gradle.spotless.FormatExtension

fun FormatExtension.applyCommon(spaces: Int = 4) {
    leadingTabsToSpaces(spaces)
    trimTrailingWhitespace()
    endWithNewline()
}
