# Koish (Wakame) — Repository Custom Instructions

## Project Overview

Koish (codename: wakame) is a large-scale Minecraft Paper plugin written in **Kotlin**, providing a custom item system with properties, behaviors, skills, enchantments, damage calculation, and many third-party integrations (hooks). It runs on **Paper 1.21+** servers.

## Module Structure

|Module|Description|
|---|---|
|`buildSrc`|Gradle build conventions.|
|`common/lazyconfig`|Configuration framework (`MAIN_CONFIG`, `entryOrElse`, `optionalEntry`).|
|`common/messaging`|Cross-server messaging utilities.|
|`standalone/*`|Standalone sub-plugins (economy, cron-scheduler, etc.).|
|`wakame-api`|Public API surface exposed to other plugins.|
|`wakame-mixin`|NMS Mixin patches (Java) + Bridge interfaces (Kotlin). Built with Horizon + Weaver. **No game logic here.**|
|`wakame-plugin`|Plugin implementation. All game logic lives here: item system (ItemBehavior, ItemProp, CastableTrigger), config reading, event listeners, tick systems, and runtime behavior.|
|`wakame-hooks/*`|Third-party plugin integrations (one sub-module per hook).|

## Build System

- **Gradle** with Kotlin DSL (`build.gradle.kts`)
- Version catalog: `gradle/local.versions.toml`
- Root settings: `settings.gradle.kts`

## Key Conventions

- **Language**: Kotlin (JVM 21)
- **Config format**: YAML, deserialized via Configurate with `@ConfigSerializable` + `NamingSchemes.SNAKE_CASE` (camelCase Kotlin properties ↔ snake_case YAML keys automatically).
- **Main config file**: `wakame-plugin/src/main/resources/configs/config.yml`
- **Comments**: Code comments and config comments are written in **Chinese (中文)**.
- **KDoc**: Public APIs should have KDoc. Comments use Chinese.
- **Trailing whitespace**: Files must end with exactly one newline (`\n`). Do NOT leave extra blank lines at the end of a file.
- **Git commits**: Follow Conventional Commits format. See `.github/git-commit-instructions.md`.
