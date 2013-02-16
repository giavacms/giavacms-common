/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.giavacms.common.model.AdminRole;
import org.giavacms.common.model.BaseCustomer;
import org.giavacms.common.model.BaseRole;
import org.giavacms.common.repository.AbstractRepository;
import org.giavacms.common.util.JSFUtils;
import org.jboss.logging.Logger;


public abstract class AbstractLoginController implements Serializable
{

   private static final long serialVersionUID = 1L;

   @Inject
   protected Logger logger;

   // --- gruppi LDAP -----------------------------------

   /**
    * Il gruppo restituito dall'LDAP nel caso un utente sia amministratore-root-dio di questa webapp, da verificare
    * attraverso la API j2ee isUserInRole()
    * 
    * E' l'unico gruppo LDAP noto a priori, mentre altri gruppi possono aggiungersi col tempo (estensione dell'uso della
    * webapp a nuovi soggetti)
    */
   public static final String JBOSS_ADMIN = "JBossAdmin";

   // --- ruoli WEBAPP ------------------------------------------

   private List<BaseRole> roles;
   private BaseRole mainRole;
   private String username;
   private BaseCustomer customer;

   @PostConstruct
   public void postConstruct()
   {
      username = JSFUtils.getUserName();
      logger.warn("Loading profile for: " + username);
      boolean loggedIn = initRole();
      trace(loggedIn);
   }

   protected void trace(boolean loggedIn)
   {
      logger.warn("Login for: " + username
               + (loggedIn ? " was succesful!" : " failed."));
   }

   private boolean initRole()
   {

      // check if admin
      if (JSFUtils.isUserInRole(getAdminRole())
               || JSFUtils.isUserInRole(JBOSS_ADMIN))
      {
         this.roles = new ArrayList<BaseRole>();
         this.roles.add(new AdminRole());
         return true;
      }

      boolean profiled = false;
      if (aliasDrivenRoles())
      {
         profiled = initRolesByAlias();
      }
      else
      {
         profiled = initRolesByGroups();
      }

      if (profiled)
      {
         this.mainRole = getMainRole();
         this.customer = getCustomerRepository().find(mainRole.getCustomerId());
         return true;
      }

      logger.info("No roles found for user: " + username);
      String redirectURL = JSFUtils.getContextParam("redirectUrl");
      if (redirectURL == null)
      {
         logger.warn("Failed to get redirectUrl context parameter");
         redirectURL = "/error.jsp";
      }
      try
      {
         JSFUtils.redirect(redirectURL);
      }
      catch (Exception e)
      {
         logger.error(e.getMessage(), e);
      }
      return false;
   }

   protected String getAdminRole()
   {
      return "Admin";
   }

   protected boolean aliasDrivenRoles()
   {
      return false;
   }

   private boolean initRolesByGroups()
   {
      List<? extends BaseRole> allRoles = getRoleRepository().getAllList();
      roles = new ArrayList<BaseRole>();
      for (BaseRole role : allRoles)
      {
         if (JSFUtils.isUserInRole(role.getGroup()))
         {
            roles.add(role);
         }
      }
      if (roles == null || roles.size() == 0)
      {
         return false;
      }
      else
      {
         return true;
      }
   }

   private boolean initRolesByAlias()
   {
      // Search<? extends BaseRole> search = new Search<BaseRole>(new
      // BaseRole(){
      // private static final long serialVersionUID = 1L;});
      // search.getObj().setAlias(username);
      // this.roles = getRoleRepository().getList(search, 0, 0);
      List<? extends BaseRole> allRoles = getRoleRepository().getAllList();
      roles = new ArrayList<BaseRole>();
      for (BaseRole role : allRoles)
      {
         if (username.equalsIgnoreCase(role.getAlias()))
         {
            roles.add(role);
         }
      }
      if (roles == null || roles.size() == 0)
      {
         return false;
      }
      else
      {
         return true;
      }
   }

   abstract protected AbstractRepository<? extends BaseRole> getRoleRepository();

   abstract protected AbstractRepository<? extends BaseCustomer> getCustomerRepository();

   public String selectCustomerIfAdmin()
   {
      this.customer = getCustomerRepository().find(this.customer.getId());
      resetDependencies();
      return null;
   }

   protected void resetDependencies()
   {
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }

   public BaseCustomer getCustomer()
   {
      if (customer == null)
         customer = new BaseCustomer()
         {
            private static final long serialVersionUID = 1L;
         };
      return customer;
   }

   public void setCustomer(BaseCustomer customer)
   {
      this.customer = customer;
   }

   public List<BaseRole> getRoles()
   {
      return roles;
   }

   public void setRoles(List<BaseRole> roles)
   {
      this.roles = roles;
   }

   public BaseRole getMainRole()
   {
      if (mainRole == null)
      {
         findMainRole();
      }
      return mainRole;
   }

   public void setMainRole(BaseRole mainRole)
   {
      this.mainRole = mainRole;
   }

   private void findMainRole()
   {
      if (roles != null)
      {
         for (BaseRole r : roles)
         {
            if (mainRole == null)
            {
               mainRole = r;
            }
            else if (betterThanCurrent(r))
            {
               mainRole = r;
            }
         }
      }
      this.customer = getCustomerRepository().find(mainRole.getCustomerId());
   }

   protected boolean betterThanCurrent(BaseRole r)
   {
      // override this
      return true;
   }

   public String getRolesAsString()
   {
      if (roles != null && roles.size() > 0)
      {
         StringBuffer sb = new StringBuffer();
         for (BaseRole r : roles)
         {
            sb.append(", ").append(r.getType());
         }
         return sb.substring(2);
      }
      return "n.d.";
   }

}
