/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.giavacms.common.model.Search;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Example;


@SuppressWarnings("unchecked")
public abstract class HAbstractRepository<T> extends AbstractRepository<T>
{

   private static final long serialVersionUID = 1L;

   protected Session getSession()
   {
      return (Session) getEm().getDelegate();
   }

   protected Criteria getRestrictionsH(Search<T> search, boolean justCount)
   {
      return getSession().createCriteria(getEntityType()).add(
               Example.create(search.getObj()));

   }

   public List<T> getList(int startRow, int pageSize, Criteria res)
   {
      List<T> result = new ArrayList<T>();
      try
      {
         return (List<T>) res.list();
      }
      catch (Exception e)
      {
         logger.info(e.getMessage());
      }
      return result;
   }

   @Override
   public List<T> getList(Search<T> ricerca, int startRow, int pageSize)
   {
      try
      {
         List<T> result = new ArrayList<T>();
         boolean count = false;
         Criteria res = getRestrictionsH(ricerca, count);
         if (res == null)
            return result;
         if (startRow >= 0)
         {
            res.setFirstResult(startRow);
         }
         if (pageSize > 0)
         {
            res.setMaxResults(pageSize);
         }
         return (List<T>) res.list();
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, null, ex);
         return null;
      }
   }

   @Override
   public List<T> getAllList()
   {
      try
      {
         return (List<T>) getSession().createCriteria(getEntityType())
                  .list();
      }
      catch (Exception ex)
      {
         logger.log(Level.SEVERE, null, ex);
         return null;
      }
   }

   @Override
   public int getListSize(Search<T> ricerca)
   {
      Long result = new Long(0);
      try
      {
         boolean count = true;
         Criteria res = getRestrictionsH(ricerca, count);
         if ((res != null) && (res.uniqueResult() != null))
            result = (Long) res.uniqueResult();
         return result.intValue();
      }
      catch (Exception e)
      {
         logger.info(e.getMessage());
      }
      return 0;
   }
}
