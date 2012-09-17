package org.webharvest.definition;

import org.webharvest.runtime.processors.ConstantProcessor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class AbstractElementDef implements IElementDef {

    private DefinitionResolver definitionResolver = DefinitionResolver.INSTANCE;

    protected XmlNode xmlNode;
    // sequence of operation definitions
    protected  List<IElementDef> operationDefs = new ArrayList<IElementDef>();
    // text content if no nested operation definitions
    protected String body;


    protected AbstractElementDef(XmlNode node, boolean createBodyDefs) {
        if (node == null) {
            throw new IllegalArgumentException("XmlNode must not be null.");
        }
        this.xmlNode = node;

        List<Serializable> elementList = node.getElementList();

        if (createBodyDefs) {
            if (elementList != null && elementList.size() > 0) {
                for (Object element : elementList) {
                    if (element instanceof XmlNode) {
                        XmlNode currElementNode = (XmlNode) element;
                        IElementDef def = definitionResolver
                                .createElementDefinition(currElementNode);
                        if (def != null) {
                            operationDefs.add(def);
                        }
                    } else {
                        operationDefs.add(new ConstantDef(element.toString(),
                                ConstantProcessor.class));
                    }
                }
            } else {
                body = node.getText();
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
        return xmlNode.getAttribute("id");
    }

    public String getShortElementName() {
        return xmlNode.getQName();
    }

    public int getLineNumber() {
        return xmlNode.getLineNumber();
    }

    public int getColumnNumber() {
        return xmlNode.getColumnNumber();
    }
}
