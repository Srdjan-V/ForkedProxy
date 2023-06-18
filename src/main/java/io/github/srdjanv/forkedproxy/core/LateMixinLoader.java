package io.github.srdjanv.forkedproxy.core;

import zone.rong.mixinbooter.ILateMixinLoader;

import java.util.Collections;
import java.util.List;

public class LateMixinLoader implements ILateMixinLoader {
    @Override
    public List<String> getMixinConfigs() {
        return Collections.singletonList("mixins.forkedproxy.late.json");
    }
}
