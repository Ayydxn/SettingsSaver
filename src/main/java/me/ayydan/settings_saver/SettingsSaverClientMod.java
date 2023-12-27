package me.ayydan.settings_saver;

import me.ayydan.settings_saver.config.GameConfigManager;
import me.ayydan.settings_saver.google.GoogleAPIGlobals;
import me.ayydan.settings_saver.google.GoogleAPIManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

@Environment(EnvType.CLIENT)
public class SettingsSaverClientMod implements ClientModInitializer
{
    private static SettingsSaverClientMod INSTANCE;

    private static final Logger LOGGER = (Logger) LogManager.getLogger("Settings Saver");
    private static final String MOD_ID = "settings_saver";

    private GameConfigManager gameConfigManager;

    private String settingsSaverVersion;

    @Override
    public void onInitializeClient()
    {
        INSTANCE = this;

        ModContainer settingsSaverModContainer = FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .orElseThrow(NullPointerException::new);

        this.settingsSaverVersion = settingsSaverModContainer.getMetadata().getVersion().getFriendlyString();

        LOGGER.info("Initializing Settings Saver... (Version: {})", this.settingsSaverVersion);

        try
        {
            if (!Files.exists(GoogleAPIGlobals.AUTH_TOKENS_DIRECTORY))
                Files.createDirectories(GoogleAPIGlobals.AUTH_TOKENS_DIRECTORY);

            GoogleAPIManager.initialize();
        }
        catch (IOException | GeneralSecurityException exception)
        {
            SettingsSaverClientMod.getLogger().error("Failed to create Google auth tokens directory!");

            exception.printStackTrace();
        }

        ClientLifecycleEvents.CLIENT_STARTED.register((client) ->
        {
            this.gameConfigManager = new GameConfigManager(client.options);
            this.gameConfigManager.saveToGoogleDrive(GoogleAPIManager.getInstance().getDriveService());
        });
    }

    public static SettingsSaverClientMod getInstance()
    {
        return INSTANCE;
    }

    public static Logger getLogger()
    {
        return LOGGER;
    }

    public GameConfigManager getGameConfigManager()
    {
        return this.gameConfigManager;
    }

    public String getVersion()
    {
        return this.settingsSaverVersion;
    }
}
