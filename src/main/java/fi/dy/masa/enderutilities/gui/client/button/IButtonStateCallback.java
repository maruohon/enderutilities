package fi.dy.masa.enderutilities.gui.client.button;

public interface IButtonStateCallback
{
    /**
     * Returns the index of the current state for the button with an ID callbackId
     * @param callbackId
     * @return The current state index for the button with an ID callbackId
     */
    public int getButtonStateIndex(int callbackId);

    /**
     * Returns whether the button is currently enabled or disabled.
     * @param callbackId The ID of the button, based on which the implementor knows which button is asking for the value
     * @return true if this button is enabled at this time
     */
    public boolean isButtonEnabled(int callbackId);
}
