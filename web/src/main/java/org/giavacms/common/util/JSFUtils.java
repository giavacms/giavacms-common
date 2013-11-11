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
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.giavacms.common.model.Search;
import org.giavacms.common.repository.Repository;
import org.primefaces.component.menuitem.MenuItem;
import org.primefaces.model.DefaultMenuModel;
import org.primefaces.model.MenuModel;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class JSFUtils
{

   static Logger logger = Logger.getLogger(JSFUtils.class.getName());

   @Deprecated
   /*
    * use BeanUtils.getBean
    */
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

   public static String getCurrentPage()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      HttpServletRequest httpRequest = (HttpServletRequest) fc
               .getExternalContext().getRequest();
      return httpRequest.getRequestURI();
   }

   public static String getPageId()
   {
      String pageId = getCurrentPage();
      if (pageId.contains("/"))
         pageId = pageId.substring(pageId.lastIndexOf("/") + 1);
      if (pageId.contains(".jsf"))
         pageId = pageId.substring(0, pageId.lastIndexOf(".jsf"));
      System.out.println("page id: " + pageId);
      return pageId;
   }

   public static String getContextPath()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      String cp = fc.getExternalContext().getRequestContextPath();
      return cp;
   }

   public static String getAbsolutePath()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      HttpServletRequest httpServletRequest = (HttpServletRequest) fc
               .getExternalContext().getRequest();
      String scheme = httpServletRequest.getScheme();
      String hostName = httpServletRequest.getServerName();
      int port = httpServletRequest.getServerPort();
      // Because this is rendered in a <div> layer, portlets for some reason
      // need the scheme://hostname:port part of the URL prepended.
      return scheme + "://" + hostName + ":" + port + getContextPath();
   }

   public static int count(Collection collection)
   {
      return collection == null ? 0 : collection.size();
   }

   /**
    * @param ricerca
    * @param ejb
    * @param idField il nome del campo del par il cui valore è da usare come selectItem.value
    * @param valueField il nome del campo del par il cui valore è da usare selectItem.label
    * @param emptyMessage messaggio da mettere in caso di no risultati: selectItem(null,"nessun entity trovato...")
    * @param labelMessage messaggio da mettere nel primo selectitem in caso di no-selezione:
    *           select(null,"scegli l'entity....")
    * @return
    */
   public static SelectItem[] setupItems(Search ricerca, Repository ejb,
            String idField, String valueField, String emptyMessage,
            String labelMessage)
   {
      Class ID_Class = null;
      Class VALUE_Class = null;
      Field ID_Field = null;
      Field VALUE_Field = null;

      ID_Class = ricerca.getObj().getClass();
      while (ID_Class != null)
      {
         try
         {
            ID_Field = ID_Class.getDeclaredField(idField);
            ID_Field.setAccessible(true);
            // esco dal ciclo
            break;
         }
         catch (Exception e)
         {
            // ciclo sui campi della superclasse ora che c'e' estensione
            ID_Class = ID_Class.getSuperclass();
         }
      }

      VALUE_Class = ricerca.getObj().getClass();
      while (VALUE_Class != null)
      {
         try
         {
            VALUE_Field = VALUE_Class.getDeclaredField(valueField);
            VALUE_Field.setAccessible(true);
            // esco dal ciclo
            break;
         }
         catch (Exception e)
         {
            VALUE_Class = VALUE_Class.getSuperclass();
         }
      }

      SelectItem[] selectItems = new SelectItem[1];
      selectItems[0] = new SelectItem(null, emptyMessage);
      List entities = ejb.getList(ricerca, 0, 0);
      if (entities != null && entities.size() > 0)
      {
         boolean allowNull = labelMessage != null && labelMessage.trim().length() > 0;
         selectItems = new SelectItem[entities.size() + (allowNull ? 1 : 0)];
         if (allowNull)
         {
            selectItems[0] = new SelectItem(null, labelMessage);
         }
         int i = (allowNull ? 1 : 0);
         for (Object o : entities)
         {
            try
            {
               selectItems[i] = new SelectItem(ID_Field.get(ID_Class.cast(o)), ""
                        + VALUE_Field.get(VALUE_Class.cast(o)));
               i++;
            }
            catch (Exception e)
            {
               logger.info(e.getClass().getCanonicalName() + " - " + e.getMessage());
            }
         }
      }
      return selectItems;
   }

   public static Object getManagedBean(String name)
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      if (fc == null)
      {
         logger.info("Faces Context Application NULL");
         return null;
      }
      return fc.getApplication().getELResolver()
               .getValue(fc.getELContext(), null, name);
      // return fc.getApplication().getVariableResolver().resolveVariable(fc,
      // name);

      // return
      // ((HttpSession)fc.getExternalContext().getSession(false)).getAttribute(name);
   }

   public static void redirect(String nameUrl) throws IOException
   {
      try
      {
         String url = getAbsolutePath() + nameUrl;
         FacesContext context = FacesContext.getCurrentInstance();
         try
         {
            context.getExternalContext().redirect(url);
            context.responseComplete();
         }
         catch (Exception e)
         {
            logger.info(e.getMessage());
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   public static Map getMap(String mapName, FacesContext fc)
   {
      // return (HashMap) fc.getApplication().getVariableResolver()
      // .resolveVariable(fc, mapName);
      return (HashMap) fc.getApplication().getELResolver()
               .getValue(fc.getELContext(), null, mapName);
   }

   public static List getArray(String name, FacesContext fc)
   {
      // return (java.util.ArrayList)
      // fc.getApplication().getVariableResolver()
      // .resolveVariable(fc, name);
      return (ArrayList) fc.getApplication().getELResolver()
               .getValue(fc.getELContext(), null, name);
   }

   public static Object getParameter(String name)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      return context.getExternalContext().getRequestParameterMap().get(name);
   }

   public static String getRemoteAddr()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      HttpServletRequest httpRequest = (HttpServletRequest) fc
               .getExternalContext().getRequest();
      return httpRequest.getRemoteAddr();
   }

   /**
    * Return the username from the context principal
    * 
    * @return the username or null if the principal is null
    */
   public static String getUserName()
   {
      FacesContext context = FacesContext.getCurrentInstance();
      HttpServletRequest req = (HttpServletRequest) context
               .getExternalContext().getRequest();
      Principal pr = req.getUserPrincipal();
      if (pr == null)
      {
         return null;
      }
      return pr.getName();
   }

   public static String getHostPort()
   {
      FacesContext fc = FacesContext.getCurrentInstance();
      HttpServletRequest httpServletRequest = (HttpServletRequest) fc
               .getExternalContext().getRequest();
      String scheme = httpServletRequest.getScheme();
      String hostName = httpServletRequest.getServerName();
      int port = httpServletRequest.getServerPort();

      return scheme + "://" + hostName + ":" + port + "/";
   }

   public static String breadcrumbs()
   {
      HttpServletRequest hsr = (HttpServletRequest) FacesContext
               .getCurrentInstance().getExternalContext().getRequest();
      String url = hsr.getRequestURL().toString();
      url = url.substring("http://".length());
      if (url.indexOf("/") >= 0)
         url = url.substring(url.indexOf("/") + 1);
      String[] crumbs = url.split("/");

      String base = "/";
      StringBuffer sb = new StringBuffer();
      for (int i = 0; i < crumbs.length; i++)
      {
         base += crumbs[i];
         String label = i == 0 ? "home" : crumbs[i];
         if (label.contains("."))
         {
            label = label.substring(0, label.indexOf("."));
            sb.append("<b>" + label + "</b>");
         }
         else
         {
            sb.append("<a href=\"" + base + "\" title=\"" + crumbs[i]
                     + "\">" + label + "</a> ");
            sb.append("<span style=\"color: black;\">&gt;</span> ");
         }
         base += "/";
      }
      return sb.toString();
   }

   public static MenuModel primeBreadcrumbs()
   {
      HttpServletRequest hsr = (HttpServletRequest) FacesContext
               .getCurrentInstance().getExternalContext().getRequest();
      String url = hsr.getRequestURL().toString();
      url = url.substring("http://".length());
      return primeBreadcrumbs(url);
   }

   public static void main2(String[] args)
   {
      primeBreadcrumbs("martina-pietraia.rhcloud.com/private/page/edit.jsf");
   }

   private static MenuModel primeBreadcrumbs(String url)
   {
      // logger.info("url: " + url);
      String contextPath = getContextPath().substring(
               getContextPath().indexOf("/") + 1);
      MenuModel model = new DefaultMenuModel();
      if (url.indexOf("/") >= 0)
         url = url.substring(url.indexOf("/") + 1);
      String[] crumbs = url.split("/");
      // logger.info("" + Arrays.asList(crumbs));
      // logger.info("context path: " + getContextPath());
      MenuItem item = null;
      String label = null;

      String base = null;
      // StringBuffer sb = new StringBuffer();
      for (int i = 0; i < crumbs.length; i++)
      {
         if (crumbs[i] == null || crumbs[i].trim().isEmpty())
         {
            continue;
         }
         if (crumbs[i].equals(contextPath))
            continue;
         // logger.info(i + ") " + crumbs[i] + " --> ");

         if (base == null)
         {
            item = new MenuItem();
            base = "/" + crumbs[i];
            label = "home";
            item.setValue(label);
            item.setUrl(base);
            model.addMenuItem(item);
            // logger.info(" |" + item.getValue() + " = " + item.getUrl());
            base += "/";
            continue;
         }

         // pezzi intermedi
         if (i != (crumbs.length - 1))
         {
            item = new MenuItem();
            base += crumbs[i];
            label = crumbs[i];
            // sb.append("<a href=\"" + base + "\" title=\"" + crumbs[i]
            // + "\">" + label + "</a> ");
            // sb.append("<span style=\"color: black;\">&gt;</span> ");
            item.setValue(crumbs[i]);
            item.setUrl(base);
            model.addMenuItem(item);
            // logger.info(" |" + item.getValue() + " = " + item.getUrl());
            base += "/";
         }

         // serve per l'ultimo pezzo del bcrumps: la pagina corrente
         else
         {
            item = new MenuItem();
            base += crumbs[i];
            label = crumbs[i].substring(0, crumbs[i].indexOf("."));
            // sb.append("<b>" + label + "</b>");
            item.setValue(label);
            item.setUrl("#");
            model.addMenuItem(item);
            // logger.info(" |" + item.getValue() + " = " + item.getUrl());
            base += "/";
         }

      }
      return model;
   }

   public static String shorten(String in, int max)
   {
      if (in == null)
         return "";
      if (in.length() < max)
         return in;
      return in.substring(0, max) + "...";
   }

   /**
    * @param role
    * @return
    */
   public static boolean isUserInRole(String role)
   {
      FacesContext context = FacesContext.getCurrentInstance();
      HttpServletRequest req = (HttpServletRequest) context
               .getExternalContext().getRequest();
      return req.isUserInRole(role);
   }

   public static String getWebRootPath(Class clazz)
   {
      String webRoot_WEBINF = getWebInfPath(clazz);
      String webRoot = webRoot_WEBINF.substring(0,
               webRoot_WEBINF.lastIndexOf("/"));
      return webRoot;
   }

   public static String getWebInfLibPath(Class clazz)
   {
      String webRoot_WEBINF = getWebInfPath(clazz);
      return webRoot_WEBINF + "/lib";
   }

   public static String getWebInfPath(Class clazz)
   {
      String webRoot_WEBINF_classes_it_slash = clazz.getClassLoader()
               .getResource("it").getPath().replaceAll("%5c", File.separator);
      String webRoot_WEBINF_classes_it = webRoot_WEBINF_classes_it_slash
               .substring(0, webRoot_WEBINF_classes_it_slash.lastIndexOf("/"));
      String webRoot_WEBINF_classes = webRoot_WEBINF_classes_it.substring(0,
               webRoot_WEBINF_classes_it.lastIndexOf("/"));
      String webRoot_WEBINF = webRoot_WEBINF_classes.substring(0,
               webRoot_WEBINF_classes.lastIndexOf("/"));
      return webRoot_WEBINF;
   }

   public static String getContextParam(String name)
   {
      try
      {
         return ((ServletContext) FacesContext.getCurrentInstance()
                  .getExternalContext().getContext()).getInitParameter(name);
      }
      catch (Exception e)
      {
         return null;
      }
   }

   /**
    * Verifica se l'elemento di cui è stato passato l'id come parametro ha degli errori di validazione.
    * 
    * @param clientId Id dell'elemento di cui verificare se ha degli errori di validazione.
    * 
    * @return Ritorne vero se e solo se l'elemento ha degli errori di validazione.
    * 
    */
   public static Boolean hasErrors(String clientId)
   {
      return FacesContext.getCurrentInstance().getMessages(clientId)
               .hasNext();
   }

   /**
    * Verifica se l'elemento il cui id è composto nel seguente modo: 'formId:elementId' ha degli errori di validazione.
    * 
    * @param elementId Id dell'elemento di cui verificare se ha degli errori di validazione.
    * 
    * @param formId Id del form che contiene l'elemento.
    * 
    * @return Ritorne vero se e solo se l'elemento ha degli errori di validazione.
    * 
    */
   public static Boolean hasErrors(String elementId, String formId)
   {
      return hasErrors(formId + ":" + elementId);
   }

   /**
    * Ritorna gli errori per un certo clientId
    * 
    * @param clientId
    * @return
    */
   public static List<FacesMessage> getErrors(String clientId)
   {

      List<FacesMessage> messages = new ArrayList<FacesMessage>();

      for (Iterator<FacesMessage> iterator = FacesContext
               .getCurrentInstance().getMessages(clientId); iterator.hasNext();)
      {
         messages.add(iterator.next());

      }

      return messages;
   }

   public static String getErrorMessage(String clientId)
   {

      Iterator<FacesMessage> iterator = FacesContext.getCurrentInstance()
               .getMessages(clientId);

      if (!iterator.hasNext())
      {
         return "";
      }

      StringBuffer sb = new StringBuffer();

      for (; iterator.hasNext();)
      {
         sb.append(iterator.next().getDetail());
         if (iterator.hasNext())
         {
            sb.append(", ");
         }
      }

      return sb.toString();
   }

   public static String getSessionId()
   {
      try
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         HttpServletRequest httpRequest = (HttpServletRequest) fc
                  .getExternalContext().getRequest();
         return httpRequest.getSession().getId();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return "";
      }
   }

   public static Map<String, String[]> getParameters()
   {
      try
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         HttpServletRequest httpRequest = (HttpServletRequest) fc
                  .getExternalContext().getRequest();
         return httpRequest.getParameterMap();
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return new HashMap<String, String[]>();
      }
   }

   public static Map<String, String[]> getQueryStringParameters()
   {
      Map<String, String[]> queryStringParameters = new HashMap<String, String[]>();
      try
      {
         FacesContext fc = FacesContext.getCurrentInstance();
         HttpServletRequest httpRequest = (HttpServletRequest) fc
                  .getExternalContext().getRequest();
         String queryString = httpRequest.getQueryString();
         if (queryString != null && !queryString.equals(""))
         {
            Map<String, String[]> allParameters = httpRequest
                     .getParameterMap();
            if (allParameters != null)
            {
               for (String p : allParameters.keySet())
               {
                  if (queryString.contains(p))
                  {
                     queryStringParameters.put(p, allParameters.get(p));
                  }
               }
            }
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
      return queryStringParameters;
   }

}
