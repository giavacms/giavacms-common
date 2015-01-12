/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.renderer;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIData;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.event.PhaseId;
import javax.faces.model.ArrayDataModel;
import javax.faces.model.DataModel;
import javax.faces.model.ListDataModel;
import javax.faces.model.ResultSetDataModel;
import javax.faces.model.ScalarDataModel;
import javax.faces.render.Renderer;

import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * (c) 2009 skiline AG
 * <p>
 * 
 * Adds a varStatus feature to the UIRepeat tag delivered with facelets. The var-status contains the following fields:
 * 
 * index ... the current index (zero based) count ... the iteration count (number of iteration, which means: the size of
 * the collection) first ... true, if the current iteration is the first (true, if index==0) last ... true, if the
 * current iteration is the last one (true, if index==count-1)
 * 
 * If you need an iteration without a collection, you can use from and to instead of value!
 * 
 * @author $Author$
 * @version $Revision$ $Date$
 */
@SuppressWarnings({ "deprecation", "unchecked", "unused", "rawtypes" })
public class UIRepeat extends UIComponentBase implements NamingContainer
{

   public static final String COMPONENT_TYPE = "facelets.ui.Repeat";

   public static final String COMPONENT_FAMILY = "facelets";

   private final static DataModel EMPTY_MODEL = new ListDataModel(
            Collections.EMPTY_LIST);

   // our data
   private Object value;

   private Object from;

   private Object to;

   private transient DataModel model;

   // variables
   private String var;

   private String varStatus;

   private int index = -1;

   // scoping
   private int offset = -1;

   private int size = -1;

   private Status status;

   public UIRepeat()
   {
      this.setRendererType("facelets.ui.Repeat");
   }

   public String getFamily()
   {
      return COMPONENT_FAMILY;
   }

   public int getOffset()
   {
      if (this.offset != -1)
      {
         return this.offset;
      }
      ValueBinding vb = this.getValueBinding("offset");
      if (vb != null)
      {
         return ((Integer) vb.getValue(FacesContext.getCurrentInstance()))
                  .intValue();
      }
      return 0;
   }

   public void setOffset(int offset)
   {
      this.offset = offset;
   }

   public int getSize()
   {
      if (this.size != -1)
      {
         return this.size;
      }
      ValueBinding vb = this.getValueBinding("size");
      if (vb != null)
      {
         return ((Integer) vb.getValue(FacesContext.getCurrentInstance()))
                  .intValue();
      }
      return -1;
   }

   public void setSize(int size)
   {
      this.size = size;
   }

   public String getVar()
   {
      return this.var;
   }

   public void setVar(String var)
   {
      this.var = var;
   }

   private void resetDataModel()
   {
      if (this.isNestedInIterator())
      {
         this.setDataModel(null);
      }
   }

   private synchronized void setDataModel(DataModel model)
   {
      this.model = model;
   }

   private synchronized DataModel getDataModel()
   {
      if (this.model == null)
      {
         Object val = this.getValue();
         if (val == null)
         {
            int from = getFromValue();
            int to = getToValue();

            if (from <= to)
            {
               // create a list, which contains the loop values
               List list = new ArrayList();
               for (int i = from; i <= to; i++)
               {
                  list.add(i);
               }
               this.model = new ListDataModel(list);
            }
            else
            {
               this.model = EMPTY_MODEL;
            }
         }
         else if (val instanceof DataModel)
         {
            this.model = (DataModel) val;
         }
         else if (val instanceof List)
         {
            this.model = new ListDataModel((List) val);
         }
         else if (Object[].class.isAssignableFrom(val.getClass()))
         {
            this.model = new ArrayDataModel((Object[]) val);
         }
         else if (val instanceof ResultSet)
         {
            this.model = new ResultSetDataModel((ResultSet) val);
         }
         else
         {
            this.model = new ScalarDataModel(val);
         }
      }
      status = new Status(model.getRowCount());
      return this.model;
   }

   public Object getValue()
   {
      if (this.value == null)
      {
         ValueBinding vb = this.getValueBinding("value");
         if (vb != null)
         {
            return vb.getValue(FacesContext.getCurrentInstance());
         }
      }
      return this.value;
   }

   public void setValue(Object value)
   {
      this.value = value;
   }

   private int getFromValue()
   {
      Object from = this.from;

      if (from == null)
      {
         ValueBinding vb = this.getValueBinding("from");
         if (vb != null)
         {
            from = vb.getValue(FacesContext.getCurrentInstance());
         }
      }

      if (from instanceof Number)
      {
         return ((Number) from).intValue();
      }
      else
      {
         return 0;
      }
   }

   private int getToValue()
   {
      Object to = this.to;

      if (to == null)
      {
         ValueBinding vb = this.getValueBinding("to");
         if (vb != null)
         {
            to = vb.getValue(FacesContext.getCurrentInstance());
         }
      }

      if (to instanceof Number)
      {
         return ((Number) to).intValue();
      }
      else
      {
         return -1;
      }
   }

   private transient StringBuffer buffer;

   private StringBuffer getBuffer()
   {
      if (this.buffer == null)
      {
         this.buffer = new StringBuffer();
      }
      this.buffer.setLength(0);
      return this.buffer;
   }

   public String getClientId(FacesContext faces)
   {
      String id = super.getClientId(faces);
      if (this.index >= 0)
      {
         id = this.getBuffer().append(id).append(
                  NamingContainer.SEPARATOR_CHAR).append(this.index)
                  .toString();
      }
      return id;
   }

   private transient Object origValue;

   private transient Object origStatus;

   private void captureOrigValue()
   {
      FacesContext faces = FacesContext.getCurrentInstance();
      Map attrs = faces.getExternalContext().getRequestMap();

      if (this.var != null)
      {
         this.origValue = attrs.get(this.var);
      }
      if (this.varStatus != null)
      {
         this.origStatus = attrs.get(this.varStatus);
      }
   }

   private void restoreOrigValue()
   {
      FacesContext faces = FacesContext.getCurrentInstance();
      Map attrs = faces.getExternalContext().getRequestMap();

      if (this.var != null)
      {
         if (this.origValue != null)
         {
            attrs.put(this.var, this.origValue);
         }
         else
         {
            attrs.remove(this.var);
         }
      }
      if (this.varStatus != null)
      {
         if (this.origStatus != null)
         {
            attrs.put(varStatus, origStatus);
         }
         else
         {
            attrs.remove(this.varStatus);
         }
      }
   }

   private Map childState;

   private Map getChildState()
   {
      if (this.childState == null)
      {
         this.childState = new HashMap();
      }
      return this.childState;
   }

   private void saveChildState()
   {
      if (this.getChildCount() > 0)
      {

         FacesContext faces = FacesContext.getCurrentInstance();

         Iterator itr = this.getChildren().iterator();
         while (itr.hasNext())
         {
            this.saveChildState(faces, (UIComponent) itr.next());
         }
      }
   }

   private void saveChildState(FacesContext faces, UIComponent c)
   {

      if (c instanceof EditableValueHolder && !c.isTransient())
      {
         String clientId = c.getClientId(faces);
         SavedState ss = (SavedState) this.getChildState().get(clientId);
         if (ss == null)
         {
            ss = new SavedState();
            this.getChildState().put(clientId, ss);
         }
         ss.populate((EditableValueHolder) c);
      }

      // continue hack
      Iterator itr = c.getFacetsAndChildren();
      while (itr.hasNext())
      {
         saveChildState(faces, (UIComponent) itr.next());
      }
   }

   private void restoreChildState()
   {
      if (this.getChildCount() > 0)
      {

         FacesContext faces = FacesContext.getCurrentInstance();

         Iterator itr = this.getChildren().iterator();
         while (itr.hasNext())
         {
            this.restoreChildState(faces, (UIComponent) itr.next());
         }
      }
   }

   private void restoreChildState(FacesContext faces, UIComponent c)
   {
      // reset id
      String id = c.getId();
      c.setId(id);

      // hack
      if (c instanceof EditableValueHolder)
      {
         EditableValueHolder evh = (EditableValueHolder) c;
         String clientId = c.getClientId(faces);
         SavedState ss = (SavedState) this.getChildState().get(clientId);
         if (ss != null)
         {
            ss.apply(evh);
         }
         else
         {
            NullState.apply(evh);
         }
      }

      // continue hack
      Iterator itr = c.getFacetsAndChildren();
      while (itr.hasNext())
      {
         restoreChildState(faces, (UIComponent) itr.next());
      }
   }

   private boolean keepSaved(FacesContext context)
   {

      Iterator clientIds = this.getChildState().keySet().iterator();
      while (clientIds.hasNext())
      {
         String clientId = (String) clientIds.next();
         Iterator messages = context.getMessages(clientId);
         while (messages.hasNext())
         {
            FacesMessage message = (FacesMessage) messages.next();
            if (message.getSeverity()
                     .compareTo(FacesMessage.SEVERITY_ERROR) >= 0)
            {
               return (true);
            }
         }
      }
      return (isNestedInIterator());
   }

   private boolean isNestedInIterator()
   {
      UIComponent parent = this.getParent();
      while (parent != null)
      {
         if (parent instanceof UIData || parent instanceof UIRepeat)
         {
            return true;
         }
         parent = parent.getParent();
      }
      return false;
   }

   private void setIndex(int index)
   {

      // save child state
      this.saveChildState();

      this.index = index;
      DataModel localModel = getDataModel();
      localModel.setRowIndex(index);

      if (this.index != -1 && this.var != null && localModel.isRowAvailable())
      {
         FacesContext faces = FacesContext.getCurrentInstance();
         Map attrs = faces.getExternalContext().getRequestMap();

         Object rowData = localModel.getRowData();
         attrs.put(var, rowData);

         if (varStatus != null)
         {
            attrs.put(varStatus, status);
         }
      }

      // restore child state
      this.restoreChildState();
   }

   private boolean isIndexAvailable()
   {
      return this.getDataModel().isRowAvailable();
   }

   public void process(FacesContext faces, PhaseId phase)
   {

      // stop if not rendered
      if (!this.isRendered())
         return;

      // clear datamodel
      this.resetDataModel();

      // reset index
      this.captureOrigValue();
      this.setIndex(-1);

      try
      {
         // has children
         if (this.getChildCount() > 0)
         {
            Iterator itr;
            UIComponent c;

            int i = this.getOffset();
            int end = this.getSize();

            end = (end >= 0) ? i + end : Integer.MAX_VALUE - 1;

            // grab renderer
            String rendererType = getRendererType();
            Renderer renderer = null;
            if (rendererType != null)
            {
               renderer = getRenderer(faces);
            }

            this.setIndex(i);
            while (i <= end && this.isIndexAvailable())
            {

               if (PhaseId.RENDER_RESPONSE.equals(phase)
                        && renderer != null)
               {
                  renderer.encodeChildren(faces, this);
               }
               else
               {
                  itr = this.getChildren().iterator();
                  while (itr.hasNext())
                  {
                     c = (UIComponent) itr.next();
                     if (PhaseId.APPLY_REQUEST_VALUES.equals(phase))
                     {
                        c.processDecodes(faces);
                     }
                     else if (PhaseId.PROCESS_VALIDATIONS
                              .equals(phase))
                     {
                        c.processValidators(faces);
                     }
                     else if (PhaseId.UPDATE_MODEL_VALUES
                              .equals(phase))
                     {
                        c.processUpdates(faces);
                     }
                     else if (PhaseId.RENDER_RESPONSE.equals(phase))
                     {

                        c.encodeAll(faces);

                     }
                  }
               }
               i++;
               this.setIndex(i);
               status.next();
            }
         }
      }
      catch (IOException e)
      {
         throw new FacesException(e);
      }
      finally
      {
         this.setIndex(-1);
         this.restoreOrigValue();
      }
   }

   public void processDecodes(FacesContext faces)
   {
      if (!this.isRendered())
         return;
      this.setDataModel(null);
      if (!this.keepSaved(faces))
         this.childState = null;
      this.process(faces, PhaseId.APPLY_REQUEST_VALUES);
      this.decode(faces);
   }

   public void processUpdates(FacesContext faces)
   {
      if (!this.isRendered())
         return;
      this.resetDataModel();
      this.process(faces, PhaseId.UPDATE_MODEL_VALUES);
   }

   public void processValidators(FacesContext faces)
   {
      if (!this.isRendered())
         return;
      this.resetDataModel();
      this.process(faces, PhaseId.PROCESS_VALIDATIONS);
   }

   private final static SavedState NullState = new SavedState();

   // from RI
   private final static class SavedState implements Serializable
   {

      private Object submittedValue;

      private static final long serialVersionUID = 2920252657338389849L;

      Object getSubmittedValue()
      {
         return (this.submittedValue);
      }

      void setSubmittedValue(Object submittedValue)
      {
         this.submittedValue = submittedValue;
      }

      private boolean valid = true;

      boolean isValid()
      {
         return (this.valid);
      }

      void setValid(boolean valid)
      {
         this.valid = valid;
      }

      private Object value;

      Object getValue()
      {
         return (this.value);
      }

      public void setValue(Object value)
      {
         this.value = value;
      }

      private boolean localValueSet;

      boolean isLocalValueSet()
      {
         return (this.localValueSet);
      }

      public void setLocalValueSet(boolean localValueSet)
      {
         this.localValueSet = localValueSet;
      }

      public String toString()
      {
         return ("submittedValue: " + submittedValue + " value: " + value
                  + " localValueSet: " + localValueSet);
      }

      public void populate(EditableValueHolder evh)
      {
         this.value = evh.getValue();
         this.valid = evh.isValid();
         this.submittedValue = evh.getSubmittedValue();
         this.localValueSet = evh.isLocalValueSet();
      }

      public void apply(EditableValueHolder evh)
      {
         evh.setValue(this.value);
         evh.setValid(this.valid);
         evh.setSubmittedValue(this.submittedValue);
         evh.setLocalValueSet(this.localValueSet);
      }

   }

   public static class Status
   {
      private int index;
      private int count;

      private Status(int count)
      {
         this.count = count;
      }

      public int getIndex()
      {
         return index;
      }

      public int getCount()
      {
         return count;
      }

      public boolean isFirst()
      {
         return index == 0;
      }

      public boolean isLast()
      {
         return index == count - 1;
      }

      public void setIndex(int index)
      {
         this.index = index;
      }

      public void setCount(int count)
      {
         this.count = count;
      }

      public void next()
      {
         index++;
      }

      public String toString()
      {
         return "UIRepeat$Status[count=" + count + ",index=" + index
                  + ",first=" + isFirst() + ",last=" + isLast() + "]";
      }
   }

   private final class IndexedEvent extends FacesEvent
   {
      private static final long serialVersionUID = 5475426608254583480L;

      private final FacesEvent target;

      private final int index;

      public IndexedEvent(UIRepeat owner, FacesEvent target, int index)
      {
         super(owner);
         this.target = target;
         this.index = index;
      }

      public PhaseId getPhaseId()
      {
         return (this.target.getPhaseId());
      }

      public void setPhaseId(PhaseId phaseId)
      {
         this.target.setPhaseId(phaseId);
      }

      public boolean isAppropriateListener(FacesListener listener)
      {
         return this.target.isAppropriateListener(listener);
      }

      public void processListener(FacesListener listener)
      {
         UIRepeat owner = (UIRepeat) this.getComponent();
         int prevIndex = owner.index;
         try
         {
            owner.setIndex(this.index);
            if (owner.isIndexAvailable())
            {
               this.target.processListener(listener);
            }
         }
         finally
         {
            owner.setIndex(prevIndex);
         }
      }

      public int getIndex()
      {
         return index;
      }

      public FacesEvent getTarget()
      {
         return target;
      }

   }

   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      if (event instanceof IndexedEvent)
      {
         IndexedEvent idxEvent = (IndexedEvent) event;
         this.resetDataModel();
         int prevIndex = this.index;
         try
         {
            this.setIndex(idxEvent.getIndex());
            if (this.isIndexAvailable())
            {
               FacesEvent target = idxEvent.getTarget();
               target.getComponent().broadcast(target);
            }
         }
         finally
         {
            this.setIndex(prevIndex);
         }
      }
      else
      {
         super.broadcast(event);
      }
   }

   public void queueEvent(FacesEvent event)
   {
      super.queueEvent(new IndexedEvent(this, event, this.index));
   }

   public void restoreState(FacesContext faces, Object object)
   {
      Object[] state = (Object[]) object;
      super.restoreState(faces, state[0]);
      this.childState = (Map) state[1];
      this.offset = ((Integer) state[2]).intValue();
      this.size = ((Integer) state[3]).intValue();
      this.var = (String) state[4];
      this.value = state[5];
   }

   public Object saveState(FacesContext faces)
   {
      Object[] state = new Object[6];
      state[0] = super.saveState(faces);
      state[1] = this.childState;
      state[2] = new Integer(this.offset);
      state[3] = new Integer(this.size);
      state[4] = this.var;
      state[5] = this.value;
      return state;
   }

   public void encodeChildren(FacesContext faces) throws IOException
   {
      if (!isRendered())
      {
         return;
      }
      this.setDataModel(null);
      if (!this.keepSaved(faces))
      {
         this.childState = null;
      }
      this.process(faces, PhaseId.RENDER_RESPONSE);
   }

   public boolean getRendersChildren()
   {
      Renderer renderer = null;
      if (getRendererType() != null)
      {
         if (null != (renderer = getRenderer(getFacesContext())))
         {
            return renderer.getRendersChildren();
         }
      }
      return true;
   }

   public String getVarStatus()
   {
      return varStatus;
   }

   public void setVarStatus(String varStatus)
   {
      this.varStatus = varStatus;
   }

   public Object getFrom()
   {
      return from;
   }

   public void setFrom(Object from)
   {
      this.from = from;
   }

   public Object getTo()
   {
      return to;
   }

   public void setTo(Object to)
   {
      this.to = to;
   }
}
