/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.renderer;

import java.util.List;
import java.util.Map;

public interface UiRepeatInterface<T>
{

   public int totalSize();

   public List<T> getPage();

   public Map<String, String> getParams();

   public int getCurrentPage();

   public String getCurrentPageParam();

   public int getPageSize();

   public void setPageSize(int p);

}