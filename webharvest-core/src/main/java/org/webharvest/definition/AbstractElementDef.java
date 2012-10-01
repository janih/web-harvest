package org.webharvest.definition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.webharvest.runtime.processors.ConstantProcessor;

public abstract class AbstractElementDef implements IElementDef {

    protected XmlNode xmlNode;
    // sequence of operation definitions
    protected List<IElementDef> operationDefs = new ArrayList<IElementDef>();
    // text content if no nested operation definitions
    protected String body;

    protected AbstractElementDef(XmlNode node, boolean createBodyDefs) {
        if (node == null) {
            throw new IllegalArgumentException("XmlNode must not be null.");
        }
        this.xmlNode = node;

        if (node != XmlNode.NULL) {
            List<Serializable> elementList = node.getElementList();

            if (createBodyDefs) {
                if (elementList != null && elementList.size() > 0) {
                    /*
                    for (final Object element : elementList) {
                        if (!(element instanceof XmlNode)) {
                            operationDefs.add(new ConstantDef(element.toString(),
                                    ConstantProcessor.class));
                        }
                    }
                    */
                } else {
                    body = node.getText();
                }
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBodyText() {
        return body;
    }

    @Override
    public String getId() {
        return xmlNode.getAttribute("id");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getShortElementName() {
        return xmlNode.getQName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getLineNumber() {
        return xmlNode.getLineNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getColumnNumber() {
        return xmlNode.getColumnNumber();
    }

    public void add(IElementDef element) {
        operationDefs.add(element);
    }

}
