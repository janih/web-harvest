package org.webharvest.runtime.processors.plugins.zip;

import org.webharvest.runtime.Scraper;
import org.webharvest.runtime.ScraperContext;
import org.webharvest.runtime.processors.WebHarvestPlugin;
import org.webharvest.runtime.variables.NodeVariable;
import org.webharvest.runtime.variables.Variable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

/**
 * ZIP processor
 */
public class ZipPlugin extends WebHarvestPlugin {

    private ZipOutputStream zipOutStream = null;

    public String getName() {
        return "zip";
    }

    public Variable executePlugin(Scraper scraper, ScraperContext context) throws InterruptedException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        zipOutStream = new ZipOutputStream(byteArrayOutputStream);
        executeBody(scraper, context);
        try {
            zipOutStream.close();
        } catch (IOException e) {
            throw new ZipPluginException(e);
        }
        return new NodeVariable(byteArrayOutputStream.toByteArray());
    }

    public String[] getValidAttributes() {
        return new String[] {};
    }

    public String[] getRequiredAttributes() {
        return new String[] {};
    }

    public Class[] getDependantProcessors() {
        return new Class[] {
            ZipEntryPlugin.class
        };
    }

    public ZipOutputStream getZipOutStream() {
        return zipOutStream;
    }
    
}