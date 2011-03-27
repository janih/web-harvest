package org.webharvest.definition;

import org.webharvest.runtime.processors.AbstractProcessor;

/**
 * Definition of function call parameter.
 */
public class CallParamDef extends ProcessorElementDef {

    private String name;

    public CallParamDef(XmlNode xmlNode, Class<? extends AbstractProcessor> processorClass) {
    	super(xmlNode, processorClass);
        this.name = xmlNode.getAttribute("name");
    }

    public String getName() {
    	return name;
    }

    public String getShortElementName() {
        return "call-param";
    }

}