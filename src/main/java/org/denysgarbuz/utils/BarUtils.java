package org.denysgarbuz.utils;

import org.denysgarbuz.events.BarEvent;
import velox.api.layer1.messages.indicators.DataStructureInterface;

public class BarUtils {


    public static BarEvent getBarEvent(DataStructureInterface.TreeResponseInterval treeResponseInterval) {
        Object result = treeResponseInterval.events.get(BarEvent.class.toString());
        if (result != null) {
            return (BarEvent) result;
        } else {
            return null;
        }
    }


}
