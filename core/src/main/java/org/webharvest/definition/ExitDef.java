package org.webharvest.definition;

import org.webharvest.runtime.processors.AbstractProcessor;

/**
 * Definition of exit processor.
 */
public class ExitDef extends ProcessorElementDef {

	private String condition;
	private String message;

    public ExitDef(XmlNode xmlNode, Class<? extends AbstractProcessor> processorClass) {
        super(xmlNode, false, processorClass);

        this.condition = xmlNode.getAttribute("condition");
        this.message = xmlNode.getAttribute("message");
    }

    public String getCondition() {
		return condition;
	}

    public String getMessage() {
        return message;
    }

    public String getShortElementName() {
        return "exit";
    }

}