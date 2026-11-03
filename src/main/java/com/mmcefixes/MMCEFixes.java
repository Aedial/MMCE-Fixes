package com.mmcefixes;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import com.mmcefixes.config.MMCEFixesConfig;


@Mod(
    modid = Tags.MODID,
    name = Tags.MODNAME,
    version = Tags.VERSION,
    dependencies = "required-after:modularmachinery;after:mixinbooter@[8.0,)",
    guiFactory = "com.mmcefixes.config.MMCEFixesGuiFactory"
)
public class MMCEFixes {
    @Mod.Instance(Tags.MODID)
    public static MMCEFixes instance;

    @SidedProxy(clientSide = "com.mmcefixes.ClientProxy", serverSide = "com.mmcefixes.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MMCEFixesConfig.load();
        proxy.preInit();
    }
}
