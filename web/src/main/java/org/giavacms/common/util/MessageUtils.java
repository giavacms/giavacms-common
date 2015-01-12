/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.util;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

public class MessageUtils
{

   public static void addFacesMessage(String summary, String message)
   {
      addFacesMessage(null, summary, message, "");
   }

   public static void addFacesMessage(Severity severity, String summary,
            String message, String forComponentId)
   {
      FacesMessage fm = new FacesMessage(message);
      fm.setSummary(summary);
      if (severity != null)
      {
         fm.setSeverity(severity);
      }
      else
      {
         fm.setSeverity(FacesMessage.SEVERITY_ERROR);
      }
      FacesContext.getCurrentInstance().addMessage(forComponentId, fm);

   }
}
