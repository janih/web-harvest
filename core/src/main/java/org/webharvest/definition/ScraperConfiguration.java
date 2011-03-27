/*  Copyright (c) 2006-2007, Vladimir Nikic
    All rights reserved.

    Redistribution and use of this software in source and binary forms,
    with or without modification, are permitted provided that the following
    conditions are met:

    * Redistributions of source code must retain the above
      copyright notice, this list of conditions and the
      following disclaimer.

    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the
      following disclaimer in the documentation and/or other
      materials provided with the distribution.

    * The name of Web-Harvest may not be used to endorse or promote
      products derived from this software without specific prior
      written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.

    You can contact Vladimir Nikic by sending e-mail to
    nikic_vladimir@yahoo.com. Please include the word "Web-Harvest" in the
    subject line.
*/
package org.webharvest.definition;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.webharvest.runtime.processors.ConstantProcessor;
import org.webharvest.runtime.scripting.ScriptingLanguage;
import org.xml.sax.InputSource;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Basic configuration.
 */
public class ScraperConfiguration {

    public static final String DEFAULT_CHARSET = "UTF-8";
    private static final ScriptingLanguage DEFAULT_SCRIPTING_LANGUAGE = ScriptingLanguage.BEANSHELL;

    // map of function definitions
    private Map<String, FunctionDef> functionDefs = new HashMap<String, FunctionDef>();

    // sequence of operationDefs
    private List<IElementDef> operations = new ArrayList<IElementDef>();

    private String charset;
    private ScriptingLanguage scriptingLanguage;
    private File sourceFile;
    private String url;

    /**
     * Creates configuration instance loaded from the specified input stream.
     *
     * @param in
     */
    public ScraperConfiguration(InputSource in) {
        createFromInputStream(in);

    }

    /**
     * Creates configuration instance loaded from the specified File.
     *
     * @param sourceFile
     * @throws FileNotFoundException
     */
    public ScraperConfiguration(File sourceFile) throws FileNotFoundException {
        this.sourceFile = sourceFile;
        createFromInputStream(new InputSource(new FileReader(sourceFile)));
    }

    /**
     * Creates configuration instance loaded from the file specified by filename.
     *
     * @param sourceFilePath
     */
    public ScraperConfiguration(String sourceFilePath) throws FileNotFoundException {
        this(new File(sourceFilePath));
    }

    /**
     * Creates configuration instance loaded from specified URL.
     *
     * @param sourceUrl
     * @throws IOException
     */
    public ScraperConfiguration(URL sourceUrl) throws IOException {
        this.url = sourceUrl.toString();
        createFromInputStream(new InputSource(new InputStreamReader(sourceUrl.openStream())));
    }

    private void createFromInputStream(InputSource in) {
        // loads configuration from input stream to the internal structure
        final XmlNode node = XmlNode.getInstance(in);

        final String xmlns = node.getUri();
        System.out.println("WH config URI: "+xmlns);

        this.charset = StringUtils.defaultIfEmpty(node.getAttribute("charset"), DEFAULT_CHARSET);

        this.scriptingLanguage = (ScriptingLanguage) ObjectUtils.defaultIfNull(
                ScriptingLanguage.recognize(node.getAttribute("scriptlang")),
                DEFAULT_SCRIPTING_LANGUAGE);

        for (Object element : node.getElementList()) {
            operations.add((element instanceof XmlNode)
                    ? DefinitionResolver.createElementDefinition((XmlNode) element)
                    : new ConstantDef(element.toString(), ConstantProcessor.class));
        }
    }

    public List<IElementDef> getOperations() {
        return operations;
    }

    public String getCharset() {
        return charset;
    }

    public ScriptingLanguage getScriptingLanguage() {
        return scriptingLanguage;
    }

    public FunctionDef getFunctionDef(String name) {
        return functionDefs.get(name);
    }

    public void addFunctionDef(FunctionDef funcDef) {
        functionDefs.put(funcDef.getName(), funcDef);
    }

    public File getSourceFile() {
        return this.sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}