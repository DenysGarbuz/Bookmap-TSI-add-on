package org.denysgarbuz.indicators;

import org.denysgarbuz.Layer1ApiBarsDemo;
import org.denysgarbuz.events.BarEvent;
import org.denysgarbuz.utils.BarUtils;
import velox.api.layer1.layers.strategies.interfaces.*;
import velox.api.layer1.messages.indicators.DataStructureInterface;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TsiCalculatable implements OnlineCalculatable {


    private DataStructureInterface dataStructureInterface;
    private InvalidateInterface invalidateInterface;

    private final Map<String, Double> pipsMap;
    private final String treeName;
    private final Class<?>[] events;


    public void setDataStructureInterface(DataStructureInterface dataStructureInterface) {
        this.dataStructureInterface = dataStructureInterface;
    }

    public InvalidateInterface getInvalidateInterface() {
        return invalidateInterface;
    }

    public TsiCalculatable(Map<String, Double> pipsMap, String treeName, Class<?>[] events) {
        this.pipsMap = pipsMap;
        this.treeName = treeName;
        this.events = events;
    }

    @Override
    public void calculateValuesInRange(String indicatorName, String indicatorAlias, long t0, long intervalWidth, int intervalsNumber, CalculatedResultListener listener) {

        List<DataStructureInterface.TreeResponseInterval> result =
                dataStructureInterface.get(Layer1ApiBarsDemo.class, treeName, t0, intervalWidth, intervalsNumber, indicatorAlias, events);

        double prevTsi = Double.NaN;
        for (int i = 1; i < intervalsNumber; i++) {
            BarEvent currBar = BarUtils.getBarEvent(result.get(i));

            boolean isBarAbleToCalculateTsi = currBar != null && currBar.ema13Abs != null && currBar.ema13 != null;

            if (isBarAbleToCalculateTsi) {
                prevTsi = 100 * (currBar.ema13 / currBar.ema13Abs) * pipsMap.get(indicatorAlias);
            }
            listener.provideResponse(prevTsi);

        }
        listener.setCompleted();
    }


    @Override
    public OnlineValueCalculatorAdapter createOnlineValueCalculator(String indicatorName, String indicatorAlias, long time, Consumer<Object> listener, InvalidateInterface invalidateInterface) {
        this.invalidateInterface = invalidateInterface;

        if (dataStructureInterface == null) {
            return new OnlineValueCalculatorAdapter() {
            };
        }
        return new OnlineValueCalculatorAdapter() {

            @Override
            public void onUserMessage(Object data) {
                if (data instanceof CustomGeneratedEventAliased) {
                    CustomGeneratedEventAliased aliasedEvent = (CustomGeneratedEventAliased) data;
                    if (indicatorAlias.equals(aliasedEvent.alias) && aliasedEvent.event instanceof BarEvent) {
                        BarEvent currBar = (BarEvent) aliasedEvent.event;

                        boolean isBarAbleToCalculateTsi = currBar.ema13Abs != null && currBar.ema13 != null;

                        if (isBarAbleToCalculateTsi) {
                            listener.accept((100 * (currBar.ema13 / currBar.ema13Abs)) * pipsMap.get(indicatorAlias));
                        }


                    }
                }
            }
        };
    }


}
