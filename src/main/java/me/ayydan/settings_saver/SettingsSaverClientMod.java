package me.ayydan.settings_saver;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
    }

    public static SettingsSaverClientMod getInstance()
    {
        return INSTANCE;
    }

    public static Logger getLogger()
    {
        return LOGGER;
    }

    public String getVersion()
    {
        return this.settingsSaverVersion;
    }
}
