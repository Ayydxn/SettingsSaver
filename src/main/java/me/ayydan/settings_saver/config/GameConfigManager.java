package me.ayydan.settings_saver.config;

import com.google.api.client.http.FileContent;
import com.google.api.client.util.Objects;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import me.ayydan.settings_saver.SettingsSaverClientMod;
import me.ayydan.settings_saver.SettingsSaverGlobals;
import me.ayydan.settings_saver.google.GoogleAPIManager;
import me.ayydan.settings_saver.google.GoogleAPIUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.lingala.zip4j.ZipFile;
import net.minecraft.client.option.GameOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

public class GameConfigManager
{
    private final Path fabricConfigDirectory;
    private final java.io.File minecraftOptionsFile;
    private final String configSaveFolderID;

    private File configZipFile;
    private String configZipFileID;

    public GameConfigManager(GameOptions minecraftGameOptions)
    {
        this.fabricConfigDirectory = FabricLoader.getInstance().getConfigDir();
        this.minecraftOptionsFile = minecraftGameOptions.getOptionsFile();
        this.configSaveFolderID = GoogleAPIManager.getInstance().getConfigSaveFolderID();
        this.configZipFileID = "";
    }

    /**
     * Saves all configuration files to the designated folder in the player's Google Drive.
     */
    public void saveToGoogleDrive(Drive googleDriveService)
    {
        try (ZipFile configZipFile = new ZipFile(SettingsSaverGlobals.CONFIG_ZIP_FILE_NAME))
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

    public void downloadFromGoogleDrive(Drive googleDriveService)
    {
        if (this.configZipFileID.isEmpty())
            throw new IllegalStateException("The config ZIP file cannot be downloaded if it's Drive ID is empty/unknown!");

        try
        {
            ByteArrayOutputStream byteArrayOutputStream = GoogleAPIUtils.downloadZipFile(googleDriveService, this.configZipFileID);
            if (byteArrayOutputStream == null)
                throw new NullPointerException("Failed to download the config ZIP file!");

            Files.write(Path.of(FabricLoader.getInstance().getGameDir() + "/config.zip"), byteArrayOutputStream.toByteArray());
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public boolean doesConfigZipFileExist(Drive googleDriveService)
    {
        boolean doesConfigZipFileAlreadyExist = false;

        try
        {
            String configSaveFolderID = GoogleAPIManager.getInstance().getConfigSaveFolderID();
            Drive.Files.List fileListRequest = googleDriveService.files().list().setQ(String.format("mimeType='application/zip' and trashed=false and parents='%s'", configSaveFolderID));
            FileList fileList = fileListRequest.execute();

            for (File file : fileList.getFiles())
            {
                if (!Objects.equal(file.getName(), SettingsSaverGlobals.CONFIG_ZIP_FILE_NAME))
                    continue;

                this.configZipFile = file;
                this.configZipFileID = file.getId();

                doesConfigZipFileAlreadyExist = true;
                break;
            }
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return doesConfigZipFileAlreadyExist;
    }

    private void uploadConfigZip(Drive googleDriveService, java.io.File configZipFile, String configSaveFolderID)
    {
        File configZipFileMetadata = new File();
        configZipFileMetadata.setName(configZipFile.getName());
        configZipFileMetadata.setMimeType("application/zip");

        FileContent configZipFileContent = new FileContent("application/zip", configZipFile);

        try
        {
            if (this.doesConfigZipFileExist(googleDriveService))
            {
                File updatedConfigZipFile = googleDriveService.files().update(this.configZipFileID, configZipFileMetadata, configZipFileContent)
                        .setFields("id")
                        .setAddParents(configSaveFolderID)
                        .execute();

                this.configZipFile = updatedConfigZipFile;
                this.configZipFileID = updatedConfigZipFile.getId();
            }
            else
            {
                configZipFileMetadata.setParents(Collections.singletonList(configSaveFolderID));

                File createdConfigZipFile = googleDriveService.files().create(configZipFileMetadata, new FileContent("application/zip", configZipFile))
                        .setFields("id")
                        .execute();

                this.configZipFile = createdConfigZipFile;
                this.configZipFileID = createdConfigZipFile.getId();
            }
        }
        catch (IOException exception)
        {
            SettingsSaverClientMod.getLogger().error("Failed to upload config ZIP file!");

            exception.printStackTrace();
        }
    }
}
