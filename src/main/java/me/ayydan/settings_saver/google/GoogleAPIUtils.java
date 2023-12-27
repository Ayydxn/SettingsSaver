package me.ayydan.settings_saver.google;

public class GoogleAPIUtils
{
    public static String getMimeTypeForFileExtension(String fileExtension)
    {
        return switch (fileExtension)
        {
            case "json" -> "application/vnd.google-apps.script+json";
            case "txt" -> "text/plain";
            default -> "application/octet-stream";
        };
    }
}
