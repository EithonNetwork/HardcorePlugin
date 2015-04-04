package se.fredsfursten.hardcoreplugin;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import se.fredsfursten.plugintools.ConfigurableFormat;
import se.fredsfursten.plugintools.PluginConfig;

public final class HardcorePlugin extends JavaPlugin implements Listener {
	private static PluginConfig configuration;
	private static String hardCoreWorldName;
	private static int doDebugPrint;

	@Override
	public void onEnable() {
		if (configuration == null) {
			configuration = new PluginConfig(this, "config.yml");
		} else {
			configuration.load();
		}
		ConfigurableFormat.enable(getPluginConfig());
		getServer().getPluginManager().registerEvents(this, this);		
		hardCoreWorldName = getConfig().getString("HardcoreWorldName");	
		doDebugPrint = getConfig().getInt("DoDebugPrint");
		Hardcore.get().enable(this);
		Commands.get().enable(this);
	}

	@Override
	public void onDisable() {
		Hardcore.get().disable();
		Commands.get().disable();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1) {
			sender.sendMessage("Incomplete command...");
			return false;
		}

		String command = args[0].toLowerCase();
		if (command.equals("ban")) {
			Commands.get().banCommand(sender, args);
		} else if (command.equals("unban")) {
			Commands.get().unbanCommand(sender, args);
		} else if (command.equals("list")) {
			Commands.get().listCommand(sender, args);
		} else {
			sender.sendMessage("Could not understand command.");
			return false;
		}
		return true;
	}

	@EventHandler
	public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
		if (doDebugPrint > 0) Bukkit.getLogger().info("Enter onPlayerTeleportEvent()");
		if (doDebugPrint > 0) Bukkit.getLogger().info("Return now if event was cancelled.");
		if (event.isCancelled()) return;
		if (doDebugPrint > 0) Bukkit.getLogger().info("Return now if not in hardcore world.");
		if (!isInHardcoreWorld(event.getTo().getWorld())) return;
		if (doDebugPrint > 0) Bukkit.getLogger().info("Return if player can teleport.");
		boolean canTeleport = Hardcore.get().canPlayerTeleport(event.getPlayer(), event.getFrom(), event.getTo());
		if (canTeleport) return;
		if (doDebugPrint > 0) Bukkit.getLogger().info("Cancel this teleport event.");
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		if (doDebugPrint > 0) Bukkit.getLogger().info("Enter onPlayerDeathEvent()");
		Player player = event.getEntity();
		if (doDebugPrint > 0) Bukkit.getLogger().info("Return now if not in hardcore world.");
		if (!isInHardcoreWorld(player.getWorld())) return;
		if (doDebugPrint > 0) Bukkit.getLogger().info("Ban this player.");
		Hardcore.get().playerDied(player);
	}

	private boolean isInHardcoreWorld(World world) {
		if (hardCoreWorldName == null) return false;
		if (hardCoreWorldName.isEmpty()) return false;
		if (doDebugPrint > 0) Bukkit.getLogger().info(String.format("World: \"%s\", hardcore=\"%s\".", world.getName(), hardCoreWorldName));
		return world.getName().equalsIgnoreCase(hardCoreWorldName);
	}

	public static FileConfiguration getPluginConfig()
	{
		return configuration.getFileConfiguration();
	}
	
	public static void debugInfo(String format, Object... args) 
	{
		configuration.debugInfo(format, args);
	}
	
	public static void reloadConfiguration()
	{
		configuration.load();
	}
}
