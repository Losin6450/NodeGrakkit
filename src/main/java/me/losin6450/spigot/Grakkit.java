package me.losin6450.spigot;

import com.caoccao.javet.enums.JSRuntimeType;
import com.caoccao.javet.exceptions.JavetException;
import me.losin6450.core.Context;
import me.losin6450.interop.Java;
import org.bukkit.plugin.java.JavaPlugin;

public class Grakkit extends JavaPlugin {

    private Context context;

    @Override
    public void onEnable() {
        try {
            this.context = new Context(getDataFolder().toPath(), JSRuntimeType.Node);
            this.context.addToGlobal("Java", Java.class);
            this.context.start();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onDisable() {
        try {
            this.context.stop();
        } catch (JavetException e) {
            throw new RuntimeException(e);
        }
    }
}
