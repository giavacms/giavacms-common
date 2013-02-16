/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.util;

import org.jboss.logging.Logger;

public class PrintUtils
{

   static Logger logger = Logger
            .getLogger(ImageUtils.class.getCanonicalName());

   // =======================================================================================

   public static double margin(double base, double offset, String testo,
            double mmPerRiga)
   {
      long count = 0;
      int lastIndex = 0;
      double coefficiente = 1.0;
      if (testo.indexOf("large") != -1)
         coefficiente = 1.1;
      if (testo.indexOf("x-large") != -1)
         coefficiente = 1.2;
      if (testo.indexOf("xx-large") != -1)
         coefficiente = 1.3;
      while (lastIndex != -1)
      {
         lastIndex = testo.indexOf("<br />", lastIndex + 1);
         if (lastIndex != -1)
         {
            count++;
         }
      }
      lastIndex = 0;
      while (lastIndex != -1)
      {
         lastIndex = testo.indexOf("</p>", lastIndex + 1);
         if (lastIndex != -1)
         {
            count++;
         }
      }
      double margin = base + offset + mmPerRiga * count * coefficiente;
      double minimum = base + offset;

      logger.debug("base = " + base);
      logger.debug("offset = " + offset);
      logger.debug("count = " + count);
      logger.debug("mmPerRiga = " + mmPerRiga);
      logger.debug("coefficiente = " + coefficiente);
      logger.debug("minimum = " + minimum);
      logger.debug("margin = " + margin);

      return margin < minimum ? minimum : margin;
   }

}
