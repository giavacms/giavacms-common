/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.controller;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;

import org.giavacms.common.annotation.OwnRepository;
import org.giavacms.common.model.Search;
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
    * Entity class
    */
   private Class<T> entityClass;

   /**
    * Search object
    */
   protected Search<T> search;

   /**
    * Repository per fare query su db
    */
   private Repository<T> repository;

   @SuppressWarnings({ "rawtypes", "unchecked" })
   public AbstractRequestController()
   {
      this.entityClass = getClassType();
      // defaultCriteria();
      search = new Search(this.entityClass);
   }

   @PostConstruct
   public void postConstruct()
   {
      injectOwnRepository();
      initParameters();
   }

   // ------------------------------------------------
   // --- Costruttore interno ------------------------
   // ------------------------------------------------

   @SuppressWarnings({ "rawtypes", "unchecked" })
   protected void injectOwnRepository()
   {
      //

      Field[] fields = getClass().getDeclaredFields();
      for (Field field : fields)
      {
         try
         {
            OwnRepository repository_anno = field
                     .getAnnotation(OwnRepository.class);

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
         catch (Exception e)
         {
            logger.error(e.getMessage(), e);
         }
      }
   }

   protected void initParameters()
   {
      params = new HashMap<String, String>();
      for (String param : getParamNames())
      {
         Object p = JSFUtils.getParameter(param);
         params.put(param, p == null ? null : p.toString());
      }
   }

   protected Object getIdValue()
   {
      return JSFUtils.getParameter(getIdParam());
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
      if (this.element == null)
      {
         Object p = getIdValue();
         this.id = (p == null) ? null : p.toString();
         if (this.id != null)
         {
            this.element = this.repository.fetch(this.id);
         }
      }
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

   public Search<T> getSearch()
   {
      if (search == null)
      {
         search = new Search<T>(this.getClassType());
      }
      return search;
   }

   public void setSearch(Search<T> search)
   {
      this.search = search;
   }

   /**
    * @return
    */
   @SuppressWarnings({ "rawtypes", "unchecked" })
   private Class<T> getClassType()
   {
      Class clazz = getClass();
      while (!(clazz.getGenericSuperclass() instanceof ParameterizedType))
      {
         clazz = clazz.getSuperclass();
      }
      ParameterizedType parameterizedType = (ParameterizedType) clazz
               .getGenericSuperclass();
      // ParameterizedType parameterizedType = (ParameterizedType) getClass()
      // .getSuperclass().getGenericSuperclass();
      return (Class<T>) parameterizedType.getActualTypeArguments()[0];
   }

}