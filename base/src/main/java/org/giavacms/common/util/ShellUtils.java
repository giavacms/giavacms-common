/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.jboss.logging.Logger;

public class ShellUtils
{

   static Logger log = Logger.getLogger(ShellUtils.class.getName());

   public static String executeCmd(String[] cmd)
   {
      String outputString = "";
      String errorString = "";
      Runtime rt = Runtime.getRuntime();
      Process proc = null;
      try
      {
         proc = rt.exec(cmd);
         int exitVal = proc.waitFor();
         log.debug("processo terminato: " + exitVal);
         InputStream stdin = proc.getInputStream();
         InputStream stder = proc.getErrorStream();
         outputString = stream2string(stdin);
         errorString = stream2string(stder);
         // log.info("Process exitValue: " + exitVal);
         if (outputString.compareTo("") == 0)
         {
            return errorString;
         }
         return outputString;
      }
      catch (Exception e)
      {
         // TODO Auto-generated catch block
         log.info("terza ecc: esecuzione wait");
         e.printStackTrace();
      }
      finally
      {
         if (proc != null)
         {
            close(proc.getOutputStream());
            close(proc.getInputStream());
            close(proc.getErrorStream());
            proc.destroy();
         }
      }
      return "";

   }

   private static String stream2string(InputStream stream) throws IOException
   {
      String lines = "0";

      while (stream.available() != 0)
      {
         byte[] b = new byte[stream.available()];
         stream.read(b);
         // log.info("dimensione b: " + b.length + " " + b.toString());
         lines = new String(b);
      }
      return lines;

   }

   public static boolean createCommandFile(String fileExec, String[] commands)
   {
      // log.info("createCommandFile: " + fileExec);
      File execCmd = new File(fileExec);
      if (!execCmd.exists())
      {
         try
         {
            // log.info("execCmd:NON ESISTE LO CREO!");
            execCmd.createNewFile();
         }
         catch (IOException e)
         {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
         }
      }
      else
      {
         // log.info("execCmd: ESISTE LO DISTRUGGO E LO RICREO!");
         execCmd.delete();
         execCmd = new File(fileExec);
      }

      PrintWriter outputStream = null;
      try
      {
         outputStream = new PrintWriter(new FileWriter(fileExec));
         for (int i = 0; i < commands.length; i++)
         {
            outputStream.println(commands[i] + "\n");
            log.info("Comando: " + commands[i]);
         }
         outputStream.close();
      }
      catch (IOException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         close(outputStream);
         return false;
      }
      finally
      {
         close(outputStream);
      }
      return true;
   }

   private static void close(Closeable c)
   {
      if (c != null)
      {
         try
         {
            c.close();
         }
         catch (IOException e)
         {
            // ignored
         }
      }
   }

}
