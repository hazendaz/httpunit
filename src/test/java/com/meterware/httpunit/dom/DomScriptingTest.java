/*
 * MIT License
 *
 * Copyright 2011-2026 Russell Gold
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
package com.meterware.httpunit.dom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;

import org.junit.jupiter.api.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.html.HTMLBodyElement;

/**
 * Tests basic scripting via the DOM.
 */
class DomScriptingTest extends AbstractHTMLElementTest {

    /**
     * Gets the document.
     *
     * @return the document
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void getDocument() throws Exception {
        Element element = createElement("body");
        assertEquals(_htmlDocument, ((Scriptable) element).get("document", null), "Returned document");
    }

    /**
     * Document get title.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void documentGetTitle() throws Exception {
        _htmlDocument.setTitle("something");
        assertEquals("something", _htmlDocument.get("title", null), "title");

        Node body = createElement("body");
        assertEquals("something", evaluateExpression(body, "document.title"), "title");
    }

    /**
     * Document put title.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void documentPutTitle() throws Exception {
        _htmlDocument.put("title", _htmlDocument, "right here");
        assertEquals("right here", _htmlDocument.getTitle(), "title after put");

        Node body = createElement("body");
        evaluateExpression(body, "document.title='new value'");
        assertEquals("new value", _htmlDocument.getTitle(), "title after script");
    }

    // todo test document.write, document.writeln - window must override getDocumentWriteBuffer,
    // discardDocumentWriteBuffer(?)

    /**
     * Element put title.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void elementPutTitle() throws Exception {
        HTMLBodyElement body = (HTMLBodyElement) createElement("body");
        Scriptable scriptableBody = (Scriptable) body;

        scriptableBody.put("title", scriptableBody, "right here");
        assertEquals("right here", body.getTitle(), "title after put");

        evaluateExpression(body, "title='new value'");
        assertEquals("new value", body.getTitle(), "title after script");
    }

    /**
     * Body attributes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void bodyAttributes() throws Exception {
        HTMLBodyElement body = addBodyElement();
        body.setBgColor("red");

        assertEquals("red", evaluateExpression(_htmlDocument, "body.bgcolor"), "initial background color");

        evaluateExpression(_htmlDocument, "body.id='blue'");
        assertEquals("blue", body.getId(), "revised foreground color");
    }

    /**
     * Numeric attributes.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void numericAttributes() throws Exception {
        HTMLBodyElement body = addBodyElement();
        HTMLAnchorElementImpl anchor = (HTMLAnchorElementImpl) createElement("a");
        body.appendChild(anchor);
        anchor.setTabIndex(4);

        assertEquals(4, evaluateExpression(anchor, "tabindex"), "initial tab index");

        evaluateExpression(anchor, "tabindex=6");
        assertEquals(6, anchor.getTabIndex(), "revised tab index");
    }

    /**
     * Adds the body element.
     *
     * @return the HTML body element
     */
    private HTMLBodyElement addBodyElement() {
        HTMLBodyElement body = (HTMLBodyElement) createElement("body");
        _htmlDocument.setBody(body);
        return body;
    }

    /**
     * Creates the element.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void createElement() throws Exception {
        Object node = evaluateExpression(_htmlDocument, "createElement( 'a' )");
        assertNotNull(node, "No node returned");
        assertTrue(node instanceof HTMLAnchorElement, "Node is not an anchor element");
    }

    /**
     * Document links collection.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void documentLinksCollection() throws Exception {
        TestWindowProxy proxy = new TestWindowProxy(_htmlDocument);
        proxy.setUrl(new URL("http://localhost"));
        _htmlDocument.getWindow().setProxy(proxy);
        HTMLBodyElement body = addBodyElement();
        appendLink(body, "red", "red.html");
        appendLink(body, "blue", "blue.html");

        assertEquals(2, evaluateExpression(_htmlDocument, "links.length"), "number of links");
        Object second = evaluateExpression(_htmlDocument, "links[1]");
        assertNotNull(second, "Did not obtain any link object");
        assertTrue(second instanceof HTMLAnchorElement, "Object is not a link element");
        assertEquals("blue", ((HTMLAnchorElement) second).getId(), "Link ID");

        assertEquals("http://localhost/red.html", evaluateExpression(_htmlDocument, "links.red.href"), "red link href");
    }

    /**
     * Append link.
     *
     * @param body
     *            the body
     * @param id
     *            the id
     * @param href
     *            the href
     */
    private void appendLink(HTMLBodyElement body, String id, String href) {
        HTMLAnchorElement anchor1 = (HTMLAnchorElement) createElement("a");
        anchor1.setId(id);
        anchor1.setHref(href);
        body.appendChild(anchor1);
    }

    /**
     * Convertable.
     *
     * @throws Exception
     *             the exception
     */
    @Test
    void convertable() throws Exception {
        assertConvertable(String.class, String.class);
        assertConvertable(Integer.class, String.class);
        assertConvertable(String.class, Integer.class);
        assertConvertable(Short.class, Integer.class);
        assertConvertable(String.class, Boolean.class);
        assertConvertable(Byte.class, int.class);
    }

    /**
     * Assert convertable.
     *
     * @param valueType
     *            the value type
     * @param parameterType
     *            the parameter type
     */
    private void assertConvertable(Class valueType, Class parameterType) {
        assertTrue(ScriptingSupport.isConvertableTo(valueType, parameterType),
                valueType.getName() + " should be convertable to " + parameterType.getName());
    }

    /**
     * Evaluate expression.
     *
     * @param node
     *            the node
     * @param expression
     *            the expression
     *
     * @return the object
     */
    private Object evaluateExpression(Node node, String expression) {
        try {
            Context context = Context.enter();
            context.initStandardObjects(null);
            return ((NodeImpl) node).evaluateExpression(expression);
        } finally {
            Context.exit();
        }
    }
}
