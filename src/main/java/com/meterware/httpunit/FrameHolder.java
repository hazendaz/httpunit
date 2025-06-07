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
package com.meterware.httpunit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.xml.sax.SAXException;

/**
 * The Class FrameHolder.
 */
class FrameHolder {

    /** Map from a frame selector to its corresponding web response. **/
    private Properties _contents = new Properties();

    /** Map from a frame selector to its subframe selectors. **/
    private Properties _subframes = new Properties();

    /** The window which owns this frame holder. **/
    private WebWindow _window;

    /** The topmost frame in this frameholder. **/
    private FrameSelector _topFrame;

    /**
     * Instantiates a new frame holder.
     *
     * @param window
     *            the window
     */
    FrameHolder(WebWindow window) {
        _window = window;
        _topFrame = FrameSelector.newTopFrame(window);
        DefaultWebResponse blankResponse = new DefaultWebResponse(window.getClient(), null, WebResponse.BLANK_HTML);
        _contents.put(_topFrame, blankResponse);
        HttpUnitOptions.getScriptingEngine().associate(blankResponse);
    }

    /**
     * Gets the top frame.
     *
     * @return the top frame
     */
    FrameSelector getTopFrame() {
        return _topFrame;
    }

    /**
     * Gets the frame contents.
     *
     * @param targetFrame
     *            the target frame
     *
     * @return the frame contents
     */
    WebResponse getFrameContents(FrameSelector targetFrame) {
        if (targetFrame == FrameSelector.TOP_FRAME) {
            targetFrame = getTopFrame();
        }
        WebResponse response = get(targetFrame);
        if (response == null) {
            throw new NoSuchFrameException(targetFrame.getName());
        }
        return response;
    }

    /**
     * Gets the subframe contents.
     *
     * @param frame
     *            the frame
     * @param subFrameName
     *            the sub frame name
     *
     * @return the subframe contents
     */
    WebResponse getSubframeContents(FrameSelector frame, String subFrameName) {
        FrameSelector[] subframes = (FrameSelector[]) _subframes.get(frame);
        if (subframes == null) {
            throw new NoSuchFrameException(subFrameName);
        }

        for (FrameSelector subframe : subframes) {
            if (subframe.getName().equalsIgnoreCase(subFrameName)) {
                return get(subframe);
            }
        }
        throw new NoSuchFrameException(subFrameName);
    }

    /**
     * Gets the parent frame contents.
     *
     * @param frame
     *            the frame
     *
     * @return the parent frame contents
     */
    WebResponse getParentFrameContents(FrameSelector frame) {
        return get(frame.getParent() == null ? _topFrame : frame.getParent());
    }

    /**
     * Gets the.
     *
     * @param targetFrame
     *            the target frame
     *
     * @return the web response
     */
    WebResponse get(FrameSelector targetFrame) {
        return (WebResponse) _contents.get(targetFrame);
    }

    /**
     * Gets the.
     *
     * @param target
     *            the target
     *
     * @return the web response
     */
    WebResponse get(String target) {
        FrameSelector frame = getFrame(_topFrame, target);
        return frame == null ? null : (WebResponse) _contents.get(frame);
    }

    /**
     * Gets the frame.
     *
     * @param target
     *            the target
     *
     * @return the frame
     */
    FrameSelector getFrame(String target) {
        return target.equals(_window.getName()) ? _topFrame : getFrame(_topFrame, target);
    }

    /**
     * Gets the frame.
     *
     * @param rootFrame
     *            the root frame
     * @param target
     *            the target
     *
     * @return the frame
     */
    private FrameSelector getFrame(FrameSelector rootFrame, String target) {
        if (target.equalsIgnoreCase(WebRequest.TOP_FRAME)) {
            return _topFrame;
        }
        if (target.equalsIgnoreCase(rootFrame.getName())) {
            return rootFrame;
        }

        return lookupFrame(rootFrame, target);
    }

    /**
     * Lookup frame.
     *
     * @param rootFrame
     *            the root frame
     * @param target
     *            the target
     *
     * @return the frame selector
     */
    private FrameSelector lookupFrame(FrameSelector rootFrame, String target) {
        FrameSelector result = getFromSubframe(rootFrame, target);
        if (result != null) {
            return result;
        }
        if (rootFrame.getName().equals(target)) {
            return rootFrame;
        }
        if (rootFrame.getParent() != null) {
            return lookupFrame(rootFrame.getParent(), target);
        }
        return null;
    }

    /**
     * Gets the from subframe.
     *
     * @param rootFrame
     *            the root frame
     * @param target
     *            the target
     *
     * @return the from subframe
     */
    private FrameSelector getFromSubframe(FrameSelector rootFrame, String target) {
        FrameSelector[] subframes = (FrameSelector[]) _subframes.get(rootFrame);
        if (subframes == null) {
            return null;
        }

        for (FrameSelector subframe : subframes) {
            if (subframe.getName().equalsIgnoreCase(target)) {
                return subframe;
            }
        }
        for (FrameSelector subframe : subframes) {
            FrameSelector result = getFromSubframe(subframe, target);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Gets the active frame names.
     *
     * @return the active frame names
     */
    List<String> getActiveFrameNames() {
        List<String> result = new ArrayList<>();
        for (Enumeration e = _contents.keys(); e.hasMoreElements();) {
            result.add(((FrameSelector) e.nextElement()).getName());
        }

        return result;
    }

    /**
     * Determines the frame in which the reply to a request will be stored.
     *
     * @param request
     *            the request
     *
     * @return the target frame
     */
    FrameSelector getTargetFrame(WebRequest request) {
        if (WebRequest.NEW_WINDOW.equalsIgnoreCase(request.getTarget())) {
            return FrameSelector.NEW_FRAME;
        }
        if (WebRequest.TOP_FRAME.equalsIgnoreCase(request.getTarget())) {
            return _topFrame;
        }
        if (WebRequest.SAME_FRAME.equalsIgnoreCase(request.getTarget())) {
            return request.getSourceFrame();
        }
        if (WebRequest.PARENT_FRAME.equalsIgnoreCase(request.getTarget())) {
            return request.getSourceFrame().getParent() == null ? _topFrame : request.getSourceFrame().getParent();
        }
        if (request.getSourceFrame().getName().equalsIgnoreCase(request.getTarget())) {
            return request.getSourceFrame();
        }
        FrameSelector targetFrame = getFrame(request.getSourceFrame(), request.getTarget());
        if (targetFrame == null) {
            targetFrame = _window.getClient().findFrame(request.getTarget());
        }
        return targetFrame != null ? targetFrame : FrameSelector.NEW_FRAME;
    }

    /**
     * Update frames.
     *
     * @param response
     *            the response
     * @param frame
     *            the frame
     * @param requestContext
     *            the request context
     *
     * @throws MalformedURLException
     *             the malformed URL exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws SAXException
     *             the SAX exception
     */
    void updateFrames(WebResponse response, FrameSelector frame, RequestContext requestContext)
            throws MalformedURLException, IOException, SAXException {
        removeSubFrames(frame);
        _contents.put(frame, response);

        if (response.isHTML()) {
            HttpUnitOptions.getScriptingEngine().associate(response);
            requestContext.addNewResponse(response);
            WebRequest[] requests = response.getFrameRequests();
            if (requests.length > 0) {
                createSubFrames(frame, response.getFrameSelectors());
                for (WebRequest request : requests) {
                    if (request.getURLString().length() != 0) {
                        response.getWindow().getSubframeResponse(request, requestContext);
                    }
                }
            }
        }
    }

    /**
     * Removes the sub frames.
     *
     * @param frame
     *            the frame
     */
    private void removeSubFrames(FrameSelector frame) {
        FrameSelector[] subframes = (FrameSelector[]) _subframes.get(frame);
        if (subframes == null) {
            return;
        }

        _subframes.remove(frame);
        for (FrameSelector subframe : subframes) {
            removeSubFrames(subframe);
            _contents.remove(subframe);
        }
    }

    /**
     * Creates the sub frames.
     *
     * @param frame
     *            the frame
     * @param subframes
     *            the subframes
     */
    private void createSubFrames(FrameSelector frame, FrameSelector[] subframes) {
        _subframes.put(frame, subframes);
        for (FrameSelector subframe : subframes) {
            _contents.put(subframe, WebResponse.createBlankResponse());
        }
    }

    /**
     * Given the qualified name of a frame and the name of a nested frame, returns the qualified name of the nested
     * frame.
     *
     * @param parentFrame
     *            the parent frame
     * @param relativeName
     *            the relative name
     *
     * @return the frame selector
     */
    static FrameSelector newNestedFrame(FrameSelector parentFrame, final String relativeName) {
        if (relativeName == null || relativeName.isEmpty()) {
            return new FrameSelector();
        }
        return new FrameSelector(relativeName, parentFrame);
    }

}
