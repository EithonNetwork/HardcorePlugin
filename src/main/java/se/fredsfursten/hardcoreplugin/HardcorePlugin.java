package se.fredsfursten.hardcoreplugin;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

import se.fredsfursten.plugintools.Misc;
import se.fredsfursten.plugintools.PluginConfig;

public final class HardcorePlugin extends JavaPlugin implements Listener {
	private static String hardCoreWorldName;

	@Override
	public void onEnable() {
		Misc.enable(this);
		PluginConfig config = PluginConfig.get(this);
		getServer().getPluginManager().registerEvents(this, this);		
		hardCoreWorldName = config.getString("HardcoreWorldName", "");	
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
		Misc.debugInfo("Enter onPlayerTeleportEvent()");
		Misc.debugInfo("Return now if event was cancelled.");
		if (event.isCancelled()) return;
		Misc.debugInfo("Return now if not in hardcore world.");
		if (!isInHardcoreWorld(event.getTo().getWorld())) return;
		Misc.debugInfo("Return if player can teleport.");
		boolean canTeleport = Hardcore.get().canPlayerTeleport(event.getPlayer(), event.getFrom(), event.getTo());
		if (canTeleport) return;
		Misc.debugInfo("Cancel this teleport event.");
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDeathEvent(PlayerDeathEvent event) {
		Misc.debugInfo("Enter onPlayerDeathEvent()");
		Player player = event.getEntity();
		Misc.debugInfo("Return now if not in hardcore world.");
		if (!isInHardcoreWorld(player.getWorld())) return;
		Misc.debugInfo("Ban this player.");
		Hardcore.get().playerDied(player);
	}

	@EventHandler
	public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
		Misc.debugInfo("Enter onPlayerRespawnEvent()");
		Player player = event.getPlayer();
		Misc.debugInfo("Return now if not in hardcore world.");
		if (!isInHardcoreWorld(player.getWorld())) return;
		Misc.debugInfo("Return if player is not banned.");
		boolean isBanned = Hardcore.get().isBanned(event.getPlayer());
		if (!isBanned) return;
		Hardcore.get().gotoSpawnArea(player);
	}

	private boolean isInHardcoreWorld(World world) {
		if (hardCoreWorldName == null) return false;
		if (hardCoreWorldName.isEmpty()) return false;
		Misc.debugInfo(String.format("World: \"%s\", hardcore=\"%s\".", world.getName(), hardCoreWorldName));
		return world.getName().equalsIgnoreCase(hardCoreWorldName);
	}
}
