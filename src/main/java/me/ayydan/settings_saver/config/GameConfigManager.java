package me.ayydan.settings_saver.config;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import me.ayydan.settings_saver.SettingsSaverClientMod;
import me.ayydan.settings_saver.google.GoogleAPIManager;
import me.ayydan.settings_saver.utils.TimeUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.lingala.zip4j.ZipFile;
import net.minecraft.client.option.GameOptions;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.UUID;

public class GameConfigManager
{
    private final Path fabricConfigDirectory;
    private final java.io.File minecraftOptionsFile;
    private final UUID configSaveUUID;
    private final String configSaveFolderName;

    public GameConfigManager(GameOptions minecraftGameOptions)
    {
        this.fabricConfigDirectory = FabricLoader.getInstance().getConfigDir();
        this.minecraftOptionsFile = minecraftGameOptions.getOptionsFile();
        this.configSaveUUID = UUID.randomUUID();
        this.configSaveFolderName = String.format("Save %s (%s | %s)", this.configSaveUUID, TimeUtils.getCurrentDateString(), TimeUtils.getCurrentTimeString());
    }

    /**
     * Saves all configuration files to the designated folder in the player's Google Drive.
     */
    public void saveToGoogleDrive(Drive googleDriveService)
    {
        String configSaveFolderID = this.createConfigSaveFolder(googleDriveService);

        try (ZipFile configZipFile = new ZipFile(FabricLoader.getInstance().getGameDir() + this.configSaveUUID.toString() + ".zip"))
        {
            configZipFile.addFile(this.minecraftOptionsFile);
            configZipFile.addFolder(this.fabricConfigDirectory.toFile());

            this.uploadConfigZip(googleDriveService, configZipFile.getFile(), configSaveFolderID);

            configZipFile.getFile().delete();
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    @NotNull
    private String createConfigSaveFolder(Drive googleDriveService)
    {
        File configSaveFolderMetadata = new File();
        configSaveFolderMetadata.setName(this.configSaveFolderName);
        configSaveFolderMetadata.setParents(Collections.singletonList(GoogleAPIManager.getInstance().getConfigSaveFolderID()));
        configSaveFolderMetadata.setMimeType("application/vnd.google-apps.folder");

        String configSaveFolderID = null;

        try
        {
            File configSaveFolder = googleDriveService.files().create(configSaveFolderMetadata)
                    .setFields("id")
                    .execute();

            configSaveFolderID = configSaveFolder.getId();
        }
        catch (IOException exception)
        {
            SettingsSaverClientMod.getLogger().error("Failed to create the config save folder!");

            exception.printStackTrace();
        }

        if (configSaveFolderID == null)
            throw new RuntimeException("Failed to get the ID of the config save folder!");

        return configSaveFolderID;
    }

    private void uploadConfigZip(Drive googleDriveService, java.io.File configZipFile, String configSaveFolderID)
    {
        File configZipFileMetadata = new File();
        configZipFileMetadata.setName(configZipFile.getName());
        configZipFileMetadata.setParents(Collections.singletonList(configSaveFolderID));
        configZipFileMetadata.setMimeType("application/zip");

        try
        {
            googleDriveService.files().create(configZipFileMetadata, new FileContent("application/zip", configZipFile))
                    .setFields("id")
                    .execute();
        }
        catch (IOException exception)
        {
            SettingsSaverClientMod.getLogger().error("Failed to upload config ZIP file!");

            exception.printStackTrace();
        }
    }
}
