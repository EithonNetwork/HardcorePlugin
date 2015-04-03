package se.fredsfursten.plugintools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class PluginConfig {
	File configFile;
	FileConfiguration config;
	private int doDebugPrint;

	public PluginConfig(JavaPlugin plugin, String fileName) {
		this.configFile = initializeConfigFile(plugin, fileName);
		this.config = new YamlConfiguration();
		load();
		this.doDebugPrint = this.config.getInt("DoDebugPrint");
	}

	private File initializeConfigFile(JavaPlugin plugin, String fileName) {
		File file = new File(plugin.getDataFolder(), fileName);
		if(!file.exists()){
			file.getParentFile().mkdirs();
			copy(plugin.getResource(fileName), file);
		}

		return file;
	}

	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while((len=in.read(buf))>0){
				out.write(buf,0,len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public FileConfiguration getFileConfiguration()
	{
		return this.config;
	}

	public void debugInfo(String format, Object... args) 
	{
		if (this.doDebugPrint == 0) return;
		try {
			Bukkit.getLogger().info(String.format(format, args));
		} catch (Exception e) {
			Bukkit.getLogger().warning(String.format("Wrong format? \"%s\", %d arguments: %s", format, args.length, e.getMessage()));
		}
	}

	public void load()
	{
		try {
			this.config.load(this.configFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void save() {
	    try {
	        this.config.save(this.configFile);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
}
