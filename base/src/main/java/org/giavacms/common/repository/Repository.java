/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.repository;

import java.util.List;

import org.giavacms.common.model.Search;


/**
 * @author fiorenzo pizza
 * 
 * @param <T>
 */
public interface Repository<T>
{

   /**
    * @return
    */
   public List<T> getAllList();

   /**
    * @param search
    * @param startRow
    * @param pageSize
    * @return
    */
   public List<T> getList(Search<T> search, int startRow, int pageSize);

   /**
    * @param search
    * @return
    */
   public int getListSize(Search<T> search);

   /**
    * Find by primary key
    * 
    * @param key
    * @return
    */
   public T find(Object key);

   /**
    * Fetch by primary key
    * 
    * @param key
    * @return
    */
   public T fetch(Object key);

   /**
    * create
    * 
    * @param domainClass
    * @return
    */
   public T create(Class<T> domainClass);

   /**
    * Make an instance persistent.
    * <p>
    * 
    * @param object
    * @return
    */
   public T persist(T object);

   /**
    * @param object
    * @return
    */
   public boolean update(T object);

   /**
    * @param key
    * @return
    */
   public boolean delete(Object key);

}
