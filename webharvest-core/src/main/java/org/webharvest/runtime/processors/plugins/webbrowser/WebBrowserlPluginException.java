package org.webharvest.runtime.processors.plugins.webbrowser;

import org.webharvest.exception.BaseException;

/**
 * Runtime exception for MailPlugin
 */
public class WebBrowserlPluginException extends BaseException {

    public WebBrowserlPluginException(String message) {
        super(message);
    }

    public WebBrowserlPluginException(Throwable cause) {
        super(cause);
    }

    public WebBrowserlPluginException(String message, Throwable cause) {
        super(message, cause);
    }

}