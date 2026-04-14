package com.gtouming.void_dimension.curios;

import net.neoforged.fml.ModList;

public class CuriosUtil {
    public static boolean CURIOS_LOADED = false;
    private static ICuriosAPI curiosAPI;

    public static void init() {
        CURIOS_LOADED = ModList.get().isLoaded("curios");
        if (curiosAPI == null) {
            if (CURIOS_LOADED) {
                try {
                    curiosAPI = new FoundAPI();
                } catch (Exception e) {
                    curiosAPI = new NoFoundAPI();
                }
            } else {
                curiosAPI = new NoFoundAPI();
            }
        }
    }

    public static ICuriosAPI curiosAPI() {
        return curiosAPI;
    }
}