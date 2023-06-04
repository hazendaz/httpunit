/*
 * MIT License
 *
 * Copyright 2011-2025 Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */
package com.meterware.httpunit.parsing;

import com.meterware.httpunit.HttpUnitOptions;

import java.io.IOException;
import java.io.StringReader;

import org.htmlunit.cyberneko.HTMLConfiguration;
import org.htmlunit.cyberneko.filters.DefaultFilter;
import org.htmlunit.cyberneko.xerces.xni.Augmentations;
import org.htmlunit.cyberneko.xerces.xni.QName;
import org.htmlunit.cyberneko.xerces.xni.XMLAttributes;
import org.htmlunit.cyberneko.xerces.xni.XMLLocator;
import org.htmlunit.cyberneko.xerces.xni.XMLString;
import org.htmlunit.cyberneko.xerces.xni.XNIException;
import org.htmlunit.cyberneko.xerces.xni.parser.XMLInputSource;


/**
 * A filter to interpret JavaScript script blocks, based on the sample Scripts program provided by NekoHTML.
 **/
class ScriptFilter extends DefaultFilter {

    /** The NekoHTML configuration. */
    private HTMLConfiguration _configuration;

    /** A string buffer to collect the script. */
    private StringBuilder _activeScriptBlock;

    /** The name of the current script language. **/
    private String _scriptLanguage;

    /** The system identifier of the source document. */
    private String _systemID = "";

    /** The number of the current script. */
    private int _scriptIndex;

    /** The parser in which this filter is running. **/
    private ScriptHandler _scriptHandler;

    /**
     * Constructs a script object with the specified configuration.
     *
     * @param config
     *            the config
     */
    ScriptFilter(HTMLConfiguration config) {
        _configuration = config;
    }

    /**
     * Sets the script handler.
     *
     * @param scriptHandler
     *            the new script handler
     */
    public void setScriptHandler(ScriptHandler scriptHandler) {
        _scriptHandler = scriptHandler;
    }

    /**
     * Start document.
     *
     * @param locator
     *            the locator
     * @param encoding
     *            the encoding
     * @param augs
     *            the augs
     *
     * @throws XNIException
     *             the XNI exception
     */
    public void startDocument(XMLLocator locator, String encoding, Augmentations augs) throws XNIException {
        _activeScriptBlock = null;
        _systemID = locator == null ? "" : locator.getLiteralSystemId() + "_";
        _scriptIndex = 0;
        super.startDocument(locator, encoding, null, augs);
    }

    /**
     * Invoked for a start element. If the element is a <script>, overrides the normal behavior to begin collecting the
     * script text.
     */
    @Override
    public void startElement(QName element, XMLAttributes attrs, Augmentations augs) throws XNIException {
        if (!isSupportedScript(element, attrs)) {
            super.startElement(element, attrs, augs);
        } else {
            _activeScriptBlock = new StringBuilder();
            _scriptLanguage = getScriptLanguage(attrs);
            String srcAttribute = attrs.getValue("src");
            if (srcAttribute != null) {
                _activeScriptBlock.append(_scriptHandler.getIncludedScript(srcAttribute));
            }
        }
    }

    /**
     * Checks if is supported script.
     *
     * @param element
     *            the element
     * @param attrs
     *            the attrs
     *
     * @return true, if is supported script
     */
    private boolean isSupportedScript(QName element, XMLAttributes attrs) {
        if (!element.rawname.equalsIgnoreCase("script") || attrs == null) {
            return false;
        }
        String value = getScriptLanguage(attrs);
        return HttpUnitOptions.isScriptingEnabled() && _scriptHandler.supportsScriptLanguage(value);
    }

    /**
     * Gets the script language.
     *
     * @param attrs
     *            the attrs
     *
     * @return the script language
     */
    private String getScriptLanguage(XMLAttributes attrs) {
        return attrs == null ? null : attrs.getValue("language");
    }

    @Override
    public void emptyElement(QName element, XMLAttributes attrs, Augmentations augs) throws XNIException {
        if (!isSupportedScript(element, attrs)) {
            super.emptyElement(element, attrs, augs);
        }
    }

    @Override
    public void characters(XMLString text, Augmentations augs) throws XNIException {
        if (_activeScriptBlock != null) {
            _activeScriptBlock.append(text.ch, text.offset, text.length);
        } else {
            super.characters(text, augs);
        }
    }

    @Override
    public void endElement(QName element, Augmentations augs) throws XNIException {
        if (_activeScriptBlock == null) {
            super.endElement(element, augs);
        } else {
            try {
                final String scriptText = _activeScriptBlock.toString();
                String replacementText = getTranslatedScript(_scriptLanguage, scriptText);
                _configuration.pushInputSource(newInputSource(replacementText));
            } catch (IOException e) { // ignore
            } finally {
                _activeScriptBlock = null;
            }
        }
    }

    /**
     * New input source.
     *
     * @param replacementText
     *            the replacement text
     *
     * @return the XML input source
     */
    private XMLInputSource newInputSource(String replacementText) {
        StringBuilder systemID = new StringBuilder(_systemID);
        systemID.append("script").append(++_scriptIndex);

        return new XMLInputSource(null, systemID.toString(), null, new StringReader(replacementText), "UTF-8");
    }

    /**
     * Gets the translated script.
     *
     * @param language
     *            the language
     * @param scriptText
     *            the script text
     *
     * @return the translated script
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected String getTranslatedScript(final String language, final String scriptText) throws IOException {
        return _scriptHandler.runScript(language, scriptText);
    }

}
