package fi.dy.masa.enderutilities.gui.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import fi.dy.masa.enderutilities.gui.client.base.GuiArea;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.gui.client.base.ScrollBar;
import fi.dy.masa.enderutilities.gui.client.base.ScrollBar.ScrollbarAction;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonHoverText;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback;
import fi.dy.masa.enderutilities.gui.client.button.GuiButtonStateCallback.ButtonState;
import fi.dy.masa.enderutilities.gui.client.button.IButtonStateCallback;
import fi.dy.masa.enderutilities.inventory.container.ContainerSoundBlock;
import fi.dy.masa.enderutilities.network.PacketHandler;
import fi.dy.masa.enderutilities.network.message.MessageGuiAction;
import fi.dy.masa.enderutilities.reference.ReferenceGuiIds;
import fi.dy.masa.enderutilities.tileentity.TileEntitySoundBlock;

public class GuiSoundBlock extends GuiEnderUtilities implements IButtonStateCallback
{
    private final TileEntitySoundBlock tesb;
    private final List<Pair<Integer, String>> allSounds = new ArrayList<Pair<Integer, String>>();
    private final List<Pair<Integer, String>> filteredSounds = new ArrayList<Pair<Integer, String>>();
    private final ScrollBar scrollBar;
    private final GuiArea areaSoundList;
    private GuiTextField searchField;
    private int startIndex;
    private String selectedName = "";

    public GuiSoundBlock(ContainerSoundBlock container, TileEntitySoundBlock te)
    {
        super(container, 176, 256, "gui.container." + te.getTEName());

        this.tesb = te;
        this.scrollBar = new ScrollBar(0, 152, 38, 212, 0, 12, 98, 0, this, this.guiTexture);
        this.areaSoundList = new GuiArea(8, 38, 142, 98);
        this.infoArea = new InfoArea(160, 5, 11, 11, "enderutilities.gui.infoarea.sound_block");
    }

    @Override
    public void initGui()
    {
        super.initGui();

        this.createButtons();
        this.initSearchField(this.tesb.filter);

        this.initSoundList();
        this.applyFilterString();

        this.selectedName = this.getSoundName(this.tesb.selectedSound);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        this.fontRenderer.drawString(I18n.format("enderutilities.container.sound_block"), 8, 6, 0x404040);

        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        this.searchField.drawTextBox();

        int max = Math.min(this.filteredSounds.size() - 1, this.startIndex + 9);
        int h = this.fontRenderer.FONT_HEIGHT + 1;

        for (int i = 0, index = this.startIndex; index <= max; index++, i++)
        {
            String str = this.filteredSounds.get(index).getRight();
            str = str.substring(0, Math.min(27, str.length()));
            int color = this.filteredSounds.get(index).getLeft() == this.tesb.selectedSound ? 0xF0F0F0 : 0x202020;
            this.fontRenderer.drawString(str, this.areaSoundList.getX() + 2, this.areaSoundList.getY() + 1 + i * h, color);
        }

        if (StringUtils.isEmpty(this.selectedName) == false)
        {
            this.fontRenderer.drawString(this.selectedName, 8, 140, 0x404040);
        }

        String str = I18n.format("enderutilities.gui.label.sound_block.pitch") + String.format(": %.3f", this.tesb.getPitch());
        this.fontRenderer.drawString(str, 60, 152, 0x404040);

        str = I18n.format("enderutilities.gui.label.sound_block.volume") + String.format(": %.3f", this.tesb.getVolume());
        this.fontRenderer.drawString(str, 60, 163, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float gameTicks, int mouseX, int mouseY)
    {
        super.drawGuiContainerBackgroundLayer(gameTicks, mouseX, mouseY);

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw the scroll bar
        this.scrollBar.render(x, y, mouseX, mouseY);
    }

    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();

        Keyboard.enableRepeatEvents(false);
    }

    private void initSearchField(String str)
    {
        Keyboard.enableRepeatEvents(true);

        this.searchField = new GuiTextField(0, this.fontRenderer, 8, 26, 141, 12);
        this.searchField.setTextColor(-1);
        this.searchField.setDisabledTextColour(-1);
        this.searchField.setEnableBackgroundDrawing(false);
        this.searchField.setMaxStringLength(50);
        this.searchField.setEnabled(true);
        this.searchField.setText(str);
        this.searchField.setFocused(false);
    }

    private void initSoundList()
    {
        this.allSounds.clear();

        for (Map.Entry<ResourceLocation, SoundEvent> entry : ForgeRegistries.SOUND_EVENTS.getEntries())
        {
            int id = SoundEvent.REGISTRY.getIDForObject(entry.getValue());
            this.allSounds.add(Pair.of(id, entry.getKey().toString()));
        }
    }

    private void applyFilterString()
    {
        String filter = this.searchField.getText();
        this.tesb.filter = filter;
        this.filteredSounds.clear();

        if (StringUtils.isEmpty(filter))
        {
            this.filteredSounds.addAll(this.allSounds);
        }
        else
        {
            for (Pair<Integer, String> pair : this.allSounds)
            {
                if (pair.getRight().contains(filter))
                {
                    this.filteredSounds.add(pair);
                }
            }
        }

        Collections.sort(this.filteredSounds);

        this.updateScrollbarScaling(this.filteredSounds.size());
    }

    private void updateScrollbarScaling(int listSize)
    {
        this.scrollBar.setPositionCount(listSize - 9);
        this.scrollBar.handleMouseInput(0, 0);
        this.startIndex = this.scrollBar.getPosition();

        // Reset the position if the list has shrunk so that the scroll position is "over the end"
        if (this.startIndex >= this.filteredSounds.size() - 9)
        {
            this.startIndex = 0;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException
    {
        if (this.searchField.textboxKeyTyped(typedChar, keyCode))
        {
            this.applyFilterString();
        }
        else
        {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        // Clear the field on right click
        if (mouseButton == 1 &&
            mouseX >= this.guiLeft + this.searchField.x &&
            mouseX < this.guiLeft + this.searchField.x + this.searchField.width &&
            mouseY >= this.guiTop + this.searchField.y &&
            mouseY < this.guiTop + this.searchField.y + this.searchField.height)
        {

            this.searchField.setText("");
            this.searchField.mouseClicked(mouseX - this.guiLeft, mouseY - this.guiTop, mouseButton);
            this.applyFilterString();
        }
        else
        {
            int mouseXA = Mouse.getEventX() * this.width / this.mc.displayWidth - this.guiLeft;
            int mouseYA = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1 - this.guiTop;

            if (this.areaSoundList.isMouseOver(mouseXA, mouseYA))
            {
                this.handleClickOnListEntry(mouseXA, mouseYA);
            }
            else
            {
                super.mouseClicked(mouseX, mouseY, mouseButton);

                this.searchField.mouseClicked(mouseX - this.guiLeft, mouseY - this.guiTop, mouseButton);
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException
    {
        int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth - this.guiLeft;
        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1 - this.guiTop;

        if (Mouse.getEventDWheel() != 0 &&
            (this.areaSoundList.isMouseOver(mouseX, mouseY) || this.scrollBar.isMouseOver(mouseX, mouseY)))
        {
            this.scrollBar.handleMouseInput(mouseX, mouseY);
        }
        else if ((Mouse.getEventButton() != 0 && Mouse.isButtonDown(0) == false) ||
                this.scrollBar.handleMouseInput(mouseX, mouseY) == false)
        {
            super.handleMouseInput();
        }
    }

    @Override
    public void scrollbarAction(int scrollbarId, ScrollbarAction action, int position)
    {
        this.startIndex = this.scrollBar.getPosition();
    }

    private String getSoundName(int id)
    {
        SoundEvent sound = SoundEvent.REGISTRY.getObjectById(id);
        return sound != null ? sound.getRegistryName().toString() : "";
    }

    private int getSoundId(int listIndex)
    {
        if (listIndex >= 0 && listIndex < this.filteredSounds.size())
        {
            Pair<Integer, String> pair = this.filteredSounds.get(listIndex);
            return pair != null ? pair.getLeft() : -1;
        }

        return -1;
    }

    private void handleClickOnListEntry(int mouseX, int mouseY)
    {
        int y = mouseY - this.areaSoundList.getY();
        // There are at maximum 10 entries on the list
        int index = (10 * y / this.areaSoundList.getHeight()) + this.startIndex;
        this.tesb.selectedSound = this.getSoundId(index);
        this.selectedName = this.tesb.selectedSound >= 0 ? this.getSoundName(this.tesb.selectedSound) : "";

        int dim = this.player.getEntityWorld().provider.getDimension();
        PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, this.tesb.getPos(),
                ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, 1000, this.tesb.selectedSound));
    }

    protected void createButtons()
    {
        this.buttonList.clear();

        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        this.buttonList.add(new GuiButtonStateCallback(0, x + 38, y + 152, 8, 8, 8, 0, this.guiTextureWidgets, this,
                ButtonState.createTranslate(0, 40, "enderutilities.gui.label.sound_block.repeat.disabled"),
                ButtonState.createTranslate(0, 88, "enderutilities.gui.label.sound_block.repeat.enabled")));

        this.buttonList.add(new GuiButtonHoverText(1, x + 49, y + 152, 8, 8, 0, 120,
                this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.sound_block.pitch"));

        this.buttonList.add(new GuiButtonHoverText(2, x + 49, y + 163, 8, 8, 0, 120,
                this.guiTextureWidgets, 8, 0, "enderutilities.gui.label.sound_block.volume"));

        this.buttonList.add(new GuiButtonHoverText(10, x + 8, y + 152, 12, 12, 176, 0,
                this.guiTexture, 12, 0, "enderutilities.gui.label.sound_block.play"));

        this.buttonList.add(new GuiButtonHoverText(11, x + 23, y + 152, 12, 12, 176, 24,
                this.guiTexture, 12, 0, "enderutilities.gui.label.sound_block.stop"));

        this.buttonList.add(new GuiButtonHoverText(20, x + 152, y + 24, 12, 12, 176, 48,
                this.guiTexture, 12, 0, "enderutilities.gui.label.sound_block.clear"));
    }

    @Override
    protected void actionPerformedWithButton(GuiButton button, int mouseButton) throws IOException
    {
        int dim = this.player.getEntityWorld().provider.getDimension();
        int amount = 0;

        if (mouseButton == 0 || mouseButton == 11)
        {
            amount = 1;
        }
        else if (mouseButton == 1 || mouseButton == 9)
        {
            amount = -1;
        }

        // Pitch and Volume
        if (button.id == 1 || button.id == 2)
        {
            if (GuiScreen.isShiftKeyDown()) { amount *= 10; }
            if (GuiScreen.isCtrlKeyDown())  { amount *= 100; }
        }
        // Clear the search field
        if (button.id == 20)
        {
            this.searchField.setText("");
            this.applyFilterString();
        }
        else
        {
            PacketHandler.INSTANCE.sendToServer(new MessageGuiAction(dim, this.tesb.getPos(),
                    ReferenceGuiIds.GUI_ID_TILE_ENTITY_GENERIC, button.id, amount));
        }
    }

    @Override
    public int getButtonStateIndex(int callbackId)
    {
        // Repeat
        if (callbackId == 0)
        {
            return this.tesb.getRepeat() ? 1 : 0;
        }

        return 0;
    }

    @Override
    public boolean isButtonEnabled(int callbackId)
    {
        return true;
    }
}
