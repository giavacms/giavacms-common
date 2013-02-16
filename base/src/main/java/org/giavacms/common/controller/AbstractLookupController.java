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
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;

import org.giavacms.common.annotation.OwnRepository;
import org.giavacms.common.model.LocalDataModel;
import org.giavacms.common.model.Search;
import org.giavacms.common.repository.Repository;
import org.giavacms.common.util.JSFUtils;
import org.giavacms.common.util.StringUtils;
import org.jboss.logging.Logger;


/**
 * 
 * @param <T>
 */
public abstract class AbstractLookupController<T> implements Serializable
{

   private static final long serialVersionUID = 1L;

   // ------------------------------------------------
   // --- Logger -------------------------------------
   // ------------------------------------------------

   protected final Logger logger = Logger.getLogger(getClass()
            .getCanonicalName());

   /**
    * Entity class
    */
   private Class<T> entityClass;

   /**
    * Search object
    */
   protected Search<T> search;

   // ------------------------------------------------
   // --- Stato dell'handler -------------------------
   // ------------------------------------------------

   private static final int PAGE_SIZE = 10;
   private static final int ROWS_PER_PAGE = 10;
   private static final int SCROLLER_PAGE = 1;

   private int rowCount;
   private int pageSize = PAGE_SIZE;
   private int rowsPerPage = ROWS_PER_PAGE;
   private int scrollerPage = SCROLLER_PAGE;

   protected static final String REDIRECT_PARAM = "?faces-redirect=true";

   /**
    * Risultato della selezione, del caricamento diretto o della crezione di un nuovo entity
    */
   private T element;

   /**
    * Risultato della ricerca
    */
   private DataModel<T> model;

   /**
    * Repository per fare query su db
    */
   private Repository<T> repository;

   /**
    * Questo flag indica se il modal panel è aperto e allora solo in quel caso il modello deve essere fetch-ato dal db.
    */
   private boolean open = false;

   // ------------------------------------------------
   // --- Costruttore interno ------------------------
   // ------------------------------------------------

   /**
    * Costruttore con parametri da invocare obbligatoriamente nel costruttore senza argomenti dei sotto-handler:
    * inizializza i parametri di ricerca e colleziona eventuali vincoli session-wide come quelli che discendono dalla
    * identita' dell'utente loggato
    */

   /**
	 * 
	 */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public AbstractLookupController()
   {
      this.entityClass = getClassType();
      // defaultCriteria();
      search = new Search(this.entityClass);

   }

   @SuppressWarnings({ "rawtypes", "unchecked" })
   private void injectRepositoryAndPages()
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
                  field.setAccessible(true);
                  Object repositoryObj = field.get(null);
                  this.repository = (Repository) repositoryObj;
               }
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }

         }
         catch (IllegalArgumentException e)
         {
            logger.info(e.getMessage());
         }
      }
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

   // ------------------------------------------------
   // --- getter/setter-------------------------------
   // ------------------------------------------------

   /**
    * @return
    */
   public Search<T> getSearch()
   {
      return this.search;
   }

   /**
    * @return
    */
   public DataModel<T> getModel()
   {
      if (model == null)
         refreshModel();
      return model;
   }

   /**
    * @param model
    */
   public void setModel(DataModel<T> model)
   {
      this.model = model;
   }

   /**
    * @return
    */
   public T getElement()
   {
      return element;
   }

   /**
    * @param element
    */
   public void setElement(T element)
   {
      this.element = element;
   }

   /**
    * @return
    */
   public int getRowCount()
   {
      return rowCount;
   }

   /**
    * @param rowCount
    */
   public void setRowCount(int rowCount)
   {
      this.rowCount = rowCount;
   }

   /**
    * @return
    */
   public int getPageSize()
   {
      return pageSize;
   }

   /**
    * @param pageSize
    */
   public void setPageSize(int pageSize)
   {
      this.pageSize = pageSize;
   }

   /**
    * @return
    */
   public int getRowsPerPage()
   {
      return rowsPerPage;
   }

   /**
    * @param rowsPerPage
    */
   public void setRowsPerPage(int rowsPerPage)
   {
      this.rowsPerPage = rowsPerPage;
   }

   /**
    * @return
    */
   public int getScrollerPage()
   {
      return scrollerPage;
   }

   /**
    * @param scrollerPage
    */
   public void setScrollerPage(int scrollerPage)
   {
      this.scrollerPage = scrollerPage;
   }

   /**
    * Metodo per ottenere l'id di ricerca (il nome del campo non è noto a priori e uguale per tutti gli entity... almeno
    * finché non introduciamo interfacce a questo scopo... ;) )
    * 
    * Serve per poter fornire una implementazione a default del metodo refreshModel(), senza doverlo riscrivere in ogni
    * sotto-handler, come invece fatto in passato
    */
   public Object getId(T t)
   {
      try
      {
         return t.getClass().getDeclaredField("id").get(t);
      }
      catch (Exception e)
      {
         e.printStackTrace();
         return null;
      }
   }

   /**
    * Metodo per ottenere l'ejb generico che effettua la ricerca
    * 
    * Serve per poter fornire una implementazione a default del metodo refreshModel(), senza doverlo riscrivere in ogni
    * sotto-handler, come invece fatto in passato
    * 
    * @return
    */
   public Repository<T> getRepository()
   {
      return repository;
   }

   public boolean isOpen()
   {
      return open;
   }

   public void setOpen(boolean open)
   {
      this.open = open;
   }

   // ============================================================================================
   // === LOGICA DI GESTIONE DELLO STATO INTERNO DELL'HANDLER
   // ============================================================================================

   /**
    * Metodo interno (protected) che carica il risultato della ricerca corrente. Possibile override da parte delle
    * sottoclassi per aggiungere listener e/o effettuare operazioni aggiuntive specifiche del particolare sotto-handler
    */
   public void refreshModel()
   {

      if (!isOpen())
      {
         setModel(new ListDataModel<T>(new ArrayList<T>()));
      }
      else
      {
         setModel(new LocalDataModel<T>(pageSize, search, getRepository()));
      }
   }

   /**
    * Metodo interno (protected) da overriddare per assicurare che i criteri di ricerca contengano sempre tutti i
    * vincoli desiderati (es: identità utente, selezioni esterne, oggetti figli non nulli, ecc...)
    * 
    * Qui ne viene fornita una implementazione di default che non fa nulla, per evitare di scrivere un metodo vuoto nei
    * sotto-handler in caso non ce ne sia bisogno
    */
   @PostConstruct
   public void defaultCriteria()
   {
      injectRepositoryAndPages();

   }

   /**
    * Implements reset specific operations
    */
   protected void preReset()
   {
      this.scrollerPage = 1;
      this.model = null;
      search = new Search<T>(this.entityClass);
      defaultCriteria();
   }

   /**
    * Metodo per forzare dall'esterno la pulizia dell'handler
    * 
    * Possibili override da parte dei sotto-handler per resettare ulteriori campi non previsti dall'handler generico o
    * per modificare la vista di destinazione ottenuta come outcome
    */
   public String reset()
   {
      preReset();
      return "";
   }

   /**
    * Implements reload specific operations
    */
   protected void preReload()
   {
      this.model = null;
   }

   /**
    * Metodo per forzare l'aggiornamento del risultato della ricerca dell'handler, SENZA PERDERE I CRITERI DI RICERCA
    * CORRENTI!!!
    * 
    * Possibili override da parte dei sotto-handler per resettare ulteriori campi non previsti dall'handler generico o
    * per modificare la vista di destinazione ottenuta come outcome
    */
   public String reload()
   {
      preReload();
      getOrderByParameter();
      return "";
   }

   /**
    * Metodo per forzare dall'esterno la pulizia dell'handler in modo trasparente all'utente (es: value di un
    * outputText), in modo da ripulire eventuali inconsistenze dovute all'uso del pulsante back del browser in luogo del
    * pulsante 'indietro' o 'annulla' delle pagine
    */
   public boolean getClear()
   {
      reset();
      return true;
   }

   // ============================================================================================
   // === LOGICA DI NAVIGAZIONE
   // ==================================================================
   // ============================================================================================

   /*
    * Questi metodi non iniziano per get perché sono da usare nelle action dei componenti ui (method binding
    * expression), non come valori (value binding expression... get/set.. dot notation... you know...)
    */

   public void getOrderByParameter()
   {
      String orderBy = (String) JSFUtils.getParameter("orderBy");
      if (!StringUtils.isEmpty(orderBy))
      {
         search.setOrder(orderBy);
      }
   }

   public void closePanel(AjaxBehaviorEvent ajaxBehaviorEvent)
   {
      reset();
      this.setOpen(false);
   }

   public String select(ActionEvent actionEvent)
   {
      if (model == null || !model.isRowAvailable())
      {
         return "";
      }

      T rowData = model.getRowData();

      if (rowData != null)
      {
         setElement(rowData);
      }

      return "";

   }

   public void selectAndClose(ActionEvent actionEvent)
   {
      if (model == null || !model.isRowAvailable())
      {
         return;
      }

      T rowData = model.getRowData();

      if (rowData != null)
      {
         setElement(rowData);
      }

      this.model = null;
      this.setOpen(false);
   }

   public void openPanel(ActionEvent actionEvent)
   {
      reset();
      setOpen(true);
      refreshModel();
   }

}