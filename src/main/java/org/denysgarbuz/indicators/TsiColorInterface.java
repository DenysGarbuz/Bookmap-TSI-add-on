package org.denysgarbuz.indicators;

import org.denysgarbuz.utils.SettingsHandler;
import velox.api.layer1.common.Log;
import velox.api.layer1.layers.strategies.interfaces.Layer1IndicatorColorInterface;
import velox.colors.ColorsChangedListener;

import java.awt.*;

public class TsiColorInterface implements Layer1IndicatorColorInterface {

    SettingsHandler settingsHandler;


    public TsiColorInterface(SettingsHandler settingsHandler) {
        this.settingsHandler = settingsHandler;
    }


    @Override
    public void setColor(String alias, String name, Color color) {
        TsiSettings settings = settingsHandler.getTsiSettingsFor(alias);
        settings.setColor(name, color);
        settingsHandler.tsiSettingsChanged(alias, settings);
    }

    @Override
    public Color getColor(String alias, String name) {

        Color color = settingsHandler.getTsiSettingsFor(alias).getColor(name);

        if (color == null) {
            switch (name) {
                case TsiIndicator.INDICATOR_LINE_COLOR_NAME:
                    color = TsiIndicator.INDICATOR_LINE_DEFAULT_COLOR;
                    break;

                default:
                    Log.warn("Layer1CustomEventsDemo: unknown color name " + name);
                    color = Color.WHITE;
                    break;
            }
        }

        return color;
    }


    @Override
    public void addColorChangeListener(ColorsChangedListener listener) {
        // every one of our colors is modified only from one place
    }


}
