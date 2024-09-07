package org.denysgarbuz;

import org.denysgarbuz.events.BarEvent;
import org.denysgarbuz.events.BarGenerator;
import org.denysgarbuz.gui.BarPanel;
import org.denysgarbuz.gui.ColorPanel;
import org.denysgarbuz.indicators.*;
import org.denysgarbuz.utils.SettingsHandler;
import velox.api.layer1.*;
import velox.api.layer1.annotations.Layer1ApiVersion;
import velox.api.layer1.annotations.Layer1ApiVersionValue;
import velox.api.layer1.annotations.Layer1Attachable;
import velox.api.layer1.annotations.Layer1StrategyName;
import velox.api.layer1.common.ListenableHelper;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.messages.UserMessageLayersChainCreatedTargeted;
import velox.api.layer1.messages.indicators.Layer1ApiDataInterfaceRequestMessage;
import velox.api.layer1.messages.indicators.SettingsAccess;
import velox.api.layer1.settings.Layer1ConfigSettingsInterface;
import velox.gui.StrategyPanel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Layer1Attachable
@Layer1StrategyName("TSI indicator by Denys Garbuz")
@Layer1ApiVersion(Layer1ApiVersionValue.VERSION2)
public class Layer1ApiBarsDemo implements Layer1ApiFinishable, Layer1ApiAdminAdapter, Layer1ApiInstrumentListener, Layer1ConfigSettingsInterface, Layer1CustomPanelsGetter {

    private static final String TREE_NAME = "Bars";
    private static final Class<?>[] INTERESTING_CUSTOM_EVENTS = new Class<?>[]{BarEvent.class};
    private static final long CANDLE_INTERVAL_DEFAULT = TimeUnit.SECONDS.toNanos(30);


    private final Layer1ApiProvider provider;
    private final Map<String, Double> pipsMap = new ConcurrentHashMap<>();

    private final List<CustomIndicator> indicators = new ArrayList<>();

    private final SettingsHandler settingsHandler;

    private final BarCalculatable barCalculatable;
    private final BarIndicator barIndicator;

    private final TsiCalculatable tsiCalculatable;
    private final TsiColorInterface tsiColorInterface;
    private final TsiIndicator tsiIndicator;

    private final BarGenerator barGenerator;


    public Layer1ApiBarsDemo(Layer1ApiProvider provider) {
        this.provider = provider;
        ListenableHelper.addListeners(provider, this);

        settingsHandler = new SettingsHandler();

        barGenerator = new BarGenerator(TREE_NAME, CANDLE_INTERVAL_DEFAULT);

        tsiColorInterface = new TsiColorInterface(settingsHandler);
        tsiCalculatable = new TsiCalculatable(pipsMap, TREE_NAME, INTERESTING_CUSTOM_EVENTS);
        tsiIndicator = new TsiIndicator(tsiCalculatable, tsiColorInterface);


        barCalculatable = new BarCalculatable(TREE_NAME, INTERESTING_CUSTOM_EVENTS, barGenerator);
        barIndicator = new BarIndicator(barCalculatable);


    }

    @Override
    public void finish() {
        indicators.forEach(i -> provider.sendUserMessage(i.getMessage(false)));
        provider.sendUserMessage(barGenerator.getMessage(false));
    }


    @Override
    public void onUserMessage(Object data) {
        if (data.getClass() == UserMessageLayersChainCreatedTargeted.class) {
            UserMessageLayersChainCreatedTargeted message = (UserMessageLayersChainCreatedTargeted) data;
            if (message.targetClass == getClass()) {

                provider.sendUserMessage(new Layer1ApiDataInterfaceRequestMessage(dataStructureInterface -> {
                    tsiCalculatable.setDataStructureInterface(dataStructureInterface);
                    barCalculatable.setDataStructureInterface(dataStructureInterface);
                }));


                addIndicator(barIndicator);
                addIndicator(tsiIndicator);
                provider.sendUserMessage(barGenerator.getMessage(true));
            }
        }
    }

    public void addIndicator(CustomIndicator indicator) {
        indicators.add(indicator);
        provider.sendUserMessage(indicator.getMessage(true));
    }

    @Override
    public void onInstrumentAdded(String alias, InstrumentInfo instrumentInfo) {
        pipsMap.put(alias, instrumentInfo.pips);
    }

    @Override
    public void onInstrumentRemoved(String alias) {
    }

    @Override
    public void onInstrumentNotFound(String symbol, String exchange, String type) {
    }

    @Override
    public void onInstrumentAlreadySubscribed(String symbol, String exchange, String type) {
    }

    @Override
    public void acceptSettingsInterface(SettingsAccess settingsAccess) {
        settingsHandler.setSettingsAccess(settingsAccess);
    }

    // GUI interface
    @Override
    public StrategyPanel[] getCustomGuiFor(String alias, String indicatorName) {
        StrategyPanel colorPanel = new ColorPanel(alias, tsiColorInterface, settingsHandler, tsiCalculatable);
        StrategyPanel barPanel = new BarPanel(barGenerator, provider, tsiCalculatable, barCalculatable);

        return new StrategyPanel[]{colorPanel, barPanel};
    }
}

