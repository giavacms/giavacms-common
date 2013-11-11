/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.renderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

import org.giavacms.common.util.JSFUtils;
import org.jboss.logging.Logger;

@FacesRenderer(componentFamily = "javax.faces.Command", rendererType = "uiRepeatPagerWithParams")
public class UIRepeatPagerWithParams extends Renderer
{

   protected static Logger logger = Logger
            .getLogger(UIRepeatPagerWithParams.class);

   private static String[] computeSymbols(int paginaCorrente,
            int elementiPerPagina, int totaleElementi,
            int massimoNumeroDiLinkVisibili)
   {
      return (String[]) compute(paginaCorrente, elementiPerPagina,
               totaleElementi, massimoNumeroDiLinkVisibili, true);
   }

   private static Integer[] computeLinks(int paginaCorrente,
            int elementiPerPagina, int totaleElementi,
            int massimoNumeroDiLinkVisibili)
   {
      return (Integer[]) compute(paginaCorrente, elementiPerPagina,
               totaleElementi, massimoNumeroDiLinkVisibili, false);
   }

   // pagina corrente parte da 1
   private static Object[] compute(int paginaEtLinkCorrente,
            int elementiPerPagina, int totaleElementi,
            int massimoNumeroDiLinkVisibili, boolean returnSymbols)
   {

      List<String> symbols = new ArrayList<String>();
      List<Integer> linkedPages = new ArrayList<Integer>();

      int numeroPagine = (totaleElementi % elementiPerPagina == 0) ? (totaleElementi / elementiPerPagina)
               : (totaleElementi / elementiPerPagina + 1);
      int minimoLink = 1;
      int massimoLink = numeroPagine;

      int correzionePerLinkPari = massimoNumeroDiLinkVisibili % 2 == 0 ? -1
               : 0;

      int massimoNumeroDiLinkVisibiliAllaSinistra = correzionePerLinkPari
               + massimoNumeroDiLinkVisibili / 2;
      int massimoNumeroDiLinkVisibiliAllaDestra = massimoNumeroDiLinkVisibili / 2;
      int minimoLinkVisibileAllaSinistra = paginaEtLinkCorrente
               - massimoNumeroDiLinkVisibiliAllaSinistra;
      int massimoLinkVisibileAllaDestra = paginaEtLinkCorrente
               + massimoNumeroDiLinkVisibiliAllaDestra;

      if (minimoLinkVisibileAllaSinistra < minimoLink)
      {
         int linkAggiuntiviVisibliAllaDestra = minimoLink
                  - minimoLinkVisibileAllaSinistra;
         minimoLinkVisibileAllaSinistra = minimoLink;
         massimoLinkVisibileAllaDestra = massimoLinkVisibileAllaDestra
                  + linkAggiuntiviVisibliAllaDestra;
      }
      if (massimoLinkVisibileAllaDestra > massimoLink)
      {
         int linkAggiuntiviVisibliAllaSinistra = massimoLinkVisibileAllaDestra
                  - massimoLink;
         massimoLinkVisibileAllaDestra = massimoLink;
         minimoLinkVisibileAllaSinistra = minimoLinkVisibileAllaSinistra
                  - linkAggiuntiviVisibliAllaSinistra;
      }
      if (minimoLinkVisibileAllaSinistra < 1)
      {
         minimoLinkVisibileAllaSinistra = 1;
      }

      boolean inizio = false;
      boolean precedente = false;
      boolean successivo = false;
      boolean fine = false;
      if (paginaEtLinkCorrente > minimoLink)
      {
         inizio = precedente = true;
      }
      if (paginaEtLinkCorrente < massimoLink)
      {
         successivo = fine = true;
      }

      if (inizio)
      {
         symbols.add("<<");
         linkedPages.add(minimoLink);
      }
      if (precedente)
      {
         symbols.add("<");
         linkedPages.add(paginaEtLinkCorrente - 1);
      }
      for (int link = minimoLinkVisibileAllaSinistra; link < paginaEtLinkCorrente; link++)
      {
         symbols.add("" + link);
         linkedPages.add(link);
      }
      symbols.add(paginaEtLinkCorrente + "");
      linkedPages.add(paginaEtLinkCorrente);
      for (int link = paginaEtLinkCorrente + 1; link <= massimoLinkVisibileAllaDestra; link++)
      {
         symbols.add("" + link);
         linkedPages.add(link);
      }
      if (successivo)
      {
         symbols.add(">");
         linkedPages.add(paginaEtLinkCorrente + 1);
      }
      if (fine)
      {
         symbols.add(">>");
         linkedPages.add(massimoLink);
      }
      if (returnSymbols)
      {
         return symbols.toArray(new String[] {});
      }
      else
      {
         return linkedPages.toArray(new Integer[] {});
      }

   }

   public void encodeBegin(FacesContext context, UIComponent component)
            throws IOException
   {

      ResponseWriter writer = context.getResponseWriter();

      String styleClass = (String) component.getAttributes()
               .get("styleClass");
      String selectedStyleClass = (String) component.getAttributes().get(
               "selectedStyleClass");
      @SuppressWarnings("rawtypes")
      UiRepeatInterface handler = (UiRepeatInterface) component
               .getAttributes().get("handler");
      int currentpage = handler.getCurrentPage();
      int pagesize = handler.getPageSize();
      int itemcount = handler.totalSize();
      int showpages = toInt(component.getAttributes().get("showpages"));

      Integer[] linkedPages = computeLinks(currentpage, pagesize, itemcount,
               showpages);
      String[] symbols = computeSymbols(currentpage, pagesize, itemcount,
               showpages);
      // Boolean[] currents = computeCurrent(currentpage, pagesize, showpages,
      // itemcount);

      writeLinks(writer, component, styleClass, selectedStyleClass,
               linkedPages, symbols, currentpage,
               JSFUtils.getQueryStringParameters(),
               handler.getCurrentPageParam());

   }

   private void writeLinks(ResponseWriter writer, UIComponent component,
            String styleClass, String selectedStyleClass,
            Integer[] linkedPages, String[] symbols, int currentPage,
            Map<String, String[]> params, String currentPageParam)
            throws IOException
   {

      for (int i = 0; i < symbols.length; i++)
      {
         if (linkedPages[i] == currentPage)
         {
            writeSpan(writer, component, symbols[i], selectedStyleClass);
         }
         else
         {
            writeLink(writer, component, symbols[i], styleClass,
                     currentPageParam, linkedPages[i], params);
         }
      }
   }

   private void writeLink(ResponseWriter writer, UIComponent component,
            String value, String styleClass, String currentPageParam,
            Integer pageToShow, Map<String, String[]> params)
            throws IOException
   {
      writer.writeText(" ", null);
      writer.startElement("a", component);
      writer.writeAttribute("href",
               makeHref("", currentPageParam, pageToShow, params), null);
      if (styleClass != null)
         writer.writeAttribute("class", styleClass, null);
      writer.writeText(value, null);
      writer.endElement("a");
   }

   private void writeSpan(ResponseWriter writer, UIComponent component,
            String value, String styleClass) throws IOException
   {
      writer.writeText(" ", null);
      writer.startElement("span", component);
      if (styleClass != null)
         writer.writeAttribute("class", styleClass, null);
      writer.writeText(value, null);
      writer.endElement("span");
   }

   private String makeHref(String base, String currentPageParam,
            Integer pageToShow, Map<String, String[]> params)
   {
      StringBuffer href = new StringBuffer(base);
      href.append("?").append(currentPageParam).append("=")
               .append(pageToShow);

      for (String n : params.keySet())
      {
         String vs[] = params.get(n);
         if (vs != null && vs.length > 0)
         {
            for (String v : vs)
            {
               if (n != null && n.trim().length() > 0 && v != null
                        && v.trim().length() > 0
                        && !currentPageParam.equalsIgnoreCase(n))
               {
                  href.append("&").append(n).append("=").append(v);
               }
            }
         }
      }
      return href.toString();
   }

   private static int toInt(Object value)
   {
      if (value == null)
         return 0;
      if (value instanceof Number)
         return ((Number) value).intValue();
      if (value instanceof String)
         return Integer.parseInt((String) value);
      throw new IllegalArgumentException("Cannot convert " + value);
   }

   public static void main(String[] args)
   {
      int totalElements = 1005;
      int linkVisibili = 10;
      int pageSize = 10;
      for (int i = 1; (i * pageSize) < (totalElements + pageSize); i++)
      {
         log(i, pageSize, totalElements, linkVisibili);
      }
   }

   private static void log(int currentPage, int pageSize, int totalElements,
            int linkVisibili)
   {
      Object[] x = UIRepeatPagerWithParams.compute(currentPage, pageSize,
               totalElements, linkVisibili, true);
      logger.info(currentPage + " \t=\t ");
      for (Object o : x)
      {
         logger.info(o.toString() + " ");
      }
      logger.info("\n");
   }

   public UIRepeatPagerWithParams()
   {
      super();
   }

}