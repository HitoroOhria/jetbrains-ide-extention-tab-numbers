# Show Tab Numbers

Show Tab Numbers is a JetBrains IDE plugin that prefixes editor tab names with their open-tab position.

For example:

- The first open tab is shown as `1. filename`
- The second open tab is shown as `2. filename`

## Local Installation

This plugin is intended for personal local use. Publishing to JetBrains Marketplace is not required.

1. Build the plugin ZIP:

   ```sh
   ./gradlew buildPlugin
   ```

2. Find the generated ZIP:

   ```sh
   ls build/distributions
   ```

3. Install it from your JetBrains IDE:

   <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Gear icon</kbd> > <kbd>Install Plugin from Disk...</kbd>

4. Select the ZIP file under `build/distributions`, then restart the IDE.

## Development Run

To launch a sandbox IDE with the plugin installed:

```sh
./gradlew runIde
```
