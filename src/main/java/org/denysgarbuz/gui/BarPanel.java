package org.denysgarbuz.gui;

import org.denysgarbuz.events.BarGenerator;
import org.denysgarbuz.indicators.BarCalculatable;
import org.denysgarbuz.indicators.TsiCalculatable;
import velox.api.layer1.Layer1ApiProvider;
import velox.gui.StrategyPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

public class BarPanel extends StrategyPanel {

    private final BarGenerator barGenerator;
    private final Layer1ApiProvider provider;
    private final TsiCalculatable tsiCalculatable;
    private final BarCalculatable barCalculatable;

    public BarPanel(BarGenerator barGenerator, Layer1ApiProvider provider, TsiCalculatable tsiCalculatable, BarCalculatable barCalculatable) {
        super("Bar", new GridBagLayout());
        this.barGenerator = barGenerator;
        this.provider = provider;
        this.tsiCalculatable = tsiCalculatable;
        this.barCalculatable = barCalculatable;

        setLayout(new GridBagLayout());
        GridBagConstraints gbConst = createGridBagConstraints(0, 1);

        // Spinner setup
        long candleIntervalSeconds = TimeUnit.NANOSECONDS.toSeconds(barGenerator.getCandleIntervalNano());
        SpinnerNumberModel spinnerModel = new SpinnerNumberModel((double) candleIntervalSeconds, 5, Integer.MAX_VALUE, 5);
        JSpinner timeSpinner = new JSpinner(spinnerModel);

        // Reload button setup
        JButton reloadButton = new JButton("Apply and reload");
        reloadButton.addActionListener(createReloadButtonListener(timeSpinner));

        // Label setup
        add(new JLabel("Set Period (seconds):"), gbConst);

        // Spinner setup
        gbConst.gridx = 1;
        add(timeSpinner, gbConst);

        // Button setup
        gbConst.gridy = 2;
        add(reloadButton, gbConst);
    }

    private ActionListener createReloadButtonListener(JSpinner timeSpinner) {
        return e -> {
            Double value = (Double) timeSpinner.getValue();
            long candleIntervalNano = TimeUnit.SECONDS.toNanos(value.longValue());

            if (candleIntervalNano == barGenerator.getCandleIntervalNano()) return;

            provider.sendUserMessage(barGenerator.getMessage(false));
            barGenerator.setCandleIntervalNano(candleIntervalNano);
            provider.sendUserMessage(barGenerator.getMessage(true));

            if (tsiCalculatable.getInvalidateInterface() != null) {
                tsiCalculatable.getInvalidateInterface().invalidate();
            }
            if (barCalculatable.getInvalidateInterface() != null) {
                barCalculatable.getInvalidateInterface().invalidate();
            }
        };
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
