/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.model;

import java.util.List;

/**
 * A simple class that represents a "page" of data out of a longer set, ie a list of objects together with info to
 * indicate the starting row and the full size of the dataset. EJBs can return instances of this type when returning
 * subsets of available data.
 * 
 * @author fiorenzo pizza
 * 
 * @param <T>
 */
public class DataPage<T>
{

   private int datasetSize;
   private int startRow;
   private List<T> data;

   /**
    * Create an object representing a sublist of a dataset.
    * 
    * @param datasetSize is the total number of matching rows available.
    * 
    * @param startRow is the index within the complete dataset of the first element in the data list.
    * 
    * @param data is a list of consecutive objects from the dataset.
    */
   public DataPage(int datasetSize, int startRow, List<T> data)
   {
      this.datasetSize = datasetSize;
      this.startRow = startRow;
      this.data = data;
   }

   /**
    * Return the number of items in the full dataset.
    */
   public int getDatasetSize()
   {
      return datasetSize;
   }

   /**
    * Return the offset within the full dataset of the first element in the list held by this object.
    */
   public int getStartRow()
   {
      return startRow;
   }

   /**
    * Return the list of objects held by this object, which is a continuous subset of the full dataset.
    */
   public List<T> getData()
   {
      return data;
   }
}
