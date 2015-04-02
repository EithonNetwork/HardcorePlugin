package se.fredsfursten.hardcoreplugin;

import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
		LocalDateTime bannedUntil = LocalDateTime.now().plusHours(this.bannedFromWorldHours);
		this.bannedPlayers.put(player, bannedUntil);
		this.bannedUntilMessage.sendMessage(player, this.bannedFromWorldHours);
		delayedSave();
	}

	public boolean playerTeleported(Player player, Location from, Location to)
	{
		LocalDateTime bannedUntil = this.bannedPlayers.get(player);
		if (bannedUntil == null) return true;
		long minutesLeft = LocalDateTime.now().until(bannedUntil, ChronoUnit.MINUTES);
		if (minutesLeft < 0) {
			this.bannedPlayers.remove(player);
			delayedSave();
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

	public void unban(Player player) {
		this.bannedPlayers.remove(player);
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
}