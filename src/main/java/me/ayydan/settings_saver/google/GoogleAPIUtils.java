package me.ayydan.settings_saver.google;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.drive.Drive;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class GoogleAPIUtils
{
    public static ByteArrayOutputStream downloadZipFile(Drive googleDriveService, String zipFileID) throws IOException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try
        {
            googleDriveService.files()
                    .get(zipFileID)
                    .executeMediaAndDownloadTo(outputStream);

            return outputStream;
        }
        catch (GoogleJsonResponseException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }

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
