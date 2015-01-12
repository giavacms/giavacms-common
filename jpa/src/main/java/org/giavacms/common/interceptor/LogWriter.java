/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.interceptor;

public interface LogWriter
{
   public void write(String className, String method, String params,
            Object result);
}
