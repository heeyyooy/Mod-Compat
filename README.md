# Mod Compat

A modular, client-side compatibility addon for Minecraft **1.21.1 / NeoForge 21.1.x**. Compatibility fixes for different mods are collected in this project and shipped in one common JAR. New integrations can be added without creating a separate addon for each pair of mods.

## Current integrations

- **Swivel + Spatial GUI** — hides Swivel's Orbit Camera crosshair while a 3D screen is active in Spatial GUI. The crosshair remains available during normal gameplay.
- **Swivel + Interactic Renewed** — makes Interactic's dropped-item tooltip and right-click pickup raycast follow the Swivel orbit-camera direction. Before sending a successful pickup request, it turns the player toward the exact targeted item and synchronizes that rotation with the server.

## Target versions

- Minecraft 1.21.1
- NeoForge 21.1.x
- Swivel `1.2.0+mc1.21-1.21.1`
- Interactic Renewed `0.2.3+1.21.1`
- Spatial GUI `1.11.1-hotfix+1.21.1`

Swivel, Interactic Renewed and Spatial GUI are optional. Install only the mods for which you need compatibility fixes. The addon is client-side and should not be installed on a dedicated server.

## Installation

1. Install Minecraft 1.21.1 with NeoForge 21.1.x.
2. Install the original mods whose compatibility fixes you want to use.
3. Put `Mod-Compat-NeoForge-1.21.1-1.0.0.jar` in the client's `mods` folder.
4. Launch the game.

## Build on Windows

Requirements: **64-bit Java 21** and an internet connection for the initial Gradle distribution and dependency downloads. A separate Gradle installation is not required.

Run this command in the project folder from PowerShell:

```powershell
.\gradlew.bat clean build
```

On the first run, `gradlew.bat` downloads Gradle 9.2.1 into the user's `.gradle` cache. Later builds reuse that local copy. The `build.bat` file is an optional shortcut that runs the same build.

The JAR is generated at:

```text
build\libs\Mod-Compat-NeoForge-1.21.1-1.0.0.jar
```

## Adding more compatibility fixes

Keep new integrations inside this project instead of creating a separate JAR for each mod pair:

- Put new Mixin classes in `src/main/java/ru/heeyyooy/modcompat/mixin/`.
- Put optional-mod reflection bridges and helper classes in `src/main/java/ru/heeyyooy/modcompat/compat/`.
- Register new Mixin classes in `src/main/resources/modcompat.mixins.json`.
- Add optional mod metadata dependencies to `src/main/resources/META-INF/neoforge.mods.toml` when appropriate.
- Update the integration list in this README.

The common artifact name stays `Mod-Compat-NeoForge-1.21.1-<version>.jar`; increase `mod_version` in `gradle.properties` for each release.

## Technical notes

- Swivel and Spatial GUI APIs are accessed reflectively, so neither is a compile-time dependency.
- Interactic's `Helpers` class is targeted through a `@Pseudo` Mixin, so Interactic is also optional.
- All compatibility Mixins are client-only.
- The addon does not overwrite or replace classes in the original mods.

## License

MIT. See [LICENSE](LICENSE).

---

# Mod Compat (Русский)

Модульный клиентский аддон совместимости для **Minecraft 1.21.1 / NeoForge 21.1.x**. Исправления совместимости разных модов собираются в одном проекте и распространяются одним JAR-файлом. Новые интеграции можно добавлять сюда, не создавая отдельный аддон для каждой пары модов.

## Реализованные интеграции

- **Swivel + Spatial GUI** — скрывает прицел Orbit Camera от Swivel, пока в Spatial GUI открыт 3D-интерфейс. В обычном игровом мире прицел продолжает работать.
- **Swivel + Interactic Renewed** — заставляет подсказку выброшенного предмета и подбор правой кнопкой мыши использовать направление камеры Swivel. Перед отправкой запроса на подбор персонаж поворачивается точно к выбранному предмету, а его поворот синхронизируется с сервером.

## Целевые версии

- Minecraft 1.21.1
- NeoForge 21.1.x
- Swivel `1.2.0+mc1.21-1.21.1`
- Interactic Renewed `0.2.3+1.21.1`
- Spatial GUI `1.11.1-hotfix+1.21.1`

Swivel, Interactic Renewed и Spatial GUI — необязательные зависимости. Устанавливай только те исходные моды, для которых нужны исправления. Аддон работает на клиенте, устанавливать его на выделенный сервер не нужно.

## Установка

1. Установи Minecraft 1.21.1 с NeoForge 21.1.x.
2. Установи исходные моды, совместимость с которыми нужна.
3. Помести `Mod-Compat-NeoForge-1.21.1-1.0.0.jar` в папку `mods` клиента.
4. Запусти игру.

## Сборка в Windows

Требуются **64-битная Java 21** и подключение к интернету для первоначальной загрузки Gradle и зависимостей. Устанавливать Gradle отдельно не требуется.

Открой PowerShell в папке проекта и выполни:

```powershell
.\gradlew.bat clean build
```

При первом запуске `gradlew.bat` скачает Gradle 9.2.1 в локальный кэш `.gradle` пользователя. При следующих сборках будет использоваться уже загруженная версия. Файл `build.bat` — дополнительный ярлык для запуска той же команды.

Готовый JAR появится здесь:

```text
build\libs\Mod-Compat-NeoForge-1.21.1-1.0.0.jar
```

## Добавление новых исправлений совместимости

Новые интеграции следует добавлять в этот проект, а не создавать отдельный JAR для каждой пары модов:

- Новые Mixin-классы размещай в `src/main/java/ru/heeyyooy/modcompat/mixin/`.
- Мосты для необязательных модов и вспомогательный код размещай в `src/main/java/ru/heeyyooy/modcompat/compat/`.
- Регистрируй Mixin-классы в `src/main/resources/modcompat.mixins.json`.
- При необходимости добавляй необязательные зависимости в `src/main/resources/META-INF/neoforge.mods.toml`.
- Обновляй список интеграций в этом README.

Общее имя JAR останется `Mod-Compat-NeoForge-1.21.1-<версия>.jar`. Для каждого нового выпуска увеличивай `mod_version` в `gradle.properties`.

## Технические примечания

- API Swivel и Spatial GUI вызываются через reflection, поэтому они не являются обязательными зависимостями при компиляции.
- Класс `Helpers` Interactic подключается через `@Pseudo` Mixin, поэтому Interactic также остаётся необязательным.
- Все интеграционные Mixin-классы работают только на клиенте.
- Аддон не заменяет и не перезаписывает классы исходных модов.

## Лицензия

MIT. См. [LICENSE](LICENSE).
