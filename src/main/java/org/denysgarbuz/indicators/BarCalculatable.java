package org.denysgarbuz.indicators;

import org.denysgarbuz.Layer1ApiBarsDemo;
import org.denysgarbuz.events.BarEvent;
import org.denysgarbuz.events.BarGenerator;
import org.denysgarbuz.utils.BarUtils;
import velox.api.layer1.layers.strategies.interfaces.*;
import velox.api.layer1.messages.indicators.DataStructureInterface;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BarCalculatable implements OnlineCalculatable {


    private DataStructureInterface dataStructureInterface;
    private InvalidateInterface invalidateInterface;

    private final String treeName;
    private final Class<?>[] events;
    private final BarGenerator barGenerator;

    private static final int MAX_BODY_WIDTH = 30;
    private static final int MIN_BODY_WIDTH = 1;
    private static final long CANDLE_INTERVAL_NS = TimeUnit.SECONDS.toNanos(30);


    public InvalidateInterface getInvalidateInterface() {
        return invalidateInterface;

    }



    public void setDataStructureInterface(DataStructureInterface dataStructureInterface) {
        this.dataStructureInterface = dataStructureInterface;
    }

    public BarCalculatable( String treeName, Class<?>[] events, BarGenerator barGenerator ) {
        this.treeName = treeName;
        this.events = events;
        this.barGenerator = barGenerator;

    }

    @Override
    public void calculateValuesInRange(String indicatorName, String indicatorAlias, long t0, long intervalWidth, int intervalsNumber,
                                       CalculatedResultListener listener) {


        List<DataStructureInterface.TreeResponseInterval> result = dataStructureInterface.get(Layer1ApiBarsDemo.class, treeName, t0,
                intervalWidth, intervalsNumber, indicatorAlias, events);

        int bodyWidth = getBodyWidth(intervalWidth);

        for (int i = 1; i <= intervalsNumber; i++) {

            BarEvent value = BarUtils.getBarEvent(result.get(i));
            if (value != null) {
                value = new BarEvent(value);

                value.setBodyWidthPx(bodyWidth);

                listener.provideResponse(value);
            } else {
                listener.provideResponse(Double.NaN);
            }
        }

        listener.setCompleted();
    }

    @Override
    public OnlineValueCalculatorAdapter createOnlineValueCalculator(String indicatorName, String indicatorAlias, long time,
                                                                    Consumer<Object> listener, InvalidateInterface invalidateInterface) {

        this.invalidateInterface = invalidateInterface;
        return new OnlineValueCalculatorAdapter() {

            int bodyWidth = MAX_BODY_WIDTH;

            @Override
            public void onIntervalWidth(long intervalWidth) {
                this.bodyWidth = getBodyWidth(intervalWidth);
            }

            @Override
            public void onUserMessage(Object data) {
                if (data instanceof CustomGeneratedEventAliased) {
                    CustomGeneratedEventAliased aliasedEvent = (CustomGeneratedEventAliased) data;
                    if (indicatorAlias.equals(aliasedEvent.alias) && aliasedEvent.event instanceof BarEvent) {
                        BarEvent event = (BarEvent)aliasedEvent.event;

                        event = new BarEvent(event);
                        event.setBodyWidthPx(bodyWidth);

                        listener.accept(event);
                    }
                }
            }
        };
    }

    private int getBodyWidth(long intervalWidth) {
        long bodyWidth = barGenerator.getCandleIntervalNano() / intervalWidth;
        bodyWidth = Math.max(bodyWidth, MIN_BODY_WIDTH);
        bodyWidth = Math.min(bodyWidth, MAX_BODY_WIDTH);
        return (int) bodyWidth;

    }



}
