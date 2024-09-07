package org.denysgarbuz.utils;

import org.denysgarbuz.indicators.TsiIndicator;
import org.denysgarbuz.indicators.TsiSettings;
import velox.api.layer1.messages.indicators.SettingsAccess;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class SettingsHandler {
    private final ConcurrentHashMap<String, TsiSettings> tsiSettingsMapping;
    private  SettingsAccess settingsAccess;
    private final ReentrantLock lock;

    public SettingsHandler() {
        this.tsiSettingsMapping = new ConcurrentHashMap<>();
        this.lock = new ReentrantLock();
    }

    public void setSettingsAccess(SettingsAccess settingsAccess) {
        this.settingsAccess = settingsAccess;
    }


    public TsiSettings getTsiSettingsFor(String alias) {
        lock.lock();
        try {
            TsiSettings settings = tsiSettingsMapping.get(alias);
            if (settings == null) {
                settings = (TsiSettings) settingsAccess.getSettings(alias, TsiIndicator.INDICATOR_NAME, TsiSettings.class);
                tsiSettingsMapping.put(alias, settings);
            }
            return settings;

        } finally {
            lock.unlock();
        }
    }


    public void tsiSettingsChanged(String alias, TsiSettings settings) {
        lock.lock();
        try {
            settingsAccess.setSettings(alias,  TsiIndicator.INDICATOR_NAME, settings, settings.getClass());
        } finally {
            lock.unlock();
        }
    }



}
