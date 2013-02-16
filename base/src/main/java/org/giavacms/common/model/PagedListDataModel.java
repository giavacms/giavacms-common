/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.model;

import java.util.logging.Logger;

import javax.faces.model.DataModel;

/**
 * A special type of JSF DataModel to allow a datatable and datascroller to page through a large set of data without
 * having to hold the entire set of data in memory at once.
 * <p>
 * Any time a managed bean wants to avoid holding an entire dataset, the managed bean should declare an inner class
 * which extends this class and implements the fetchData method. This method is called as needed when the table requires
 * data that isn't available in the current data page held by this object.
 * <p>
 * This does require the managed bean (and in general the business method that the managed bean uses) to provide the
 * data wrapped in a DataPage object that provides info on the full size of the dataset.
 */
public abstract class PagedListDataModel<T> extends DataModel<T>
{
   Logger log = Logger.getLogger(PagedListDataModel.class.getName());
   // mantiene la posizione del cursore nel datamodel. usato in alternativa a
   // rowIndex a sua volta usato dal data table
   public int entityIndex;

   // mantiene il puntatore al numero di pagina corrente
   public int currentPage;

   public int lastPage;

   int pageSize;

   int rowIndex;

   DataPage<T> page;

   /*
    * Create a datamodel that pages through the data showing the specified number of rows on each page.
    */
   public PagedListDataModel(int pageSize)
   {
      super();
      this.pageSize = pageSize;
      this.rowIndex = -1;
      this.page = null;
   }

   /**
    * Not used in this class; data is fetched via a callback to the fetchData method rather than by explicitly assigning
    * a list.
    */
   @Override
   public void setWrappedData(Object o)
   {
      throw new UnsupportedOperationException("setWrappedData");
   }

   @Override
   public int getRowIndex()
   {
      return rowIndex;
   }

   /**
    * Specify what the "current row" within the dataset is. Note that the UIData component will repeatedly call this
    * method followed by getRowData to obtain the objects to render in the table.
    */
   @Override
   public void setRowIndex(int index)
   {
      rowIndex = index;
   }

   /**
    * Return the total number of rows of data available (not just the number of rows in the current page!).
    */
   @Override
   public int getRowCount()
   {
      return getPage().getDatasetSize();
   }

   /**
    * Return a DataPage object; if one is not currently available then fetch one. Note that this doesn't ensure that the
    * datapage returned includes the current rowIndex row; see getRowData.
    */
   private DataPage<T> getPage()
   {
      if (page != null)
         return page;

      int rowIndex = getRowIndex();
      int startRow = rowIndex;
      if (rowIndex == -1)
      {
         // even when no row is selected, we still need a page
         // object so that we know the amount of data available.
         startRow = 0;
      }

      // invoke method on enclosing class
      page = fetchPage(startRow, pageSize);
      return page;
   }

   /**
    * Return the object corresponding to the current rowIndex. If the DataPage object currently cached doesn't include
    * that index then fetchPage is called to retrieve the appropriate page.
    */
   @Override
   public T getRowData()
   {
      if (rowIndex < 0)
      {
         throw new IllegalArgumentException(
                  "Invalid rowIndex for PagedListDataModel; not within page");
      }

      // ensure page exists; if rowIndex is beyond dataset size, then
      // we should still get back a DataPage object with the dataset size
      // in it...
      if (page == null)
      {
         page = fetchPage(rowIndex, pageSize);
      }
      // imposta l'indice locale del dataset
      setEntityIndex(rowIndex % getPageSize());
      // righe in dataset
      int datasetSize = page.getDatasetSize();
      int startRow = page.getStartRow();
      int nRows = page.getData().size();
      int endRow = startRow + nRows;

      if (rowIndex >= datasetSize)
      {
         throw new IllegalArgumentException("Invalid rowIndex");
      }

      if (rowIndex < startRow)
      {
         page = fetchPage(rowIndex, pageSize);
         startRow = page.getStartRow();
      }
      else if (rowIndex >= endRow)
      {
         page = fetchPage(rowIndex, pageSize);
         startRow = page.getStartRow();
      }
      int num = rowIndex - startRow;
      if (num < 0)
      {
         return null;
      }

      return page.getData().get(num);
   }

   @Override
   public Object getWrappedData()
   {
      return page.getData();
   }

   /**
    * Return true if the rowIndex value is currently set to a value that matches some element in the dataset. Note that
    * it may match a row that is not in the currently cached DataPage; if so then when getRowData is called the required
    * DataPage will be fetched by calling fetchData.
    */
   @Override
   public boolean isRowAvailable()
   {
      DataPage<T> page = getPage();
      if (page == null)
         return false;

      int rowIndex = getRowIndex();
      if (rowIndex < 0)
      {
         return false;
      }
      else if (rowIndex >= page.getDatasetSize())
      {
         return false;
      }
      else
      {
         return true;
      }
   }

   public int getNumPages()
   {
      int num = getRowCount() / getPageSize();
      if (num * getPageSize() < getRowCount())
      {
         // ultima pagina non completa: ritorna
         // il numero di pagine complete +1
         return num + 1;
      }
      // anche l'ultima pagina è completa
      return num;
   }

   public int getCurrentPage()
   {
      return currentPage;
   }

   public void invalidateSession()
   {
      log.info("invalidatye entityIndex: " + entityIndex);
      log.info("invalidatye rowIndex: " + rowIndex);
      log.info("invalidatye pageSize: " + pageSize);
      log.info("invalidatye getDatasetSize: " + page.getDatasetSize());
      log.info("invalidatye currentPage: " + currentPage);
      log.info("invalidatye getStartRow: " + page.getStartRow());
      setRowIndex(lastPage + 1);
      setCurrentPage(0);
      log.info("invalidatye rowIndex: " + rowIndex);
      this.page = null;

   }

   public void setCurrentPage(int cp)
   {
      currentPage = cp;
      if (currentPage < 0)
         currentPage = 0;
   }

   protected void setCurrentPage(int startRow, int pageSize)
   {
      setCurrentPage(startRow / pageSize);
   }

   public int getPageSize()
   {
      return pageSize;
   }

   public void setPageSize(int pageSize)
   {
      this.pageSize = pageSize;
   }

   public int getEntityIndex()
   {
      return entityIndex;
   }

   public void setEntityIndex(int ei)
   {
      entityIndex = ei;
      if (entityIndex < 0)
         entityIndex = 0;
      if (entityIndex >= getRowCount())
         entityIndex = getRowCount() - 1;
   }

   /**
    * Method which must be implemented in cooperation with the managed bean class to fetch data on demand.
    */
   public abstract DataPage<T> fetchPage(int startRow, int pageSize);
}
