/**
 * BetonQuestUploader - a server handling quest package uploading
 * Copyright (C) 2016  Jakub Sapalski
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package pl.betoncraft.betonquestuploader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Spigot plugin listens on one of the ports for incoming .zip files,
 * authenticates the uploader, checks if the .zip contains a BetonQuest package
 * and installs it in the BetonQuest plugin.
 *
 * @author Jakub Sapalski
 */
public class Receiver extends JavaPlugin implements CommandExecutor {
	
	public final static int DEFAULT_PORT = 8125;
	public final static int DEFAULT_MAX_CONNECTIONS = 12;
	
	private HttpServer server;
	private boolean debug;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		getCommand("bqureload").setExecutor(this);
		getCommand("bquadd").setExecutor(this);
		int port = getConfig().getInt("port", DEFAULT_PORT);
		int maxConnections = getConfig().getInt("max_connections", DEFAULT_MAX_CONNECTIONS);
		debug = getConfig().getBoolean("debug", false);
		try {
			server = HttpServer.create(new InetSocketAddress(port), maxConnections);
			server.createContext("/", exchange -> {
	        	try {
					boolean accepted = receive(exchange);
					String response = accepted ? "ACCEPT" : "REJECT";
					exchange.sendResponseHeaders(200, response.length());
					OutputStream os = exchange.getResponseBody();
					os.write(response.getBytes());
					os.close();
				} catch (Exception e) {
					getLogger().warning("Something went wrong with a request.");
					if (debug) {
						e.printStackTrace();
					}
				}
			});
	        server.setExecutor(null);
	        server.start();
		} catch (IOException e) {
			getLogger().severe("Something happend while setting up receiver, the plugin will be disabled.");
			setEnabled(false);
			if (debug) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onDisable() {
		if (server != null) {
			server.stop(0);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("bqureload")) {
			onDisable();
			onEnable();
			sender.sendMessage("§2Reloaded!");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("bquadd")) {
			if (args.length != 3) {
				sender.sendMessage("§c/bquadd <name> <password> <package>");
				return true;
			}
			String player = args[0];
			String password = args[1];
			String pack = args[2];
			getConfig().set("users." + player + ".password", password);
			getConfig().set("users." + player + ".package", pack);
			saveConfig();
			sender.sendMessage("§2Player added!");
			return true;
		}
		return false;
	}

	private boolean receive(HttpExchange exchange) throws IOException {
		if (!exchange.getProtocol().equals("HTTP/1.1")) {
			return false;
		}
		if (!exchange.getRequestMethod().equals("POST")) {
			return false;
		}
		String contentType = exchange.getRequestHeaders().getFirst("Content-type");
		String[] typeParts = contentType.split(Pattern.quote(";"));
		if (typeParts.length != 2) {
			return false;
		}
		if (!typeParts[0].trim().equals("multipart/form-data")) {
			return false;
		}
		if (!typeParts[1].trim().startsWith("boundary=")) {
			return false;
		}
		String boundary = typeParts[1].trim().substring(9);
		// data
		String user = null;
		String pass = null;
		String tempName = "temp-" + new Random().nextInt(1024*1024) + ".zip";
		File file = new File(getDataFolder(), tempName);
		file.createNewFile();
		OutputStream out = new FileOutputStream(file);
		// reading
		InputStream stream = exchange.getRequestBody();
		String type = null;
		while (true) {
			byte[] bytes = new byte[1024*1024];
			int b;
			int length = 0;
			while ((b = stream.read()) != -1) {
				bytes[length] = (byte) b;
				length++;
				if (b == '\n') {
					break;
				}
			}
			if (length == 0) {
				break;
			}
			String line = new String(bytes, 0, length).trim();
			if (line.equals("--" + boundary + "--")) {
				break;
			}
			if (line.equals("--" + boundary)) {
				type = "wait";
				continue;
			}
			if (type == null) {
				continue;
			}
			switch (type) {
			case "wait":
				if (line.startsWith("Content-Disposition: form-data")) {
					String[] dispositionParts = line.split(Pattern.quote(";"));
					if (dispositionParts.length != 2 && dispositionParts.length != 3) {
						continue;
					}
					if (!dispositionParts[1].trim().startsWith("name=")) {
						continue;
					}
					String checkType = dispositionParts[1].trim().substring(5).replace("\"", "");
					if (!Arrays.asList(new String[]{"user", "pass", "file"}).contains(checkType)) {
						continue;
					}
					type = checkType;
				}
				continue;
			case "user":
				user = line;
				continue;
			case "pass":
				pass = line;
				continue;
			case "file":
				if (line.equals("Content-Type: application/octet-stream")) {
					type = "stream";
				}
				continue;
			case "stream":
				out.write(bytes, 0, length);
				continue;
			}
		}
		out.close();
		stream.close();
		if (pass == null || user == null) {
			return false;
		}
		String password = getConfig().getString("users." + user + ".password");
		if (password == null) {
			return false;
		}
		if (!pass.equals(password)) {
			return false;
		}
		String pack = getConfig().getString("users." + user + ".package");
		if (pack == null) {
			return false;
		}
		ZipFile zip = new ZipFile(file);
		File betonDir = new File(getDataFolder().getParentFile(), "BetonQuest");
		File oldPack = new File(betonDir, pack);
		if (oldPack.exists()) {
			deleteFiles(oldPack);
		}
		oldPack.mkdir();
		Enumeration<? extends ZipEntry> entries = zip.entries();
		ZipEntry entry;
		while (entries.hasMoreElements() && (entry = entries.nextElement()) != null) {
			String entryName = entry.getName().replace('\\', File.separatorChar).replace('/', File.separatorChar);
			if (!entryName.endsWith(".yml")) {
				continue;
			}
			int first = entryName.indexOf(File.separatorChar);
			int last = entryName.lastIndexOf(File.separatorChar);
            String filePath = oldPack.getPath() + File.separatorChar + entryName.substring(first + 1);
            String dirPath = first == last ? "" : 
            		oldPack.getPath() + File.separatorChar + entryName.substring(first + 1, last);
            File packDir = new File(dirPath);
            packDir.mkdirs();
            if (!entry.isDirectory()) {
            	BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
                byte[] bytesIn = new byte[1024];
                int read = 0;
                InputStream in = zip.getInputStream(entry);
                while ((read = in.read(bytesIn)) != -1) {
                    bos.write(bytesIn, 0, read);
                }
                bos.close();
                in.close();
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdirs();
            }
        }
		zip.close();
		file.delete();
		getLogger().info(user + " uploaded his package.");
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "betonquest reload");
		return true;
	}
	
	private void deleteFiles(File dir) {
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) {
				deleteFiles(file);
			}
			file.delete();
		}
	}
	
}
