package com.yshs.searchonmcmod;

import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.yshs.searchonmcmod.KeyBindings.COPY_ITEM_NAME_KEY;
import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

/**
 * 主类
 */
@Slf4j
public class SearchOnMcmod implements ModInitializer {
    /**
     * 是否按下搜索按键
     */
    public static boolean searchKeyDown = false;
    /**
     * 是否按下复制名称按键
     */
    public static boolean copyNameKeyDown = false;

    @Override
    public void onInitialize() {
        // 注册按键绑定
        KeyBindingHelper.registerKeyBinding(SEARCH_ON_MCMOD_KEY);
        KeyBindingHelper.registerKeyBinding(COPY_ITEM_NAME_KEY);
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
        if (!searchKeyDown && !copyNameKeyDown) {
            return;
        }
        if (copyNameKeyDown) {
            copyNameKeyDown = false;
            copyHoveredItemName(itemStack.getHoverName().getString());
        }
        if (!searchKeyDown) {
            return;
        }
        searchKeyDown = false;
        // 1. 得到物品的描述ID
        String descriptionId = itemStack.getItem().getDescriptionId();
        if (StringUtils.isBlank(descriptionId)) {
            return;
        }
        // 2. 转换为注册表名
        String registryName = MainUtil.convertDescriptionIdToRegistryName(descriptionId);
        // 3. 如果注册表名为空气，则不进行搜索
        if ("minecraft:air".equals(registryName)) {
            return;
        }
        // 4. 如果注册表明为空，但是物品的描述ID不为空，则进行搜索
        if (StringUtils.isBlank(registryName) && StringUtils.isNotBlank(descriptionId)) {
            try {
                MainUtil.openSearchPage(descriptionId);
            } catch (Exception e) {
                handleSearchFailure("MC百科搜索: 打开搜索页面失败", e);
            }
            return;
        }

        // 得到物品的本地化名称
        String localizedName = itemStack.getHoverName().getString();

        CompletableFuture.runAsync(() -> {
            // 5. 查找并得到物品在MCMOD中的ID
            String itemMCMODID;
            try {
                itemMCMODID = MainUtil.fetchItemMCMODID(registryName);
            } catch (Exception e) {
                handleSearchFailure("MC百科搜索: 无法通过百科 API 获取物品 MCMOD ID", e);
                return;
            }

            try {
                // 6. 如果mcmodItemID为0，则进行搜索
                if ("0".equals(itemMCMODID)) {
                    MainUtil.openSearchPage(localizedName);
                    return;
                }

                // 7. 判断物品页面是否存在，如果不存在则进行搜索
                if (!MainUtil.itemPageExist(itemMCMODID)) {
                    MainUtil.openSearchPage(localizedName);
                    return;
                }

                // 8. 打开MCMOD的物品页面
                MainUtil.openItemPage(itemMCMODID);
            } catch (Exception e) {
                handleSearchFailure("MC百科搜索: 打开MC百科页面失败", e);
            }
        });
    }

    private static void handleSearchFailure(String message, Exception e) {
        log.error(message, e);
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            player.sendSystemMessage(Component.translatable("text.searchonmcmod.search_failed"));
        }
    }

    private static void copyHoveredItemName(String localizedName) {
        if (StringUtils.isBlank(localizedName)) {
            log.warn("复制鼠标指针下方物品名称失败：名称为空");
            return;
        }
        Minecraft.getInstance().keyboardHandler.setClipboard(localizedName);
        log.info("已复制鼠标指针下方物品名称到剪贴板: {}", localizedName);
    }

}
