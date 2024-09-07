package org.denysgarbuz.indicators;

import org.denysgarbuz.Layer1ApiBarsDemo;
import velox.api.layer1.layers.strategies.interfaces.OnlineCalculatable;
import velox.api.layer1.messages.indicators.IndicatorColorScheme;
import velox.api.layer1.messages.indicators.IndicatorLineStyle;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator;

import java.awt.*;

public class BarIndicator implements CustomIndicator {
    private static final String INDICATOR_NAME_BARS_MAIN = "Bars: main chart";
    private static final String INDICATOR_LINE_COLOR_NAME = "Trade markers line";
    private static final Color INDICATOR_LINE_DEFAULT_COLOR = Color.WHITE;

    private final OnlineCalculatable onlineCalculatable;


    public BarIndicator(OnlineCalculatable onlineCalculatable) {
        this.onlineCalculatable = onlineCalculatable;
    }

    @Override
    public Layer1ApiUserMessageModifyIndicator getMessage(boolean isAdd) {
        return Layer1ApiUserMessageModifyIndicator.builder(Layer1ApiBarsDemo.class, INDICATOR_NAME_BARS_MAIN)
                .setIsAdd(isAdd)
                .setGraphType(Layer1ApiUserMessageModifyIndicator.GraphType.PRIMARY)
                .setOnlineCalculatable(onlineCalculatable)
                .setIndicatorColorScheme(new IndicatorColorScheme() {
                    @Override
                    public ColorDescription[] getColors() {
                        return new ColorDescription[]{
                                new ColorDescription(Layer1ApiBarsDemo.class, INDICATOR_LINE_COLOR_NAME, INDICATOR_LINE_DEFAULT_COLOR, false),
                        };
                    }

                    @Override
                    public String getColorFor(Double value) {
                        return INDICATOR_LINE_COLOR_NAME;
                    }

                    @Override
                    public ColorIntervalResponse getColorIntervalsList(double valueFrom, double valueTo) {
                        return new ColorIntervalResponse(new String[]{INDICATOR_LINE_COLOR_NAME}, new double[]{});
                    }
                })
                .setIndicatorLineStyle(IndicatorLineStyle.NONE)
                .build();
    }
}
