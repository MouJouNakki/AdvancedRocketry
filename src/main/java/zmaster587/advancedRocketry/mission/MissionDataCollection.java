package zmaster587.advancedRocketry.mission;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import zmaster587.advancedRocketry.api.ARConfiguration;
import zmaster587.advancedRocketry.api.DataStorage;
import zmaster587.advancedRocketry.api.IInfrastructure;
import zmaster587.advancedRocketry.api.fuel.FuelRegistry;
import zmaster587.advancedRocketry.entity.EntityRocket;
import zmaster587.advancedRocketry.entity.EntityStationDeployedRocket;
import zmaster587.advancedRocketry.satellite.SatelliteData;
import zmaster587.advancedRocketry.tile.hatch.TileDataBus;
import zmaster587.libVulpes.LibVulpes;
import zmaster587.libVulpes.util.HashedBlockPosition;

public class MissionDataCollection extends MissionResourceCollection {

	private List<? extends SatelliteData> satellites;
	
	public MissionDataCollection() {
		super();
	}
	
	public MissionDataCollection(long l, EntityRocket entityRocket, LinkedList<IInfrastructure> connectedInfrastructure, ArrayList<? extends SatelliteData> satellites) {
		super((long) (l*ARConfiguration.getCurrentConfig().dataCollectionTimeMult), entityRocket, connectedInfrastructure);
		this.satellites = satellites;
	}
	
	@Override
	public String getName() {
		return LibVulpes.proxy.getLocalizedString("mission.datacollection.name");
	}

	@Override
	public void onMissionComplete() {

		if((int)rocketStats.getStatTag("intakePower") > 0 && satellites.size() > 0) {
			//Fill data buses
			for(SatelliteData satellite : satellites) {
				DataStorage data = satellite.getDataObject();
				for(TileDataBus tile : this.rocketStorage.getDataTiles()) {
					if(data.getData() <= 0)
						break;
					int transferredData = tile.getDataObject().addData((int)Math.floor(data.getData()*ARConfiguration.getCurrentConfig().dataCollectionMult), data.getDataType(), true);
					data.removeData((int) Math.floor(transferredData/ARConfiguration.getCurrentConfig().dataCollectionMult), true);
				}
			}
		}

		World world = DimensionManager.getWorld(launchDimension);
		if (world == null)
		{
			DimensionManager.initDimension(launchDimension);
			world = DimensionManager.getWorld(launchDimension);
		}
		
		EntityStationDeployedRocket rocket = new EntityStationDeployedRocket(world, rocketStorage, rocketStats, x, y, z);

		FuelRegistry.FuelType fuelType = rocket.getRocketFuelType();
		if(fuelType != null) {
			rocket.setFuelAmount(fuelType, 0);
			if (fuelType == FuelRegistry.FuelType.LIQUID_BIPROPELLANT)
				rocket.setFuelAmount(FuelRegistry.FuelType.LIQUID_OXIDIZER, 0);
		}
		rocket.readMissionPersistentNBT(missionPersistantNBT);

		EnumFacing dir = rocket.forwardDirection;
		rocket.forceSpawn = true;

		rocket.setPosition(dir.getFrontOffsetX()*64d + rocket.launchLocation.x + (rocketStorage.getSizeX() % 2 == 0 ? 0 : 0.5d), y, dir.getFrontOffsetZ()*64d + rocket.launchLocation.z + (rocketStorage.getSizeZ() % 2 == 0 ? 0 : 0.5d));
		world.spawnEntity(rocket);
		rocket.setInOrbit(true);
		rocket.setInFlight(true);
		//rocket.motionY = -1.0;

		for(HashedBlockPosition i : infrastructureCoords) {
			TileEntity tile = world.getTileEntity(new BlockPos(i.x, i.y, i.z));
			if(tile instanceof IInfrastructure) {
				((IInfrastructure)tile).unlinkMission();
				rocket.linkInfrastructure(((IInfrastructure)tile));
			}
		}
	}

}
