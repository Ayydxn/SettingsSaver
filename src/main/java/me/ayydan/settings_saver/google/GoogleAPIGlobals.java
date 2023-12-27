package me.ayydan.settings_saver.google;

import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.common.collect.Lists;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;
import java.util.List;

public class GoogleAPIGlobals
{
    public static final List<String> DRIVE_SCOPES = Lists.newArrayList(DriveScopes.DRIVE_FILE);

    public static final GsonFactory GSON_FACTORY = GsonFactory.getDefaultInstance();

    public static final Path AUTH_TOKENS_DIRECTORY = FabricLoader.getInstance().getGameDir().resolve("auth_tokens");

    public static final String CREDENTIALS_FILE = "/assets/settings_saver/config/credentials.json";
    public static final String APPLICATION_NAME = "Ayydan's Minecraft Settings Saver";
}
