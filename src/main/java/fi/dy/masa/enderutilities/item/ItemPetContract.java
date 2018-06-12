package fi.dy.masa.enderutilities.item;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import fi.dy.masa.enderutilities.item.base.ItemEnderUtilities;
import fi.dy.masa.enderutilities.reference.Reference;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class ItemPetContract extends ItemEnderUtilities
{
    public ItemPetContract(String name)
    {
        super(name);

        this.setMaxStackSize(64);
        this.setMaxDamage(0);
        this.setHasSubtypes(false);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack)
    {
        String name = super.getUnlocalizedName();
        return this.isSigned(stack) ? name + "_signed" : name + "_blank";
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack)
    {
        String name = super.getItemStackDisplayName(stack);

        if (this.isSigned(stack))
        {
            return name + " - " + this.getTargetName(stack);
        }

        return name;
    }

    private boolean isSigned(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();

        return nbt != null &&
               nbt.hasKey("OwnableL", Constants.NBT.TAG_LONG) &&
               nbt.hasKey("OwnableM", Constants.NBT.TAG_LONG) &&
               nbt.hasKey("OwnerL", Constants.NBT.TAG_LONG) &&
               nbt.hasKey("OwnerM", Constants.NBT.TAG_LONG);
    }

    /**
     * Returns the owner's UUID. DO NOT call if isSigned() doesn't return true!
     * @param stack
     * @return
     */
    private UUID getOwnerUUID(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        return new UUID(nbt.getLong("OwnerM"), nbt.getLong("OwnerL"));
    }

    /**
     * Returns the target ownable entity's UUID. DO NOT call if isSigned() doesn't return true!
     * @param stack
     * @return
     */
    private UUID getSubjectUUID(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();
        return new UUID(nbt.getLong("OwnableM"), nbt.getLong("OwnableL"));
    }

    @Nullable
    private String getTargetName(ItemStack stack)
    {
        NBTTagCompound nbt = stack.getTagCompound();

        if (nbt != null)
        {
            String pre = TextFormatting.WHITE.toString() + TextFormatting.ITALIC.toString();
            String rst = TextFormatting.RESET.toString() + TextFormatting.WHITE.toString();
            String name = nbt.getString("CustomName");
            String id = null;

            if (nbt.hasKey("EntityString", Constants.NBT.TAG_STRING))
            {
                id = nbt.getString("EntityString");
            }

            if (id != null)
            {
                String translated = I18n.format("entity." + id + ".name");

                // Translation found
                if (id.equals(translated) == false)
                {
                    id = translated;
                }

                name = name.length() > 0 ? pre + name + rst + " (" + id + ")" : id;

                if (nbt.hasKey("Health", Constants.NBT.TAG_FLOAT))
                {
                    name += String.format(" (%.1f HP)", nbt.getFloat("Health"));
                }
            }

            return name;
        }

        return null;
    }

    private void signContract(ItemStack stack, EntityPlayer oldOwner, EntityLivingBase target)
    {
        NBTTagCompound nbt = NBTUtils.getCompoundTag(stack, null, true);
        UUID uuidOwner = oldOwner.getUniqueID();
        nbt.setLong("OwnerM", uuidOwner.getMostSignificantBits());
        nbt.setLong("OwnerL", uuidOwner.getLeastSignificantBits());

        UUID uuidTarget = target.getUniqueID();
        nbt.setLong("OwnableM", uuidTarget.getMostSignificantBits());
        nbt.setLong("OwnableL", uuidTarget.getLeastSignificantBits());

        nbt.setFloat("Health", target.getHealth());
        String str = EntityList.getEntityString(target);

        if (str != null)
        {
            nbt.setString("EntityString", str);
        }

        if (target.hasCustomName())
        {
            nbt.setString("CustomName", target.getCustomNameTag());
        }

        oldOwner.getEntityWorld().playSound(null, oldOwner.getPosition(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.PLAYERS, 0.5f, 1f);
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase target, EnumHand hand)
    {
        if (target instanceof EntityTameable)
        {
            if (player.getEntityWorld().isRemote == false)
            {
                if (this.isSigned(stack))
                {
                    UUID targetOwner = ((EntityTameable) target).getOwnerId();
                    UUID signedOwner = this.getOwnerUUID(stack);

                    // This contract is signed by the current owner of the target,
                    // and the contract is for this target entity, and the player is not the owner
                    // -> change the owner of the target.
                    if (targetOwner != null && targetOwner.equals(signedOwner) &&
                        target.getUniqueID().equals(this.getSubjectUUID(stack)) &&
                        player.getUniqueID().equals(signedOwner) == false)
                    {
                        ((EntityTameable) target).setTamed(true);
                        ((EntityTameable) target).setOwnerId(player.getUniqueID());

                        // See EntityTameable#handleStatusUpdate()
                        player.getEntityWorld().setEntityState(target, (byte) 7);

                        stack.shrink(1);
                    }
                }
                // Blank contract - if the target is tamed, and owned by the player, sign the contract
                else
                {
                    if (((EntityTameable) target).isTamed() && ((EntityTameable) target).isOwner(player))
                    {
                        ItemStack stackSigned = stack.copy();
                        stackSigned.setCount(1);

                        this.signContract(stackSigned, player, target);

                        if (player.addItemStackToInventory(stackSigned) == false)
                        {
                            player.dropItem(stackSigned, false);
                        }

                        stack.shrink(1);
                    }
                }
            }

            return true;
        }

        return super.itemInteractionForEntity(stack, player, target, hand);
    }

    @Override
    protected void addItemOverrides()
    {
        this.addPropertyOverride(new ResourceLocation(Reference.MOD_ID, "signed"), new IItemPropertyGetter()
        {
            @Override
            public float apply(ItemStack stack, World worldIn, EntityLivingBase entityIn)
            {
                return ItemPetContract.this.isSigned(stack) ? 1.0F : 0.0F;
            }
        });
    }
}
