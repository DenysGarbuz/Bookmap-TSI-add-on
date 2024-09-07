package org.denysgarbuz.events;

import velox.api.layer1.layers.strategies.interfaces.CustomGeneratedEvent;
import velox.api.layer1.layers.strategies.interfaces.OnlineCalculatable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Function;

public class BarEvent implements CustomGeneratedEvent, OnlineCalculatable.DataCoordinateMarker {
    private static final long serialVersionUID = 1L;
    public long time;

    // each event will cache its ema values, this way each new event can calculate new ema values based on previous ones;
    public Double ema25 ;
    public Double ema13  ;
    public Double ema25Abs ;
    public Double ema13Abs ;


    public double open;
    public double low;
    public double high;
    public double close;

    transient int bodyWidthPx;


    public BarEvent(long time) {
        this(time, Double.NaN);
    }

    public BarEvent(long time, double open) {
        this(time, open, -1);
    }

    public BarEvent(long time, double open, int bodyWidthPx) {
        this(time, open, open, open, open, bodyWidthPx);
    }

    public BarEvent(long time, double open, double low, double high, double close, int bodyWidthPx) {
        super();
        this.time = time;
        this.open = open;
        this.low = low;
        this.high = high;
        this.close = close;
        this.bodyWidthPx = bodyWidthPx;
    }

    public BarEvent(BarEvent other) {
        this(other.time, other.open, other.low, other.high, other.close, other.bodyWidthPx);
        this.ema25 = other.ema25;
        this.ema13 = other.ema13;
        this.ema25Abs = other.ema25Abs;
        this.ema13Abs = other.ema13Abs;

    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public Object clone() {
        BarEvent ev = new BarEvent(time, open, low, high, close, bodyWidthPx);
        ev.ema25 = this.ema25;
        ev.ema13 = this.ema13;
        ev.ema25Abs = this.ema25Abs;
        ev.ema13Abs = this.ema13Abs;
        return ev;
    }

    @Override
    public String toString() {
        return "[" + time + ": " + open + "/" + low + "/" + high + "/" + close + "]";
    }

    @Override
    public double getMinY() {
        return open;
    }

    @Override
    public double getMaxY() {
        return high;
    }

    @Override
    public double getValueY() {
        return low;
    }

    public void update(double price) {
        if (Double.isNaN(price)) {
            return;
        }

        // If bar was not initialized yet
        if (Double.isNaN(open)) {
            open = price;
            low = price;
            high = price;
        } else {
            low = Math.min(low, price);
            high = Math.max(high, price);
        }
        close = price;
    }

    public void update(BarEvent nextBar) {
        // Inefficient, but simple
        update(nextBar.open);
        update(nextBar.low);
        update(nextBar.high);
        update(nextBar.close);
    }

    public void setBodyWidthPx(int bodyWidthPx) {
        this.bodyWidthPx = bodyWidthPx;
    }

    @Override
    public OnlineCalculatable.Marker makeMarker(Function<Double, Integer> yDataCoordinateToPixelFunction) {


        int top = yDataCoordinateToPixelFunction.apply(high);
        int bottom = yDataCoordinateToPixelFunction.apply(low);
        int openPx = yDataCoordinateToPixelFunction.apply(open);
        int closePx = yDataCoordinateToPixelFunction.apply(close);

        int bodyLow = Math.min(openPx, closePx);
        int bodyHigh = Math.max(openPx, closePx);

        int imageHeight = top - bottom + 1;
        BufferedImage bufferedImage = new BufferedImage(bodyWidthPx, imageHeight, BufferedImage.TYPE_INT_ARGB);
        int imageCenterX = bufferedImage.getWidth() / 2;

        Graphics2D graphics = bufferedImage.createGraphics();
        // Clear background
        graphics.setBackground(new Color(0, 0, 0, 0));
        graphics.clearRect(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());

        /*
         * Draw "shadow", also known as "wick". Here we'll take advantage of the fact
         * we'll later draw a non-transparent body over it. If body would be
         * semi-transparent you'd have to take that into account and leave (or make) a
         * gap in the shadow.
         */
        graphics.setColor(Color.WHITE);
        graphics.drawLine(imageCenterX, 0, imageCenterX, imageHeight);


        /*
         * Draw body. Keep in mind that BufferedImage coordinate system starts from the
         * left top corner and Y axis points downwards
         */
        graphics.setColor(open < close ? Color.GREEN : Color.RED);
        graphics.fillRect(0, top - bodyHigh, bodyWidthPx, bodyHigh - bodyLow + 1);

        graphics.dispose();

        /*
         * This one is a little tricky. We have a reference point which we'll pass as
         * markerY. Now we need to compute offsets so that icon is where we want it to
         * be. Since we took close as a reference point, we want to offset the icon so
         * that close is at the markerY. Zero offset would align bottom of the icon with
         * a value, so we do this:
         */
        int iconOffsetY = bottom - closePx;
        /*
         * This one is simple, we just want to center the bar vertically over where it
         * should be.
         */
        int iconOffsetX = -imageCenterX;
        return new OnlineCalculatable.Marker(close, iconOffsetX, iconOffsetY, bufferedImage);
    }

}