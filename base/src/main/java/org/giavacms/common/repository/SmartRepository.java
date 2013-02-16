/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.repository;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.giavacms.common.annotation.Condition;
import org.giavacms.common.annotation.NotNullAndNotEmpty;
import org.giavacms.common.annotation.Smart;
import org.giavacms.common.model.Search;


/**
 * @author fiorenzo pizza
 * 
 * @param <T>
 */
public abstract class SmartRepository<T> extends AbstractRepository<T>
         implements Serializable, Repository<T>
{

   private static final long serialVersionUID = 1L;

   /**
    * criteri di default, comuni a tutti, ma specializzabili da ogni EJB tramite overriding
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Override
   protected Query getRestrictions(Search<T> search, boolean justCount)
   {

      Class c = search.getObj().getClass();

      // se non c'è smart tutto come prima
      Smart smart_anno = (Smart) c.getAnnotation(Smart.class);
      if (smart_anno == null)
      {
         return super.getRestrictions(search, justCount);
      }

      // altrimenti costruiamo la query qui
      Map<String, Object> params = new HashMap<String, Object>();

      String alias = smart_anno.alias();
      StringBuffer sb = new StringBuffer(getBaseList(search.getObj()
               .getClass(), alias, justCount));

      String separator = " where ";
      String operator = smart_anno.operator();

      Field[] fields = c.getDeclaredFields();
      for (Field field : fields)
      {

         Condition condition = field.getAnnotation(Condition.class);
         if (condition == null)
         {
            continue;
         }
         field.setAccessible(true);
         Object v = null;
         try
         {
            v = field.get(search.getObj());
         }
         catch (Exception e)
         {
            // should not happen;
            e.printStackTrace();
            continue;
         }

         NotNullAndNotEmpty notNullAndNotEmpty = field
                  .getAnnotation(NotNullAndNotEmpty.class);
         if (v == null && notNullAndNotEmpty != null)
         {
            continue;
         }

         // aggiunta alla query
         sb.append(separator).append(condition.ejbql());
         // aggiunta alla mappa
         List<String> paramNames = estrai(condition.ejbql());
         for (String paramName : paramNames)
         {
            params.put(paramName, v);
            // come si gestisce il caso like??
            // params.put("cognome", likeParam(
            // search.getObj().getCognome().toUpperCase()));
         }
         // separatore
         separator = operator;

      }

      Method[] methods = c.getDeclaredMethods();
      for (Method method : methods)
      {

         Condition condition = method.getAnnotation(Condition.class);
         if (condition == null)
         {
            continue;
         }

         method.setAccessible(true);
         Object[] vs = null;
         ;
         try
         {
            vs = (Object[]) method.invoke(search.getObj(),
                     new Object[] {});
         }
         catch (Exception e)
         {
            // should not happen
            e.printStackTrace();
            continue;
         }

         if (vs == null)
         {
            continue;
         }

         NotNullAndNotEmpty notNullAndNotEmpty = method
                  .getAnnotation(NotNullAndNotEmpty.class);
         for (Object v : vs)
         {
            if (v == null && notNullAndNotEmpty != null)
            {
               continue;
            }
         }

         // aggiunta alla query
         sb.append(separator).append(condition.ejbql());
         // aggiunta alla mappa
         List<String> paramNames = estrai(condition.ejbql());
         for (int i = 0; i < vs.length; i++)
         {
            params.put(paramNames.get(i), vs[i]);
            // come si gestisce il caso like??
            // params.put("cognome", likeParam(
            // search.getObj().getCognome().toUpperCase()));
         }
         // separatore
         separator = operator;

      }

      if (!justCount)
      {
         sb.append(getOrderBy(alias, search.getOrder()));
      }

      Query q = getEm().createQuery(sb.toString());
      for (String param : params.keySet())
      {
         q.setParameter(param, params.get(param));
      }

      return q;

   }

   private List<String> estrai(String ejbql)
   {
      List<String> names = new ArrayList<String>();
      int beginP = ejbql.indexOf(":");
      while (beginP >= 0)
      {
         String subql = ejbql.substring(beginP + 1);
         int endP = subql.indexOf(" ");
         if (endP > 0)
         {
            names.add(subql.substring(0, endP));
         }
         else
         {
            names.add(subql);
         }
         ejbql = subql;
         beginP = ejbql.indexOf(":");
      }
      return names;
   }

}