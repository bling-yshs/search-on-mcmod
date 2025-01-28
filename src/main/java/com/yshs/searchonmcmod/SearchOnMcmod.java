package com.yshs.searchonmcmod;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameData;
import lombok.SneakyThrows;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.Optional;

import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

@Mod(modid = SearchOnMcmod.MOD_ID, name = "SearchOnMcmod", acceptedMinecraftVersions = "[1.7.10]")
public class SearchOnMcmod {

    public static final String MOD_ID = "searchonmcmod";
    public static final Logger log = LogManager.getLogger(MOD_ID);
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
        String registryName = GameData.getItemRegistry().getNameForObject(event.itemStack.getItem());
        if (registryName == null) {
            // 如果注册表名为空，直接搜索物品的本地化名称
            MainUtil.openSearchPage(event.itemStack.getDisplayName());
            return;
        }
        // 3. 如果注册表名为空气，则不进行搜索
        if ("minecraft:air".equals(registryName)) {
            return;
        }
        // 5. 查找并得到物品在MCMOD中的ID
        Optional<String> optionalItemMCMODID = MainUtil.fetchItemMCMODID(registryName);
        if (!optionalItemMCMODID.isPresent()) {
            return;
        }
        String itemMCMODID = optionalItemMCMODID.get();

        // 6. 如果mcmodItemID为0，则进行搜索
        if ("0".equals(itemMCMODID)) {
            // 得到物品的本地化名称
            String localizedName = event.itemStack.getDisplayName();
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
    public void onKeyPressed(GuiScreenEvent event) {
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
    public void onKeyReleased(GuiScreenEvent event) {
        if (!Keyboard.isKeyDown(SEARCH_ON_MCMOD_KEY.getKeyCode()) && keyDown == true) {
            keyDown = false;
            log.info("按键已释放，keyDown设置为false");
        }
    }

}
