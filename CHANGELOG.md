# Changelog

## 1.0.0

- Initial release of the modular Mod Compat addon for NeoForge 1.21.1.
- Hide Swivel's Orbit Camera crosshair while Spatial GUI is actively displaying a 3D screen.
- Make Interactic Renewed's dropped-item tooltip and pickup raycast follow the Swivel orbit-camera direction.
- When right-clicking a targeted dropped item, aim the player directly at that item and synchronize the rotation before Interactic sends its pickup request.
- Keep all target mods optional and all compatibility hooks client-side.
- Add a self-bootstrapping Windows Gradle launcher (`.\\gradlew.bat clean build`).
