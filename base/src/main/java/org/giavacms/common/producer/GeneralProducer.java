/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.producer;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.giavacms.common.annotation.HttpParam;
import org.giavacms.common.annotation.RequestUri;

public class GeneralProducer implements Serializable
{

   private static final long serialVersionUID = 1L;

   @Produces
   @HttpParam
   public String getParamValue(InjectionPoint ip)
   {
      ServletRequest request = (ServletRequest) FacesContext
               .getCurrentInstance().getExternalContext().getRequest();
      String name = ip.getAnnotated().getAnnotation(HttpParam.class).value();
      if (name == null || "".equals(name.trim()))
         name = ip.getMember().getName();
      return request.getParameter(name);
   }

   @Produces
   @RequestScoped
   public FacesContext getFacesContext()
   {
      return FacesContext.getCurrentInstance();
   }

   @Produces
   @RequestUri
   public String getRequestUri()
   {
      String requestURI = ((HttpServletRequest) FacesContext.getCurrentInstance()
               .getExternalContext().getRequest()).getRequestURI();
      return requestURI;
   }

}
