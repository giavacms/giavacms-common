/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.util;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class JNDIUtils
{

   /**
    * @param appName The app name is the application name of the deployed EJBs. This is typically the ear name without
    *           the .ear suffix. However, the application name could be overridden in the application.xml of the EJB
    *           deployment on the server. Since we haven't deployed the application as a .ear, the app name for us will
    *           be an empty string final
    * 
    * @param moduleName This is the module name of the deployed EJBs on the server. This is typically the jar name of
    *           the EJB deployment, without the .jar suffix, but can be overridden via the ejb-jar.xml In this example,
    *           we have deployed the EJBs in a jboss-as-ejb-remote-app.jar, so the module name is
    *           jboss-as-ejb-remote-app final String moduleName = "jboss-as-ejb-remote-app"; AS7 allows each deployment
    *           to have an (optional) distinct name. We haven't specified a distinct name for our EJB deployment, so
    *           this is an empty string
    * 
    * @param distinctName The EJB name which by default is the simple class name of the bean implementation class
    * 
    * @param beanName CalculatorBean.class.getSimpleName(); the remote view fully qualified class name
    * 
    * @param viewClassName RemoteCalculator.class.getName(); let's do the lookup
    * 
    * @return
    * 
    * @throws NamingException
    */

   /**
    * 
    * @param appName
    * @param moduleName
    * @param distinctName
    * @param beanName
    * @param viewClassName
    * @return
    * @throws NamingException
    */
   public static Object lookupStatelessEjb(String appName, String moduleName,
            String distinctName, String beanName, String viewClassName)
            throws NamingException
   {
      Hashtable<String, String> jndiProperties = new Hashtable<String, String>();
      jndiProperties.put(Context.URL_PKG_PREFIXES,
               "org.jboss.ejb.client.naming");

      jndiProperties
               .put("remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED",
                        "false");

      jndiProperties.put("remote.connections", "default");

      jndiProperties.put("remote.connection.default.host", "localhost");
      jndiProperties.put("remote.connection.default.port", "4447");
      jndiProperties
               .put("remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS",
                        "false");

      jndiProperties.put("remote.connection.two.host", "localhost");
      jndiProperties.put("remote.connection.two.port", "4447");
      jndiProperties
               .put("remote.connection.two.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS",
                        "false");

      Context context = new InitialContext(jndiProperties);
      return context.lookup("ejb:" + appName + "/" + moduleName + "/"
               + distinctName + "/" + beanName + "!" + viewClassName);
   }

}
