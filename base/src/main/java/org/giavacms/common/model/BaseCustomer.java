/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.model;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

@MappedSuperclass
public abstract class BaseCustomer implements Serializable
{

   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Long id;
   private String name;

   // --------------------------------------------

   @Transient
   private boolean delete;

   @Transient
   private boolean modify;

   // --------------------------------------------

   public Long getId()
   {
      return id;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public boolean isDelete()
   {
      return delete;
   }

   public void setDelete(boolean delete)
   {
      this.delete = delete;
   }

   public boolean isModify()
   {
      return modify;
   }

   public void setModify(boolean modify)
   {
      this.modify = modify;
   }

}
