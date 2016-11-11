package fi.dy.masa.enderutilities.gui.client;

public interface IButtonCallback
{
    /**
     * Gets the current U coordinate value associated to this callbackId,
     * which will then be used to render button.
     * @param callbackId The ID of the button, based on which the implementor knows which button is asking for the value
     * @param defaultU The default value from the button, which can be returned if no modification is needed
     * @return
     */
    public int getButtonU(int callbackId, int defaultU);

    /**
     * Gets the current V coordinate value associated to this callbackId,
     * which will then be used to render button.
     * @param callbackId The ID of the button, based on which the implementor knows which button is asking for the value
     * @param defaultV The default value from the button, which can be returned if no modification is needed
     * @return
     */
    public int getButtonV(int callbackId, int defaultV);
}
