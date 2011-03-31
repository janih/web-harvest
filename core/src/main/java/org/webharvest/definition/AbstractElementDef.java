package org.webharvest.definition;

import org.webharvest.runtime.processors.ConstantProcessor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractElementDef implements IElementDef {

    // sequence of operation definitions
    List<IElementDef> operationDefs = new ArrayList<IElementDef>();
    // text content if no nested operation definitions
    String body;
    // element ID
    String id;
    // descriptive name
    protected String descName = "*";
    // location of this element in source XML
    protected int lineNumber;
    protected int columnNumber;

    protected AbstractElementDef(XmlNode node, boolean createBodyDefs) {
        if (node != null) {
            this.lineNumber = node.getLineNumber();
            this.columnNumber = node.getColumnNumber();

            this.id = node.getAttribute("id");

            List<Serializable> elementList = node.getElementList();

            if (createBodyDefs) {
                if (elementList != null && elementList.size() > 0) {
                    for (Object element : elementList) {
                        if (element instanceof XmlNode) {
                            XmlNode currElementNode = (XmlNode) element;
                            IElementDef def = DefinitionResolver.createElementDefinition(currElementNode);
                            if (def != null) {
                                operationDefs.add(def);
                            }
                        } else {
                            operationDefs.add(new ConstantDef(element.toString(), ConstantProcessor.class));
                        }
                    }
                } else {
                    body = node.getText();
                }
            }
        }
    }

    public boolean hasOperations() {
        return operationDefs != null && operationDefs.size() > 0;
    }

    public IElementDef[] getOperationDefs() {
        IElementDef[] defs = new IElementDef[operationDefs.size()];
        Iterator<IElementDef> it = operationDefs.iterator();
        int index = 0;
        while (it.hasNext()) {
            defs[index++] = it.next();
        }

        return defs;
    }

    public String getBodyText() {
        return body;
    }

    public String getId() {
        return id;
    }

    public String getShortElementName() {
        return descName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }
}
