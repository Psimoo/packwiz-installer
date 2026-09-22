# packwiz-installer
An installer for launching packwiz modpacks with MultiMC. You'll need [the bootstrapper](https://github.com/comp500/packwiz-installer-bootstrap/releases) to actually use this.

This build never deletes files from your game folder. Whenever the installer would normallyremove a file, it moves it into a `packwiz_Outdated` folder inside the pack folder (the gamedirectory) instead.
这个分支将packwiz要更新的旧mod移动到Packwiz_Outdated文件夹而不是直接删除
