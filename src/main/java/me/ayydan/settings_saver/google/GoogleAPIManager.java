package me.ayydan.settings_saver.google;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.util.Objects;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import me.ayydan.settings_saver.SettingsSaverClientMod;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;

public class GoogleAPIManager
{
    private static GoogleAPIManager INSTANCE;

    private final Drive googleDriveService;
    private final String configSaveFolderName = "Ayydan's Minecraft Settings Saver";

    private String configSaveFolderID;

    private GoogleAPIManager() throws IOException, GeneralSecurityException
    {
        this.googleDriveService = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(), GoogleAPIGlobals.GSON_FACTORY, this.createCredentials())
                .setApplicationName(GoogleAPIGlobals.APPLICATION_NAME)
                .build();

        this.createConfigSaveFolder();
    }

    public static void initialize() throws IOException, GeneralSecurityException
    {
        if (INSTANCE != null)
        {
            SettingsSaverClientMod.getLogger().warn("The Google API Manager cannot be initialized more than once!");
            return;
        }

        SettingsSaverClientMod.getLogger().info("Initializing Google API Manager...");

        INSTANCE = new GoogleAPIManager();
    }

    private Credential createCredentials() throws IOException, GeneralSecurityException
    {
        InputStream credentialsFileInputStream = GoogleAPIManager.class.getResourceAsStream(GoogleAPIGlobals.CREDENTIALS_FILE);
        if (credentialsFileInputStream == null)
            throw new FileNotFoundException("Failed to open Google credentials file!");

        GoogleClientSecrets googleClientSecrets = GoogleClientSecrets.load(GoogleAPIGlobals.GSON_FACTORY,
                new InputStreamReader(credentialsFileInputStream));

        GoogleAuthorizationCodeFlow authorizationCodeFlow = new GoogleAuthorizationCodeFlow.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GoogleAPIGlobals.GSON_FACTORY, googleClientSecrets, GoogleAPIGlobals.DRIVE_SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(GoogleAPIGlobals.AUTH_TOKENS_DIRECTORY.toFile()))
                .setAccessType("offline")
                .build();

        LocalServerReceiver localServerReceiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();

        return new AuthorizationCodeInstalledApp(authorizationCodeFlow, localServerReceiver, new AuthorizationCodeInstalledApp.DefaultBrowser())
                .authorize("user");
    }

    private void createConfigSaveFolder() throws IOException
    {
        Drive.Files.List fileListRequest = this.googleDriveService.files().list().setQ("mimeType='application/vnd.google-apps.folder' and trashed=false");
        FileList fileList = fileListRequest.execute();
        boolean doesConfigSaveFolderAlreadyExist = false;

        for (File file : fileList.getFiles())
        {
            if (Objects.equal(file.getName(), this.configSaveFolderName))
            {
                this.configSaveFolderID = file.getId();

                doesConfigSaveFolderAlreadyExist = true;
                break;
            }
        }

        if (doesConfigSaveFolderAlreadyExist)
            return;

        File configSaveFolderMetadata = new File();
        configSaveFolderMetadata.setName(this.configSaveFolderName);
        configSaveFolderMetadata.setMimeType("application/vnd.google-apps.folder");

        try
        {
            File configSaveFolder = this.googleDriveService.files().create(configSaveFolderMetadata)
                    .setFields("id")
                    .execute();

            this.configSaveFolderID = configSaveFolder.getId();
        }
        catch (IOException exception)
        {
            SettingsSaverClientMod.getLogger().error("Failed to create the config save folder!");

            exception.printStackTrace();
        }
    }

    public static GoogleAPIManager getInstance()
    {
        return INSTANCE;
    }

    public Drive getDriveService()
    {
        return this.googleDriveService;
    }

    public String getConfigSaveFolderID()
    {
        return this.configSaveFolderID;
    }
}
