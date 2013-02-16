/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

public abstract class Uploadable implements Serializable
{

   private static final long serialVersionUID = 1L;

   private String name;
   private String mime;
   private byte[] data;

   public byte[] getData()
   {
      return data;
   }

   public void setData(byte[] data)
   {
      this.data = data;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      int extDot = name.lastIndexOf('.');
      if (extDot > 0)
      {
         String extension = name.substring(extDot + 1);
         if ("bmp".equals(extension))
         {
            mime = "image/bmp";
         }
         else if ("jpg".equals(extension))
         {
            mime = "image/jpeg";
         }
         else if ("gif".equals(extension))
         {
            mime = "image/gif";
         }
         else if ("png".equals(extension))
         {
            mime = "image/png";
         }
         else
         {
            mime = "image/unknown";
         }
      }
      this.name = name;
   }

   public long getLength()
   {
      return data == null ? 0 : data.length;
   }

   public long getLengthKB()
   {
      return getLength() / 1000;
   }

   public String getMime()
   {
      return mime;
   }

   public StreamedContent getContent()
   {
      return new DefaultStreamedContent(new ByteArrayInputStream(data), mime);
   }

   public long getTimeStamp()
   {
      return System.currentTimeMillis();
   }

   public void paint(OutputStream stream, Object object) throws IOException
   {
      stream.write(getData());
      stream.close();
   }

}