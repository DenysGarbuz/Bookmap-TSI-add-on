package org.denysgarbuz.events;

public class EmaCalculation {

    public int periodLength;

    public EmaCalculation(int periodLength) {
        this.periodLength = periodLength;
        this.a = 2. / (periodLength + 1);
    }

    // alpha ( 2 / (numberOfPeriods + 1))
    public double a;

    public double sum = 0;
    public Double sma = null;
    public Double ema = null;
    public int period = 0;

}
