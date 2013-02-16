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
public abstract class BaseRole implements Serializable
{

   private static final long serialVersionUID = 1L;

   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private Long id;

   private String alias;
   private String group;
   private String description;
   private String type;

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

   public String getAlias()
   {
      return alias;
   }

   public void setAlias(String alias)
   {
      this.alias = alias;
   }

   public String getGroup()
   {
      return group;
   }

   public void setGroup(String group)
   {
      this.group = group;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
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

   public Object getCustomerId()
   {
      return null;
   }

}
