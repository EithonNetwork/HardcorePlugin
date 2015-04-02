package se.fredsfursten.hardcoreplugin;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Commands {
	private static Commands singleton = null;
	private static final String UNBAN_COMMAND = "/hardcore unban <player>";

	private JavaPlugin plugin = null;

	private Commands() {
	}

	static Commands get()
	{
		if (singleton == null) {
			singleton = new Commands();
		}
		return singleton;
	}

	void enable(JavaPlugin plugin){
		this.plugin = plugin;
	}

	void disable() {
	}

	@SuppressWarnings("deprecation")
	void unbanCommand(CommandSender sender, String[] args)
	{
		if (sender instanceof Player) {
			if (!verifyPermission((Player) sender, "hardcore.buy")) return;
		}
		if (!arrayLengthIsWithinInterval(args, 2, 2)) {
			sender.sendMessage(UNBAN_COMMAND);
			return;
		}

		Player player = null;
		try {
			UUID id = UUID.fromString(args[1]);
			player = Bukkit.getPlayer(id);
		} catch (Exception e) {
		}
		if (player == null) player = Bukkit.getPlayer(args[1]);
		if (player == null) {
			sender.sendMessage(String.format("Unknown player: %s", args[1]));
			return;
		}

		Hardcore.get().unban(player);
		sender.sendMessage(String.format("Player %s has been unbanned from the hardcore world.", player.getName()));
	}


	private boolean verifyPermission(Player player, String permission)
	{
		if (player.hasPermission(permission)) return true;
		player.sendMessage("You must have permission " + permission);
		return false;
	}

	private boolean arrayLengthIsWithinInterval(Object[] args, int min, int max) {
		return (args.length >= min) && (args.length <= max);
	}
}
