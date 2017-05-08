package fi.dy.masa.enderutilities.tileentity;

import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import fi.dy.masa.enderutilities.EnderUtilities;
import fi.dy.masa.enderutilities.gui.client.GuiSoundBlock;
import fi.dy.masa.enderutilities.gui.client.base.GuiEnderUtilities;
import fi.dy.masa.enderutilities.inventory.container.ContainerSoundBlock;
import fi.dy.masa.enderutilities.network.message.ISyncableTile;
import fi.dy.masa.enderutilities.network.message.MessageAddEffects;
import fi.dy.masa.enderutilities.network.message.MessageSyncTileEntity;
import fi.dy.masa.enderutilities.reference.ReferenceNames;

public class TileEntitySoundBlock extends TileEntityEnderUtilities implements ISyncableTile
{
    private String soundName;
    private float volume = 1f;
    private float pitch = 1f;
    private boolean repeat;
    private boolean redstoneState;
    /** Current filter string in the GUI */
    @SideOnly(Side.CLIENT)
    public String filter = "";
    public int selectedSound = -1;

    public TileEntitySoundBlock()
    {
        super(ReferenceNames.NAME_TILE_SOUND_BLOCK);
    }

    public float getVolume()
    {
        return this.volume;
    }

    public float getPitch()
    {
        return this.pitch;
    }

    public boolean getRepeat()
    {
        return this.repeat;
    }

    public void setVolume(float volume)
    {
        this.volume = MathHelper.clamp(volume, 0f, 2f);
    }

    public void setPitch(float pitch)
    {
        this.pitch = MathHelper.clamp(pitch, 0f, 20f);
    }

    public void setRepeat(boolean repeat)
    {
        this.repeat = repeat;
    }

    @Override
    public void readFromNBTCustom(NBTTagCompound nbt)
    {
        super.readFromNBTCustom(nbt);

        if (nbt.hasKey("Sound", Constants.NBT.TAG_STRING))
        {
            this.soundName = nbt.getString("Sound");
        }

        this.redstoneState = nbt.getBoolean("Powered");
        this.repeat = nbt.getBoolean("Repeat");
        this.volume = nbt.getFloat("Volume");
        this.pitch = nbt.getFloat("Pitch");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt = super.writeToNBT(nbt);

        if (this.soundName != null)
        {
            nbt.setString("Sound", this.soundName);
        }

        nbt.setFloat("Volume", this.volume);
        nbt.setFloat("Pitch", this.pitch);
        nbt.setBoolean("Repeat", this.repeat);
        nbt.setBoolean("Powered", this.redstoneState);

        return nbt;
    }

    @Override
    public NBTTagCompound getUpdatePacketTag(NBTTagCompound nbt)
    {
        nbt = super.getUpdatePacketTag(nbt);

        nbt.setInteger("sb1", this.getPitchVolumeForSync());
        boolean play = this.getWorld() != null && this.getWorld().isBlockPowered(this.getPos());
        nbt.setInteger("sb2", this.getRepeatAndSoundIdForSync(play));

        return nbt;
    }

    @Override
    public void handleUpdateTag(NBTTagCompound tag)
    {
        super.handleUpdateTag(tag);
        this.syncTile(new int[] { tag.getInteger("sb1"), tag.getInteger("sb2") }, null);
    }

    public void sendPlaySoundPacket(boolean stop)
    {
        if (this.soundName != null)
        {
            SoundEvent sound = this.getSound();

            if (sound != null)
            {
                int id = SoundEvent.REGISTRY.getIDForObject(sound);
                float x = this.getPos().getX() + 0.5f;
                float y = this.getPos().getY() + 0.5f;
                float z = this.getPos().getZ() + 0.5f;
                MessageAddEffects message = new MessageAddEffects(id, this.pitch, this.volume, this.repeat, stop, x, y, z);
                this.sendPacketToWatchers(message);
            }
        }
    }

    private void playOrStopSound(boolean stop)
    {
        SoundEvent sound = this.getSound();

        if (sound != null)
        {
            int id = SoundEvent.REGISTRY.getIDForObject(sound);
            float x = this.getPos().getX() + 0.5f;
            float y = this.getPos().getY() + 0.5f;
            float z = this.getPos().getZ() + 0.5f;
            EnderUtilities.proxy.playSound(id, this.pitch, this.volume, this.repeat, stop, x, y, z);
        }
    }

    @Nullable
    private SoundEvent getSound()
    {
        return this.soundName != null ? ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(this.soundName)) : null;
    }

    @Override
    public void onLeftClickBlock(EntityPlayer player)
    {
        this.sendPlaySoundPacket(true);
        this.sendPlaySoundPacket(false);
    }

    @Override
    public void onNeighborBlockChange(World worldIn, BlockPos pos, IBlockState state, Block blockIn)
    {
        boolean powered = this.getWorld().isBlockPowered(this.getPos());

        if (powered != this.redstoneState)
        {
            if (powered)
            {
                this.sendPlaySoundPacket(true);
                this.sendPlaySoundPacket(false);
            }

            this.redstoneState = powered;
        }
    }

    @Override
    public void performGuiAction(EntityPlayer player, int action, int element)
    {
        switch (action)
        {
            case 0:
                this.repeat = ! this.repeat;
                break;
            case 1:
                this.setPitch(this.pitch + (float) element / 1000f);
                break;
            case 2:
                this.setVolume(this.volume + (float) element / 1000f);
                break;
            case 10: // Play
                this.sendPlaySoundPacket(false);
                return;
            case 11: // Stop
                this.sendPlaySoundPacket(true);
                return;
            case 1000:
                // Stop a previously playing sound before setting the new sound
                this.sendPlaySoundPacket(true);
                this.setSoundFromID(element);
                break;
        }

        this.markDirty();
        this.sendSyncPacket(action == 1000);
    }

    private int getPitchVolumeForSync()
    {
        return ((int) (this.volume * 1000)) << 16 | (int) (this.pitch * 1000);
    }

    private int getRepeatAndSoundIdForSync(boolean playNow)
    {
        int value = this.repeat ? 0x80000000 : 0;

        if (this.soundName != null)
        {
            SoundEvent sound = this.getSound();

            if (sound != null)
            {
                value |= SoundEvent.REGISTRY.getIDForObject(sound);
            }
            else
            {
                value |= 0xFFFF;
            }

            if (playNow)
            {
                value |= 0x40000000;
            }
        }

        return value;
    }

    private void sendSyncPacket(boolean playNow)
    {
        int value0 = this.getPitchVolumeForSync();
        int value1 = this.getRepeatAndSoundIdForSync(playNow);

        this.sendPacketToWatchers(new MessageSyncTileEntity(this.getPos(), value0, value1));
    }

    @Override
    public void syncTile(int[] values, ItemStack[] stacks)
    {
        if (values.length == 1)
        {
            this.playOrStopSound(false);
        }
        else if (values.length == 2)
        {
            this.setPitchAndVolume(values[0]);
            this.setSoundAndRepeat(values[1]);

            if ((values[1] & 0x40000000) != 0)
            {
                this.playOrStopSound(false);
            }
        }
    }

    private void setPitchAndVolume(int value)
    {
        this.pitch = (float) (value & 0xFFFF) / 1000f;
        this.volume = (float) (value >>> 16) / 1000f;
    }

    private void setSoundAndRepeat(int value)
    {
        this.setSoundFromID((short) (value & 0xFFFF));
        this.repeat = (value & 0x80000000) != 0;
    }

    private void setSoundFromID(int id)
    {
        this.selectedSound = id;

        if (id >= 0)
        {
            SoundEvent sound = SoundEvent.REGISTRY.getObjectById(id);
            this.soundName = sound != null ? sound.getRegistryName().toString() : null;
        }
        else
        {
            this.soundName = null;
        }
    }

    public ContainerSoundBlock getContainer(EntityPlayer player)
    {
        return new ContainerSoundBlock(player, this);
    }

    @SideOnly(Side.CLIENT)
    public GuiEnderUtilities getGui(EntityPlayer player)
    {
        return new GuiSoundBlock(this.getContainer(player), this);
    }
}
