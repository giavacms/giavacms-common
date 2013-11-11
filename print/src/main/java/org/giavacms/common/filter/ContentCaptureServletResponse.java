/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class ContentCaptureServletResponse extends HttpServletResponseWrapper
{

   private ByteArrayOutputStream contentBuffer;
   private PrintWriter writer;

   public ContentCaptureServletResponse(HttpServletResponse originalResponse)
   {
      super(originalResponse);
   }

   @Override
   public PrintWriter getWriter() throws IOException
   {
      if (writer == null)
      {
         contentBuffer = new ByteArrayOutputStream();
         writer = new PrintWriter(contentBuffer);
      }
      return writer;
   }

   public String getContent()
   {
      writer.flush();
      String xhtmlContent = new String(contentBuffer.toByteArray());
     
      return xhtmlContent;
   }
}
