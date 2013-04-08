/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.persistence.Id;

import org.jboss.logging.Logger;

/**
 * Metodi di utilità per la reflection
 */
public class ReflectionUtils
{

   static Logger logger = Logger.getLogger(ReflectionUtils.class);

   /**
    * Trova il valore del campo annotato con {@code @Id}
    * 
    * @param <U>
    * @param clazz
    * @param u
    * @return
    */
   public static <U> Object findIdFieldValue(Class<?> clazz, U u)
   {
      Field[] fields = clazz.getDeclaredFields();
      for (Field field : fields)
      {
         Id id_anno = field.getAnnotation(Id.class);
         if (id_anno != null)
         {
            try
            {
               field.setAccessible(true);
               return field.get(u);
            }
            catch (Exception e)
            {
               return null;
            }
         }
      }

      // cerco anche nella classe padre se ce n'è una
      if (clazz.getGenericSuperclass() != null)
      {
         return findIdFieldValue(clazz.getSuperclass(), u);
      }

      return null;
   }

   /**
    * List directory contents for a resource folder. Not recursive. This is basically a brute-force implementation.
    * Works for regular files and also JARs.
    * 
    * @author Greg Briggs
    * @param clazz Any java class that lives in the same place as the resources you want.
    * @param path Should end with "/", but not start with one.
    * @return Just the name of each member item, not the full paths.
    * @throws URISyntaxException
    * @throws IOException
    */
   @SuppressWarnings("rawtypes")
   public static String[] getResourceListing(Class clazz, String path)
   {
      try
      {
         URL dirURL = clazz.getClassLoader().getResource(path);
         if (dirURL != null && dirURL.getProtocol().equals("file"))
         {
            /* A file path: easy enough */
            return new File(dirURL.toURI()).list();
         }

         if (dirURL == null)
         {
            /*
             * In case of a jar file, we can't actually find a directory. Have to assume the same jar as clazz.
             */
            String me = clazz.getName().replace(".", "/") + ".class";
            dirURL = clazz.getClassLoader().getResource(me);
         }

         if (dirURL.getProtocol().equals("jar"))
         {
            /* A JAR path */
            String jarPath = dirURL.getPath().substring(5,
                     dirURL.getPath().indexOf("!")); // strip out only the
            // JAR file
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries(); // gives ALL
            // entries in
            // jar
            Set<String> result = new HashSet<String>(); // avoid duplicates
            // in case it is a
            // subdirectory
            while (entries.hasMoreElements())
            {
               String name = entries.nextElement().getName();
               if (name.startsWith(path))
               { // filter according to the path
                  String entry = name.substring(path.length());
                  int checkSubdir = entry.indexOf("/");
                  if (checkSubdir >= 0)
                  {
                     // if it is a subdirectory, we just return the
                     // directory name
                     entry = entry.substring(0, checkSubdir);
                  }
                  result.add(entry);
               }
            }
            return result.toArray(new String[result.size()]);
         }

      }
      catch (Exception e)
      {
         logger.error(e.getMessage(), e);
      }
      return new String[] {};
   }
}
