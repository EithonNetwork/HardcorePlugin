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
		this.bannedFromWorldHours = this.plugin.getConfig().getInt("BannedFromWorldHours");
		this.bannedUntilMessage = new ConfigurableFormat("BannedUntilMessage", 1,
				"Due to dying in the hardcore world, you have now been banned from this world for %d hours.");
		this.stillBannedHoursMessage = new ConfigurableFormat("StillBannedMinutesMessage", 2,
				"Due to your earlier death in the hardcore world, you are banned for another %d hours and %d minutes.");
		this.stillBannedMinutesMessage = new ConfigurableFormat("StillBannedMinutesMessage", 1,
				"Due to your earlier death in the hardcore world, you are banned for another %d minutes more.");
		this.bannedPlayers = new PlayerCollection<LocalDateTime>();
		this.storageFile = new File(this.plugin.getDataFolder(), "donations.bin");
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

	public boolean playerTeleported(Player player, Location from, Location to)
	{
		LocalDateTime bannedUntil = this.bannedPlayers.get(player);
		if (bannedUntil == null) return true;
		long minutesLeft = LocalDateTime.now().until(bannedUntil, ChronoUnit.MINUTES);
		if (minutesLeft < 0) {
			unban(player);
			return true;
		}
		if (minutesLeft < 120) {
			this.stillBannedMinutesMessage.sendMessage(player, minutesLeft);
		} else {
			long hoursLeft = minutesLeft/60;
			long restMinutes = minutesLeft - hoursLeft*60;
			this.stillBannedHoursMessage.sendMessage(player, hoursLeft, restMinutes);
		}
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

	public void unban(Player player) {
		this.bannedPlayers.remove(player);
		delayedSave();
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
		if (true) return;
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
		if (true) return;
		if(!this.storageFile.exists()) return;
		try {
			this.bannedPlayers = SavingAndLoadingBinary.load(this.storageFile);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void info(CommandSender sender) {
		Set<UUID> players = this.bannedPlayers.getPlayers();
		if (players.size() == 0) {
			sender.sendMessage("No players are banned from the hardcore world");
			return;
		}
		for (UUID playerId : players) {
			Player player = Bukkit.getPlayer(playerId);
			LocalDateTime time = this.bannedPlayers.get(player);
			sender.sendMessage(String.format("%s: %s", player.getName(), time.toString()));
		}
	}
}