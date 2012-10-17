package org.webharvest;

import java.util.List;

import org.webharvest.definition.IElementDef;

//TODO Add documentation
public interface HarvestLoadCallback {

    // TODO Add documentation
    void onSuccess(List<IElementDef> elements);

}
