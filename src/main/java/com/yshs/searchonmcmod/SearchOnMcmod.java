package com.yshs.searchonmcmod;

import static com.yshs.searchonmcmod.KeyBindings.COPY_ITEM_NAME_KEY;
import static com.yshs.searchonmcmod.KeyBindings.SEARCH_ON_MCMOD_KEY;

import java.util.concurrent.CompletableFuture;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * 主类
 */
@Mod(
    modid = SearchOnMcmod.MODID,
    version = Tags.VERSION,
    name = "Search on MCMOD",
    acceptedMinecraftVersions = "[1.7.10]")
public class SearchOnMcmod {

    /**
     * MOD ID
     */
    public static final String MODID = "searchonmcmod";
    public static final Logger LOG = LogManager.getLogger(MODID);

    private KeyPressState searchKeyState = KeyPressState.RELEASED;
    private KeyPressState copyNameKeyState = KeyPressState.RELEASED;

    /**
     * 初始化客户端事件和按键绑定
     *
     * @param event 初始化事件
     */
    @Mod.EventHandler
    @SideOnly(Side.CLIENT)
    public void init(FMLInitializationEvent event) {
        // 注册按键
        ClientRegistry.registerKeyBinding(SEARCH_ON_MCMOD_KEY);
        ClientRegistry.registerKeyBinding(COPY_ITEM_NAME_KEY);

        // 注册事件处理器
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * 在物品 tooltip 渲染事件时触发
     *
     * @param event 物品 tooltip 事件，渲染物品信息时触发
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onItemTooltipEvent(ItemTooltipEvent event) {
        boolean shouldCopyName = copyNameKeyState == KeyPressState.PRESSED_UNCONSUMED;
        boolean shouldSearch = searchKeyState == KeyPressState.PRESSED_UNCONSUMED;
        copyNameKeyState = consumePressOnce(copyNameKeyState);
        searchKeyState = consumePressOnce(searchKeyState);

        if (!shouldCopyName && !shouldSearch) {
            return;
        }

        ItemStack itemStack = event.itemStack;
        if (itemStack == null) {
            return;
        }

        String localizedName = itemStack.getDisplayName();

        if (shouldCopyName) {
            copyHoveredItemName(localizedName);
        }

        if (!shouldSearch) {
            return;
        }

        LOG.info("触发了 MC 百科搜索");

        // 得到物品的注册名
        Item item = itemStack.getItem();
        if (item == null) {
            handleSearchFailure("MC 百科搜索: 无法获取物品", new IllegalStateException("物品为空"));
            return;
        }

        String registryName = Item.itemRegistry.getNameForObject(item);
        if (registryName == null) {
            handleSearchFailure("MC 百科搜索: 无法获取物品注册名", new IllegalStateException("物品未注册: " + item));
            return;
        }

        final String finalLocalizedName = localizedName;
        CompletableFuture.runAsync(new Runnable() {

            @Override
            public void run() {
                String itemMCMODID;
                try {
                    itemMCMODID = MainUtil.fetchItemMCMODID(registryName);
                } catch (Exception e) {
                    handleSearchFailure("MC 百科搜索: 无法通过百科 API 获取物品 MCMOD ID", e);
                    return;
                }

                try {
                    if ("0".equals(itemMCMODID)) {
                        MainUtil.openSearchPage(finalLocalizedName);
                        return;
                    }
                    MainUtil.openItemPage(itemMCMODID);
                } catch (Exception e) {
                    handleSearchFailure("MC 百科搜索: 打开 MC 百科页面失败", e);
                }
            }
        });
    }

    /**
     * 处理 GUI 绘制前的按键状态
     *
     * @param event GUI 绘制前事件
     */
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onDrawScreenPre(GuiScreenEvent.DrawScreenEvent.Pre event) {
        searchKeyState = updateKeyState(SEARCH_ON_MCMOD_KEY, searchKeyState);
        copyNameKeyState = updateKeyState(COPY_ITEM_NAME_KEY, copyNameKeyState);
    }

    /**
     * 更新按键状态
     *
     * @param keyBinding 按键绑定
     * @param state      当前按键状态
     * @return 更新后的按键状态
     */
    private static KeyPressState updateKeyState(KeyBinding keyBinding, KeyPressState state) {
        int keyCode = keyBinding.getKeyCode();
        if (keyCode == Keyboard.KEY_NONE) {
            return state;
        }
        if (Keyboard.isKeyDown(keyCode)) {
            if (state == KeyPressState.RELEASED) {
                return KeyPressState.PRESSED_UNCONSUMED;
            }
            return state;
        }
        return KeyPressState.RELEASED;
    }

    /**
     * 消费一次按键状态
     *
     * @param state 当前按键状态
     * @return 消费后的按键状态
     */
    private static KeyPressState consumePressOnce(KeyPressState state) {
        if (state == KeyPressState.PRESSED_UNCONSUMED) {
            return KeyPressState.PRESSED_CONSUMED;
        }
        return state;
    }

    /**
     * 复制鼠标指向的物品名称
     *
     * @param localizedName 本地化物品名称
     */
    private static void copyHoveredItemName(String localizedName) {
        if (localizedName == null || localizedName.isEmpty()) {
            LOG.warn("复制鼠标指针下方物品名称失败：名称为空");
            return;
        }
        GuiScreen.setClipboardString(localizedName);
        LOG.info("已复制鼠标指针下方物品名称到剪贴板: {}", localizedName);
    }

    /**
     * 处理搜索失败
     *
     * @param message 错误日志消息
     * @param e       异常
     */
    private static void handleSearchFailure(String message, Exception e) {
        LOG.error(message, e);
    }

    private enum KeyPressState {
        // 按键松开
        RELEASED,
        // 按键按下未消费
        PRESSED_UNCONSUMED,
        // 按键按下已消费
        PRESSED_CONSUMED
    }
}
