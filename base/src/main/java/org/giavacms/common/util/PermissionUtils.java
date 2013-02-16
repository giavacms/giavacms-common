/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.util;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.giavacms.common.controller.AbstractPermissionController;


/**
 * Funzioni facelets per i permessi.
 * 
 */
public class PermissionUtils
{

   /**
    * metodo da associare a una funzione facelets per scrivere cose del tipo
    * 
    * <h:outputText value="XXX" rendered="#{fn:permit('SELECT_CLIENT')}"
    * 
    * e ottenere la visualizzazione/nascondimento di parti di pagina
    * 
    */
   public static boolean permit(String op)
   {
      AbstractPermissionController sph = (AbstractPermissionController) BeanUtils
               .getBean(AbstractPermissionController.class);
      return sph.checkACL(sph.getPermissions().get(op));
   }

   /**
    * metodo da associare a una funzione facelets per scrivere cose del tipo
    * 
    * <h:outputText value="XXX" rendered="#{fn:redirect('SELECT_CLIENT')}"
    * 
    * e ottenere il redirect su altre pagine in caso il ruolo previsto dalla ACL corrispondente non sia assunto
    * dall'utente corrente
    */
   public static void redirect(String acl)
   {
      AbstractPermissionController sph = (AbstractPermissionController) BeanUtils
               .getBean(AbstractPermissionController.class);
      redirectTo(acl, sph.getRedirectUrl());
   }

   public static void redirectTo(String acl, String to)
   {
      AbstractPermissionController sph = (AbstractPermissionController) BeanUtils
               .getBean(AbstractPermissionController.class);
      if (acl == null || "".equals(acl))
      {
         return;
      }
      if (to == null || to.isEmpty())
      {
         to = sph.getRedirectUrl();
      }
      for (String a : acl.split(","))
      {
         if (sph.checkACL(sph.getPermissions().get(a.trim())))
            return;
      }
      try
      {
         ExternalContext extCtx = FacesContext.getCurrentInstance()
                  .getExternalContext();
         extCtx.redirect(extCtx.encodeActionURL(JSFUtils.getAbsolutePath()
                  + "/" + to));
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

   /**
    * metodo da associare a una funzione facelets per scrivere cose del tipo
    * 
    * <h:outputText value="XXX" rendered="#{fn:redirectByRoles('USER_ADMIN')}"
    * 
    * e ottenere il redirect su altre pagine in caso il ruolo indicato non sia assunto dall'utente corrente
    */
   public static void redirectByRoles(String roles)
   {
      AbstractPermissionController sph = (AbstractPermissionController) BeanUtils
               .getBean(AbstractPermissionController.class);
      redirectByRolesTo(roles, sph.getRedirectUrl());
   }

   public static void redirectByRolesTo(String roles, String to)
   {
      AbstractPermissionController sph = BeanUtils
               .getBean(AbstractPermissionController.class);
      if (roles == null || "".equals(roles))
      {
         return;
      }
      if (to == null || to.isEmpty())
      {
         to = sph.getRedirectUrl();
      }
      for (String role : roles.split(","))
      {
         if (sph.isUserInRole(role.trim()))
            return;
      }
      // ultima chance
      if (sph.getDevelopment())
      {
         for (String role : roles.split(","))
            if (sph.getLoginAlias() != null
                     && sph.getLoginAlias().equals(role.trim()))
               return;
      }
      try
      {
         ExternalContext extCtx = FacesContext.getCurrentInstance()
                  .getExternalContext();
         extCtx.redirect(extCtx.encodeActionURL(JSFUtils.getAbsolutePath()
                  + "/" + to));
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

}
