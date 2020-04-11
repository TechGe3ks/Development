package xyz.olivermartin.multichat.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.UUID;

public class LocalSpigotNicknameFile extends LocalNicknameFile {

	public LocalSpigotNicknameFile(File configPath, String fileName) {
		super(configPath, fileName, MultiChatLocalPlatform.SPIGOT);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected boolean loadFile(File file) {

		FileInputStream fileInputStream;

		try {

			fileInputStream = new FileInputStream(file);

			LocalNameManager nameManager = MultiChatLocal.getInstance().getNameManager();

			if (nameManager.getMode() == LocalNameManagerMode.FILE) {

				LocalFileNameManager fileNameManager = (LocalFileNameManager) nameManager;

				ObjectInputStream in = new ObjectInputStream(fileInputStream);

				Map<UUID, String> mapUUIDNick = (Map<UUID, String>) in.readObject();
				Map<UUID, String> mapUUIDName = (Map<UUID, String>) in.readObject();
				Map<String, UUID> mapNickUUID = (Map<String, UUID>) in.readObject();
				Map<String, UUID> mapNameUUID = (Map<String, UUID>) in.readObject();
				Map<String, String> mapNickFormatted = (Map<String, String>) in.readObject();
				Map<String, String> mapNameFormatted = (Map<String, String>) in.readObject();

				in.close();

				fileNameManager.setMapUUIDNick(mapUUIDNick);
				fileNameManager.setMapUUIDName(mapUUIDName);
				fileNameManager.setMapNickUUID(mapNickUUID);
				fileNameManager.setMapNameUUID(mapNameUUID);
				fileNameManager.setMapNickFormatted(mapNickFormatted);
				fileNameManager.setMapNameFormatted(mapNameFormatted);

				fileInputStream.close();

				return true;

			} else {

				fileInputStream.close();
				// Cannot load as not in file based mode!
				return false;

			}

		} catch (IOException | ClassNotFoundException e) {

			return false;

		}
	}

	@Override
	protected boolean saveFile(File file) {

		FileOutputStream fileOutputStream;

		try {

			LocalNameManager nameManager = MultiChatLocal.getInstance().getNameManager();

			fileOutputStream = new FileOutputStream(file);

			if (nameManager.getMode() == LocalNameManagerMode.FILE) {

				LocalFileNameManager fileNameManager = (LocalFileNameManager) nameManager;

				ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);

				out.writeObject(fileNameManager.getMapUUIDNick());
				out.writeObject(fileNameManager.getMapUUIDName());
				out.writeObject(fileNameManager.getMapNickUUID());
				out.writeObject(fileNameManager.getMapNameUUID());
				out.writeObject(fileNameManager.getMapNickFormatted());
				out.writeObject(fileNameManager.getMapNameFormatted());

				out.close();

				return true;

			} else {

				fileOutputStream.close();
				// Cannot save as not in file based mode!
				return false;

			}

		} catch (IOException e) {

			return false;

		}

	}

}
