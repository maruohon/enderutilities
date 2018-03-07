package fi.dy.masa.enderutilities.capabilities;

public interface IPortalCooldownCapability
{
    long getLastInPortalTime();

    void setLastInPortalTime(long time);
}
