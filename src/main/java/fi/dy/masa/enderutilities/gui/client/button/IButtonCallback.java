package fi.dy.masa.enderutilities.gui.client.button;

public interface IButtonCallback
{
    /**
     * Gets the current U coordinate value associated to this callbackId,
     * which will then be used to render the button.
     * @param callbackId The ID of the button, based on which the implementor knows which button is asking for the value
     * @param defaultU The default value from the button, which can be returned if no modification is needed
     * @return The U coordinate of the button texture to use
     */
    public int getButtonU(int callbackId, int defaultU);

    /**
     * Gets the current V coordinate value associated to this callbackId,
     * which will then be used to render the button.
     * @param callbackId The ID of the button, based on which the implementor knows which button is asking for the value
     * @param defaultV The default value from the button, which can be returned if no modification is needed
     * @return The V coordinate of the button texture to use
     */
    public int getButtonV(int callbackId, int defaultV);

    /**
     * Returns whether the button is currently enabled or disabled.
     * @param callbackId The ID of the button, based on which the implementor knows which button is asking for the value
     * @return true if this button is enabled at this time
     */
    public boolean isButtonEnabled(int callbackId);
}
