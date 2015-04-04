package se.fredsfursten.hardcoreplugin;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.bukkit.entity.Player;

public class BannedPlayer implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private UUID _id;
	private String _name;
	private LocalDateTime _bannedToTime;

	public BannedPlayer(Player player, int bannedHours) {
		this._id = player.getUniqueId();
		this._name = player.getName();
		this._bannedToTime = LocalDateTime.now().plusHours(bannedHours);
	}

	public long getMinutesLeft() {
		return LocalDateTime.now().until(this._bannedToTime, ChronoUnit.MINUTES);
	}

	public String getName() {
		return this._name;
	}

	public UUID getUniqueId() {
		return this._id;
	}
}
