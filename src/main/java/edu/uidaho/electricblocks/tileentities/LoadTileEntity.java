package edu.uidaho.electricblocks.tileentities;

import java.util.UUID;

import com.google.gson.JsonObject;

import edu.uidaho.electricblocks.interfaces.IMultimeter;
import edu.uidaho.electricblocks.RegistryHandler;
import edu.uidaho.electricblocks.electric.Watt;
import edu.uidaho.electricblocks.guis.LoadScreen;
import edu.uidaho.electricblocks.simulation.SimulationTileEntity;
import edu.uidaho.electricblocks.simulation.SimulationType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;

/**
 * Tile entity associated with the @LoadBlock
 */
public class LoadTileEntity extends SimulationTileEntity implements IMultimeter {

    private boolean inService = false;
    private Watt maxPower = new Watt(100);
    private Watt resultPower = new Watt(0);
    private Watt reactivePower = new Watt(0);

    public LoadTileEntity() {
        super(RegistryHandler.LOAD_TILE_ENTITY.get(), SimulationType.LOAD);
    }

    /**
     * Adds Load specific information to the NBT Tags
     * @param compound The NBT tag being updated
     * @return A complete NBT tag with Load specific information
     */
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        compound.putBoolean("inService", inService);
        compound.putDouble("maxPower", maxPower.getWatts());
        compound.putDouble("resultPower", resultPower.getWatts());
        compound.putDouble("reactivePower", reactivePower.getWatts());
        compound.putUniqueId("simId", simId);
        return compound;
    }

    /**
     * Extracts information from an NBT Tag about the Load
     * @param compound The NBT Tag to extract info from
     */
    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        inService = compound.getBoolean("inService");
        maxPower = new Watt(compound.getDouble("maxPower"));
        resultPower = new Watt(compound.getDouble("resultPower"));
        reactivePower = new Watt(compound.getDouble("reactivePower"));
        simId = compound.getUniqueId("simId");
    }

    /**
     * Turns the lamp on and off
     */
    public void toggleInService(PlayerEntity player) {
        inService = !inService;
        requestSimulation(player);
    }

    public void setInService(boolean inService) {
        this.inService = inService;
    }

    public boolean isInService() {
        return inService;
    }

    /**
     * Return the max power
     * @return
     */
    public Watt getMaxPower() {
        return maxPower;
    }

    public void setMaxPower(Watt maxPower) {
        this.maxPower = maxPower;
    }

    public Watt getResultPower() {
        return resultPower;
    }

    public void setResultPower(Watt resultPower) {
        this.resultPower = resultPower;
    }

    public Watt getReactivePower() {
        return reactivePower;
    }

    public void setReactivePower(Watt reactivePower) {
        this.reactivePower = reactivePower;
    }

    @Override
    public void receiveSimulationResults(JsonObject results) {
        double resultPower = results.get("p_mw").getAsDouble() * 1000000;
        setResultPower(new Watt(resultPower));
        double reactivePower = results.get("q_mvar").getAsDouble() * 1000000;
        setReactivePower(new Watt(reactivePower));
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
        obj.addProperty("p_mw", maxPower.getMegaWatts());
        obj.addProperty("bus", busId.toString());

        json.add(busId.toString(), bus);
        json.add(getSimulationID().toString(), obj);
        return json;
    }

    @Override
    public void zeroSim() {
        resultPower = new Watt(0);
        reactivePower = new Watt(0);
        notifyUpdate();
    }

    @Override
    public void initEmbeddedBusses() {
        embededBusses.put("main", UUID.randomUUID());
    }

    @Override
    public void updateOrToggle(PlayerEntity player) {
        toggleInService(player);
    }

    @Override
    public void viewOrModify(PlayerEntity player) {
        Minecraft.getInstance().displayGuiScreen(new LoadScreen(this, player));
    }

    @Override
    public void disable() {
        inService = false;
        maxPower = new Watt(0);
    }
    
}
