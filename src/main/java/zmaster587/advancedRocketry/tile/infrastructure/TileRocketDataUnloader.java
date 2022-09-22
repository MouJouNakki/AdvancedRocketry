package zmaster587.advancedRocketry.tile.infrastructure;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import zmaster587.advancedRocketry.api.IInfrastructure;
import zmaster587.advancedRocketry.tile.TileGuidanceComputer;
import zmaster587.advancedRocketry.tile.hatch.TileDataBus;
import zmaster587.advancedRocketry.util.IDataInventory;
import zmaster587.advancedRocketry.tile.hatch.TileDataBus;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.inventory.modules.*;
import zmaster587.libVulpes.items.ItemLinker;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.network.PacketMachine;
import zmaster587.libVulpes.tile.multiblock.hatch.TileInventoryHatch;
import zmaster587.libVulpes.util.INetworkMachine;
import zmaster587.libVulpes.util.ZUtils.RedstoneState;

public class TileRocketDataUnloader extends TileRocketDataLoader implements IDataInventory, IInfrastructure, ITickable, IButtonInventory, INetworkMachine {
	public TileRocketDataUnloader( ) {
		super();
		inventory.setCanInsertSlot(0, false);
		inventory.setCanInsertSlot(1, true);
		inventory.setCanExtractSlot(0, true);
		inventory.setCanExtractSlot(1, false);
	}
	public TileRocketDataUnloader(int number) {
		super(number);
		inventory.setCanInsertSlot(0, false);
		inventory.setCanInsertSlot(1, true);
		inventory.setCanExtractSlot(0, true);
		inventory.setCanExtractSlot(1, false);
	}
	@Override
	public int getMaxLinkDistance() {
		return 32;
	}
	@Override
	public String getModularInventoryName() {
		return "tile.loader.7.name";
	}
	@Override
	public void update() {
		//Move data
		if(!world.isRemote && rocket != null ) {
			boolean isAllowedToOperate = (inputstate == RedstoneState.OFF || isStateActive(inputstate, getStrongPowerForSides(world, getPos())));

			List<TileDataBus> tiles = rocket.storage.getDataTiles();
			boolean foundData = false;
			boolean rocketContainsNoData = true;
			//Function returns if something can be moved
			for(TileDataBus tile : tiles) {
				if(tile instanceof TileDataBus) {
					TileDataBus inv = ((TileDataBus)tile);
					if(inv.getData() > 0) {
						rocketContainsNoData = false;
						int transferredData = this.getDataObject().addData(inv.getData(), inv.getDataObject().getDataType(), isAllowedToOperate);
						if(transferredData > 0)
							foundData = true;
						inv.getDataObject().removeData(transferredData, isAllowedToOperate);
					}
					
					if(foundData)
						break;
				}
			}
			//Update redstone state
			setRedstoneState(rocketContainsNoData);

		}
	}
}
