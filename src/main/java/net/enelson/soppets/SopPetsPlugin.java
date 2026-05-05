package net.enelson.soppets;

import net.enelson.soppets.command.PetsCommand;
import net.enelson.soppets.listener.PlayerConnectionListener;
import net.enelson.soppets.listener.PetMenuListener;
import net.enelson.soppets.listener.PetProtectionListener;
import net.enelson.soppets.listener.PetSpawnBypassListener;
import net.enelson.soppets.service.FmmBridgeService;
import net.enelson.soppets.service.PaperPetFollowService;
import net.enelson.soppets.service.VanillaPetAppearanceService;
import net.enelson.soppets.service.PetDefinitionService;
import net.enelson.soppets.service.PetMenuService;
import net.enelson.soppets.service.PetSessionService;
import net.enelson.soppets.service.PlayerPetStorage;
import org.bukkit.plugin.java.JavaPlugin;

public final class SopPetsPlugin extends JavaPlugin {
    private PlayerPetStorage playerPetStorage;
    private PetDefinitionService petDefinitionService;
    private FmmBridgeService fmmBridgeService;
    private PetSessionService petSessionService;
    private PetMenuService petMenuService;
    private PaperPetFollowService paperPetFollowService;
    private VanillaPetAppearanceService vanillaPetAppearanceService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.playerPetStorage = new PlayerPetStorage(this);
        this.petDefinitionService = new PetDefinitionService(this);
        this.fmmBridgeService = new FmmBridgeService(this);
        this.paperPetFollowService = new PaperPetFollowService(this);
        this.vanillaPetAppearanceService = new VanillaPetAppearanceService(this);
        this.petSessionService = new PetSessionService(this, this.playerPetStorage, this.petDefinitionService, this.fmmBridgeService, this.vanillaPetAppearanceService);
        this.petMenuService = new PetMenuService(this);

        getCommand("pets").setExecutor(new PetsCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PetMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new PetProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PetSpawnBypassListener(this), this);
        this.petSessionService.startFollowTask();
    }

    @Override
    public void onDisable() {
        if (this.petSessionService != null) {
            this.petSessionService.shutdown();
        }
    }

    public void reloadPlugin() {
        reloadConfig();
        this.playerPetStorage.reload();
        this.petDefinitionService.reload();
        this.fmmBridgeService.reload();
        this.petSessionService.reload();
    }

    public PlayerPetStorage getPlayerPetStorage() {
        return this.playerPetStorage;
    }

    public PetDefinitionService getPetDefinitionService() {
        return this.petDefinitionService;
    }

    public FmmBridgeService getFmmBridgeService() {
        return this.fmmBridgeService;
    }

    public PetSessionService getPetSessionService() {
        return this.petSessionService;
    }

    public PetMenuService getPetMenuService() {
        return this.petMenuService;
    }

    public PaperPetFollowService getPaperPetFollowService() {
        return this.paperPetFollowService;
    }

    public VanillaPetAppearanceService getVanillaPetAppearanceService() {
        return this.vanillaPetAppearanceService;
    }
}
