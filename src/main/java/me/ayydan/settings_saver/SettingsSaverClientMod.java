package me.ayydan.settings_saver;

import com.google.api.services.drive.Drive;
import me.ayydan.settings_saver.google.GoogleAPIManager;
import me.ayydan.settings_saver.SettingsSaverConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

@Environment(EnvType.CLIENT)
public class SettingsSaverClientMod implements ClientModInitializer
{
    private static SettingsSaverClientMod INSTANCE;

    private static final Logger LOGGER = (Logger) LogManager.getLogger("Settings Saver");
    private static final String MOD_ID = "settings_saver";

    public SettingsSaverConfigManager settingsSaverConfigManager;

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

        ClientLifecycleEvents.CLIENT_STOPPING.register((client) ->
                this.settingsSaverConfigManager.saveToGoogleDrive(GoogleAPIManager.getInstance().getDriveService()));
    }

    public static SettingsSaverClientMod getInstance()
    {
        return INSTANCE;
    }

    public static Logger getLogger()
    {
        return LOGGER;
    }

    public SettingsSaverConfigManager getGameConfigManager()
    {
        return this.settingsSaverConfigManager;
    }

    public String getVersion()
    {
        return this.settingsSaverVersion;
    }
}
