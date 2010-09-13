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
package org.webharvest;

import org.apache.log4j.PropertyConfigurator;
import org.webharvest.definition.DefinitionResolver;
import org.webharvest.definition.ScraperConfiguration;
import org.webharvest.exception.PluginException;
import org.webharvest.gui.Ide;
import org.webharvest.runtime.Scraper;
import org.webharvest.utils.CommonUtil;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Startup class  for Web-Harvest.
 */
public class CommandLine {

    private static Map<String, String> getArgValue(String[] args, boolean caseSensitive) {
        Map<String, String> params = new HashMap<String, String>();
        for (String curr : args) {
            String argName = caseSensitive ? curr : curr.toLowerCase();
            String argValue = "";

            int eqIndex = curr.indexOf('=');
            if (eqIndex >= 0) {
                argName = curr.substring(0, eqIndex).trim();
                argValue = curr.substring(eqIndex + 1).trim();
            }

            params.put(caseSensitive ? argName : argName.toLowerCase(), argValue);
        }

        return params;
    }

    private static Map<String, String> getArgValue(String[] args) {
        return getArgValue(args, false);
    }

    public static void main(String[] args) throws IOException {
        Map<String, String> params = getArgValue(args);

        if (params.size() == 0) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new Ide().createAndShowGUI();
                }
            });
        } else if (params.containsKey("-h") || params.containsKey("/h")) {
            printHelp();
            System.exit(0);
        } else {
            String configFilePath = params.get("config");
            if (configFilePath == null || "".equals(configFilePath)) {
                System.err.println("You must specify configuration file path using config=<path> argument!");
                printHelp();
                System.exit(1);
            }

            String workingDir = params.get("workdir");
            if (workingDir == null || "".equals(workingDir)) {
                workingDir = ".";
            }

            String logLevel = params.get("loglevel");
            if (logLevel == null || "".equals(logLevel)) {
                logLevel = "INFO";
            }

            Properties props = new Properties();

            String logPropsFile = params.get("logpropsfile");
            if (logPropsFile != null && !"".equals(logPropsFile)) {
                FileInputStream fis = new FileInputStream(new File(logPropsFile));
                props.load(fis);
                fis.close();
            } else {
                props.setProperty("log4j.rootLogger", logLevel.toUpperCase() + ", stdout");
                props.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
                props.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
                props.setProperty("log4j.appender.stdout.layout.ConversionPattern", "%-5p (%20F:%-3L) - %m\n");

                props.setProperty("log4j.appender.file", "org.apache.log4j.DailyRollingFileAppender");
                props.setProperty("log4j.appender.file.File", workingDir + "/out.log");
                props.setProperty("log4j.appender.file.DatePattern", "yyyy-MM-dd");
                props.setProperty("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
                props.setProperty("log4j.appender.file.layout.ConversionPattern", "%-5p (%20F:%-3L) - %m\n");
            }

            PropertyConfigurator.configure(props);

            // register plugins if specified
            String pluginsString = params.get("plugins");
            if (!CommonUtil.isEmpty(pluginsString)) {
                for (String plugin : CommonUtil.tokenize(pluginsString, ",")) {
                    try {
                        DefinitionResolver.registerPlugin(plugin);
                    } catch (PluginException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }

            final String configLowercase = configFilePath.toLowerCase();

            final Scraper scraper = new Scraper(configLowercase.startsWith("http://") || configLowercase.startsWith("https://")
                    ? new ScraperConfiguration(new URL(configFilePath))
                    : new ScraperConfiguration(configFilePath),
                    workingDir);

            String isDebug = params.get("debug");
            if (CommonUtil.isBooleanTrue(isDebug)) {
                scraper.setDebug(true);
            }

            String proxyHost = params.get("proxyhost");
            if (proxyHost != null && !"".equals(proxyHost)) {
                String proxyPort = params.get("proxyport");
                if (proxyPort != null && !"".equals(proxyPort)) {
                    int port = Integer.parseInt(proxyPort);
                    scraper.getHttpClientManager().setHttpProxy(proxyHost, port);
                } else {
                    scraper.getHttpClientManager().setHttpProxy(proxyHost);
                }
            }

            String proxyUser = params.get("proxyuser");
            if (proxyUser != null && !"".equals(proxyUser)) {
                String proxyPassword = params.get("proxypassword");
                String proxyNTHost = params.get("proxynthost");
                String proxyNTDomain = params.get("proxyntdomain");
                scraper.getHttpClientManager().setHttpProxyCredentials(proxyUser, proxyPassword, proxyNTHost, proxyNTDomain);
            }

            // adds initial variables to the scraper's content, if any
            Map<String, String> caseSensitiveParams = getArgValue(args, true);
            for (Map.Entry<String, String> entry : caseSensitiveParams.entrySet()) {
                final String key = entry.getKey();
                if (key.startsWith("#")) {
                    String varName = key.substring(1);
                    if (varName.length() > 0) {
                        scraper.addVariableToContext(varName, entry.getValue());
                    }
                }
            }

            scraper.execute();
        }
    }

    private static void printHelp() {
        System.out.println("");
        System.out.println("To open Web-Harvest GUI:");
        System.out.println("   java -jar webharvestXX.jar");
        System.out.println("or just double-click webharvestXX.jar from the file manager.");
        System.out.println("");
        System.out.println("Command line use:");
        System.out.println("   java -jar webharvestXX.jar [-h] config=<path> [workdir=<path>] [debug=yes|no]");
        System.out.println("             [proxyhost=<proxy server> [proxyport=<proxy server port>]]");
        System.out.println("             [proxyuser=<proxy username> [proxypassword=<proxy password>]]");
        System.out.println("             [proxynthost=<NT host name>]");
        System.out.println("             [proxyntdomain=<NT domain name>]");
        System.out.println("             [loglevel=<level>]");
        System.out.println("             [logpropsfile=<path>]");
        System.out.println("             [plugins=<list of plugin classes>]");
        System.out.println("             [#var1=<value1> [#var2=<value2>...]]");
        System.out.println("");
        System.out.println("   -h            - shows this help.");
        System.out.println("   config        - path or URL of configuration (URL must begin with \"http://\" or \"https://\").");
        System.out.println("   workdir       - path of the working directory (default is current directory).");
        System.out.println("   debug         - specify if Web-Harvest generates debugging output (default is no).");
        System.out.println("   proxyhost     - specify proxy server.");
        System.out.println("   proxyport     - specify port for proxy server.");
        System.out.println("   proxyuser     - specify proxy server username.");
        System.out.println("   proxypassword - specify proxy server password.");
        System.out.println("   proxynthost   - NTLM authentication scheme - the host the request is originating from.");
        System.out.println("   proxyntdomain - NTLM authentication scheme - the domain to authenticate within.");
        System.out.println("   loglevel      - specify level of logging for Log4J (trace,info,debug,warn,error,fatal).");
        System.out.println("   logpropsfile  - file path to custom Log4J properties. If specified, loglevel is ignored.");
        System.out.println("   plugins       - comma-separated list of full plugins' class names.");
        System.out.println("   #varN, valueN - specify initial variables of the Web-Harvest context. To be recognized, ");
        System.out.println("                   each variable name must have prefix #. ");
    }

}