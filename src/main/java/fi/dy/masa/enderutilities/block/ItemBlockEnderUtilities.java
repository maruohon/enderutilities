package fi.dy.masa.enderutilities.block;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockEnderUtilities extends ItemBlock
{
	protected String[] blockNames;

	public ItemBlockEnderUtilities(Block block)
	{
		super(block);
		this.setMaxDamage(0);
	}

	public void setNames(String[] names)
	{
		this.blockNames = names;
		this.setHasSubtypes(true);
	}

	@Override
	public int getMetadata(int meta)
	{
		return meta;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		if (this.blockNames != null && stack.getItemDamage() < this.blockNames.length)
		{
			return "tile." + this.blockNames[stack.getItemDamage()];
		}
		return super.getUnlocalizedName(stack);
	}
}
