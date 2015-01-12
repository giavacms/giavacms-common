/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.jboss.logging.Logger;

public class DateUtils
{

   static Logger logger = Logger.getLogger(DateUtils.class);
   static DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");

   public static Date toBeginOfDay(Date date)
   {
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      cal.set(Calendar.HOUR_OF_DAY, 0);
      cal.set(Calendar.MINUTE, 0);
      cal.set(Calendar.SECOND, 0);
      cal.set(Calendar.MILLISECOND, 0);
      logger.debug(dateFormat.format(date) + " -- toBeginOfDay --> " + cal.getTime());
      return cal.getTime();
   }

   public static Date toEndOfDay(Date date)
   {
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      cal.set(Calendar.HOUR_OF_DAY, 23);
      cal.set(Calendar.MINUTE, 59);
      cal.set(Calendar.SECOND, 59);
      cal.set(Calendar.MILLISECOND, 999);
      logger.debug(dateFormat.format(date) + " -- toEndOfDay ----> " + cal.getTime());
      return cal.getTime();
   }

   public static Date getLinuxDate()
   {
      try
      {
         String data = ShellUtils.executeCmd(new String[] { "date",
                  "+%m/%d/%Y %H:%M:%S" });
         logger.debug("DATA LINUX: " + data);
         SimpleDateFormat formatLINUX = new SimpleDateFormat(
                  "MM/dd/yyyy HH:mm:ss");
         Date parsed1 = formatLINUX.parse(data);
         logger.debug("linux date: " + dateFormat.format(parsed1));
         return parsed1;
      }
      catch (ParseException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
         return null;
      }
   }

}
