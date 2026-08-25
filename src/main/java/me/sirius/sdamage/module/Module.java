package me.sirius.sdamage.module;

public interface Module {

    String id();

    boolean isConfigEnabled();

    boolean isActive();

    void enable();

    void disable();
}
