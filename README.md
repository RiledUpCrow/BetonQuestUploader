# BetonQuest Uploader

This program is both a Spigot plugin and a command line tool. After installing it on a server it listens for incoming BetonQuest packages and, after authenticating the user, puts them in the BetonQuest directory. When running from the command line, it can send the package to the server. It's best to use with BetonQuest Editor.

## As a plugin

Each player can have a single package assigned. Everything he uploads will be placed in this package, even if the name of his upload is different. Every player has his own password used to validate that he's the one sending it.

Put the `BetonQuestUploader.jar` file in your _plugins_ directory and restart/reload your server. Open _config.yml_ file and set the port number. You can also turn on debugging (displaying of errors in the console) and set the maximum number of connections. After saving changes use `/bqureload` command to reload the plugin. Use `/bquadd name password package` command to add users to the _config.yml_ file. You can also edit it manually and use reload command.

## As BetonQuest Editor export tool

To use this program as exporting tool you need set up _export.bat_ (on Windows) or _export.sh_ (on Mac/Linux) script to run this _jar_ file. The syntax is basically `java -jar BetonQuestUploader.jar address:port player password package`. An example script for Windows is in the root of this repository. You need to change first 4 variables to match your settings.

After editing it put the script and the _jar_ file in the same directory as BetonQuest Editor. After running it and creating a package you can press Ctrl+E to upload the package to the server. Assuming all data is correct, the server will display a message in the console.

## Compiling

To compile this program you need JDK 8, Maven and a copy of the source code. Issue `mvn package` command in the source code directory and after a few seconds the _.jar_ file will appear in _target/_ directory.

## License

BetonQuestUploader is licensed under GNU General Public License version 3.