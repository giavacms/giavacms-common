/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.model;

import java.util.List;

/**
 * @author fiorenzo pizza
 * 
 * @param <T>
 */
public interface DataProcessor<T>
{

   public void process(List<T> list, Search<T> ricerca);

}
