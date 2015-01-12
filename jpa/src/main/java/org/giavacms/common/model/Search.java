/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.model;

import java.io.Serializable;

/**
 * @author fiorenzo pizza
 * 
 *         Class used to manage filters and ordering for lists
 * 
 *         The defaultOrder property is used to store the name of the field to be used as a default for ordering
 * 
 *         The order property contains the actual field used for ordering
 * 
 * @param <T>
 */
public class Search<T> implements Serializable
{

   private static final long serialVersionUID = 1L;

   private T obj;
   private T from;
   private T to;
   private boolean withH;

   private Class<T> classType;

   // --------- Ordering ----------------------------------------

   private String defaultOrder;
   private String order;
   private String grouping;
   private boolean orderAsc = true;

   // --------- Service ----------------------------------------

   private boolean active = true;

   /**
    * @param t
    */
   // public Search() {
   // classType = getClassType();
   // this.obj = init(classType);
   // this.from = init(classType);
   // this.to = init(classType);
   // }
   /**
    * @param t
    */
   public Search(Class<T> t)
   {
      classType = t;
      this.obj = init(t);
      this.from = init(t);
      this.to = init(t);
   }

   /**
    * @param o
    */
   public Search(T o)
   {
      this.obj = o;
   }

   /**
    * @param t
    * @return
    */
   private T init(Class<T> t)
   {
      try
      {
         return t.newInstance();
      }
      catch (InstantiationException e)
      {
         e.printStackTrace();
         return null;
      }
      catch (IllegalAccessException e)
      {
         e.printStackTrace();
         return null;
      }
   }

   /**
    * @return
    */
   // @SuppressWarnings("unchecked")
   // private Class<T> getClassType() {
   // ParameterizedType parameterizedType = (ParameterizedType)
   // getClass().getGenericSuperclass();
   // return (Class<T>) parameterizedType.getActualTypeArguments()[0];
   // }

   /**
    * @return
    */
   public T getObj()
   {
      return obj;
   }

   /**
    * @param t
    */
   public void setObj(T t)
   {
      this.obj = t;
   }

   /**
    * @return
    */
   public T getFrom()
   {
      return from;
   }

   /**
    * @param from
    */
   public void setFrom(T from)
   {
      this.from = from;
   }

   /**
    * @return
    */
   public T getTo()
   {
      return to;
   }

   /**
    * @param to
    */
   public void setTo(T to)
   {
      this.to = to;
   }

   /**
    * @return
    */
   public String getDefaultOrder()
   {
      return defaultOrder;
   }

   /**
    * @param defaultOrder
    */
   public void setDefaultOrder(String defaultOrder)
   {
      this.defaultOrder = defaultOrder;
   }

   /**
    * @return
    */
   public String getOrder()
   {
      return order;
   }

   /**
    * @param order
    */
   public void setOrder(String order)
   {
      this.order = order;
   }

   /**
    * @return
    */
   public boolean isOrderAsc()
   {
      return orderAsc;
   }

   /**
    * @param orderAsc
    */
   public void setOrderAsc(boolean orderAsc)
   {
      this.orderAsc = orderAsc;
   }

   // --------- Clear ----------------------------------------

   /**
    * Clear the active filters but not the ordering settings
    */
   public void clear()
   {
      this.obj = init(classType);
      this.from = init(classType);
      this.to = init(classType);
   }

   /**
    * Clear the active filters and the the ordering settings
    */
   public void clearAll()
   {
      clear();
      setOrder(getDefaultOrder());
      setOrderAsc(true);
   }

   /**
    * @return
    */
   public boolean isActive()
   {
      return active;
   }

   /**
    * @param active
    */
   public void setActive(boolean active)
   {
      this.active = active;
   }

   /**
    * @return
    */
   public boolean isWithH()
   {
      return withH;
   }

   /**
    * @param withH
    */
   public void setWithH(boolean withH)
   {
      this.withH = withH;
   }

   public String getGrouping()
   {
      return grouping;
   }

   public void setGrouping(String group)
   {
      this.grouping = group;
   }

}
