/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.model;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import org.giavacms.common.repository.Repository;


/**
 * @author fiorenzo pizza
 * 
 * @param <T>
 */
public class LocalDataModel<T> extends PagedListDataModel<T>
{

   private Search<T> search;
   private Repository<T> repository;
   private List<DataProcessor<T>> processors = new ArrayList<DataProcessor<T>>();

   /**
    * @param pageSize
    * @param search
    * @param repository
    */
   public LocalDataModel(int pageSize, Search<T> search,
            Repository<T> repository)
   {
      super(pageSize);
      this.search = search;
      this.repository = repository;
   }

   /**
    * @param startRow
    * @param pageSize
    */
   public DataPage<T> fetchPage(int startRow, int pageSize)
   {
      try
      {
         return getDataPage(startRow, pageSize);
      }
      catch (NamingException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   /**
    * @param startRow
    * @param pageSize
    * @return
    * @throws NamingException
    */
   private DataPage<T> getDataPage(int startRow, int pageSize)
            throws NamingException
   {
      List<T> data = repository.getList(search, startRow, pageSize);
      // --- aggiunta per permettere elaborazioni personalizzate sul dm prima
      // di mostrarlo nella view ----
      for (DataProcessor<T> processor : processors)
      {
         processor.process(data, search);
      }
      // --- fine ---------------------------------------------------------
      DataPage<T> dataPage = new DataPage<T>(repository.getListSize(search),
               startRow, data);
      return dataPage;
   }

   /**
    * @param processor
    */
   public void addProcessor(DataProcessor<T> processor)
   {
      if (processor != null)
         this.processors.add(processor);
   }
}
