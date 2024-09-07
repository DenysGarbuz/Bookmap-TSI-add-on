package org.denysgarbuz.indicators;

import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator;

public interface CustomIndicator {
    Layer1ApiUserMessageModifyIndicator getMessage(boolean isAdd);


}
