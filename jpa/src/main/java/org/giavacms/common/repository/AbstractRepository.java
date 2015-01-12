/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.repository;

import java.io.Serializable;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.giavacms.common.annotation.LogOperation;
import org.giavacms.common.model.BaseEntity;
import org.giavacms.common.model.Group;
import org.giavacms.common.model.Search;
import org.jboss.logging.Logger;

/**
 * @author fiorenzo pizza
 * 
 * @param <T>
 */
public abstract class AbstractRepository<T> implements Serializable,
         Repository<T>
{

   private static final long serialVersionUID = 1L;

   // --- JPA ---------------------------------

   /**
    * @return
    */
   protected abstract EntityManager getEm();

   public abstract void setEm(EntityManager em);

   // --- Logger -------------------------------

   protected static final Logger logger = Logger
            .getLogger(AbstractRepository.class.getName());

   // --- Mandatory logic --------------------------------

   // protected abstract Class<T> getEntityType();
   @SuppressWarnings("unchecked")
   protected Class<T> getEntityType()
   {
      ParameterizedType parameterizedType = (ParameterizedType) getClass()
               .getGenericSuperclass();
      return (Class<T>) parameterizedType.getActualTypeArguments()[0];
   }

   // --- CRUD --------------

   /*
    * (non-Javadoc)
    * 
    * @see com.eggsweb.commons.repository.Repository#create(java.lang.Class<T>)
    */
   public T create(Class<T> domainClass)
   {
      try
      {
         return domainClass.newInstance();
      }
      catch (Exception ex)
      {
logger.error(ex.getMessage(),ex);
         return null;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.eggsweb.commons.repository.Repository#persist(java.lang.Object)
    */
   @LogOperation
   public T persist(T object)
   {
      try
      {
         object = prePersist(object);
         if (object != null)
         {
            getEm().persist(object);
         }
         return object;
      }
      catch (Exception e)
      {
logger.error(e.getMessage(),e);
         return null;
      }
   }

   /**
    * Override this if needed
    * 
    * @param object
    * @return the object to be persisted
    */
   protected T prePersist(T object)
   {
      return object;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.eggsweb.commons.repository.Repository#find(java.lang.Object)
    */
   public T find(Object key)
   {
      try
      {
         return getEm().find(getEntityType(), key);
      }
      catch (Exception e)
      {
logger.error(e.getMessage(),e);
         return null;
      }
   }

   public String testKey(String key)
   {
      String keyNotUsed = key;
      boolean found = false;
      int i = 0;
      while (!found)
      {
         logger.info("key to search: " + keyNotUsed);
         T obj = getEm().find(getEntityType(), keyNotUsed);
         logger.info("found: " + obj);
         if (obj != null)
         {
            i++;
            keyNotUsed = key + "-" + i;
         }
         else
         {
            found = true;
            return keyNotUsed;
         }
      }
      return "";
   }

   /*
    * (non-Javadoc)
    * 
    * per ora definisco il metodo in modo da poterne eseguire l'invocazione..
    * 
    * poi l'implementazione via reflections verrà pian piano...
    */
   public T fetch(Object key)
   {
      try
      {
         return getEm().find(getEntityType(), key);
      }
      catch (Exception e)
      {
logger.error(e.getMessage(),e);
         return null;
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.eggsweb.commons.repository.Repository#update(java.lang.Object)
    */
   @LogOperation
   public boolean update(T object)
   {
      try
      {
         object = preUpdate(object);
         getEm().merge(object);
         return true;
      }
      catch (Exception e)
      {
logger.error(e.getMessage(),e);
         return false;
      }
   }

   /**
    * Override this if needed
    * 
    * @param object
    * @return
    */
   protected T preUpdate(T object)
   {
      return object;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.eggsweb.commons.repository.Repository#delete(java.lang.Object)
    */
   @LogOperation
   public boolean delete(Object key)
   {
      try
      {
         T obj = getEm().find(getEntityType(), key);
         if (obj != null)
         {
            getEm().remove(obj);
            // getEm().flush();
         }
         return true;
      }
      catch (Exception e)
      {
logger.error(e.getMessage(),e);
         return false;
      }
   }

   // --- LIST ------------------------------------------

   /*
    * (non-Javadoc)
    * 
    * @see com.eggsweb.commons.repository.Repository#getAllList()
    */
   @SuppressWarnings("unchecked")
   public List<T> getAllList()
   {
      try
      {
         CriteriaQuery<T> criteriaQuery = (CriteriaQuery<T>) getEm()
                  .getCriteriaBuilder().createQuery();
         criteriaQuery.select(criteriaQuery.from(getEntityType()));
         return getEm().createQuery(criteriaQuery).getResultList();
      }
      catch (Exception ex)
      {
logger.error(ex.getMessage(),ex);
         return new ArrayList<T>();
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.eggsweb.commons.repository.Repository#getList(com.eggsweb.commons .par.Search, int, int)
    */
   @SuppressWarnings("unchecked")
   public List<T> getList(Search<T> search, int startRow, int pageSize)
   {
      try
      {
         List<T> result = null;
         boolean count = false;
         Query res = getRestrictions(search, count);
         if (res == null)
            return result;
         if (startRow >= 0)
         {
            res.setFirstResult(startRow);
         }
         if (pageSize > 0)
         {
            res.setMaxResults(pageSize);
         }

         result = (List<T>) res.getResultList();

         return result == null ? new ArrayList<T>() : result;

      }
      catch (Exception ex)
      {
logger.error(ex.getMessage(),ex);
         return new ArrayList<T>();
      }
   }

   @SuppressWarnings("unchecked")
   public List<Group<T>> getGroups(Search<T> search, int startRow, int pageSize)
   {
      List<Group<T>> result = new ArrayList<Group<T>>();
      try
      {
         if (search.getGrouping() == null || search.getGrouping().trim().length() == 0
                  || search.getGrouping().trim().split(",").length == 0)
         {
            List<T> list = getList(search, startRow, pageSize);
            for (T t : list)
            {
               result.add(new Group<T>(1L, t));
            }
            return result;
         }
         Map<String, Object> params = new HashMap<String, Object>();
         String alias = "c";
         StringBuffer sb = new StringBuffer();
         String groups[] = search.getGrouping().trim().split(",");
         String countAlias = "counting";
         sb.append("select count(").append(alias).append(".").append(groups[0]).append(") as ").append(countAlias)
                  .append(", ");
         for (int i = 0; i < groups.length; i++)
         {
            sb.append(alias).append(".").append(groups[i]).append(i == groups.length - 1 ? "" : ", ");
         }
         sb.append(" from ").append(search.getObj().getClass().getSimpleName()).append(" ").append(alias);
         String separator = " where ";
         applyRestrictions(search, alias, separator, sb, params);
         sb.append(" group by ");
         for (int i = 0; i < groups.length; i++)
         {
            sb.append(alias).append(".").append(groups[i]).append(i == groups.length - 1 ? "" : ", ");
         }
         sb.append(" order by ").append(countAlias).append(" desc ");
         Query q = getEm().createQuery(sb.toString());
         for (String param : params.keySet())
         {
            q.setParameter(param, params.get(param));
         }
         if (startRow >= 0)
         {
            q.setFirstResult(startRow);
         }
         if (pageSize > 0)
         {
            q.setMaxResults(pageSize);
         }
         List<Object[]> resultList = (List<Object[]>) q.getResultList();
         if (resultList == null || resultList.size() == 0)
         {
            return result;
         }
         Long max = (Long) resultList.get(0)[0];
         for (Object[] resultItem : resultList)
         {
            T t = construct(Arrays.asList(groups), Arrays.asList(resultItem)
                     .subList(1, resultItem.length));
            if (t != null)
            {
               result.add(new Group<T>((Long) resultItem[0], t, max));
            }
         }
         return result;
      }
      catch (Exception ex)
      {
         logger.error(ex.getMessage(), ex);
         return result;
      }
   }

   /**
    * Override this
    * 
    * @param asList
    * @param subList
    * @return
    */
   protected T construct(List<String> fieldNames, List<Object> fieldValues)
   {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see com.eggsweb.commons.repository.Repository#getListSize(com.eggsweb.commons .par.Search)
    */
   public int getListSize(Search<T> search)
   {
      Long result = new Long(0);
      try
      {

         boolean count = true;
         Query res = getRestrictions(search, count);

         if ((res != null))
         {
            result = (Long) res.getSingleResult();
         }

         return result == null ? 0 : result.intValue();

      }
      catch (Exception e)
      {
         logger.info(e.getMessage());
      }
      return 0;
   }

   /**
    * @param startRow
    * @param pageSize
    * @param res
    * @return
    */
   @SuppressWarnings("unchecked")
   public List<T> getList(int startRow, int pageSize, Query res)
   {
      try
      {
         List<T> result = new ArrayList<T>();
         result = (List<T>) res.getResultList();
         if (result != null)
            return result;
      }
      catch (Exception e)
      {
         logger.info(e.getMessage());
      }
      return new ArrayList<T>();
   }

   /**
    * criteri di default, comuni a tutti, ma specializzabili da ogni EJB tramite overriding
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   protected Query getRestrictions(Search<T> search, boolean justCount)
   {
      if (search.getObj() != null)
      {

         Map<String, Object> params = new HashMap<String, Object>();
         String alias = "c";
         StringBuffer sb = new StringBuffer(getBaseList(search.getObj()
                  .getClass(), alias, justCount));
         String separator = " where ";

         applyRestrictions(search, alias, separator, sb, params);

         if (!justCount)
         {
            sb.append(getOrderBy(alias, search.getOrder()));
         }

         Query q = getEm().createQuery(sb.toString());
         for (String param : params.keySet())
         {
            q.setParameter(param, params.get(param));
         }

         return q;

      }
      else
      {

         Class entityType = search.getObj().getClass();
         if (entityType == null)
         {
            entityType = getEntityType();
         }

         CriteriaBuilder criteriaBuilder = getEm().getCriteriaBuilder();
         CriteriaQuery<T> criteriaQuery = criteriaBuilder
                  .createQuery(entityType);
         Root<T> rec = criteriaQuery.from(entityType);

         if (justCount)
         {
            criteriaQuery.select((Expression) criteriaBuilder.count(rec));
         }

         else
         {
            // comanda quello della search, se non inizializzato usiamo
            // quello
            // di default del repository
            String orderBy = search.getOrder();
            if (orderBy == null)
            {
               orderBy = getDefaultOrderBy();
            }
            Path path = rec.get(orderBy);
            Order order = null;
            if (path != null)
            {
               if (search.isOrderAsc())
               {
                  order = criteriaBuilder.asc((Expression) rec
                           .get(orderBy));
               }
               else
               {
                  order = criteriaBuilder.desc((Expression) rec
                           .get(orderBy));
               }
            }
            if (order != null)
            {
               criteriaQuery.orderBy(order);
            }
         }
         return getEm().createQuery(criteriaQuery);
      }
   }

   /**
    * metodo da sovrascrivere per applicare parametri alla query, con relative condizioni d'uso
    * 
    * esempio:
    * 
    * String leftOuterJoinAlias = "s"; if (search.getObj().getNumero() != null &&
    * search.getObj().getNumero().trim().length() > 0) { sb.append(" left outer join ").append(alias)
    * .append(".serviziPrenotati ").append(leftOuterJoinAlias); // sb.append(" on "
    * ).append(leftOuterJoinAlias).append(".allegati.id = ").append (alias).append(".id"); }
    * 
    * if (search.getObj().getAttivo() != null) { sb.append(separator).append(" ").append(alias)
    * .append(".attivo = :attivo "); // aggiunta alla mappa params.put("attivo", search.getObj().getAttivo()); //
    * separatore separator = " and "; }
    * 
    * if (search.getObj().getNumero() != null && !search.getObj().getNumero().trim().isEmpty()) {
    * sb.append(separator).append(leftOuterJoinAlias) .append(".servizio.numero = :numero and ")
    * .append(leftOuterJoinAlias) .append(".servizio.tipo = :tipoServizio "); // aggiunta alla mappa
    * params.put("numero", search.getObj().getNumero()); params.put("tipoServizio", TipoServizioEnum.OMB); // separatore
    * separator = " and "; }
    * 
    * @param search
    * @param alias
    * @param separator
    * @param sb
    * @param params
    */
   protected void applyRestrictions(Search<T> search, String alias,
            String separator, StringBuffer sb, Map<String, Object> params)
   {
   }

   /**
    * @param criteriaBuilder
    * @param current
    * @param toAdd
    * @return
    */
   protected Predicate addAndPredicate(CriteriaBuilder criteriaBuilder,
            Predicate current, Predicate toAdd)
   {
      return (current != null) ? criteriaBuilder.and(current, toAdd) : toAdd;
   }

   /**
    * Serve per inizializzare un oggetto attraverso la rilettura dal db Il parametro fields contiene la lista dei nomi
    * delle property su cui fare la left fetch join
    * 
    * @param object
    * @param fields
    * @return
    */
   @SuppressWarnings("unchecked")
   public T initialize(BaseEntity object, String... fields) throws Exception
   {
      T p = null;
      try
      {
         Collection<String> ff = new ArrayList<String>();
         if (fields != null)
         {
            for (String arg : fields)
            {
               ff.add(arg);
            }
         }
         if (object != null && object.getId() != null)
         {
            StringBuffer sb = new StringBuffer();
            sb.append("FROM " + object.getClass().getSimpleName() + " rec ");
            if (ff.size() > 0)
            {
               Field[] objectFields = object.getClass()
                        .getDeclaredFields();
               for (Field objectField : objectFields)
               {
                  if (ff.contains(objectField.getName()))
                  {
                     sb.append(" LEFT JOIN FETCH rec."
                              + objectField.getName());
                  }
               }
            }
            sb.append(" WHERE rec.id = :id");
            @SuppressWarnings("rawtypes")
            TypedQuery q = getEm().createQuery(sb.toString(),
                     object.getClass());
            q.setParameter("id", object.getId());
            p = (T) q.getSingleResult();
         }
      }
      catch (Exception e)
      {
         logger.error(e.getMessage(), e);
         throw new Exception(e);
      }
      return p;
   }

   protected String getBaseList(Class<? extends Object> clazz, String alias,
            boolean count)
   {
      if (count)
      {
         return "select count(" + alias + ") from " + clazz.getSimpleName()
                  + " " + alias + " ";
      }
      else
      {
         return "select " + alias + " from " + clazz.getSimpleName() + " "
                  + alias + " ";
      }
   }

   protected abstract String getDefaultOrderBy();

   public String getOrderBy(String alias, String orderBy)
   {
      try
      {
         if (orderBy == null || orderBy.length() == 0)
         {
            orderBy = getDefaultOrderBy();
         }
         StringBuffer result = new StringBuffer();
         String[] orders = orderBy.split(",");
         for (String order : orders)
         {
            result.append(", ").append(alias).append(".")
                     .append(order.trim()).append(" ");
         }
         return " order by " + result.toString().substring(2);
      }
      catch (Exception e)
      {
         return "";
      }
   }

   protected String likeParam(String param)
   {
      return "%" + param + "%";
   }

   protected String likeParamL(String param)
   {
      return "%" + param;
   }

   protected String likeParamR(String param)
   {
      return param + "%";
   }

}
