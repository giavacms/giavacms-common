/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.controller;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.giavacms.common.module.ModuleProvider;
import org.giavacms.common.module.ModuleRegistry;
import org.giavacms.common.util.BeanUtils;
import org.giavacms.common.util.FileUtils;
import org.giavacms.common.util.JSFUtils;
import org.jboss.logging.Logger;


public abstract class AbstractPermissionController
{

   @Inject
   // protected Logger logger =
   // Logger.getLogger(getClass().getCanonicalName());
   protected Logger logger;

   // - VARIABILI MEMBRO
   // ---------------------------------------------------------------

   /**
    * variabile per discriminare comportamenti che non devono succedere in produzione
    * 
    * inoltre
    * 
    * if ( development == true ) un alias uguale al nome del ruolo dà accesso al ruolo stesso
    * 
    * development deve essere passato come context param in web.xml
    */
   private Boolean development = null;

   /**
    * url dove redirigere accessi non autorizzati
    * 
    * redirectUrl deve essere passato come context param in web.xml
    */
   private String redirectUrl = null;

   /**
    * percorso sotto META-INF dove trovare il file della ACL
    * 
    * aclPath deve essere passato come context param in web.xml
    */
   private String aclPath = null;

   /**
    * file nel percorso sotto META-INF dove trovare la ACL
    * 
    * aclFile deve essere passato come context param in web.xml
    */
   private String aclFile = null;

   /**
    * questa mappa viene caricata dalla classe che estende PermController in ciascun progetto e che è configurata come
    * bean jsf con scope di session
    */
   protected Map<String, List<String>> permissions = null;

   // - GETTER/SETTER E INIZIALIZZAZIONE VARIABILI MEMBRO
   // ------------------------------------------------------------------------

   public boolean isDevelopment()
   {
      return getDevelopment();
   }

   public boolean getDevelopment()
   {
      if (development == null)
      {
         try
         {
            development = Boolean.parseBoolean(JSFUtils
                     .getContextParam("development"));
         }
         catch (Exception e)
         {
            development = false;
         }
      }
      return development;
   }

   public String getRedirectUrl()
   {
      if (redirectUrl == null)
      {
         redirectUrl = JSFUtils.getContextParam("redirectUrl");
      }
      return redirectUrl == null ? "error.jsp" : redirectUrl;
   }

   public Map<String, List<String>> getPermissions()
   {
      if (permissions == null)
         initPermissions();
      return permissions;
   }

   public String getAclPath()
   {
      if (aclPath == null)
      {
         try
         {
            aclPath = (JSFUtils.getContextParam("aclPath"));
         }
         catch (Exception e)
         {
            logger.error(e.getMessage(), e);
         }
      }
      return aclPath;
   }

   public String getAclFile()
   {
      if (aclFile == null)
      {
         try
         {
            aclFile = JSFUtils.getContextParam("aclFile");
         }
         catch (Exception e)
         {
            logger.error(e.getMessage(), e);
         }
      }
      return aclFile;
   }

   public boolean checkACL(List<String> roles)
   {
      if (roles == null)
         return false;
      for (String role : roles)
      {
         if (isUserInRole(role))
         {
            return true;
         }
      }
      // ultima chance...
      if (getDevelopment())
      {
         for (String role : roles)
            if (getLoginAlias() != null && getLoginAlias().equals(role))
               return true;
      }
      return false;
   }

   // - METODI CHE E' POSSIBILE SOVRASCRIVERE E/O IMPLEMENTARE IN UN BEAN JSF
   // CON SCOPE DI SESSIONE CHE ESTENDE QUESTA CLASSE -------------

   /**
    * l'implementazione di questo metodo permette di valorizzare la mappa, ad esempio leggendo l'ACL da file
    */
   public void initPermissions()
   {
      permissions = new HashMap<String, List<String>>();
      Properties p = new Properties();
      try
      {
         p.load(new FileInputStream(FileUtils
                  .getAbsoluteConfigurationFilename(getClass()
                           .getClassLoader(), getAclPath(), getAclFile())));
         for (Object o : p.keySet())
         {
            // permissions.put(o.toString(), Arrays.asList(
            // p.getProperty(o.toString()).split(",") ) );
            List<String> acl = new ArrayList<String>();
            for (String ac : p.getProperty(o.toString()).split(","))
            {
               acl.add(ac.trim());
            }
            permissions.put(o.toString(), acl);
         }
      }
      catch (Exception e)
      {
         logger.warn("Error loading permissions: " + e.getMessage());
      }
      try
      {
         ModuleRegistry appRegistry = BeanUtils.getBean(ModuleRegistry.class);
         if (appRegistry != null)
         {
            for (ModuleProvider module : appRegistry.getModules())
            {
               Map<String, String> map = module.getPermissions();
               for (String o : map.keySet())
               {
                  List<String> acl = new ArrayList<String>();
                  for (String ac : map.get(o).split(","))
                  {
                     acl.add(ac.trim());
                  }
                  permissions.put(o.toString(), acl);
               }
            }
         }
      }
      catch (Exception e)
      {
         logger.warn("Error loading module permissions: " + e.getMessage());
      }
   }

   /**
    * La sovrascrittura di questo metodo permette di risolvere applicazione per applicazione l'esatto oggetto dove è
    * mantenuto
    * 
    * (es: loginController.getReferente().getAlias() )
    * 
    */
   public String getLoginAlias()
   {
      HttpServletRequest req = (HttpServletRequest) FacesContext
               .getCurrentInstance().getExternalContext().getRequest();
      return req.getUserPrincipal().getName();
   }

   /**
    * La sovrascrittura di questo metodo permette di risolvere applicazione per applicazione l'esatto oggetto dove è
    * mantenuto
    * 
    * (es: loginController.getReferente().getRoles().contains(role) )
    * 
    */
   public boolean isUserInRole(String role)
   {
      HttpServletRequest req = (HttpServletRequest) FacesContext
               .getCurrentInstance().getExternalContext().getRequest();
      return req.isUserInRole(role);
   }

   // -----------------------------------------------------------------------------------------------------

   public void checkRoles(ComponentSystemEvent event)
   {

      String acl = "" + event.getComponent().getAttributes().get("roles");

      for (String a : acl.split(","))
      {
         if (checkACL(getPermissions().get(a.trim())))
            return;
      }
      try
      {
         // ExternalContext extCtx = FacesContext.getCurrentInstance()
         // .getExternalContext();
         // extCtx.redirect(extCtx.encodeActionURL(JSFUtils.getAbsolutePath()
         // + "/" + getRedirectUrl()));
         FacesContext context = FacesContext.getCurrentInstance();
         ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) context
                  .getApplication().getNavigationHandler();
         handler.performNavigation("forbidden");
      }
      catch (Exception e)
      {
         e.printStackTrace();
         // Se siamo qui il redirect è fallito.
         // A questo punto, piuttosto che lasciare andare l'utente dove
         // non deve.. runtime exception!
         throw new RuntimeException("Accesso non consentito");
      }
   }

   public String reloadPermissions()
   {
      initPermissions();
      return null;
   }

}
