# BetonQuest Uploader

This program is both a Spigot plugin and a command line tool. After installing it on a server it listens for incoming BetonQuest packages and, after authenticating the user, puts them in the BetonQuest directory. When running from the command line, it can send the package to the server. It's best to use with [BetonQuest Editor](https://github.com/Co0sh/BetonQuest-Editor).

## As a plugin

Each player can have a single directory assigned. Everything he uploads will be placed in this folder, even if the name of his upload or packages is different. Every player has his own password which is used to validate that he's the one sending quests.

Put the `BetonQuestUploader.jar` file in your _plugins_ directory and restart/reload your server. Open _config.yml_ file and set the port number. You can also turn on debugging (displaying of errors in the console) and set the maximum number of connections. After saving changes use `/bqureload` command to reload the plugin. Use `/bquadd name password directory` command to add users to the _config.yml_ file. You can also edit it manually and use reload command.

After receiving the package, the plugin will unpack it to the player's directory and register new packages in BetonQuest's _config.yml_ file. Then it will reload the plugin so all changes are applied.

## As BetonQuest Editor export tool

To use this program as an exporting tool you have to start it from the command line with a few arguments. The syntax is:

    java -jar BetonQuestUploader.jar address:port player password package.zip

You can also use an exporting script (this is the approach currently used by BetonQuest Editor). Example scripts: [_export.bat_](export.bat) (on Windows) and [_export.sh_](export.sh) (on Mac/Linux). Remember to edit the variables on top to match your server and user credentials. Put the script and _BetonQuestUploader.jar_ in the same directory as BetonQuest Editor. After creating/loading a package you can press `Ctrl+E` to upload the package to the server.

## Download and compiling

You can download compiled binaries on the [Releases](https://github.com/Co0sh/BetonQuestUploader/releases) page.

To compile this program you need JDK 8, Maven and a copy of the source code. Issue `mvn package` command in the source code directory and after a few seconds the _.jar_ file will appear in _target/_ directory.

## License

BetonQuestUploader is licensed under GNU General Public License version 3.