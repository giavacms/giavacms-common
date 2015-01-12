/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.giavacms.common.repository.Repository;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;


/**
 * To make this class work, use p:dataTable as follows:
 * 
 * 
 * <p:dataTable rowIndexVar="rowIndex" var="r" value="#{handler.model}" * * * * * * * * rows="#{handler.pageSize}"
 * paginator="true" dynamic="true" lazy="true">
 * 
 * @author pisi79
 * 
 * @param <T>
 */
public class LocalLazyDataModel<T> extends LazyDataModel<T> implements
         Serializable
{

   private static final long serialVersionUID = 1L;

   private List<T> list;
   private Search<T> search;
   private Repository<T> repository;
   private List<DataProcessor<T>> processors = new ArrayList<DataProcessor<T>>();

   public LocalLazyDataModel(List<T> list)
   {
      this.list = list;
      setRowCount(list.size());
   }

   /**
    * @param search
    * @param repository
    */
   public LocalLazyDataModel(Search<T> search, Repository<T> repository)
   {
      super();

      // Sel l'oggetto search non ha la flag di attivo, la lista deve essere
      // vuota
      setRowCount(search.isActive() ? repository.getListSize(search) : 0);

      this.search = search;
      this.repository = repository;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.primefaces.model.LazyDataModel#load(int, int, java.lang.String, boolean, java.util.Map)
    */
   public List<T> load(int first, int pageSize, String sortField,
            boolean sortOrder, Map<String, Object> filters)
   {
      List<T> data = null;
      if (search.isActive())
      {
         // Imposto il campo di ordinamento
         search.setOrder(sortField);
         // Imposto la direzione di ordinamento
         search.setOrderAsc(sortOrder);
         data = repository.getList(search, first, pageSize);
         // --- aggiunta per permettere elaborazioni personalizzate sul dm
         // prima
         // di mostrarlo nella view ----
         for (DataProcessor<T> processor : processors)
         {
            processor.process(data, search);
         }
      }
      return data;
   }

   /**
    * @param processor
    */
   public void addProcessor(DataProcessor<T> processor)
   {
      if (processor != null)
         this.processors.add(processor);
   }

   @Override
   public List<T> load(int first, int pageSize, String arg2, SortOrder arg3,
            Map<String, Object> arg4)
   {
      if (repository != null)
      {
         return load(first, pageSize, arg2, SortOrder.ASCENDING == arg3, arg4);
      }
      else
      {
         // paginiamo la lista originale, senza applicare i filtri di ricerca o di ordinamento... andrebbe fatto se si
         // vuole anche filtrare in ram e non solo paginare
         List<T> page = new ArrayList<T>();
         for (int i = first; i < (first + pageSize) && i < list.size(); i++)
         {
            page.add(list.get(i));
         }
         return page;
      }
   }

   @Override
   public void setRowIndex(int rowIndex)
   {
      if (getPageSize() != 0)
      {
         super.setRowIndex(rowIndex);
      }
   }

}
