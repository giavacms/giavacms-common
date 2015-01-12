package org.giavacms.common.view;

import java.util.List;
import java.util.Map;

import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.giavacms.common.filter.MappingFilter;
import org.jboss.logging.Logger;

public class MappingViewHandler extends ViewHandlerWrapper
{
   private static final String ROOT_PATH = "/";

   Logger logger = Logger.getLogger(getClass());

   private ViewHandler wrappedHandler;

   public MappingViewHandler(ViewHandler defaultHandler)
   {
      this.wrappedHandler = defaultHandler;
   }

   @Override
   public ViewHandler getWrapped()
   {
      return wrappedHandler;
   }

   /**
    * This is the only method needed to be extended. First, we get the normal URL form the original ViewHandler. Then we
    * simply return the same URL with the extension stripped of.
    */
   public String getActionURL(FacesContext context, String viewId)
   {
      HttpServletRequest httpServletRequest = (HttpServletRequest) context
               .getExternalContext().getRequest();
      Object originalUri = httpServletRequest.getAttribute(MappingFilter.ORIGINAL_URI_ATTRIBUTE_NAME);
      if (originalUri != null && !originalUri.toString().isEmpty())
      {
         return originalUri.toString();
      }
      else
      {
         return getWrapped().getActionURL(context, viewId);
      }
   }

   @Override
   public String getRedirectURL(FacesContext paramFacesContext,
            String paramString, Map<String, List<String>> paramMap,
            boolean paramBoolean)
   {
      logger.debug("getRedirectURL: " + paramString);

      if (paramString == null || paramString.isEmpty())
      {
         return super.getRedirectURL(paramFacesContext, paramString, paramMap,
                  paramBoolean);
      }
      for (String reservedPath : MappingFilter.getReservedPaths())
      {
         if (paramString.startsWith(reservedPath))
         {
            return super.getRedirectURL(paramFacesContext, paramString, paramMap,
                     paramBoolean);
         }
      }

      paramString = paramString.replace(MappingFilter.getPagesPath(), ROOT_PATH);
      int dotIdx = paramString.lastIndexOf(".");
      if (dotIdx > 0)
      {
         paramString = paramString.substring(0, dotIdx);
      }

      logger.debug("getRedirectURL rewrite:" + paramString);
      HttpServletRequest httpServletRequest = (HttpServletRequest) paramFacesContext
               .getExternalContext().getRequest();
      httpServletRequest.setAttribute(MappingFilter.ORIGINAL_URI_ATTRIBUTE_NAME, paramString);

      return super.getRedirectURL(paramFacesContext, paramString, paramMap,
               paramBoolean);
   }

}
