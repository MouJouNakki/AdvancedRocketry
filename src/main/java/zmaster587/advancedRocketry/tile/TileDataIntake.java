package zmaster587.advancedRocketry.tile;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import zmaster587.advancedRocketry.api.AdvancedRocketryBlocks;
import zmaster587.advancedRocketry.api.IDataIntake;
import zmaster587.advancedRocketry.item.ItemSatelliteIdentificationChip;
import zmaster587.libVulpes.tile.multiblock.hatch.TileInventoryHatch;
import zmaster587.libVulpes.inventory.modules.IModularInventory;

import java.util.List;

import javax.annotation.Nonnull;

public class TileDataIntake extends TileInventoryHatch implements IDataIntake, IModularInventory {

	public TileDataIntake() {
		super(1);
		inventory.setCanInsertSlot(0, true);
		inventory.setCanExtractSlot(0, true);
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isItemValidForSlot(int slot, @Nonnull ItemStack itemStack) {
		Item item = itemStack.getItem();

		return slot == 0 && item instanceof ItemSatelliteIdentificationChip;
	}
	@Override
	public String getModularInventoryName() {
		return AdvancedRocketryBlocks.blockDataIntake.getLocalizedName();
	}
}
