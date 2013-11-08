/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.giavacms.common.model.Uploadable;
import org.giavacms.common.model.UploadableProcessor;
import org.giavacms.common.util.FileUtils;
import org.jboss.logging.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;


public class UploadableController<T extends Uploadable> implements Serializable
{

   private static final long serialVersionUID = 1L;

   Logger logger = Logger.getLogger(getClass());

   Class<T> t;
   List<T> files;
   private List<UploadableProcessor<T>> processors = new ArrayList<UploadableProcessor<T>>();

   public UploadableController(Class<T> t)
   {
      files = new ArrayList<T>();
      this.t = t;
   }

   public void handleFileUpload(FileUploadEvent event)
   {
      // FacesMessage msg = new FacesMessage("Succesful", event.getFile()
      // .getFileName() + " is uploaded.");
      // FacesContext.getCurrentInstance().addMessage(null, msg);
      T i;
      try
      {
         i = t.newInstance();
         i.setName(FileUtils.getLastPartOf(event.getFile().getFileName()));
         i.setData(event.getFile().getContents());
         boolean okToUpload = true;
         for (UploadableProcessor<T> up : processors)
         {
            if (!okToUpload)
            {
               continue;
            }
            okToUpload = okToUpload & up.process(i);
         }
         if (okToUpload)
         {
            files.add(i);
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public List<T> getFiles()
   {
      return files;
   }

   public void remove(String index)
   {
      getFiles().remove(Integer.parseInt(index));
   }

   public void setFiles(List<T> files)
   {
      this.files = files;
   }

   public void paint(OutputStream stream, Object object) throws IOException
   {
      stream.write(getFiles().get((Integer) object).getData());
      stream.close();
   }

   public StreamedContent getContent(Integer index)
   {
      return new DefaultStreamedContent(new ByteArrayInputStream(getFiles()
               .get(index).getData()), getFiles().get(index).getMime());
   }

   public long getTimeStamp()
   {
      return System.currentTimeMillis();
   }

   public String clearUploadData()
   {
      files.clear();
      return null;
   }

   /**
    * @param processor
    */
   public void addProcessor(UploadableProcessor<T> processor)
   {
      if (processor != null)
         this.processors.add(processor);
   }

}