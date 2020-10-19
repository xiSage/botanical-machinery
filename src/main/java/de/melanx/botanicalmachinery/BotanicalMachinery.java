package de.melanx.botanicalmachinery;

import de.melanx.botanicalmachinery.blocks.screens.*;
import de.melanx.botanicalmachinery.config.ClientConfig;
import de.melanx.botanicalmachinery.config.ServerConfig;
import de.melanx.botanicalmachinery.core.ModGroup;
import de.melanx.botanicalmachinery.core.registration.ModBlocks;
import de.melanx.botanicalmachinery.core.registration.Registration;
import de.melanx.botanicalmachinery.data.DataCreator;
import de.melanx.botanicalmachinery.network.BotanicalMachineryNetwork;
import io.github.noeppi_noeppi.libx.mod.registration.ModXRegistration;
import net.minecraft.client.gui.ScreenManager;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("botanicalmachinery")
public class BotanicalMachinery extends ModXRegistration {
    public static BotanicalMachinery instance;
    public static BotanicalMachineryNetwork network;

    public BotanicalMachinery() {
        super("botanicalmachinery", new ModGroup("botanicalmachinery"));
        instance = this;
        network = new BotanicalMachineryNetwork(this);

        this.addRegistrationHandler(ModBlocks::register);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.CLIENT_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ServerConfig.SERVER_CONFIG);
        ClientConfig.loadConfig(ClientConfig.CLIENT_CONFIG, FMLPaths.CONFIGDIR.get().resolve(this.modid + "-client.toml"));
        ServerConfig.loadConfig(ServerConfig.SERVER_CONFIG, FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).resolve(this.modid + "-server.toml"));

        FMLJavaModLoadingContext.get().getModEventBus().addListener(DataCreator::onGatherData);
        Registration.init();
    }

    public static BotanicalMachinery getInstance() {
        return instance;
    }

    public static BotanicalMachineryNetwork getNetwork() {
        return network;
    }

    @Override
    protected void setup(FMLCommonSetupEvent event) {

    }

    @Override
    protected void clientSetup(FMLClientSetupEvent event) {
        ScreenManager.registerFactory(Registration.CONTAINER_ALFHEIM_MARKET.get(), ScreenAlfheimMarket::new);
        ScreenManager.registerFactory(Registration.CONTAINER_INDUSTRIAL_AGGLOMERATION_FACTORY.get(), ScreenIndustrialAgglomerationFactory::new);
        ScreenManager.registerFactory(Registration.CONTAINER_MANA_BATTERY.get(), ScreenManaBattery::new);
        ScreenManager.registerFactory(Registration.CONTAINER_MECHANICAL_APOTHECARY.get(), ScreenMechanicalApothecary::new);
        ScreenManager.registerFactory(Registration.CONTAINER_MECHANICAL_BREWERY.get(), ScreenMechanicalBrewery::new);
        ScreenManager.registerFactory(Registration.CONTAINER_MECHANICAL_DAISY.get(), ScreenMechanicalDaisy::new);
        ScreenManager.registerFactory(Registration.CONTAINER_MECHANICAL_MANA_POOL.get(), ScreenMechanicalManaPool::new);
        ScreenManager.registerFactory(Registration.CONTAINER_MECHANICAL_RUNIC_ALTAR.get(), ScreenMechanicalRunicAltar::new);

//        RenderTypeLookup.setRenderLayer(Registration.BLOCK_MECHANICAL_DAISY.get(), RenderType.getCutout());
//        RenderTypeLookup.setRenderLayer(Registration.BLOCK_ALFHEIM_MARKET.get(), RenderType.getCutout());
//        RenderTypeLookup.setRenderLayer(Registration.BLOCK_MECHANICAL_MANA_POOL.get(), RenderType.getCutout());
//        RenderTypeLookup.setRenderLayer(Registration.BLOCK_MECHANICAL_RUNIC_ALTAR.get(), RenderType.getCutout());
//        RenderTypeLookup.setRenderLayer(Registration.BLOCK_INDUSTRIAL_AGGLOMERATION_FACTORY.get(), RenderType.getCutout());
//        RenderTypeLookup.setRenderLayer(Registration.BLOCK_MECHANICAL_APOTHECARY.get(), RenderType.getCutout());
//        RenderTypeLookup.setRenderLayer(Registration.BLOCK_MECHANICAL_BREWERY.get(), RenderType.getCutout());
//
//        ClientRegistry.bindTileEntityRenderer(Registration.TILE_MECHANICAL_DAISY.get(), TesrMechanicalDaisy::new);
//        ClientRegistry.bindTileEntityRenderer(Registration.TILE_ALFHEIM_MARKET.get(), TesrAlfheimMarket::new);
//        ClientRegistry.bindTileEntityRenderer(Registration.TILE_MECHANICAL_MANA_POOL.get(), TesrMechanicalManaPool::new);
//        ClientRegistry.bindTileEntityRenderer(Registration.TILE_MECHANICAL_RUNIC_ALTAR.get(), TesrMechanicalRunicAltar::new);
//        ClientRegistry.bindTileEntityRenderer(Registration.TILE_INDUSTRIAL_AGGLOMERATION_FACTORY.get(), TesrIndustrialAgglomerationFactory::new);
//        ClientRegistry.bindTileEntityRenderer(Registration.TILE_MECHANICAL_APOTHECARY.get(), TesrMechanicalApothecary::new);
//        ClientRegistry.bindTileEntityRenderer(Registration.TILE_MECHANICAL_BREWERY.get(), TesrMechanicalBrewery::new);
    }
}
