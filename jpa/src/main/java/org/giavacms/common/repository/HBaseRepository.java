/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.repository;

import javax.persistence.EntityManager;

public abstract class HBaseRepository<T> extends HAbstractRepository<T>
{

   private static final long serialVersionUID = 1L;

   // --- JPA ---------------------------------

   public static final String UNIT_NAME = "pu";

   private EntityManager em;

   @Override
   protected EntityManager getEm()
   {
      return em;
   }

   @Override
   public void setEm(EntityManager em)
   {
      this.em = em;
   }

}
