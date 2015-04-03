package se.fredsfursten.hardcoreplugin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import se.fredsfursten.plugintools.Misc;

public class Commands {
	private static Commands singleton = null;
	private static final String BAN_COMMAND = "/hardcore ban <player> [<hours>]";
	private static final String UNBAN_COMMAND = "/hardcore unban <player>";
	private static final String INFO_COMMAND = "/hardcore info";

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

	public void banCommand(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			if (!verifyPermission((Player) sender, "hardcore.ban")) return;
		}
		if (!arrayLengthIsWithinInterval(args, 2, 3)) {
			sender.sendMessage(BAN_COMMAND);
			return;
		}

		Player player = Misc.getPlayerFromString(args[1]);
		if (player == null) {
			sender.sendMessage(String.format("Unknown player: %s", args[1]));
			return;
		}

		int hours = 0;
		if (args.length > 2) hours = Integer.parseInt(args[2]);

		hours = Hardcore.get().ban(player, hours);
		sender.sendMessage(String.format("Player %s has now been banned from the hardcore world for %d hours.",
				player.getName(), hours));
	}


	public void infoCommand(CommandSender sender, String[] args) {
		if (sender instanceof Player) {
			if (!verifyPermission((Player) sender, "hardcore.info")) return;
		}
		if (!arrayLengthIsWithinInterval(args, 1, 1)) {
			sender.sendMessage(INFO_COMMAND);
			return;
		}

		Hardcore.get().info(sender);
	}

	void unbanCommand(CommandSender sender, String[] args)
	{
		if (sender instanceof Player) {
			if (!verifyPermission((Player) sender, "hardcore.unban")) return;
		}
		if (!arrayLengthIsWithinInterval(args, 2, 2)) {
			sender.sendMessage(UNBAN_COMMAND);
			return;
		}

		Player player = Misc.getPlayerFromString(args[1]);
		if (player == null) {
			sender.sendMessage(String.format("Unknown player: %s", args[1]));
			return;
		}

		boolean wasReallyUnbanned = Hardcore.get().unban(player);

		if (wasReallyUnbanned) {
			sender.sendMessage(String.format("Player %s has been unbanned from the hardcore world.", player.getName()));
		} else {
			sender.sendMessage(String.format("Player %s is not banned in the hardcore world.", player.getName()));
		}
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
