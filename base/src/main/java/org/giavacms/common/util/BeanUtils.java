/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.util;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.Context;
import javax.naming.InitialContext;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BeanUtils
{

   public static <T> T getBean(Class<T> beanClass)
   {
      try
      {
         Context initCtx = new InitialContext();
         Context envCtx = (Context) initCtx.lookup("java:comp/");
         BeanManager beanManager = (BeanManager) envCtx
                  .lookup("BeanManager");

         Bean phBean = (Bean) beanManager.getBeans(beanClass).iterator()
                  .next();
         CreationalContext cc = beanManager.createCreationalContext(phBean);
         T bean = (T) beanManager.getReference(phBean, beanClass, cc);
         return bean;
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

}
