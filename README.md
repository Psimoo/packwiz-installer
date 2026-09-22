# packwiz-installer
An installer for launching packwiz modpacks with MultiMC. You'll need [the bootstrapper](https://github.com/comp500/packwiz-installer-bootstrap/releases) to actually use this.

## Outdated files are kept, not deleted

This build never deletes files from your game folder. Whenever the installer would normally
remove a file, it moves it into a `packwiz_Outdated` folder inside the pack folder (the game
directory) instead.

Files are moved there in these situations:

- A file was removed from the pack's index - this is the usual case when a mod is updated and
  the old jar (for example `mods/some-mod-1.0.jar`) is replaced by a newer version.
- A file is for the other side (client/server), or an optional mod that is currently disabled.
- A file's install location changed, so the copy at the old path is no longer used.

Details:

- The path relative to the pack folder is preserved, so `mods/some-mod-1.0.jar` becomes
  `packwiz_Outdated/mods/some-mod-1.0.jar`. This avoids clashes between files with the same name
  in different folders.
- If a file with that name already exists in `packwiz_Outdated`, a number is appended, for example
  `some-mod-1.0 (1).jar`. Existing backups are never overwritten.
- The folder is only created when something actually needs to be moved.
- If a file cannot be moved (for example it is locked by another program on Windows), the installer
  falls back to copying and then deleting the original. If that also fails, the file is left where it
  is and a warning is printed - it is never deleted.
- Moved files are no longer tracked by packwiz, so they will not be touched by later installs. You can
  delete the `packwiz_Outdated` folder at any time to free up disk space; re-installing a mod that was
  moved there simply downloads it again.

## Building

The build produces `build/dist/packwiz-installer.jar`:

```
./gradlew copyJar
```

Note that the Gradle build uses the `com.palantir.git-version` plugin, so it must be run from a git
checkout (a directory containing `.git`).
