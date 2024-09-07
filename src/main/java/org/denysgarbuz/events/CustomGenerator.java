package org.denysgarbuz.events;

import velox.api.layer1.messages.Layer1ApiUserMessageAddStrategyUpdateGenerator;

public interface CustomGenerator {

    Layer1ApiUserMessageAddStrategyUpdateGenerator getMessage(boolean isAdd);

}
