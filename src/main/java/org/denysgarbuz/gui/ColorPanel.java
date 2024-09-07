package org.denysgarbuz.gui;

import org.denysgarbuz.indicators.TsiCalculatable;
import org.denysgarbuz.indicators.TsiColorInterface;
import org.denysgarbuz.indicators.TsiIndicator;
import org.denysgarbuz.utils.SettingsHandler;
import velox.api.layer1.layers.strategies.interfaces.InvalidateInterface;
import velox.api.layer1.messages.indicators.IndicatorColorInterface;
import velox.colors.ColorsChangedListener;
import velox.gui.StrategyPanel;
import velox.gui.colors.ColorsConfigItem;

import java.awt.*;

public class ColorPanel extends StrategyPanel {

    public ColorPanel(String alias, TsiColorInterface tsiColorInterface, SettingsHandler settingsHandler, TsiCalculatable tsiCalculatable) {
        super("Colors", new GridBagLayout());
        setLayout(new GridBagLayout());

        GridBagConstraints gbConst = createGridBagConstraints(0, 0);

        IndicatorColorInterface indicatorColorInterface = new IndicatorColorInterface() {
            @Override
            public void set(String name, Color color) {
                tsiColorInterface.setColor(alias, name, color);
            }

            @Override
            public Color getOrDefault(String name, Color defaultValue) {
                Color color = settingsHandler.getTsiSettingsFor(alias).getColor(name);
                return color == null ? defaultValue : color;
            }

            @Override
            public void addColorChangeListener(ColorsChangedListener listener) {
            }
        };

        ColorsConfigItem configItemLines = new ColorsConfigItem(TsiIndicator.INDICATOR_LINE_COLOR_NAME, TsiIndicator.INDICATOR_LINE_COLOR_NAME, true,
                TsiIndicator.INDICATOR_LINE_DEFAULT_COLOR, indicatorColorInterface, new ColorsChangedListener() {
            @Override
            public void onColorsChanged() {
                InvalidateInterface invalidateInterface = tsiCalculatable.getInvalidateInterface();
                if (invalidateInterface != null) {
                    invalidateInterface.invalidate();
                }
            }
        });

        add(configItemLines, gbConst);
    }

    private GridBagConstraints createGridBagConstraints(int gridx, int gridy) {
        GridBagConstraints gbConst = new GridBagConstraints();
        gbConst.gridx = gridx;
        gbConst.gridy = gridy;
        gbConst.weightx = 1;
        gbConst.insets = new Insets(5, 5, 5, 5);
        gbConst.fill = GridBagConstraints.HORIZONTAL;
        return gbConst;
    }
}
