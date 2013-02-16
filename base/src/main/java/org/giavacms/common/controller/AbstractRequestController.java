/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.controller;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

import org.giavacms.common.annotation.BackPage;
import org.giavacms.common.annotation.EditPage;
import org.giavacms.common.annotation.ListPage;
import org.giavacms.common.annotation.OwnRepository;
import org.giavacms.common.annotation.PrintPage;
import org.giavacms.common.annotation.ViewPage;
import org.giavacms.common.renderer.UiRepeatInterface;
import org.giavacms.common.repository.Repository;
import org.giavacms.common.util.BeanUtils;
import org.giavacms.common.util.JSFUtils;
import org.jboss.logging.Logger;


public abstract class AbstractRequestController<T> implements Serializable,
         UiRepeatInterface<T>
{

   private static final long serialVersionUID = 1L;

   // ------------------------------------------------
   // --- Logger -------------------------------------
   // ------------------------------------------------

   protected final Logger logger = Logger.getLogger(getClass()
            .getCanonicalName());

   private int pageSize = 10;
   protected Object id;
   protected T element;
   protected Map<String, String> params;

   /**
    * Pagina di provenienza, settabile dall'esterno attraverso i corrispondenti metodi Possibile override per forzare
    * una determinata backpage tramite gli handler concreti che estendono questa classe
    */
   private String backPage = null;

   /**
    * Pagina per la vista elenco
    */
   private String listPage = null;

   /**
    * Pagina per la vista dettaglio
    */
   private String viewPage = null;

   /**
    * Pagina per la vista modifica
    */
   private String editPage = null;

   /**
    * Pagina per la stampa
    */
   private String printPage = null;

   /**
    * Repository per fare query su db
    */
   private Repository<T> repository;

   public AbstractRequestController()
   {
      this.injectTSessionAndPages();
      init();
   }

   // ------------------------------------------------
   // --- Costruttore interno ------------------------
   // ------------------------------------------------

   @SuppressWarnings("rawtypes")
   protected void injectTSessionAndPages()
   {
      //

      Field[] fields = getClass().getDeclaredFields();
      for (Field field : fields)
      {
         try
         {
            OwnRepository repository_anno = field
                     .getAnnotation(OwnRepository.class);
            PrintPage print_anno = field.getAnnotation(PrintPage.class);
            BackPage back_anno = field.getAnnotation(BackPage.class);
            ListPage list_anno = field.getAnnotation(ListPage.class);
            EditPage edit_anno = field.getAnnotation(EditPage.class);
            ViewPage view_anno = field.getAnnotation(ViewPage.class);

            if (print_anno != null)
            {
               field.setAccessible(true);
               Object page = field.get(null);
               this.printPage = "" + page;
            }
            if (back_anno != null)
            {
               field.setAccessible(true);
               Object page = field.get(null);
               this.backPage = "" + page;
            }
            if (list_anno != null)
            {
               field.setAccessible(true);
               Object page = field.get(null);
               this.listPage = "" + page;
            }
            if (edit_anno != null)
            {
               field.setAccessible(true);
               Object page = field.get(null);
               this.editPage = "" + page;
            }
            if (view_anno != null)
            {
               field.setAccessible(true);
               Object page = field.get(null);
               this.viewPage = "" + page;
            }
            try
            {
               if (repository_anno != null)
               {
                  Class clazz = repository_anno.value();
                  this.repository = (Repository<T>) BeanUtils
                           .getBean(clazz);
               }
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }

         }
         catch (IllegalArgumentException e)
         {
            logger.error(e.getMessage(), e);
         }
         catch (IllegalAccessException e)
         {
            logger.error(e.getMessage(), e);
         }
      }
   }

   @PostConstruct
   protected void init()
   {
      params = new HashMap<String, String>();
      for (String param : getParamNames())
      {
         Object p = JSFUtils.getParameter(param);
         params.put(param, p == null ? null : p.toString());
      }
      Object p = JSFUtils.getParameter(getIdParam());
      this.id = (p == null) ? null : p.toString();
      if (this.id != null)
      {
         this.element = this.repository.fetch(this.id);
      }
   }

   protected abstract String[] getParamNames();

   protected abstract String getIdParam();

   public Object getId()
   {
      return id;
   }

   public void setId(Object id)
   {
      this.id = id;
   }

   public T getElement()
   {
      return element;
   }

   public void setElement(T element)
   {
      this.element = element;
   }

   protected String getAppContext()
   {
      return "";
   }

   public Map<String, String> getParams()
   {
      return params;
   }

   public int getCurrentPage()
   {
      try
      {
         return Integer.parseInt(""
                  + JSFUtils.getParameter(getCurrentPageParam()));
      }
      catch (Exception e)
      {
         return 1;
      }
   }

   public int getPageSize()
   {
      return pageSize;
   }

   public void setPageSize(int pageSize)
   {
      this.pageSize = pageSize;
   }

   abstract protected List<T> loadPage(int startRow, int pageSize);

   public List<T> getPage()
   {
      return loadPage((getCurrentPage() - 1) * getPageSize(), getPageSize());
   }

   public List<T> getPageOfSize(int size)
   {
      setPageSize(size);
      return getPage();
   }

   /**
    * Pagina di provenienza, settabile dall'esterno (cioè da altri handler!) Possibile override per forzare una
    * determinata backpage tramite gli handler concreti che estendono questa classe
    */
   public void backPage(String backPage)
   {
      this.backPage = backPage;
   }

   public String backPage()
   {
      return this.backPage;
   }

   public String viewPage()
   {
      return viewPage;
   }

   public String listPage()
   {
      return listPage;
   }

   public String editPage()
   {
      return editPage;
   }

   public String printPage()
   {
      return printPage;
   }

   // commodities

   protected void addFacesMessage(String summary, String message)
   {
      addFacesMessage(null, summary, message, "");
   }

   protected void addFacesMessage(String summary)
   {
      addFacesMessage(null, summary, summary, "");
   }

   protected void addFacesMessage(Severity severity, String summary,
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