# Koish (Wakame) — Repository Custom Instructions

## Project Overview

Koish (codename: wakame) is a large-scale Minecraft Paper plugin written in **Kotlin**, providing a custom item system with properties, behaviors, skills, enchantments, damage calculation, and many third-party integrations (hooks). It runs on **Paper 1.21+** servers.

## Module Structure

| Module | Description |
|---|---|
| `buildSrc` | Gradle build conventions. |
| `common/lazyconfig` | Configuration framework (`MAIN_CONFIG`, `entryOrElse`, `optionalEntry`). |
| `common/messaging` | Cross-server messaging utilities. |
| `standalone/*` | Standalone sub-plugins (economy, cron-scheduler, etc.). |
| `wakame-api` | Public API surface exposed to other plugins. |
| `wakame-mixin` | Core abstractions & interfaces (ItemBehavior, ItemProp, CastableTrigger, etc.). Compiled as a Mixin library loaded at server boot. **No plugin logic here.** |
| `wakame-plugin` | Plugin implementation. Feature logic, config reading, event listeners, tick systems, and all runtime behavior live here. |
| `wakame-hooks/*` | Third-party plugin integrations (one sub-module per hook). |

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
