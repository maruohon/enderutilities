package fi.dy.masa.enderutilities.gui.client;

public interface IButtonCallback
{
    /**
     * Gets the current U coordinate value associated to this callbackId,
     * which will then be used to render button.
     * @param callbackId
     * @return
     */
    public int getButtonU(int callbackId);

    /**
     * Gets the current V coordinate value associated to this callbackId,
     * which will then be used to render button.
     * @param callbackId
     * @return
     */
    public int getButtonV(int callbackId);
}
