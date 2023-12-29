package me.ayydan.settings_saver.mixin.client;

import com.google.api.services.drive.Drive;
import me.ayydan.settings_saver.SettingsSaverClientMod;
import me.ayydan.settings_saver.SettingsSaverConfigManager;
import me.ayydan.settings_saver.google.GoogleAPIGlobals;
import me.ayydan.settings_saver.google.GoogleAPIManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.option.GameOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin
{
    @Shadow @Final public GameOptions options;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", shift = At.Shift.BEFORE, ordinal = 0))
    public void initializeGoogleAPI(RunArgs args, CallbackInfo ci)
    {
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
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;setShaderGlintAlpha(D)V"))
    public void saveOrDownloadConfig(RunArgs args, CallbackInfo ci)
    {
        Drive googleDriveService = GoogleAPIManager.getInstance().getDriveService();

        SettingsSaverConfigManager settingsSaverConfigManager = new SettingsSaverConfigManager(this.options);
        if (settingsSaverConfigManager.doesConfigZipFileExist(googleDriveService))
        {
            settingsSaverConfigManager.downloadFromGoogleDrive(googleDriveService, true);
        }
        else
        {
            settingsSaverConfigManager.saveToGoogleDrive(googleDriveService);
        }

        SettingsSaverClientMod.getInstance().settingsSaverConfigManager = settingsSaverConfigManager;
    }
}
