package org.denysgarbuz.events;

import org.denysgarbuz.Layer1ApiBarsDemo;
import velox.api.layer1.data.*;
import velox.api.layer1.layers.strategies.interfaces.CustomEventAggregatble;
import velox.api.layer1.layers.strategies.interfaces.CustomGeneratedEvent;
import velox.api.layer1.layers.strategies.interfaces.CustomGeneratedEventAliased;
import velox.api.layer1.messages.GeneratedEventInfo;
import velox.api.layer1.messages.Layer1ApiUserMessageAddStrategyUpdateGenerator;
import velox.api.layer1.messages.indicators.StrategyUpdateGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BarGenerator implements CustomGenerator {
    // interval in which new bar events will be created (nanoseconds)
    private long candleIntervalNano;
    private final String treeName;

    public BarGenerator(String treeName, long candleIntervalNano) {
        this.treeName = treeName;
        this.candleIntervalNano = candleIntervalNano;
    }

    public long getCandleIntervalNano() {
        return candleIntervalNano;
    }

    public void setCandleIntervalNano(long candleIntervalNano) {
        this.candleIntervalNano = candleIntervalNano;
    }

    public static final CustomEventAggregatble BAR_EVENTS_AGGREGATOR = new CustomEventAggregatble() {


        @Override
        public CustomGeneratedEvent getInitialValue(long t) {
            return new BarEvent(t);
        }


        @Override
        public void aggregateAggregationWithValue(CustomGeneratedEvent aggregation, CustomGeneratedEvent value) {
            BarEvent aggregationEvent = (BarEvent) aggregation;
            BarEvent valueEvent = (BarEvent) value;
            aggregationEvent.update(valueEvent);

            //simply pass last values as aggregation
            if (aggregationEvent.ema13 == null || valueEvent.ema13 != null) {
                aggregationEvent.ema25 = valueEvent.ema25;
                aggregationEvent.ema25Abs = valueEvent.ema25Abs;
                aggregationEvent.ema13 = valueEvent.ema13;
                aggregationEvent.ema13Abs = valueEvent.ema13Abs;
            }


        }

        @Override
        public void aggregateAggregationWithAggregation(CustomGeneratedEvent aggregation1,
                                                        CustomGeneratedEvent aggregation2) {
            BarEvent aggregationEvent1 = (BarEvent) aggregation1;
            BarEvent aggregationEvent2 = (BarEvent) aggregation2;
            aggregationEvent1.update(aggregationEvent2);

            if (aggregationEvent1.ema13 == null || aggregationEvent2.ema13 != null) {
                aggregationEvent1.ema25 = aggregationEvent2.ema25;
                aggregationEvent1.ema25Abs = aggregationEvent2.ema25Abs;
                aggregationEvent1.ema13 = aggregationEvent2.ema13;
                aggregationEvent1.ema13Abs = aggregationEvent2.ema13Abs;
            }


        }
    };


    @Override
    public Layer1ApiUserMessageAddStrategyUpdateGenerator getMessage(boolean isAdd) {
        return new Layer1ApiUserMessageAddStrategyUpdateGenerator(Layer1ApiBarsDemo.class, treeName, isAdd, true, true, new StrategyUpdateGenerator() {
            private Consumer<CustomGeneratedEventAliased> consumer;

            private long time = 0;


            private final Map<String, BarEvent> aliasToLastBar = new HashMap<>();
            // preserving close price of previous bar to calculate price change
            private final Map<String, Double> aliasToPrevClose = new HashMap<>();

            // for each instrument save current state of different ema calculations.
            // This way we can calculate EMAs on the go recovering previous state of calculation for certain instrument;
            private final Map<String, EmaCalculation> aliasToEmaCalculation25 = new HashMap<>();
            private final Map<String, EmaCalculation> aliasToEmaCalculation13 = new HashMap<>();
            private final Map<String, EmaCalculation> aliasToEmaCalculation25Abs = new HashMap<>();
            private final Map<String, EmaCalculation> aliasToEmaCalculation13Abs = new HashMap<>();



            //Using preserved state of calculation calculate EMA for new value;
            private Double getEma(EmaCalculation calc, Double value){
                if (value == null) return null;


                if (calc.period < calc.periodLength) {
                    calc.sum += value;
                    calc.period  += 1;
                    if (calc.period  == calc.periodLength) {
                        calc.sma = calc.sum / calc.periodLength;
                    }
                } else  {
                    if (calc.ema == null) calc.ema = calc.sma;
                    calc.ema = (calc.a * value) + (1 - calc.a) * calc.ema;
                }


                return calc.ema;
            }

            private void setEmaForBar(BarEvent bar, String alias){
                Double prevClose = aliasToPrevClose.getOrDefault(alias, bar.close);

                EmaCalculation calc25 = aliasToEmaCalculation25.get(alias);
                bar.ema25 = getEma(calc25, bar.close - prevClose);

                EmaCalculation calc25Abs = aliasToEmaCalculation25Abs.get(alias);
                bar.ema25Abs = getEma(calc25Abs, Math.abs(bar.close - prevClose));

                EmaCalculation calc13 = aliasToEmaCalculation13.get(alias);
                bar.ema13 = getEma(calc13, bar.ema25);

                EmaCalculation calc13Abs = aliasToEmaCalculation13Abs.get(alias);
                bar.ema13Abs = getEma(calc13Abs, bar.ema25Abs);

                aliasToPrevClose.put(alias, bar.close);
            }




            @Override
            public void setGeneratedEventsConsumer(Consumer<CustomGeneratedEventAliased> consumer) {
                this.consumer = consumer;
            }

            @Override
            public Consumer<CustomGeneratedEventAliased> getGeneratedEventsConsumer() {
                return consumer;
            }

            @Override
            public void onStatus(StatusInfo statusInfo) {
            }

            @Override
            public void onOrderUpdated(OrderInfoUpdate orderInfoUpdate) {
            }

            @Override
            public void onOrderExecuted(ExecutionInfo executionInfo) {
            }

            @Override
            public void onBalance(BalanceInfo balanceInfo) {
            }


            @Override
            public void onTrade(String alias, double price, int size, TradeInfo tradeInfo) {
                BarEvent bar = aliasToLastBar.get(alias);

                long barStartTime = getBarStartTime(time);

                if (bar == null) {
                    bar = new BarEvent(barStartTime);
                    aliasToLastBar.put(alias, bar);
                }

                if (barStartTime != bar.time) {
                    bar.setTime(time);
                    setEmaForBar(bar, alias);

                    consumer.accept(new CustomGeneratedEventAliased(bar, alias));
                    bar = new BarEvent(barStartTime, bar.close);

                    aliasToLastBar.put(alias, bar);
                }


                if (size != 0) {
                    bar.update(price);
                }
            }

            @Override
            public void onMarketMode(String alias, MarketMode marketMode) {
            }

            @Override
            public void onDepth(String alias, boolean isBid, int price, int size) {
            }

            @Override
            public void onInstrumentAdded(String alias, InstrumentInfo instrumentInfo) {

                aliasToEmaCalculation25.put(alias, new EmaCalculation(25));
                aliasToEmaCalculation13.put(alias, new EmaCalculation(13));
                aliasToEmaCalculation25Abs.put(alias, new EmaCalculation(25));
                aliasToEmaCalculation13Abs.put(alias, new EmaCalculation(13));
            }

            @Override
            public void onInstrumentRemoved(String alias) {
                aliasToLastBar.remove(alias);
            }

            @Override
            public void onInstrumentNotFound(String symbol, String exchange, String type) {
            }

            @Override
            public void onInstrumentAlreadySubscribed(String symbol, String exchange, String type) {
            }

            @Override
            public void onUserMessage(Object data) {
            }

            @Override
            public void setTime(long time) {
                this.time = time;

                /*
                 * Publish finished bars. Bookmap call this method periodically even if nothing
                 * is happening at around 50ms intervals (avoid relying on exact value as it
                 * might be changed in the future).
                 */
                long barStartTime = getBarStartTime(time);
                for (Map.Entry<String, BarEvent> entry : aliasToLastBar.entrySet()) {
                    String alias = entry.getKey();
                    BarEvent bar = entry.getValue();
                    if (barStartTime != bar.time) {

                        setEmaForBar(bar, alias);

                        consumer.accept(new CustomGeneratedEventAliased(bar, alias));
                        bar = new BarEvent(barStartTime, bar.close);
                        entry.setValue(bar);
                    }
                }
            }

            private long getBarStartTime(long time) {
                return time - time % candleIntervalNano;
            }

        }, new GeneratedEventInfo[]{new GeneratedEventInfo(BarEvent.class, BarEvent.class, BAR_EVENTS_AGGREGATOR)});
    }




}
