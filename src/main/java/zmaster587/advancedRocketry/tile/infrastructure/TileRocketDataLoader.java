package zmaster587.advancedRocketry.tile.infrastructure;

import zmaster587.advancedRocketry.api.DataStorage.DataType;
import zmaster587.advancedRocketry.tile.hatch.TileDataBus;

import java.util.LinkedList;
import java.util.List;

import io.netty.buffer.ByteBuf;
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
import zmaster587.advancedRocketry.api.AdvancedRocketryBlocks;
import zmaster587.advancedRocketry.api.DataStorage;
import zmaster587.advancedRocketry.api.EntityRocketBase;
import zmaster587.advancedRocketry.api.IInfrastructure;
import zmaster587.advancedRocketry.api.IMission;
import zmaster587.advancedRocketry.block.multiblock.BlockARHatch;
import zmaster587.advancedRocketry.entity.EntityRocket;
import zmaster587.advancedRocketry.inventory.modules.ModuleAutoData;
import zmaster587.advancedRocketry.item.ItemData;
import zmaster587.advancedRocketry.tile.TileGuidanceComputer;
import zmaster587.advancedRocketry.tile.hatch.TileDataBus;
import zmaster587.advancedRocketry.util.IDataInventory;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.inventory.modules.*;
import zmaster587.libVulpes.items.ItemLinker;
import zmaster587.libVulpes.network.PacketHandler;
import zmaster587.libVulpes.network.PacketMachine;
import zmaster587.libVulpes.tile.multiblock.hatch.TileInventoryHatch;
import zmaster587.libVulpes.util.INetworkMachine;
import zmaster587.libVulpes.util.ZUtils.RedstoneState;
import zmaster587.libVulpes.tile.multiblock.TileMultiBlock;

import javax.annotation.Nonnull;

public class TileRocketDataLoader extends TileInventoryHatch implements IDataInventory, IInfrastructure, ITickable,  IButtonInventory, INetworkMachine, IGuiCallback {

	EntityRocket rocket;
	ModuleRedstoneOutputButton redstoneControl;
	RedstoneState state;
	ModuleRedstoneOutputButton inputRedstoneControl;
	RedstoneState inputstate;
	ModuleBlockSideSelector sideSelectorModule;
	DataStorage data;
	
	private final static int ALLOW_REDSTONEOUT = 2;
	
	public TileRocketDataLoader() {
		data = new DataStorage(DataStorage.DataType.UNDEFINED);
		data.setMaxData(2000);
		redstoneControl = new ModuleRedstoneOutputButton(174, 4, 0, "", this, LibVulpes.proxy.getLocalizedString("msg.dataLoader.loadingState"));
		state = RedstoneState.ON;
		inputRedstoneControl = new ModuleRedstoneOutputButton(174, 32, 1, "", this, LibVulpes.proxy.getLocalizedString("msg.dataLoader.allowLoading"));
		inputstate = RedstoneState.OFF;
		inputRedstoneControl.setRedstoneState(inputstate);
		sideSelectorModule = new ModuleBlockSideSelector(90, 15, this, LibVulpes.proxy.getLocalizedString("msg.dataLoader.none"), LibVulpes.proxy.getLocalizedString("msg.dataLoader.allowredstoneoutput"), LibVulpes.proxy.getLocalizedString("msg.dataLoader.allowredstoneinput"));
	}
	
	public TileRocketDataLoader(int number) {
		super(number);
		data = new DataStorage(DataStorage.DataType.UNDEFINED);
		data.setMaxData(2000);

		inventory.setCanInsertSlot(0, true);
		inventory.setCanInsertSlot(1, false);
		inventory.setCanExtractSlot(0, false);
		inventory.setCanExtractSlot(1, true);
		redstoneControl = new ModuleRedstoneOutputButton(174, 4, 0, "", this, LibVulpes.proxy.getLocalizedString("msg.dataLoader.loadingState"));
		state = RedstoneState.ON;
		inputRedstoneControl = new ModuleRedstoneOutputButton(174, 32, 1, "", this, LibVulpes.proxy.getLocalizedString("msg.dataLoader.allowLoading"));
		inputstate = RedstoneState.OFF;
		inputRedstoneControl.setRedstoneState(inputstate);
		sideSelectorModule = new ModuleBlockSideSelector(90, 15, this, LibVulpes.proxy.getLocalizedString("msg.dataLoader.none"), LibVulpes.proxy.getLocalizedString("msg.dataLoader.allowredstoneoutput"), LibVulpes.proxy.getLocalizedString("msg.dataLoader.allowredstoneinput"));
	}
	@Override
	public String getModularInventoryName() {
		return "tile.loader.8.name";
	}
	@Override
	public void loadData(int id) {

		ItemStack itemStack = inventory.getStackInSlot(0);

		if(itemStack != ItemStack.EMPTY && itemStack.getItem() instanceof ItemData) {
			ItemData itemData = (ItemData)itemStack.getItem();
			itemData.removeData(itemStack, this.data.addData(itemData.getData(itemStack), itemData.getDataType(itemStack), true), DataStorage.DataType.UNDEFINED);

			inventory.setInventorySlotContents(1, decrStackSize(0, 1));
		}
	}
	@Override
	public void storeData(int id) {
		ItemStack itemStack = inventory.getStackInSlot(0);

		if(!itemStack.isEmpty() && itemStack.getItem() instanceof ItemData && inventory.getStackInSlot(1) == ItemStack.EMPTY) {
			ItemData itemData = (ItemData)itemStack.getItem();
			this.data.removeData(itemData.addData(itemStack, this.data.getData(), this.data.getDataType()), true);

			inventory.setInventorySlotContents(1, decrStackSize(0, 1));
		}
	}


	public void setData(int data, DataStorage.DataType dataType) {
		this.data.setData(data, dataType);
	}

	@Override
	public int addData(int data, DataStorage.DataType dataType, EnumFacing dir, boolean commit) {
		return this.data.addData(data, dataType, commit);
	}

	public int getData() {
		return data.getData();
	}

	public final DataStorage getDataObject() {
		return data;
	}

	public int setMaxData() {
		return data.getMaxData();
	}

	public void setMaxData(int maxData) {
		data.setMaxData(maxData);
	}
	
	/**
	 * @param type the datatype to lock the tile to or null to unlock
	 * @see DataStorage
	 */
	public void lockData(DataStorage.DataType type) {
		data.lockDataType(type);
	}

	@Override
	public List<ModuleBase> getModules(int ID, EntityPlayer player) {
		LinkedList<ModuleBase> modules = new LinkedList<>();
		modules.add(new ModuleAutoData(40, 20, 0, 1, this, this, data));
		return modules;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
	}

	@Override
	public void setInventorySlotContents(int slot, @Nonnull ItemStack stack) {
		inventory.setInventorySlotContents(slot, stack);
		ItemStack itemStack = inventory.getStackInSlot(0);

		if(itemStack != ItemStack.EMPTY && itemStack.getItem() instanceof ItemData  && inventory.getStackInSlot(1) == ItemStack.EMPTY) {
			ItemData itemData = (ItemData)itemStack.getItem();
			if(itemData.getData(itemStack) > 0 && data.getData() != data.getMaxData()) {
				loadData(0);
			} else if (data.getData() != 0 && 1000 > itemData.getData(itemStack) ) {
				storeData(0);
			}
		}
		inventory.markDirty();
		markDirty();
		this.handleUpdateTag(getUpdateTag());

		if(this.hasMaster() && this.getMasterBlock() instanceof TileMultiBlock)
			((TileMultiBlock)this.getMasterBlock()).onInventoryUpdated();
	}

	@Override
	public boolean canExtractItem(int index, @Nonnull ItemStack stack, EnumFacing direction) {
		return index == 1;
	}

	@Override
	public boolean canInsertItem(int index, @Nonnull ItemStack itemStackIn, EnumFacing direction) {
		return index == 0 && isItemValidForSlot(index, itemStackIn);
	}

	@Override
	protected NBTTagCompound writeToNBTHelper(NBTTagCompound nbtTagCompound) {
		super.writeToNBTHelper(nbtTagCompound);
		data.writeToNBT(nbtTagCompound);
		return nbtTagCompound;
	}
	
	@Override
	protected void readFromNBTHelper(NBTTagCompound nbtTagCompound) {
		super.readFromNBTHelper(nbtTagCompound);
		data.readFromNBT(nbtTagCompound);
	}
	
	@Override
	public void writeDataToNetwork(ByteBuf out, byte id) { }

	@Override
	public void readDataFromNetwork(ByteBuf in, byte packetId, NBTTagCompound nbt) { }

	@Override
	public void useNetworkData(EntityPlayer player, Side side, byte id, NBTTagCompound nbt) { }

	@Override
	public int extractData(int maxAmount, DataType type, EnumFacing dir, boolean commit) {
		if(type == DataStorage.DataType.UNDEFINED || this.data.getDataType() == type)
			return this.data.removeData(maxAmount, commit);
		return 0;
	}
	@Override
	public boolean allowRedstoneOutputOnSide(EnumFacing facing) {
		return sideSelectorModule.getStateForSide(facing.getOpposite()) == 1;
	}

	//@Override
	//public List<ModuleBase> getModules(int ID, EntityPlayer player) {
	//	List<ModuleBase> list = super.getModules(ID, player);
	//	list.add(redstoneControl);
	//	list.add(inputRedstoneControl);
	//	list.add(sideSelectorModule);
	//	return list;
	//}

	protected boolean getStrongPowerForSides(World world, BlockPos pos) {
		for(int i = 0; i < 6; i++) {
			if(sideSelectorModule.getStateForSide(i) == ALLOW_REDSTONEOUT && world.getRedstonePower(pos.offset(EnumFacing.VALUES[i]), EnumFacing.VALUES[i]) > 0)
				return true;
		}
		return false;
	}

	@Override
	public void update() {
		//Move data
		if(!world.isRemote && rocket != null ) {
			boolean isAllowedToOperate = (inputstate == RedstoneState.OFF || isStateActive(inputstate, getStrongPowerForSides(world, getPos())));

			List<TileDataBus> tiles = rocket.storage.getDataTiles();
			boolean foundData = false;
			boolean rocketContainsData = false;
			//Function returns if something can be moved
			for(TileDataBus tile : tiles) {
				if(tile instanceof TileDataBus) {
					TileDataBus inv = ((TileDataBus)tile);
					if(inv.getData() > 0) {
						rocketContainsData = true;
						int transferredData = inv.getDataObject().addData(this.getData(), this.getDataObject().getDataType(), isAllowedToOperate);
						if(transferredData > 0)
							foundData = true;
						this.getDataObject().removeData(transferredData, isAllowedToOperate);
					}
					
					if(foundData)
						break;
				}
			}
			//Update redstone state
			setRedstoneState(!rocketContainsData);

		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, getBlockMetadata(), getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return writeToNBT(new NBTTagCompound());
	}

	protected void setRedstoneState(boolean condition) {
		condition = isStateActive(state, condition);
		((BlockARHatch)AdvancedRocketryBlocks.blockLoader).setRedstoneState(world,world.getBlockState(pos), pos, condition);
	}

	protected boolean isStateActive(RedstoneState state, boolean condition) {
		if(state == RedstoneState.INVERTED)
			return !condition;
		else if(state == RedstoneState.OFF)
			return false;
		return condition;
	}

	@Override
	public boolean onLinkStart(@Nonnull ItemStack item, TileEntity entity,
							   EntityPlayer player, World world) {

		ItemLinker.setMasterCoords(item, this.pos);

		if(this.rocket != null) {
			this.rocket.unlinkInfrastructure(this);
			this.unlinkRocket();
		}

		if(player.world.isRemote)
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("%s %s",new TextComponentTranslation("msg.dataLoader.link"), ": " + getPos().getX() + " " + getPos().getY() + " " + getPos().getZ()));
		return true;
	}

	@Override
	public boolean onLinkComplete(@Nonnull ItemStack item, TileEntity entity,
			EntityPlayer player, World world) {
		if(player.world.isRemote)
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentTranslation("msg.linker.error.firstMachine"));
		return false;
	}

	@Override
	public void unlinkRocket() {
		rocket = null;
		((BlockARHatch)AdvancedRocketryBlocks.blockLoader).setRedstoneState(world, world.getBlockState(pos), pos, false);
		//On unlink prevent the tile from ticking anymore

		//if(!worldObj.isRemote)
		//worldObj.loadedTileEntityList.remove(this);
	}

	@Override
	public boolean disconnectOnLiftOff() {
		return true;
	}

	@Override
	public boolean linkRocket(EntityRocketBase rocket) {
		//On linked allow the tile to tick
		//if(!worldObj.isRemote)
		//worldObj.loadedTileEntityList.add(this);
		this.rocket = (EntityRocket) rocket;
		return true;
	}

	@Override
	public boolean canUpdate() {
		return true;
	}

	@Override
	public boolean linkMission(IMission mission) {
		return false;
	}

	@Override
	public void unlinkMission() {

	}

	@Override
	public int getMaxLinkDistance() {
		return 32;
	}

	public boolean canRenderConnection() {
		return true;
	}

//	@Override
//	public void readFromNBT(NBTTagCompound nbt) {
//		super.readFromNBT(nbt);
//
//		state = RedstoneState.values()[nbt.getByte("redstoneState")];
//		redstoneControl.setRedstoneState(state);
//
//		inputstate = RedstoneState.values()[nbt.getByte("inputRedstoneState")];
//		inputRedstoneControl.setRedstoneState(inputstate);
//
//		sideSelectorModule.readFromNBT(nbt);
//	}

//	@Override
//	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
//		super.writeToNBT(nbt);
//		nbt.setByte("redstoneState", (byte) state.ordinal());
//		nbt.setByte("inputRedstoneState", (byte) inputstate.ordinal());
//		sideSelectorModule.writeToNBT(nbt);
//		return nbt;
//	}

	@Override
	public void onInventoryButtonPressed(int buttonId) {
		if(buttonId == 0)
			state = redstoneControl.getState();
		if(buttonId == 1)
			inputstate = inputRedstoneControl.getState();
		PacketHandler.sendToServer(new PacketMachine(this, (byte)0));
	}

//	@Override
//	public void writeDataToNetwork(ByteBuf out, byte id) {
//		out.writeByte(state.ordinal());
//		out.writeByte(inputstate.ordinal());
//		for(int i = 0; i < 6; i++)
//			out.writeByte(sideSelectorModule.getStateForSide(i));
//	}

//	@Override
//	public void readDataFromNetwork(ByteBuf in, byte packetId,
//			NBTTagCompound nbt) {
//		nbt.setByte("state", in.readByte());
//		nbt.setByte("inputstate", in.readByte());
//
//		byte[] bytes = new byte[6];
//		for(int i = 0; i < 6; i++)
//			bytes[i] = in.readByte();
//		nbt.setByteArray("bytes", bytes);
//	}

//	@Override
//	public void useNetworkData(EntityPlayer player, Side side, byte id,
//			NBTTagCompound nbt) {
//		state = RedstoneState.values()[nbt.getByte("state")];
//		inputstate = RedstoneState.values()[nbt.getByte("inputstate")];
//
//		byte[] bytes = nbt.getByteArray("bytes");
//		for(int i = 0; i < 6; i++)
//			sideSelectorModule.setStateForSide(i, bytes[i]);
//
//		if(rocket == null)
//			setRedstoneState(state == RedstoneState.INVERTED);
//
//		markDirty();
//		world.markChunkDirty(getPos(), this);
//	}


	@Override
	public void onModuleUpdated(ModuleBase module) {
		PacketHandler.sendToServer(new PacketMachine(this, (byte)0));
	}
}
