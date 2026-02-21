package com.yshs.searchonmcmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.yshs.searchonmcmod.KeyBindings.COPY_ITEM_NAME_KEY;
import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

/**
 * 主类
 */
@Mod(modid = SearchOnMcmod.MOD_ID)
public class SearchOnMcmod {
    /**
     * MOD ID
     */
    public static final String MOD_ID = "searchonmcmod";
    private static final Logger log = LogManager.getLogger();
    private KeyPressState searchKeyState = KeyPressState.RELEASED;
    private KeyPressState copyNameKeyState = KeyPressState.RELEASED;

    /**
     * 构造函数
     */
    public SearchOnMcmod() {
        MinecraftForge.EVENT_BUS.register(this);
        ClientRegistry.registerKeyBinding(SEARCH_ON_MCMOD_KEY);
        ClientRegistry.registerKeyBinding(COPY_ITEM_NAME_KEY);
    }

    /**
     * 在物品tooltip渲染事件时触发
     *
     * @param event 物品tooltip事件，渲染物品信息时触发
     */
    @SubscribeEvent
    public void onRenderTooltipEvent(ItemTooltipEvent event) {
        boolean shouldCopyName = copyNameKeyState == KeyPressState.PRESSED_UNCONSUMED;
        boolean shouldSearch = searchKeyState == KeyPressState.PRESSED_UNCONSUMED;
        copyNameKeyState = consumePressOnce(copyNameKeyState);
        searchKeyState = consumePressOnce(searchKeyState);

        if (!shouldCopyName && !shouldSearch) {
            return;
        }
        String localizedName = event.getItemStack().getDisplayName();

        if (shouldCopyName) {
            copyHoveredItemName(localizedName);
        }

        if (!shouldSearch) {
            return;
        }

        log.info("触发了MC百科搜索");
        // 1. 得到物品的注册表名
        ResourceLocation registryName = event.getItemStack().getItem().getRegistryName();
        if (registryName == null) {
            // 如果注册表名为空，直接搜索物品的本地化名称
            MainUtil.openSearchPage(localizedName);
            return;
        }
        String registryNameStr = registryName.toString();
        // 3. 如果注册表名为空气，则不进行搜索
        if ("minecraft:air".equals(registryNameStr)) {
            return;
        }
        // 4. 得到物品的metadata(如果有的话)
        int metadata = event.getItemStack().getMetadata();
        // 5. 异步获取物品的MCMODID
        CompletableFuture.runAsync(() -> {
            Optional<String> optionalItemMCMODID;
            try {
                optionalItemMCMODID = MainUtil.fetchItemMCMODID(registryNameStr, metadata);
            } catch (Exception e) {
                log.error("MC百科搜索: 无法通过百科 API 获取物品 MCMOD ID，请检查您的网络情况", e);
                // 发送提示消息
                if (Minecraft.getMinecraft().player != null) {
                    Minecraft.getMinecraft().player
                            .sendMessage(new TextComponentTranslation("text.searchonmcmod.mcmodid_not_found"));
                }
                return;
            }
            if (!optionalItemMCMODID.isPresent()) {
                return;
            }
            String itemMCMODID = optionalItemMCMODID.get();

            // 6. 如果mcmodItemID为0，则进行搜索
            if ("0".equals(itemMCMODID)) {
                // 到https://search.mcmod.cn/s?key=%s去搜索
                MainUtil.openSearchPage(localizedName);
                return;
            }

            // 7. 判断物品页面是否存在，如果不存在则进行搜索
            if (!MainUtil.itemPageExist(itemMCMODID)) {
                // 到https://search.mcmod.cn/s?key=%s去搜索
                MainUtil.openSearchPage(localizedName);
                return;
            }

            // 8. 打开MCMOD的物品页面
            MainUtil.openItemPage(itemMCMODID);

        });
    }

    // 键位状态机依赖按键事件的下压/释放状态来保证一次按压只触发一次动作。
    @SubscribeEvent
    public void onKeyboardInput(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        int eventKey = Keyboard.getEventKey();
        if (eventKey == Keyboard.KEY_NONE) {
            return;
        }

        if (Keyboard.getEventKeyState()) {
            searchKeyState = markPressed(eventKey, SEARCH_ON_MCMOD_KEY, searchKeyState);
            copyNameKeyState = markPressed(eventKey, COPY_ITEM_NAME_KEY, copyNameKeyState);
            return;
        }

        searchKeyState = markReleased(eventKey, SEARCH_ON_MCMOD_KEY, searchKeyState);
        copyNameKeyState = markReleased(eventKey, COPY_ITEM_NAME_KEY, copyNameKeyState);
    }

    private static KeyPressState markPressed(int eventKey, net.minecraft.client.settings.KeyBinding keyBinding, KeyPressState state) {
        if (!keyBinding.isActiveAndMatches(eventKey)) {
            return state;
        }
        if (state == KeyPressState.RELEASED) {
            return KeyPressState.PRESSED_UNCONSUMED;
        }
        return state;
    }

    private static KeyPressState markReleased(int eventKey, net.minecraft.client.settings.KeyBinding keyBinding, KeyPressState state) {
        if (eventKey != keyBinding.getKeyCode()) {
            return state;
        }
        return KeyPressState.RELEASED;
    }

    private static KeyPressState consumePressOnce(KeyPressState state) {
        if (state == KeyPressState.PRESSED_UNCONSUMED) {
            return KeyPressState.PRESSED_CONSUMED;
        }
        return state;
    }

    private static void copyHoveredItemName(String localizedName) {
        if (StringUtils.isBlank(localizedName)) {
            log.warn("复制鼠标指针下方物品名称失败：名称为空");
            return;
        }
        GuiScreen.setClipboardString(localizedName);
        log.info("已复制鼠标指针下方物品名称到剪贴板: {}", localizedName);
    }

    private enum KeyPressState {
        RELEASED,
        PRESSED_UNCONSUMED,
        PRESSED_CONSUMED
    }
}
