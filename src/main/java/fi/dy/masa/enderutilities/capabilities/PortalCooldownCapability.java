package fi.dy.masa.enderutilities.capabilities;

public class PortalCooldownCapability implements IPortalCooldownCapability
{
    private long lastInPortal;

    @Override
    public long getLastInPortalTime()
    {
        return this.lastInPortal;
    }

    @Override
    public void setLastInPortalTime(long time)
    {
        this.lastInPortal = time;
    }
}
