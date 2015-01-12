/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.filter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.giavacms.common.util.StringUtils;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;

public class RendererFilter implements Filter
{

   FilterConfig config;

   static Logger logger = Logger.getLogger(RendererFilter.class
            .getCanonicalName());

   public void init(FilterConfig config) throws ServletException
   {
      System.setProperty("xr.util-logging.loggingEnabled", "false");
      this.config = config;
   }

   public synchronized void doFilter(ServletRequest req, ServletResponse resp,
            FilterChain filterChain) throws IOException, ServletException
   {
      HttpServletRequest request = (HttpServletRequest) req;
      HttpServletResponse response = (HttpServletResponse) resp;

      // Check to see if this filter should apply.
      @SuppressWarnings("unused")
      String renderType = request.getParameter("RenderOutputType");
      // if (renderType != null) {
      // Capture the content for this request
      ContentCaptureServletResponse capContent = new ContentCaptureServletResponse(
               response);
      filterChain.doFilter(request, capContent);

      try
      {
         // Parse the XHTML content to a document that is readable by the
         // XHTML renderer.
         StringReader contentReader = new StringReader(
                  capContent.getContent());
         InputSource source = new InputSource(contentReader);

         DocumentBuilderFactory factory = DocumentBuilderFactory
                  .newInstance();
         factory.setValidating(false);
         DocumentBuilder documentBuilder = factory.newDocumentBuilder();
         documentBuilder.setEntityResolver(new LocalHostEntityResolver());
         Document xhtmlContent = documentBuilder.parse(source);

         String title = "documento";
         NodeList titleNL = xhtmlContent.getElementsByTagName("title");
         if (titleNL != null && titleNL.getLength() > 0)
         {
            Node titleN = titleNL.item(0);
            if (titleN != null && titleN.getFirstChild() != null)
            {
               String titleS = StringUtils.clean(titleN.getFirstChild()
                        .getNodeValue());
               if (titleS != null && titleS.length() > 0)
               {
                  title = titleS;
               }
            }
         }

         ITextRenderer renderer = new ITextRenderer();

         try
         {
            String[] fonts = { "arialbd.ttf",
                     "Times_New_Roman_Bold_Italic.ttf", "arialbi.ttf",
                     "Times_New_Roman_Bold.ttf", "ariali.ttf",
                     "Times_New_Roman_Italic.ttf", "arial.ttf",
                     "Times_New_Roman.ttf", "Courier_New_Bold_Italic.ttf",
                     "Verdana_Bold_Italic.ttf", "Courier_New_Bold.ttf",
                     "Verdana_Bold.ttf", "Courier_New_Italic.ttf",
                     "Verdana_Italic.ttf", "Courier_New.ttf", "Verdana.ttf" };

            for (String font : fonts)
            {
               renderer.getFontResolver().addFont("fonts/" + font,
                        BaseFont.EMBEDDED);
            }

         }
         catch (Exception e)
         {
            e.printStackTrace();
         }

         renderer.setDocument(xhtmlContent, null);
         renderer.layout();

         response.setContentType("application/pdf");
         response.setHeader("Content-disposition", "attachment; filename="
                  + title + ".pdf");
         OutputStream browserStream = response.getOutputStream();
         renderer.createPDF(browserStream);
         return;

      }
      catch (SAXException e)
      {
         throw new ServletException(e);
      }
      catch (DocumentException e)
      {
         throw new ServletException(e);
      }
      catch (ParserConfigurationException e)
      {
         throw new ServletException(e);
      }

   }

   protected InputStream getFontsStream(String archive_name)
   {
      try
      {
         URL url = Thread.currentThread().getContextClassLoader()
                  .getResource(archive_name);
         // this works if the archive_name url begins with vfs://
         // (i.e. it is packaged within a jar library)
         return url.openStream();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   protected File getFontsFile(String archive_name)
   {
      try
      {
         URL url = Thread.currentThread().getContextClassLoader()
                  .getResource(archive_name);
         // this works if the archive_name url begins with file://
         // (i.e. not packaged within another jar library)
         return new File(url.toURI());
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   public static void main(String[] args) throws Exception
   {
      RendererFilter rf = new RendererFilter();
      logger.info("--------------------------------------------");
      InputStream is = rf.getFontsStream("fonts.jar");
      if (is != null)
      {
         JarInputStream fontsArchive = new JarInputStream(is);
         ZipEntry entry = fontsArchive.getNextEntry();
         while (entry != null)
         {
            logger.info(entry.getName());
            fontsArchive.closeEntry();
            entry = fontsArchive.getNextJarEntry();
         }
         fontsArchive.close();
         logger.info("--------------------------------------------");
      }
      File f = rf.getFontsFile("fonts.jar");
      if (f != null)
      {
         JarFile fontsFile = new JarFile(f);
         Enumeration<JarEntry> fonts = fontsFile.entries();
         while (fonts.hasMoreElements())
         {
            JarEntry entryF = fonts.nextElement();
            logger.info(entryF.getName());
         }
         fontsFile.close();
         logger.info("--------------------------------------------");
      }
   }

   @Override
   public void destroy()
   {
   }

}
