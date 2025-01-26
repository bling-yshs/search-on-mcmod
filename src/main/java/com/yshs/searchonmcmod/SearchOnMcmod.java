package com.yshs.searchonmcmod;

import lombok.SneakyThrows;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.Optional;

import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

/**
 * 主类
 */
@Mod(modid = SearchOnMcmod.MOD_ID)
public class SearchOnMcmod {
    public static final String MOD_ID = "searchonmcmod";
    private static final Logger log = LogManager.getLogger();
    private static boolean keyDown = false;

    /**
     * 构造函数
     */
    public SearchOnMcmod() {
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(SEARCH_ON_MCMOD_KEY);
    }

    /**
     * 在物品tooltip渲染事件时触发
     *
     * @param event 物品tooltip事件，渲染物品信息时触发
     */
    @SubscribeEvent
    @SneakyThrows
    public void onRenderTooltipEvent(ItemTooltipEvent event) {
        if (keyDown == false) {
            return;
        }
        keyDown = false;
        log.info("触发了");
        // 1. 得到物品的注册表名
        ResourceLocation registryName = event.getItemStack().getItem().getRegistryName();
        if (registryName == null) {
            // 如果注册表名为空，直接搜索物品的本地化名称
            MainUtil.openSearchPage(event.getItemStack().getDisplayName());
            return;
        }
        String registryNameStr = registryName.toString();
        // 3. 如果注册表名为空气，则不进行搜索
        if ("minecraft:air".equals(registryNameStr)) {
            return;
        }
        // 5. 查找并得到物品在MCMOD中的ID
        Optional<String> optionalItemMCMODID = MainUtil.fetchItemMCMODID(registryNameStr);
        if (!optionalItemMCMODID.isPresent()) {
            return;
        }
        String itemMCMODID = optionalItemMCMODID.get();

        // 6. 如果mcmodItemID为0，则进行搜索
        if ("0".equals(itemMCMODID)) {
            // 得到物品的本地化名称
            String localizedName = event.getItemStack().getDisplayName();
            // 然后到https://search.mcmod.cn/s?key=%s去搜索
            MainUtil.openSearchPage(localizedName);
            return;
        }

        // 6. 打开MCMOD的物品页面
        MainUtil.openItemPage(itemMCMODID);

    }

    /**
     * 在按键按下事件时触发
     *
     * @param event 按键按下事件，监听是否按下搜索按键
     */
    @SubscribeEvent
    public void onKeyPressed(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (Keyboard.isKeyDown(SEARCH_ON_MCMOD_KEY.getKeyCode()) && keyDown == false) {
            keyDown = true;
            log.info("按键已按下，keyDown设置为true");
        }
    }

    /**
     * 在按键释放事件时触发
     *
     * @param event 按键释放事件，监听是否释放搜索按键
     */
    @SubscribeEvent
    public void onKeyReleased(GuiScreenEvent.KeyboardInputEvent.Post event) {
        if (!Keyboard.isKeyDown(SEARCH_ON_MCMOD_KEY.getKeyCode()) && keyDown == true) {
            keyDown = false;
            log.info("按键已释放，keyDown设置为false");
        }
    }

}
