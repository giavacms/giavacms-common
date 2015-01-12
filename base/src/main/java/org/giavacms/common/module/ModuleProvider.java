/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.module;

import java.util.List;
import java.util.Map;

public interface ModuleProvider
{

   String getName();

   String getDescription();

   String getMenuFragment();

   String getPanelFragment();

   int getPriority();

   Map<String, String> getPermissions();

   List<String> getAllowableOperations();

}
