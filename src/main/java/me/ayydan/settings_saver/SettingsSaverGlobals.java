package me.ayydan.settings_saver;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class SettingsSaverGlobals
{
    public static final String CONFIG_ZIP_FILE_NAME = "config.zip";

    public static final Path TEMP_CONFIG_ZIP_FILE_PATH = Path.of(FabricLoader.getInstance().getGameDir() + "/" + CONFIG_ZIP_FILE_NAME);
}
