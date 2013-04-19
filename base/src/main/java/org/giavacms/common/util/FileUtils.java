/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.jboss.logging.Logger;

/**
 * @author fiorenzo pizza
 * 
 */
public class FileUtils
{

   static Logger logger = Logger.getLogger(FileUtils.class.getCanonicalName());
   static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.

   public static String getAbsoluteConfigurationFilename(
            ClassLoader classLoader, String holdingResource,
            String relativeFilename)
   {
      return classLoader.getResource(holdingResource).getPath() + "/"
               + relativeFilename;
   }

   public static String getExtension(String filename)
   {
      if (filename == null)
      {
         return "";
      }
      int dotIndex = filename.indexOf(".");
      if (dotIndex < 0)
      {
         return "";
      }
      if (filename.length() == dotIndex + 1)
      {
         return "";
      }
      return filename.substring(filename.indexOf(".") + 1);
   }

   /**
    * Read and write a file using an explicit encoding. Removing the encoding from this code will simply cause the
    * system's default encoding to be used instead.
    */
   public static boolean writeTextFile(String fileName, String content,
            String encoding)
   {
      logger.debug("Writing text " + content + " to file named " + fileName
               + (encoding == null ? "" : (". Encoding: " + encoding)));
      Writer out = null;
      boolean result = false;
      try
      {
         if (encoding == null)
         {
            out = new OutputStreamWriter(new FileOutputStream(fileName));
         }
         else
         {
            out = new OutputStreamWriter(new FileOutputStream(fileName),
                     encoding);
         }
         out.write(content);
         result = true;
      }
      catch (Exception e)
      {
         logger.error(e.getMessage(), e);
      }
      finally
      {
         try
         {
            out.close();
         }
         catch (Exception e)
         {
         }
      }
      return result;
   }

   public static List<String> readLinesFromTextFile(String fileName,
            String encoding)
   {
      logger.debug("Reading from file named " + fileName);
      Scanner scanner = null;
      List<String> result = new ArrayList<String>();
      try
      {
         if (encoding == null)
         {
            scanner = new Scanner(new File(fileName));
         }
         else
         {
            scanner = new Scanner(new File(fileName), encoding);
         }
         while (scanner.hasNextLine())
         {
            result.add(scanner.nextLine());
         }
      }
      catch (Exception e)
      {
         logger.error(e.getMessage(), e);
      }
      finally
      {
         scanner.close();
      }
      return result;
   }

   public static byte[] getBytesFromFile(File file)
   {
      InputStream is = null;
      try
      {
         is = new FileInputStream(file);
         // Get the size of the file
         long length = file.length();
         if (length > Integer.MAX_VALUE)
         {
            // File is too large
            throw new IOException("File is too large: " + file.getName());
         }
         // Create the byte array to hold the data
         byte[] bytes = new byte[(int) length];
         // Read in the bytes
         int offset = 0;
         int numRead = 0;
         while (offset < bytes.length
                  && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)
         {
            offset += numRead;
         }
         // Ensure all the bytes have been read in
         if (offset < bytes.length)
         {
            throw new IOException("Could not completely read file "
                     + file.getName());
         }
         return bytes;
      }
      catch (Exception e)
      {
         logger.error(e.getMessage(), e);
         return null;
      }
      finally
      {
         if (is != null)
         {
            try
            {
               // Close the input stream in any case
               is.close();
            }
            catch (Exception e)
            {
            }
         }
      }
   }

   public static String cleanName(String fileName)
   {
      fileName = fileName.replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll(
               "[\\s]", "-");
      return fileName.toLowerCase();
   }

   public static String clean(String fileName)
   {
      String name = getLastPartOf(fileName);
      String prefix, ext;
      if (name.lastIndexOf(".") >= 0)
      {
         prefix = name.substring(0, name.lastIndexOf("."));
         ext = name.substring(name.lastIndexOf("."));
      }
      else
      {
         prefix = name;
         ext = "";
      }
      return cleanName(prefix) + ext;
   }

   public static String getLastPartOf(String absoluteFileName)
   {
      if (absoluteFileName == null)
         return "";
      if ("".equals(absoluteFileName))
         return "";
      if (absoluteFileName.contains("\\"))
         return absoluteFileName.substring(absoluteFileName
                  .lastIndexOf("\\") + 1);
      if (absoluteFileName.contains("/"))
         return absoluteFileName
                  .substring(absoluteFileName.lastIndexOf("/") + 1);
      return absoluteFileName;
   }

   public static String generateTempFolder()
   {
      String tmp = generateTempFolder(System.getProperty("java.io.tmpdir"));
      logger.info("generateTempFolder(): " + tmp);
      return System.getProperty("java.io.tmpdir") + "/" + tmp;
   }

   public static String generateTempFolder(String parent_folder)
   {
      Date data = new Date();
      String tmp = "" + data.getTime();
      File dir = new File(parent_folder + "/" + tmp);
      try
      {
         if (dir.mkdir())
         {
            logger.info("Directory Created: " + dir.getAbsolutePath());
            ShellUtils.executeCmd(new String[] { "/bin/chmod", "777",
                     dir.getAbsolutePath() });
            logger.info("Aggiorno i diritti");
         }
         else
            logger.info("Directory is not created");
      }
      catch (Exception e)
      {
         logger.error(e.getMessage(), e);
      }
      logger.info("generateTempFolder(): " + tmp);
      return tmp;
   }

   // /**************************************************
   // /***********FROM APACHE FILEUTILS************
   // /**************************************************

   public static boolean deleteQuietly(String abs_filename)
   {
      return deleteQuietly(new File(abs_filename));
   }

   public static boolean deleteQuietly(File file)
   {
      if (file == null)
      {
         return false;
      }
      try
      {
         if (file.isDirectory())
         {
            cleanDirectory(file);
         }
      }
      catch (Exception e)
      {
      }

      try
      {
         return file.delete();
      }
      catch (Exception e)
      {
         return false;
      }
   }

   public static void cleanDirectory(File directory) throws IOException
   {
      if (!directory.exists())
      {
         String message = directory + " does not exist";
         throw new IllegalArgumentException(message);
      }

      if (!directory.isDirectory())
      {
         String message = directory + " is not a directory";
         throw new IllegalArgumentException(message);
      }

      File[] files = directory.listFiles();
      if (files == null)
      { // null if security restricted
         throw new IOException("Failed to list contents of " + directory);
      }

      IOException exception = null;
      for (int i = 0; i < files.length; i++)
      {
         File file = files[i];
         try
         {
            forceDelete(file);
         }
         catch (IOException ioe)
         {
            exception = ioe;
         }
      }

      if (null != exception)
      {
         throw exception;
      }
   }

   public static void forceDelete(File file) throws IOException
   {
      if (file.isDirectory())
      {
         deleteDirectory(file);
      }
      else
      {
         boolean filePresent = file.exists();
         if (!file.delete())
         {
            if (!filePresent)
            {
               throw new FileNotFoundException("File does not exist: "
                        + file);
            }
            String message = "Unable to delete file: " + file;
            throw new IOException(message);
         }
      }
   }

   public static void deleteDirectory(File directory) throws IOException
   {
      if (!directory.exists())
      {
         return;
      }

      cleanDirectory(directory);
      if (!directory.delete())
      {
         String message = "Unable to delete directory " + directory + ".";
         throw new IOException(message);
      }
   }

   // /**************************************************
   // /**************************************************
   // /**************************************************

   public static byte[] getBytesFromUrl(URL url)
   {
      try
      {
         InputStream is = url.openStream();
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         byte buffer[] = new byte[1];
         while (is.read(buffer) != -1)
         {
            os.write(buffer);
         }
         return os.toByteArray();
      }
      catch (Exception e)
      {
         logger.error(e.getMessage(), e);
         return null;
      }
   }

   public static boolean writeBytesToFile(File file, byte[] bytes)
   {
      try
      {
         return writeBytesToOutputStream(new FileOutputStream(file), bytes);
      }
      catch (Exception e)
      {
         logger.error(e.getMessage(), e);
         return false;
      }
   }

   public static boolean writeBytesToOutputStream(OutputStream outputStream,
            byte[] bytes)
   {

      boolean result = false;

      BufferedInputStream input = null;
      BufferedOutputStream output = null;

      try
      {

         // Open file.
         input = new BufferedInputStream(new ByteArrayInputStream(bytes),
                  DEFAULT_BUFFER_SIZE);

         output = new BufferedOutputStream(outputStream, DEFAULT_BUFFER_SIZE);

         // Write file contents to response.
         byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
         int length;
         while ((length = input.read(buffer)) > 0)
         {
            output.write(buffer, 0, length);
         }

         // Finalize task.
         output.flush();
         result = true;

      }
      catch (Exception e)
      {
         logger.error(e.getMessage(), e);
      }
      finally
      {
         // Gently close streams.
         close(output);
         close(input);
      }
      return result;
   }

   public static void close(Closeable resource)
   {
      if (resource != null)
      {
         try
         {
            resource.close();
         }
         catch (IOException e)
         {
            logger.error(e.getMessage(), e);
         }
      }
   }

}
