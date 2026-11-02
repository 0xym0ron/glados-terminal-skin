package com.oxytuned.aperture;

import java.util.Map;
import me.jxl.kiosk.plugins.KioskPlugin;
import me.jxl.kiosk.plugins.PluginHost;

public final class AperturePlugin implements KioskPlugin {
    private final ApertureSkin skin = new ApertureSkin();
    @Override public void start(PluginHost host, Map<String,Object> settings) { skin.start(host); }
    @Override public void configure(Map<String,Object> settings) { }
    @Override public void execute(String command, Map<String,Object> args) { throw new IllegalArgumentException("Unknown command: " + command); }
    @Override public void onEvent(String event, Map<String,Object> payload) { skin.onEvent(event,payload); }
    @Override public void stop() { skin.stop(); }
}
