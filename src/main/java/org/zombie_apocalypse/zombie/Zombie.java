package org.zombie_apocalypse.zombie;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import org.zombie_apocalypse.zombie.client.ClientKeybinds;
import org.zombie_apocalypse.zombie.network.ModNetworking;

@Mod(Zombie.MODID)
public class Zombie {

    public static final String MODID = "zombie";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    // ------------------------------
    // Blocks
    // ------------------------------
    public static final RegistryObject<Block> CUSTOM_GRASS = BLOCKS.register("custom_grass",
            () -> new CustomGrassBlock(BlockBehaviour.Properties
                    .of()
                    .mapColor(MapColor.GRASS)
                    .strength(0.6f)
                    .noOcclusion()
                    .noLootTable())); // block wouldn't drop any item by default

    // ------------------------------
    // Items
    // ------------------------------
    public static final RegistryObject<Item> CUSTOM_GRASS_ITEM = ITEMS.register("custom_grass",
            () -> new BlockItem(CUSTOM_GRASS.get(), new Item.Properties()));

    public static final RegistryObject<Item> SPECIAL_HOE = ITEMS.register("special_hoe",
            () -> new HoeItem(Tiers.WOOD, 0, 0f, new Item.Properties()));

    public static final RegistryObject<Item> DIAMOND_ITEM = ITEMS.register("diamond_item",
            () -> new Item(new Item.Properties()));

    public Zombie() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register setup
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        // Register blocks & items
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);

        // Register MCForge events
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");

        // networking REGISTER
        event.enqueueWork(ModNetworking::register);
    }


    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(CUSTOM_GRASS_ITEM.get());
            event.accept(SPECIAL_HOE.get());
            event.accept(DIAMOND_ITEM.get());
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    // ------------------------------
    // Custom Grass Block
    // ------------------------------
    public static class CustomGrassBlock extends Block {
        public CustomGrassBlock(Properties properties) {
            super(properties);
        }

        @Override
        public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
            ItemStack held = player.getItemInHand(hand);

            // If player holds special item - for now it is special_hoe it drops another special item - for now its diamond
            if (held.getItem() == SPECIAL_HOE.get()) {
                if (!world.isClientSide) {
                    popResource(world, pos, new ItemStack(Items.DIAMOND, 1));
                    world.removeBlock(pos, false);
                }
                return InteractionResult.SUCCESS;
            }

            return InteractionResult.PASS;
        }
    }


    // ------------------------------
    // Client events
    // ------------------------------
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {

            LOGGER.info("HELLO FROM CLIENT SETUP");
        }
    }

}

