package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import org.lwjgl.input.Keyboard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import fi.dy.masa.enderutilities.item.ItemBuildersWand;
import fi.dy.masa.enderutilities.item.ItemBuildersWand.Mode;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageSendString;
import fi.dy.masa.enderutilities.network.message.MessageSendString.Type;
import fi.dy.masa.enderutilities.reference.ReferenceTextures;
import fi.dy.masa.enderutilities.setup.EnderUtilitiesItems;

public class GuiScreenBuilderWandTemplate extends GuiScreen
{
    protected final Minecraft mc;
    protected final ResourceLocation guiTexture;
    protected GuiTextField nameField;
    protected int guiLeft;
    protected int guiTop;
    protected int xSize;
    protected int ySize;

    public GuiScreenBuilderWandTemplate()
    {
        this.mc = Minecraft.getMinecraft();
        this.guiTexture = ReferenceTextures.getGuiTexture("gui.builderswand");
        this.xSize = 192;
        this.ySize = 82;
    }

    @Override
    public void initGui()
    {
        super.initGui();

        Keyboard.enableRepeatEvents(true);

        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;

        this.nameField = new GuiTextField(0, this.fontRendererObj, this.guiLeft + 10, this.guiTop + 25, 173, 12);
        this.nameField.setTextColor(-1);
        this.nameField.setDisabledTextColour(-1);
        this.nameField.setEnableBackgroundDrawing(false);
        this.nameField.setMaxStringLength(60);
        this.nameField.setEnabled(true);
        this.nameField.setText(this.getTemplateNameFromItem());
        this.nameField.setFocused(true);
        this.nameField.setCursorPositionEnd();

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(1, this.guiLeft + 8, this.guiTop + 40, 80, 20, I18n.format("enderutilities.gui.label.setname")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks)
    {
        this.drawGuiBackground(partialTicks, mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);

        String s = I18n.format("enderutilities.gui.label.builderswand.template");
        int textWidth = this.fontRendererObj.getStringWidth(s);
        int x = (this.width / 2);
        this.fontRendererObj.drawString(s, x - (textWidth / 2), this.guiTop + 6, 0x404040);

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();

        this.nameField.drawTextBox();
    }

    protected void drawGuiBackground(float gameTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.bindTexture(this.guiTexture);
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (this.nameField.textboxKeyTyped(typedChar, keyCode) == false)
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void actionPerformed(GuiButton button)
    {
        if (button.id == 1)
        {
            PacketHandler.INSTANCE.sendToServer(new MessageSendString(Type.ITEM, this.nameField.getText()));

            this.mc.displayGuiScreen(null);
        }
    }

    protected void bindTexture(ResourceLocation rl)
    {
        this.mc.renderEngine.bindTexture(rl);
    }

    private String getTemplateNameFromItem()
    {
        ItemStack stack = this.mc.thePlayer.getHeldItemMainhand();

        if (stack != null && stack.getItem() == EnderUtilitiesItems.buildersWand &&
                ItemBuildersWand.Mode.getMode(stack) == Mode.COPY)
        {
            return ((ItemBuildersWand) stack.getItem()).getTemplateName(stack, Mode.COPY);
        }

        return "";
    }
}
