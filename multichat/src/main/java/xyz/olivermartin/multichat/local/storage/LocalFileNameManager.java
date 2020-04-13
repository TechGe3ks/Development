package xyz.olivermartin.multichat.local.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.PatternSyntaxException;

import xyz.olivermartin.multichat.local.MultiChatLocal;
import xyz.olivermartin.multichat.local.MultiChatLocalPlatform;

/**
 * MultiChatLocal's Name Manager using file based storage as a backend
 * 
 * <p>Manages players' names, nicknames, uuids etc.</p>
 * 
 * @author Oliver Martin (Revilo410)
 *
 */
public abstract class LocalFileNameManager extends LocalNameManager {

	protected Map<UUID,String> mapUUIDNick;
	protected Map<UUID,String> mapUUIDName;
	protected Map<String,UUID> mapNickUUID;
	protected Map<String,UUID> mapNameUUID;

	protected Map<String,String> mapNickFormatted;
	protected Map<String,String> mapNameFormatted;

	protected MultiChatLocalPlatform platform;

	public LocalFileNameManager(MultiChatLocalPlatform platform) {

		super(LocalNameManagerMode.FILE);
		this.platform = platform;
		setDefaultData();

	}

	private void setDefaultData() {

		mapUUIDNick = new HashMap<UUID,String>();
		mapUUIDName = new HashMap<UUID,String>();
		mapNickUUID = new HashMap<String,UUID>();
		mapNameUUID = new HashMap<String,UUID>();

		mapNickFormatted = new HashMap<String,String>();
		mapNameFormatted = new HashMap<String,String>();

	}

	public Map<UUID,String> getMapUUIDName() {
		return mapUUIDName;
	}

	public Map<UUID,String> getMapUUIDNick() {
		return mapUUIDNick;
	}

	public Map<String,UUID> getMapNameUUID() {
		return mapNameUUID;
	}

	public Map<String,UUID> getMapNickUUID() {
		return mapNickUUID;
	}

	public Map<String, String> getMapNameFormatted() {
		return mapNameFormatted;
	}

	public Map<String, String> getMapNickFormatted() {
		return mapNickFormatted;
	}

	//

	public void setMapUUIDName(Map<UUID,String> mapUUIDName) {
		this.mapUUIDName = mapUUIDName;
	}

	public void setMapUUIDNick(Map<UUID,String> mapUUIDNick) {
		this.mapUUIDNick = mapUUIDNick;
	}

	public void setMapNameUUID(Map<String, UUID> mapNameUUID) {
		this.mapNameUUID = mapNameUUID;
	}

	public void setMapNickUUID(Map<String, UUID> mapNickUUID) {
		this.mapNickUUID = mapNickUUID;
	}

	public void setMapNameFormatted(Map<String, String> mapNameFormatted) {
		this.mapNameFormatted = mapNameFormatted;
	}

	public void setMapNickFormatted(Map<String, String> mapNickFormatted) {
		this.mapNickFormatted = mapNickFormatted;
	}

	/**
	 * Returns the FORMATTED NICKNAME of a player if they have one set, otherwise returns their username
	 * 
	 * @param uuid The Unique ID of the player to lookup
	 * @param withPrefix Should the nickname prefix also be returned if it is set?
	 * @return The NICKNAME of the player if it is set, otherwise their username
	 */
	public String getCurrentName(UUID uuid, boolean withPrefix) {

		synchronized (mapUUIDNick) {
			if (mapUUIDNick.containsKey(uuid)) {
				if (MultiChatLocal.getInstance().getConfigManager().getLocalConfig().isShowNicknamePrefix() && withPrefix) {
					return MultiChatLocal.getInstance().getConfigManager().getLocalConfig().getNicknamePrefix() + mapNickFormatted.get(mapUUIDNick.get(uuid));
				} else {
					return mapNickFormatted.get(mapUUIDNick.get(uuid));
				}
			} 
		}

		synchronized (mapUUIDName) {
			if (mapUUIDName.containsKey(uuid)) {
				return mapNameFormatted.get(mapUUIDName.get(uuid));
			}
		}

		return "";
	}

	/**
	 * Returns the username of a player
	 * 
	 * @param uuid The Unique ID of the player to lookup
	 * @return The username of the player
	 */
	public String getName(UUID uuid) {

		synchronized (mapUUIDName) {
			if (mapUUIDName.containsKey(uuid)) {
				return mapNameFormatted.get(mapUUIDName.get(uuid));
			}
		}

		return "";

	}

	/**
	 * Gets the UUID of a player from their UNFORMATTED nickname
	 * THIS MEANS THE NICKNAME PROVIDED MUST BE IN LOWERCASE WITH ALL FORMATTING CODES REMOVED
	 * 
	 * @param nickname The UNFORMATTED nickname of the player
	 * @return An optional which may contain their UUID if the nickname was found in the system
	 */
	protected Optional<UUID> getUUIDFromUnformattedNickname(String nickname) {

		nickname = nickname.toLowerCase();

		synchronized (mapNickUUID) {
			if (mapNickUUID.containsKey(nickname)) {
				return Optional.of(mapNickUUID.get(nickname));
			}
		}

		return Optional.empty();

	}

	/**
	 * Returns a player's UUID given their username
	 * 
	 * @param username The player's username
	 * @return An optional value which may contain their UUID if the username was found
	 */
	public Optional<UUID> getUUIDFromName(String username) {

		username = username.toLowerCase();

		synchronized (mapNameUUID) {
			if (mapNameUUID.containsKey(username)) {
				return Optional.of(mapNameUUID.get(username));
			}
		}

		return Optional.empty();

	}

	/**
	 * Register a player as online
	 * <p>Also performs any setup needed to equip nicknames etc.</p>
	 * @param player
	 */
	public void registerPlayer(UUID uuid, String username) {

		String oldUsername;

		synchronized (mapUUIDName) {

			if (mapUUIDName.containsKey(uuid)) {

				oldUsername = mapUUIDName.get(uuid);

				if (!oldUsername.equalsIgnoreCase(username)) {

					synchronized (mapNameUUID) {

						mapUUIDName.remove(uuid);
						mapUUIDName.put(uuid, username.toLowerCase());
						mapNameUUID.remove(oldUsername);
						mapNameUUID.put(username.toLowerCase(), uuid);

					}

				}

				mapNameFormatted.remove(oldUsername);
				mapNameFormatted.put(username.toLowerCase(), username);

			} else {

				synchronized (mapNameUUID) {

					mapUUIDName.put(uuid, username.toLowerCase());
					mapNameUUID.put(username.toLowerCase(), uuid);
					mapNameFormatted.put(username.toLowerCase(), username);

				}

			}

		}

		online.add(uuid);

	}

	/**
	 * Register a player into the system without them being online
	 * <p>Used mainly for legacy conversion of old nickname file</p>
	 * @param uuid Player's UUID
	 */
	public void registerOfflinePlayerByUUID(UUID uuid, String username) {

		synchronized (mapUUIDName) {

			if (mapUUIDName.containsKey(uuid)) {

				/*
				 * EMPTY : Player does not need registering
				 */

			} else {

				synchronized (mapNameUUID) {

					mapUUIDName.put(uuid, username.toLowerCase());
					mapNameUUID.put(username.toLowerCase(), uuid);
					mapNameFormatted.put(username.toLowerCase(), username);

				}

			}

		}

	}

	/**
	 * Register a player as offline
	 * @param player
	 */
	public void unregisterPlayer(UUID uuid) {

		online.remove(uuid);

	}

	/**
	 * Set the nickname of a player
	 * @param uuid
	 * @param nickname
	 */
	public void setNickname(UUID uuid, String nickname) {

		if (!mapUUIDName.containsKey(uuid)) {
			return;
		}

		if (mapUUIDNick.containsKey(uuid)) {
			removeNickname(uuid);
		}

		String unformattedNickname = stripAllFormattingCodes(nickname.toLowerCase());

		synchronized (mapNickUUID) {

			// Check for duplicates
			if (mapNickUUID.containsKey(unformattedNickname)) {
				if (mapNickUUID.get(unformattedNickname) != uuid) {
					return;
				}
			}

			mapUUIDNick.put(uuid, unformattedNickname);
			mapNickUUID.put(unformattedNickname, uuid);
			mapNickFormatted.put(unformattedNickname, nickname);

		}

	}

	/**
	 * @param username
	 * @return If this player has logged into the server before
	 */
	public boolean existsPlayer(String username) {
		return mapNameUUID.containsKey(username.toLowerCase());
	}

	/**
	 * @param nickname
	 * @return If this nickname is currently in use
	 */
	public boolean existsNickname(String nickname) {
		return mapNickUUID.containsKey(stripAllFormattingCodes(nickname.toLowerCase()));
	}

	/**
	 * Return the UUIDs of players who have nicknames containing characters provided in the nickname argument
	 * @param nickname The characters of the nickname to check
	 * @return An optional which might contain a players UUID if a partial match was found
	 */
	public Optional<Set<UUID>> getPartialNicknameMatches(String nickname) {

		Set<String> nickSet = mapNickUUID.keySet();
		nickname = stripAllFormattingCodes(nickname.toLowerCase());
		Set<UUID> uuidSet = new HashSet<UUID>();

		for (String nick : nickSet) {

			if (nick.startsWith(nickname)) {
				uuidSet.add(mapNickUUID.get(nick));
			}

		}

		if (!uuidSet.isEmpty()) return Optional.of(uuidSet);

		for (String nick : nickSet) {

			if (nick.contains(nickname)) {
				uuidSet.add(mapNickUUID.get(nick));
			}

		}

		if (!uuidSet.isEmpty()) return Optional.of(uuidSet);

		try {
			for (String nick : nickSet) {

				if (nick.matches(nickname)) {
					uuidSet.add(mapNickUUID.get(nick));
				}

			}
		} catch (PatternSyntaxException e) {
			/*
			 * Its not a valid regex, so we will just say there are no matches!
			 */
		}

		if (!uuidSet.isEmpty()) return Optional.of(uuidSet);

		return Optional.empty();

	}

	/**
	 * Return the UUIDs of players who have names containing characters provided in the name argument
	 * @param name The characters of the name to check
	 * @return An optional which might contain a players UUID if a partial match was found
	 */
	public Optional<Set<UUID>> getPartialNameMatches(String name) {

		Set<String> nameSet = mapNameUUID.keySet();
		name = stripAllFormattingCodes(name.toLowerCase());
		Set<UUID> uuidSet = new HashSet<UUID>();

		for (String n : nameSet) {

			if (n.startsWith(name)) {
				uuidSet.add(mapNameUUID.get(n));
			}

		}

		if (!uuidSet.isEmpty()) return Optional.of(uuidSet);

		for (String n : nameSet) {

			if (n.contains(name)) {
				uuidSet.add(mapNameUUID.get(n));
			}

		}

		if (!uuidSet.isEmpty()) return Optional.of(uuidSet);

		try {
			for (String n : nameSet) {

				if (n.matches(name)) {
					uuidSet.add(mapNameUUID.get(n));
				}

			}
		} catch (PatternSyntaxException e) {
			/*
			 * Its not a valid regex, so we will just say there are no matches!
			 */
		}

		if (!uuidSet.isEmpty()) return Optional.of(uuidSet);

		return Optional.empty();

	}

	/**
	 * @param uuid
	 * @return If this player is currently online on the server
	 */
	public boolean isOnline(UUID uuid) {
		return online.contains(uuid);
	}

	/**
	 * Removes the nickname for a specified player
	 * @param uuid
	 */
	public void removeNickname(UUID uuid) {

		synchronized (mapUUIDNick) {

			if (!mapUUIDNick.containsKey(uuid)) {
				return;
			}

			String nickname = mapUUIDNick.get(uuid);

			mapUUIDNick.remove(uuid);
			mapNickUUID.remove(nickname);
			mapNickFormatted.remove(nickname);

		}

	}

}