package se.fredsfursten.hardcoreplugin;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import se.fredsfursten.plugintools.ConfigurableFormat;
import se.fredsfursten.plugintools.PlayerCollection;
import se.fredsfursten.plugintools.SavingAndLoadingBinary;

public class Hardcore {
	private static Hardcore singleton = null;

	private File storageFile;
	private int bannedFromWorldHours;
	private int doDebugPrint;
	private ConfigurableFormat bannedUntilMessage;
	private ConfigurableFormat stillBannedHoursMessage;
	private ConfigurableFormat stillBannedMinutesMessage;
	private PlayerCollection<LocalDateTime> bannedPlayers;

	private JavaPlugin plugin = null;

	private Hardcore() {
	}

	static Hardcore get()
	{
		if (singleton == null) {
			singleton = new Hardcore();
		}
		return singleton;
	}

	void enable(JavaPlugin plugin){
		this.plugin = plugin;
		this.doDebugPrint = this.plugin.getConfig().getInt("DoDebugPrint");
		this.bannedFromWorldHours = this.plugin.getConfig().getInt("BannedFromWorldHours");
		this.bannedUntilMessage = new ConfigurableFormat("BannedUntilMessage", 1,
				"Due to dying in the hardcore world, you have now been banned from this world for %d hours.");
		this.stillBannedHoursMessage = new ConfigurableFormat("StillBannedMinutesMessage", 2,
				"Due to your earlier death in the hardcore world, you are banned for another %d hours and %d minutes.");
		this.stillBannedMinutesMessage = new ConfigurableFormat("StillBannedMinutesMessage", 1,
				"Due to your earlier death in the hardcore world, you are banned for another %d minutes more.");
		this.bannedPlayers = new PlayerCollection<LocalDateTime>();
		this.storageFile = new File(this.plugin.getDataFolder(), "banned.bin");
		delayedLoad();
	}

	void disable() {
	}

	public void playerDied(Player player)
	{
		int hours = ban(player);
		this.bannedUntilMessage.sendMessage(player, hours);
		delayedSave();
	}

	public boolean canPlayerTeleport(Player player, Location from, Location to)
	{
		long minutesLeft = minutesLeftOfBan(player);
		if (minutesLeft <= 0) {
			if (this.doDebugPrint > 0) Bukkit.getLogger().info(String.format("%s is allowed to teleport", player.getName()));
			return true;
		}
		if (minutesLeft < 120) {
			this.stillBannedMinutesMessage.sendMessage(player, minutesLeft);
		} else {
			long hoursLeft = minutesLeft/60;
			long restMinutes = minutesLeft - hoursLeft*60;
			this.stillBannedHoursMessage.sendMessage(player, hoursLeft, restMinutes);
		}
		if (this.doDebugPrint > 0) Bukkit.getLogger().info(String.format("%s is not allowed to teleport", player.getName()));
		return false;
	}

	public int ban(Player player) {
		return ban(player, 0);
	}

	public int ban(Player player, int hours) {
		if (hours <= 0) hours = this.bannedFromWorldHours;
		LocalDateTime bannedUntil = LocalDateTime.now().plusHours(hours);
		this.bannedPlayers.put(player, bannedUntil);
		delayedSave();
		return hours;
	}

	public boolean unban(Player player) {
		if (!isBanned(player)) {
			if (this.doDebugPrint > 0) Bukkit.getLogger().info(String.format("isBanned(%s) == false", player.getName()));
			return false;
		}
		if (this.doDebugPrint > 0) Bukkit.getLogger().info(String.format("Removing %s from bannedPlayers list.", player.getName()));
		this.bannedPlayers.remove(player);
		delayedSave();
		return true;
	}

	public boolean isBanned(Player player) {
		return minutesLeftOfBan(player) > 0;
	}

	private long minutesLeftOfBan(Player player) {
		LocalDateTime bannedUntil = this.bannedPlayers.get(player);
		if (bannedUntil == null) {
			if (this.doDebugPrint > 0) Bukkit.getLogger().info(String.format("%s is not in bannedPlayers list.", player.getName()));
			return 0;
		}
		long minutesLeft = LocalDateTime.now().until(bannedUntil, ChronoUnit.MINUTES);
		if (this.doDebugPrint > 0) Bukkit.getLogger().info(String.format("%s has %d minutes left.", player.getName(), minutesLeft));
		if (minutesLeft <= 0) {
			if (this.doDebugPrint > 0) Bukkit.getLogger().info(String.format("%s is removed from bannedPlayers list.", player.getName()));
			this.bannedPlayers.remove(player);
			delayedSave();
			return 0;
		}
		return minutesLeft;
	}

	private void delayedSave() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				saveNow();
			}
		});
	}

	void saveNow()
	{
		try {
			SavingAndLoadingBinary.save(this.bannedPlayers, this.storageFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void delayedLoad() {
		BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
		scheduler.scheduleSyncDelayedTask(this.plugin, new Runnable() {
			public void run() {
				loadNow();
			}
		}, 200L);
	}

	void loadNow()
	{
		if(!this.storageFile.exists()) return;
		try {
			this.bannedPlayers = SavingAndLoadingBinary.load(this.storageFile);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void list(CommandSender sender) {
		Set<UUID> players = this.bannedPlayers.getPlayers();
		if (players.size() == 0) {
			sender.sendMessage("No players are banned from the hardcore world");
			return;
		}
		for (UUID playerId : players) {
			Player player = Bukkit.getPlayer(playerId);
			if (player != null) {
				LocalDateTime time = this.bannedPlayers.get(playerId);
				sender.sendMessage(String.format("%s: %s", player.getName(), time.toString()));
			}
		}
	}
}