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
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import javax.faces.model.DataModel;

import org.giavacms.common.annotation.BackPage;
import org.giavacms.common.annotation.EditPage;
import org.giavacms.common.annotation.ListPage;
import org.giavacms.common.annotation.OwnRepository;
import org.giavacms.common.annotation.PrintPage;
import org.giavacms.common.annotation.ViewPage;
import org.giavacms.common.model.LocalDataModel;
import org.giavacms.common.model.Search;
import org.giavacms.common.repository.Repository;
import org.giavacms.common.util.BeanUtils;
import org.giavacms.common.util.JSFUtils;
import org.giavacms.common.util.StringUtils;
import org.jboss.logging.Logger;


/**
 * 
 * @param <T>
 */
public abstract class AbstractController<T> implements Serializable
{

   private static final long serialVersionUID = 1L;

   // ------------------------------------------------
   // --- Logger -------------------------------------
   // ------------------------------------------------

   protected final Logger logger = Logger.getLogger(getClass().getCanonicalName());

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

   public static final String REDIRECT_PARAM = "?faces-redirect=true";

   /**
    * Risultato della ricerca
    */
   private DataModel<T> model;

   /**
    * Risultato della selezione, del caricamento diretto o della crezione di un nuovo entity
    */
   private T element;

   /**
    * Risultato multiplo della selezione
    */
   private List<T> elements;

   /**
    * Riga attualmente selezionata per le modifiche inline.
    */
   private T rowElement;

   /**
    * Stato della pagina di gestione
    */
   private Boolean editMode;

   /**
    * Toggle dei campi in lettura/scrittura per gestire casi di una stessa pagina in cui un utente può solo leggere
    * alcuni campi, mentre un altro li può modificare e salvare (ad esempio quando per entity molto semplici non ci sono
    * una pagina di gestione e una di visualizzazione, ma una sola pagina e alcuni utenti non devono poter editare i
    * dati)
    */
   private boolean readOnlyMode;

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

   /**
    * Variabile booleana di default a true che, se settata a false nel default criteria della classe che implementa,
    * permette di non caricare la lista prima di avere opportuni parametri di ricerca (ad esempio in caso di query
    * pesanti)
    */
   private boolean loaded = true;

   // ------------------------------------------------
   // --- Costruttore interno ------------------------
   // ------------------------------------------------

   /**
    * Costruttore con parametri da invocare obbligatoriamente nel costruttore senza argomenti dei sotto-handler:
    * inizializza i parametri di ricerca e colleziona eventuali vincoli session-wide come quelli che discendono dalla
    * identita' dell'utente loggato
    */
   // @SuppressWarnings({ "unchecked", "rawtypes" })
   // public AbstractController(Class<T> clazz) {
   // this.entityClass = clazz;
   // search = new Search(clazz);
   // // defaultCriteria();
   // }

   /**
	 * 
	 */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public AbstractController()
   {
      this.entityClass = getClassType();
      // defaultCriteria();
      search = new Search(this.entityClass);

   }

   @SuppressWarnings("unused")
   @PostConstruct
   private void init()
   {
      injectRepositoryAndPages();
      initController();
      defaultCriteria();
   }

   /**
    * Metodo per inizializzare i controller
    */
   public void initController()
   {

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
            PrintPage print_anno = field.getAnnotation(PrintPage.class);
            BackPage back_anno = field.getAnnotation(BackPage.class);
            ListPage list_anno = field.getAnnotation(ListPage.class);
            EditPage edit_anno = field.getAnnotation(EditPage.class);
            ViewPage view_anno = field.getAnnotation(ViewPage.class);
            OwnRepository repository_anno = field
                     .getAnnotation(OwnRepository.class);

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
                  this.repository = (Repository<T>) BeanUtils.getBean(clazz);
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
         catch (IllegalAccessException e)
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
   public List<T> getElements()
   {
      return elements;
   }

   /**
    * @param elements
    */
   public void setElements(List<T> elements)
   {
      this.elements = elements;
   }

   public T getRowElement()
   {
      return rowElement;
   }

   public void setRowElement(T rowElement)
   {
      this.rowElement = rowElement;
   }

   /**
    * @return
    */
   public boolean isEditMode()
   {
      return editMode != null ? editMode : this.element == null ? false
               : getId(this.element) == null ? false : true;
   }

   /**
    * @param editMode
    */
   public void setEditMode(boolean editMode)
   {
      this.editMode = editMode;
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

   public boolean isReadOnlyMode()
   {
      return readOnlyMode;
   }

   public void setReadOnlyMode(boolean roMode)
   {
      this.readOnlyMode = roMode;
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
         Field f = t.getClass().getDeclaredField("id");
         f.setAccessible(true);
         return f.get(t);
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

   // ============================================================================================
   // === LOGICA DI GESTIONE DELLO STATO INTERNO DELL'HANDLER
   // ============================================================================================

   /**
    * Metodo interno (protected) che carica il risultato della ricerca corrente. Possibile override da parte delle
    * sottoclassi per aggiungere listener e/o effettuare operazioni aggiuntive specifiche del particolare sotto-handler
    */
   public void refreshModel()
   {
      // setModel(new LocalLazyDataModel<T>(search, getRepository()));
      setModel(new LocalDataModel<T>(pageSize, search, getRepository()));
   }

   /**
    * Metodo interno (protected) da overriddare per assicurare che i criteri di ricerca contengano sempre tutti i
    * vincoli desiderati (es: identità utente, selezioni esterne, oggetti figli non nulli, ecc...)
    * 
    * Qui ne viene fornita una implementazione di default che non fa nulla, per evitare di scrivere un metodo vuoto nei
    * sotto-handler in caso non ce ne sia bisogno
    */
   public void defaultCriteria()
   {

   }

   /**
    * Implements reset specific operations
    */
   protected void preReset()
   {
      this.scrollerPage = 1;
      this.element = null;
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
      return listPageNoRedirect();
   }

   /**
    * Implements reload specific operations
    */
   protected void preReload()
   {
      this.element = null;
      this.model = null;
      this.loaded = true;
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
      return listPageNoRedirect();
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

   public String reloadAjax()
   {
      reload();
      return null;
   }

   public String resetAjax()
   {
      reset();
      return null;
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

   /**
    * Pagina di provenienza, settabile dall'esterno (cioè da altri handler!) Possibile override per forzare una
    * determinata backpage tramite gli handler concreti che estendono questa classe
    */
   public void backPage(String backPage)
   {
      this.backPage = backPage;
   }

   /**
    * @return
    */
   public String backPage()
   {
      return backPage;
   }

   /**
    * @return
    */
   public String viewPage()
   {
      return viewPage + REDIRECT_PARAM;
   }

   /**
    * @return
    */
   public String listPage()
   {
      return listPage + REDIRECT_PARAM;
   }

   /**
    * @return
    */
   public String listPageNoRedirect()
   {
      return listPage;
   }

   /**
    * @return
    */
   public String editPage()
   {
      return editPage + REDIRECT_PARAM;
   }

   /**
    * @return
    */
   public String editPageNoRedirect()
   {
      return editPage;
   }

   /**
    * @return
    */
   public String printPage()
   {
      return printPage;
   }

   // ============================================================================================
   // === LOGICA DI BUSINESS
   // ============================================================================================

   public String addElement()
   {
      try
      {
         this.element = getRepository().create(entityClass);
      }
      catch (Exception e)
      {
         logger.info(e.getMessage());
         e.printStackTrace();
      }
      this.editMode = false;
      this.readOnlyMode = false;
      // impostazioni locali
      // da specializzare in sottoclassi
      // vista di destinazione
      return editPage();
   }

   public String viewElement()
   {
      // fetch dei dati
      T t = (T) getModel().getRowData();
      t = getRepository().fetch(getId(t));
      // impostazioni locali
      // da specializzare in sottoclassi
      // settaggi nel super handler
      this.element = t;
      this.editMode = false;
      this.readOnlyMode = true;
      // vista di destinazione
      return viewPage();
   }

   /**
    * @return
    */
   public String modElement()
   {
      // fetch dei dati;
      T t = (T) getModel().getRowData();
      t = getRepository().fetch(getId(t));
      // impostazioni locali
      // da specializzare in sottoclassi
      // settaggi nel super handler
      this.element = t;
      this.editMode = true;
      this.readOnlyMode = false;
      // vista di destinazione
      return editPage();
   }

   /**
    * @return
    */
   public String printElement()
   {
      // fetch dei dati;
      T t = (T) getModel().getRowData();
      t = getRepository().fetch(getId(t));
      // impostazioni locali
      // da specializzare in sottoclassi
      // settaggi nel super handler
      this.element = t;
      // vista di destinazione
      return printPage();
   }

   public String view(Object key)
   {

      T t = getRepository().find(key);

      // impostazioni locali
      // da specializzare in sottoclassi
      // settaggi nel super handler
      this.element = t;
      this.editMode = false;
      this.readOnlyMode = true;
      // vista di destinazione
      return viewPage();
   }

   // ---------------------------------------------------------------------------------------------------------------------
   // --- metodi che richiedono un oggetto element gia' valido e ne modificano
   // solo lo stato in sessione ------------------
   // ---------------------------------------------------------------------------------------------------------------------

   /**
    * @return
    */
   public String modCurrent()
   {
      // fetch dei dati
      element = getRepository().fetch(getId(element));
      editMode = true;
      readOnlyMode = false;
      // vista di arrivo
      return editPage();
   }

   /**
    * @return
    */
   public String viewCurrent()
   {
      // fetch dei dati
      element = getRepository().fetch(getId(element));
      editMode = false;
      readOnlyMode = true;
      // vista di arrivo
      return viewPage();
   }

   /**
    * @return
    */
   public String printCurrent()
   {
      // fetch dei dati
      element = getRepository().fetch(getId(element));
      // vista di arrivo
      return printPage();
   }

   // ----------------------------------------------------------------------------------------------------------
   // --- metodi che richiedono un oggetto element gia' valido e ne modificano
   // lo stato su db ------------------
   // ----------------------------------------------------------------------------------------------------------

   /**
    * @return
    */
   public String save()
   {
      try
      {
         // recupero e preelaborazioni dati in input
         // nelle sottoclassi!! ovverride!
         // salvataggio
         setElement(getRepository().persist(getElement()));
         // refresh locale
         setEditMode(false);
         setReadOnlyMode(true);
         refreshModel();
         return viewPage();
      }
      catch (Exception exc)
      {
         return editPageNoRedirect();
      }
   }

   /**
    * @return
    */
   public String update()
   {
      try
      {
         // recupero dati in input
         // nelle sottoclassi!! ovverride!
         // salvataggio
         getRepository().update(getElement());
         // refresh locale
         setElement(getRepository().fetch(getId(getElement())));
         setEditMode(false);
         setReadOnlyMode(true);
         refreshModel();
         return viewPage();
      }
      catch (Exception exc)
      {
         return editPageNoRedirect();
      }
   }

   /**
    * @return
    */
   public String delete()
   {
      try
      {
         // operazione su db
         getRepository().delete(getId(element));
         // refresh super handler
         setEditMode(false);
         setReadOnlyMode(true);
         refreshModel();
         element = null;
         return listPage();
      }
      catch (Exception exc)
      {
         return editPageNoRedirect();
      }
   }

   public String modInline()
   {
      T rowElement = (T) getModel().getRowData();
      setRowElement(rowElement);
      setModificabile(rowElement, true);
      return null;
   }

   @SuppressWarnings("unchecked")
   public void updateInline()
   {
      T rowElement = (T) getModel().getRowData();
      setRowElement(null);
      // setModificabile(rowElement,false);
      if (getRepository().update(rowElement))
      {
         List<T> rows = (List<T>) getModel().getWrappedData();
         rows.set(getModel().getRowIndex(), getRepository().find(getId(rowElement)));
      }
      else
      {
         addFacesMessage("Errore nell'aggiornamento dei dati");
      }
   }

   @SuppressWarnings("unchecked")
   public void annullaInline()
   {
      T rowElement = (T) getModel().getRowData();
      setRowElement(null);
      List<T> rows = (List<T>) getModel().getWrappedData();
      rows.set(getModel().getRowIndex(), getRepository().find(getId(rowElement)));
      setModificabile(rowElement, false);
   }

   public void deleteInline()
   {
      T rowElement = (T) getModel().getRowData();
      setRowElement(null);
      if (getRepository().delete(getId(rowElement)))
      {
         setModel(null);
      }
      else
      {
         addFacesMessage("Errore durante la cancellazione");
      }
   }

   protected void setModificabile(T t, boolean modificabile)
   {
      try
      {
         Field modificabileField = t.getClass().getDeclaredField(
                  "modificabile");
         modificabileField.setAccessible(true);
         modificabileField.set(t, modificabile);
      }
      catch (Exception e)
      {
         logger.error(e.getMessage(), e);
      }
   }

   // --------------------------------------------------------------------------
   // Varie
   // --------------------------------------------------------------------------

   public boolean isLoaded()
   {
      return loaded;
   }

   public void setLoaded(boolean loaded)
   {
      this.loaded = loaded;
   }

   // --------------------------------------------------------------------------
   // Varie
   // --------------------------------------------------------------------------

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