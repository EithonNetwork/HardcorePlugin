package se.fredsfursten.plugintools;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigurableFormat {

	private static FileConfiguration fileConfiguration;
	private String _path;
	private int _parameters;
	private String _formatValue;

	public ConfigurableFormat(String path, int parameters, String defaultValue) {
		this._path = path;
		this._parameters = parameters;
		String value = fileConfiguration.getString(path);
		if (value == null) {
			Bukkit.getLogger().warning(String.format(
					"No configuration setting for format with path %s. Will use the default value, \"%s\".",
					path, defaultValue));
			value = defaultValue;
		}

		this._formatValue = value;
	}

	public static void enable(FileConfiguration configuration) {
		fileConfiguration = configuration;
	}

	public String getFormat() {
		return this._formatValue;
	}

	public void reportFailure(CommandSender sender, Exception e) {
		String message = String.format(
				"The %s (\"%s\") from config.yml is not correctly formatted. Verify that the %d parameter(s) are correctly used.",
				this._path, this._formatValue, this._parameters, e.getMessage());
		if (e != null) {
			message = String.format("%s\rThis was the exception message:\r%s", message, e.getMessage());
		}
		Bukkit.getLogger().warning(message);
		if (sender != null) {
			sender.sendMessage(message);
		}
	}

	public boolean sendMessage(CommandSender sender, Object... args) {
		String message = getMessage(sender, args);
		if (message == null) return false;
		sender.sendMessage(message);
		return true;
	}

	public String getMessage(Object... args) {
		try {
			return String.format(this._formatValue, args);
		} catch (Exception e) {
			reportFailure(null, e);
			return null;
		}
	}

	private String getMessage(CommandSender sender, Object... args) {
		try {
			return String.format(this._formatValue, args);
		} catch (Exception e) {
			reportFailure(sender, e);
			return null;
		}
	}
}
