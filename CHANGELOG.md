# Changelog

## 1.0.1

- Fix Interactic right-click pickup with Swivel Orbit Camera: synchronize the aimed rotation with the server before the pickup payload is sent.
- Keep the local player rotation restoration and avoid sending aim updates during ordinary tooltip rendering.


## 1.0.0

- Initial release of the modular Mod Compat addon for NeoForge 1.21.1.
- Hide Swivel's Orbit Camera crosshair while Spatial GUI is actively displaying a 3D screen.
- Make Interactic Renewed's dropped-item tooltip and pickup raycast follow Swivel's orbit-camera aim.
- Restore the player rotation immediately after Interactic's raycast.
- Keep all target mods optional and all compatibility hooks client-side.
- Add a self-bootstrapping Windows Gradle launcher (`.\\gradlew.bat clean build`).
