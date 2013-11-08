/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@MappedSuperclass
public abstract class BaseEntity implements Serializable
{

   private static final long serialVersionUID = 1L;

   private Long id;

   @Temporal(TemporalType.TIMESTAMP)
   private Date createdOn;

   @Temporal(TemporalType.TIMESTAMP)
   private Date modifiedOn;

   public Long getId()
   {
      return id;
   }

   public void setId(Long id)
   {
      this.id = id;
   }

   public Date getCreatedOn()
   {
      return createdOn;
   }

   public void setCreatedOn(Date createdOn)
   {
      this.createdOn = createdOn;
   }

   public Date getModifiedOn()
   {
      return modifiedOn;
   }

   public void setModifiedOn(Date modifiedOn)
   {
      this.modifiedOn = modifiedOn;
   }

   @PrePersist
   public void initTimeStamps()
   {
      if (createdOn == null)
      {
         createdOn = new Date();
      }
      modifiedOn = createdOn;
   }

   @PreUpdate
   public void updateTimeStamp()
   {
      modifiedOn = new Date();
   }
}
