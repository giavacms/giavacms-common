/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.producer;

import java.io.Serializable;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.ServletRequest;

import org.giavacms.common.annotation.HttpParam;


@Named
public class GeneralProducer implements Serializable
{

   private static final long serialVersionUID = 1L;

   @Produces
   @HttpParam("")
   String getParamValue(InjectionPoint ip)
   {
      ServletRequest request = (ServletRequest) FacesContext
               .getCurrentInstance().getExternalContext().getRequest();
      return request.getParameter(ip.getAnnotated()
               .getAnnotation(HttpParam.class).value());

   }

}
