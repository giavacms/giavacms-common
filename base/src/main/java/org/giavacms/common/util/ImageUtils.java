/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.swing.ImageIcon;

import org.jboss.logging.Logger;

public class ImageUtils
{

   static Logger logger = Logger
            .getLogger(ImageUtils.class.getCanonicalName());

   // =======================================================================================

   public static Integer getImageWidthProportional(Object imageData,
            Integer maxWidth, Integer maxHeight)
   {
      ImageIcon imageIcon = new ImageIcon((byte[]) imageData);
      return getImageSizeProportional(imageIcon, maxWidth, maxHeight)[0];
   }

   public static Integer getImageHeightProportional(Object imageData,
            Integer maxWidth, Integer maxHeight)
   {
      ImageIcon imageIcon = new ImageIcon((byte[]) imageData);
      return getImageSizeProportional(imageIcon, maxWidth, maxHeight)[1];
   }

   public static Integer getImageWidthProportionalByUrl(String url,
            Integer maxWidth, Integer maxHeight)
   {
      if (url == null)
         return 0;
      ImageIcon imageIcon = new ImageIcon(getRealPath() + url);
      return getImageSizeProportional(imageIcon, maxWidth, maxHeight)[0];
   }

   public static Integer getImageHeightProportionalByUrl(String url,
            Integer maxWidth, Integer maxHeight)
   {
      if (url == null)
         return 0;
      ImageIcon imageIcon = new ImageIcon(getRealPath() + url);
      return getImageSizeProportional(imageIcon, maxWidth, maxHeight)[1];
   }

   // =======================================================================================

   public static String getRealPath()
   {
      ServletContext servletContext = (ServletContext) FacesContext
               .getCurrentInstance().getExternalContext().getContext();
      String folder = servletContext.getRealPath("") + File.separator;
      return folder;
   }

   public static Integer[] getImageSizeProportional(ImageIcon imageIcon,
            int maxWidth, int maxHeight)
   {

      double ratioH = (double) maxHeight / imageIcon.getIconHeight();
      double ratioW = (double) maxWidth / imageIcon.getIconWidth();

      int targetWidth = imageIcon.getIconWidth();
      int targetHeight = imageIcon.getIconHeight();

      if (ratioW < ratioH)
      {
         if (ratioW < 1)
         {
            targetWidth = (int) (imageIcon.getIconWidth() * ratioW);
            targetHeight = (int) (imageIcon.getIconHeight() * ratioW);
         }
      }
      else /* if ratioH < ratioW */if (ratioH < 1)
      {
         targetWidth = (int) (imageIcon.getIconWidth() * ratioH);
         targetHeight = (int) (imageIcon.getIconHeight() * ratioH);
      }

      return new Integer[] { targetWidth, targetHeight };

   }

   // =======================================================================================

   public static byte[] resizeImage(byte[] imageData, int maxWidthOrHeight,
            String type) throws IOException
   {
      // Create an ImageIcon from the image data
      ImageIcon imageIcon = new ImageIcon(imageData);
      int width = imageIcon.getIconWidth();
      int height = imageIcon.getIconHeight();
      // log.info("imageIcon width: " + width + "  height: " + height);

      // landscape (W>H) or portrait image (W<=H)?
      boolean isPortraitImage;
      if (width <= height)
         // vertical image (portrait)
         isPortraitImage = true;
      else
         // horizontal image (landscape)
         isPortraitImage = false;

      // vertical image, i have to care about height
      if (isPortraitImage && maxWidthOrHeight > 0
               && height > maxWidthOrHeight)
      {
         // Determine the shrink ratio
         double ratio = (double) maxWidthOrHeight
                  / imageIcon.getIconHeight();
         logger.debug("resize ratio: " + ratio);
         width = (int) (imageIcon.getIconWidth() * ratio);
         height = maxWidthOrHeight;
         logger.debug("imageIcon post scale width: " + width + "  height: "
                  + height);
      }

      // horizontal image, i have to care about width
      if (!isPortraitImage && maxWidthOrHeight > 0
               && width > maxWidthOrHeight)
      {
         // Determine the shrink ratio
         double ratio = (double) maxWidthOrHeight / imageIcon.getIconWidth();
         logger.debug("resize ratio: " + ratio);
         height = (int) (imageIcon.getIconHeight() * ratio);
         width = maxWidthOrHeight;
         logger.debug("imageIcon post scale width: " + width + "  height: "
                  + height);
      }

      // Create a new empty image buffer to "draw" the resized image into
      BufferedImage bufferedResizedImage = new BufferedImage(width, height,
               BufferedImage.TYPE_INT_RGB);
      // Create a Graphics object to do the "drawing"
      Graphics2D g2d = bufferedResizedImage.createGraphics();
      g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
               RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      // Draw the resized image
      g2d.drawImage(imageIcon.getImage(), 0, 0, width, height, null);
      g2d.dispose();
      // Now our buffered image is ready
      // Encode it as a JPEG
      ByteArrayOutputStream encoderOutputStream = new ByteArrayOutputStream();
      ImageIO.write(bufferedResizedImage, type.toUpperCase(),
               encoderOutputStream);
      // QUESTE CLASSI NON GIRANO SOTTO JAVA 6
      // JPEGImageEncoder encoder =
      // JPEGCodec.createJPEGEncoder(encoderOutputStream);
      // encoder.encode(bufferedResizedImage);
      byte[] resizedImageByteArray = encoderOutputStream.toByteArray();
      return resizedImageByteArray;
   }

   // =======================================================================================

}
