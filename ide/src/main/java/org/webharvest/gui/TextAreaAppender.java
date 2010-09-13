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
package org.webharvest.gui;

import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.SimpleLayout;

import javax.swing.*;

/**
 * Simple example of creating a Log4j appender that will
 * write to a JTextArea.
 *
 * @author: Vladimir Nikic
 * Date: Apr 20, 2007
 */
public class TextAreaAppender extends WriterAppender {

	private JTextArea textArea = null;
    private int textSize = 0;

    public TextAreaAppender(JTextArea textArea) {
        this.textArea = textArea;
        this.layout = new SimpleLayout();
        setImmediateFlush(true);
    }

    /** Set the target JTextArea for the logging information to appear. */
	public void setTextArea(JTextArea jTextArea) {
		this.textArea = jTextArea;
    }

    /**
	 * Format and then append the loggingEvent to the stored
	 * JTextArea.
	 */
	public void append(LoggingEvent loggingEvent) {
		final String message = this.layout.format(loggingEvent);
        final boolean atTheEnd = textArea.getCaretPosition() == this.textSize;

        this.textSize += message.length();
        this.textArea.append(message);

        if (atTheEnd) {
            textArea.setCaretPosition(this.textSize);
        }
	}
    
}