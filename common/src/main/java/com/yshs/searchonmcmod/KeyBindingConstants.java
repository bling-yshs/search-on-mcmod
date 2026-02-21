package com.yshs.searchonmcmod;

import org.lwjgl.glfw.GLFW;

/**
 * 按键绑定常量类
 * 用于在 Fabric 和 NeoForge 之间共享按键配置
 */
public class KeyBindingConstants {

    /**
     * 搜索按键名称
     */
    public static final String SEARCH_KEY_NAME = "key.searchonmcmod.search_on_mcmod";

    /**
     * 复制名称按键名称
     */
    public static final String COPY_ITEM_NAME_KEY_NAME = "key.searchonmcmod.copy_item_name";

    /**
     * 分类
     */
    public static final String CATEGORY = "categories";

    /**
     * 搜索默认按键码 (B 键)
     */
    public static final int SEARCH_DEFAULT_KEY_CODE = GLFW.GLFW_KEY_B;

    /**
     * 复制名称默认按键码（默认未绑定）
     */
    public static final int COPY_ITEM_NAME_DEFAULT_KEY_CODE = GLFW.GLFW_KEY_UNKNOWN;

    private KeyBindingConstants() {
        // 工具类，禁止实例化
    }
}
