package com.yshs.searchonmcmod;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

/**
 * 主类
 */
@Slf4j
public class SearchOnMcmod implements ModInitializer {
    /**
     * 是否按下按键
     */
    public static boolean keyDown = false;

    /**
     * 初始化 MOD
     */
    @Override
    public void onInitialize() {
        // 注册按键绑定
        KeyBindingHelper.registerKeyBinding(SEARCH_ON_MCMOD_KEY);
        // 渲染物品信息时触发
        ItemTooltipCallback.EVENT.register(this::onRenderTooltipEvent);
    }

    /**
     * 当渲染物品tooltip时触发的
     *
     * @param itemStack     物品栈
     * @param tooltipFlag   物品信息提示标志
     * @param componentList 物品信息列表
     */
    public void onRenderTooltipEvent(ItemStack itemStack, TooltipFlag tooltipFlag, List<Component> componentList) {
        if (keyDown == false) {
            return;
        }
        keyDown = false;
        if (itemStack.isEmpty()) {
            return;
        }
        Item item = itemStack.getItem();
        ResourceLocation id = Registry.ITEM.getKey(item);
        if (id == null) {
            log.warn("无法获取物品注册名");
            handleSearchFailure();
            return;
        }

        String registryName = id.toString();
        log.info("物品注册名: {}", registryName);

        // 得到物品的本地化名称
        String localizedName = itemStack.getHoverName().getString();

        CompletableFuture.runAsync(() -> {
            String itemMCMODID;
            try {
                itemMCMODID = MainUtil.fetchItemMCMODID(registryName);
            } catch (Exception e) {
                log.error("MC百科搜索: 无法通过百科 API 获取物品 MCMOD ID，请检查您的网络情况", e);
                handleSearchFailure();
                return;
            }

            if ("0".equals(itemMCMODID)) {
                log.warn("API 返回 ID 为 0，回退到搜索页面");
                if (StringUtils.isBlank(localizedName)) {
                    handleSearchFailure();
                    return;
                }
                MainUtil.openSearchPage(localizedName);
                return;
            }

            MainUtil.openItemPage(itemMCMODID);
        });
    }

    /**
     * 显示通用错误提示
     */
    private static void handleSearchFailure() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            LocalPlayer player = minecraft.player;
            if (player != null) {
                player.displayClientMessage(new TranslatableComponent("text.searchonmcmod.error_see_log"), false);
            }
        });
    }

}
