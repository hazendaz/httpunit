/*
 * MIT License
 *
 * Copyright 2011-2024 Russell Gold
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
package com.meterware.servletunit;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
 **/
class FilterConfigImpl implements FilterConfig {

    private String _name;
    private ServletContext _servletContext;
    private Hashtable _initParams;

    FilterConfigImpl(String name, ServletContext servletContext, Hashtable initParams) {
        _name = name;
        _servletContext = servletContext;
        _initParams = initParams;
    }

    @Override
    public String getFilterName() {
        return _name;
    }

    @Override
    public ServletContext getServletContext() {
        return _servletContext;
    }

    @Override
    public String getInitParameter(String s) {
        return (String) _initParams.get(s);
    }

    @Override
    public Enumeration getInitParameterNames() {
        return _initParams.keys();
    }

}
