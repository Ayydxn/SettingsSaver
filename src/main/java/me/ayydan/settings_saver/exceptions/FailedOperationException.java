package me.ayydan.settings_saver.exceptions;

public class FailedOperationException extends RuntimeException
{
    public FailedOperationException(String message, OperationType operationType)
    {
        super(String.format("%s (Operation Type: %s)", message, operationType.name));
    }

    public enum OperationType
    {
        GoogleAPI("Google API");

        private final String name;

        OperationType(String name)
        {
            this.name = name;
        }
    }
}
