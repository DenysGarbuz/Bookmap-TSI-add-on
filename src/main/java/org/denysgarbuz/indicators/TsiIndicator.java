package org.denysgarbuz.indicators;

import org.denysgarbuz.Layer1ApiBarsDemo;
import velox.api.layer1.layers.strategies.interfaces.Layer1IndicatorColorInterface;
import velox.api.layer1.layers.strategies.interfaces.OnlineCalculatable;
import velox.api.layer1.messages.indicators.IndicatorColorScheme;
import velox.api.layer1.messages.indicators.IndicatorLineStyle;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator;

import java.awt.*;

public class TsiIndicator implements CustomIndicator {

    public final static Color INDICATOR_LINE_DEFAULT_COLOR = Color.WHITE;
    public final static String INDICATOR_LINE_COLOR_NAME = "TSI Indicator color";
    public final static String INDICATOR_NAME = "TSI indicator";

    private final OnlineCalculatable onlineCalculatable;
    private final Layer1IndicatorColorInterface colorInterface;

    public TsiIndicator(OnlineCalculatable onlineCalculatable, Layer1IndicatorColorInterface indicatorColorInterface) {
        this.onlineCalculatable = onlineCalculatable;
        this.colorInterface = indicatorColorInterface;
    }

    @Override
    public Layer1ApiUserMessageModifyIndicator getMessage(boolean isAdd) {


        IndicatorColorScheme colorScheme = new IndicatorColorScheme() {
            @Override
            public ColorDescription[] getColors() {
                return new ColorDescription[]{
                        new ColorDescription(Layer1ApiBarsDemo.class, INDICATOR_LINE_COLOR_NAME, INDICATOR_LINE_DEFAULT_COLOR, false)

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
        };


        return Layer1ApiUserMessageModifyIndicator.builder(Layer1ApiBarsDemo.class, INDICATOR_NAME)
                .setIsAdd(isAdd)
                .setGraphType(Layer1ApiUserMessageModifyIndicator.GraphType.BOTTOM)
                .setOnlineCalculatable(onlineCalculatable)
                .setIndicatorColorScheme(colorScheme)
                .setIndicatorLineStyle(IndicatorLineStyle.DEFAULT)
                .setIsShowColorSettings(false)
                .setDefaultTooltipBackgrondColor(Color.cyan)
                .setDefaultTooltipTextColor(Color.black)
                .setColorInterface(colorInterface)
                .build();
    }


}
