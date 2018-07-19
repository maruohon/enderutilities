package fi.dy.masa.enderutilities.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import fi.dy.masa.enderutilities.util.nbt.NBTUtils;

public class TemplateMetadata
{
    protected BlockPos endPosRelative = BlockPos.ORIGIN;
    protected EnumFacing facing = EnumFacing.EAST;
    protected String templateName = "";
    protected String author = "?";

    public TemplateMetadata()
    {
    }

    public BlockPos getRelativeEndPosition()
    {
        return this.endPosRelative;
    }

    public EnumFacing getFacing()
    {
        return this.facing;
    }

    public String getTemplateName()
    {
        return this.templateName;
    }

    public void setTemplateName(String name)
    {
        this.templateName = name;
    }

    public String getAuthor()
    {
        return this.author;
    }

    public void setValues(BlockPos size, EnumFacing facing, String templateName, String author)
    {
        this.endPosRelative = size;
        this.facing = facing;
        this.templateName = templateName;
        this.author = author;
    }

    public void read(NBTTagCompound nbt)
    {
        NBTTagList tagList = nbt.getTagList("endPosRelative", 3);
        this.endPosRelative = new BlockPos(tagList.getIntAt(0), tagList.getIntAt(1), tagList.getIntAt(2));
        this.facing = EnumFacing.byIndex(nbt.getByte("facing"));
        this.templateName = nbt.getString("name");
        this.author = nbt.getString("author");
    }

    public void write(NBTTagCompound nbt)
    {
        nbt.setTag("endPosRelative", NBTUtils.writeInts(this.endPosRelative.getX(), this.endPosRelative.getY(), this.endPosRelative.getZ()));
        nbt.setByte("facing", (byte)this.facing.getIndex());
        nbt.setString("name", this.templateName);
        nbt.setString("author", this.author);
    }
}
