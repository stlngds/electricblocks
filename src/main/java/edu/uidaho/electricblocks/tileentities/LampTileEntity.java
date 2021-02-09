package edu.uidaho.electricblocks.tileentities;

import com.google.gson.JsonObject;

import edu.uidaho.electricblocks.RegistryHandler;
import edu.uidaho.electricblocks.utils.MetricUnit;
import edu.uidaho.electricblocks.guis.LampScreen;
import edu.uidaho.electricblocks.interfaces.IMultimeter;
import edu.uidaho.electricblocks.simulation.SimulationTileEntity;
import edu.uidaho.electricblocks.simulation.SimulationType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants;

import java.util.UUID;

/**
 * LampTileEntity stores information about the lamp block.
 */
public class LampTileEntity extends SimulationTileEntity implements IMultimeter {

    private boolean inService = false; // Whether or not the lamp is on
    private MetricUnit maxPower = new MetricUnit(60); // Maximum power this lamp can take
    private MetricUnit resultPower = new MetricUnit(0); // Amount of power being received
    private MetricUnit reactivePower = new MetricUnit(0); // TODO: Make sure this gets updated!!!

    public LampTileEntity() {
        super(RegistryHandler.LAMP_TILE_ENTITY.get(), SimulationType.LOAD);
    }

    /**
     * Adds Lamp specific information to the NBT Tags
     * @param compound The NBT tag being updated
     * @return A complete NBT tag with Lamp specific information
     */
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putBoolean("inService", inService);
        compound.putDouble("maxPower", maxPower.get());
        compound.putDouble("resultPower", resultPower.get());
        compound.putUniqueId("simId", simId);
        return compound;
    }

    /**
     * Extracts information from an NBT Tag about the Lamp
     * @param compound The NBT Tag to extract info from
     */
    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        inService = compound.getBoolean("inService");
        maxPower = new MetricUnit(compound.getDouble("maxPower"));
        resultPower = new MetricUnit(compound.getDouble("resultPower"));
        simId = compound.getUniqueId("simId");
        world.getLightManager().checkBlock(pos); // TODO: Move this somewhere else as it can cause an NPE
    }

    /**
     * Turns the lamp on and off
     */
    public void toggleInService(PlayerEntity player) {
        inService = !inService;
        requestSimulation(player);
    }

    /**
     * This function takes the active power that the lamp and compares it to the required power for the lamp to work.
     * @return a light value from [0-15]
     */
    public int getScaledLightValue() {
        double percentPower = resultPower.get() / maxPower.get();
        return (int) Math.round(percentPower * 15);
    }

    public boolean isInService() {
        return inService;
    }

    public double getLightPercentage() {
        return this.resultPower.get() / this.maxPower.get() * 100;
    }
    public MetricUnit getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(MetricUnit maxPower) {
        this.maxPower = maxPower;
    }

    public MetricUnit getResultPower() {
        return resultPower;
    }

    public void setResultPower(MetricUnit resultPower) {
        this.resultPower = resultPower;
        
    }

    public void setInService(boolean inService) {
        this.inService = inService;
    }

    public MetricUnit getReactivePower() {
        return reactivePower;
    }

    @Override
    public void receiveSimulationResults(JsonObject results) {
        setResultPower(new MetricUnit(results.get("p_mw").getAsDouble(), MetricUnit.MetricPrefix.MEGA));
        notifyUpdate();
    }

    @Override
    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        JsonObject bus = new JsonObject();
        UUID busId = embededBusses.get("main");
        bus.addProperty("etype", SimulationType.BUS.toString());

        JsonObject obj = new JsonObject();
        obj.addProperty("etype", getSimulationType().toString());
        obj.addProperty("in_service", inService);
        obj.addProperty("p_mw", maxPower.getMega());
        obj.addProperty("bus", busId.toString());

        json.add(busId.toString(), bus);
        json.add(getSimulationID().toString(), obj);
        return json;
    }

    @Override
    public void zeroSim() {
        resultPower = new MetricUnit(0);
        CompoundNBT tag = new CompoundNBT();
        write(tag);
        markDirty();
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), Constants.BlockFlags.BLOCK_UPDATE);
    }

    @Override
    public void initEmbeddedBusses() {
        embededBusses.put("main", UUID.randomUUID());
    }

    @Override
    public void disable() {
        inService = false;
        maxPower = new MetricUnit(0);
    }

    @Override
    public void updateOrToggle(PlayerEntity player) {
        toggleInService(player);
    }

    @Override
    public void viewOrModify(PlayerEntity player) {
        Minecraft.getInstance().displayGuiScreen(new LampScreen(this, player));
    }
}
