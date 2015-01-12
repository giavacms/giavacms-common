/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.producer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Observes;
import javax.faces.model.SelectItem;

import org.giavacms.common.event.ResetEvent;
import org.jboss.logging.Logger;

/**
 * 
 * @param <T>
 */
public abstract class AbstractProducer implements Serializable
{

   private static final long serialVersionUID = 1L;

   // ------------------------------------------------
   // --- Logger -------------------------------------
   // ------------------------------------------------

   protected final Logger logger = Logger.getLogger(getClass().getCanonicalName());

   @SuppressWarnings("rawtypes")
   protected Map<Class, SelectItem[]> items = null;

   @SuppressWarnings("rawtypes")
   @PostConstruct
   public void reset()
   {
      logger.info("reset");
      items = new HashMap<Class, SelectItem[]>();
   }

   @SuppressWarnings("rawtypes")
   public void resetItemsForClass(Class clazz)
   {
      if (items.containsKey(clazz))
      {
         items.remove(clazz);
      }
   }

   public void observeReset(@Observes ResetEvent resetEvent)
   {
      if (resetEvent != null && resetEvent.getObservedClass() != null)
      {
         resetItemsForClass(resetEvent.getObservedClass());
      }
   }
}