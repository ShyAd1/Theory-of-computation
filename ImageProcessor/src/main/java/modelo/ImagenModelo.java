package modelo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ImagenModelo {

    private BufferedImage imagenActual;    // Imagen que se está mostrando y modificando
    private BufferedImage imagenOriginal;  // Copia de la imagen original para reset
    private BufferedImage imagenConRuido;  // Imagen con Ruido
    private double totalScale = 1.0;

    // Constructor
    public ImagenModelo() {
        this.imagenActual = null;
        this.imagenOriginal = null;
    }

    public BufferedImage getImagenConRuido() {
        return imagenConRuido;
    }

    // Método para cargar una nueva imagen
    public void cargarImagen(BufferedImage nuevaImagen) {
        if (nuevaImagen == null) {
            return;
        }
        // Hacer una copia profunda de la nueva imagen para imagenOriginal
        imagenOriginal = deepCopy(nuevaImagen);
        // Hacer una copia profunda para imagenActual
        imagenActual = deepCopy(nuevaImagen);
    }

    // Método para resetear la imagen actual a la original
    public void resetearImagen() {
        if (imagenOriginal == null) {
            return;
        }
        // Restaurar imagenActual a partir de imagenOriginal
        imagenActual = deepCopy(imagenOriginal);
    }

    // Getter para imagenActual
    public BufferedImage getImagenActual() {
        return imagenActual;
    }

    public void guardarImagen(File archivo) throws IOException {
        if (imagenActual != null) {
            ImageIO.write(imagenActual, "jpg", archivo);
        }
    }

    public void setImagenConRuido(BufferedImage imagenConRuido) {
        this.imagenConRuido = imagenConRuido;
    }

    public void setImagenActual(BufferedImage imagenActual) {
        this.imagenActual = imagenActual;
    }

// SLIDERS DE BRILLO Y CONTRASTE
    public void ajustarBrillo(int factor) {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        // Trabajar sobre una copia de imagenOriginal
        BufferedImage result = deepCopy(imagenOriginal);

        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                int rgb = result.getRGB(x, y);
                int r = Math.min(Math.max(((rgb >> 16) & 0xFF) + factor, 0), 255);
                int g = Math.min(Math.max(((rgb >> 8) & 0xFF) + factor, 0), 255);
                int b = Math.min(Math.max((rgb & 0xFF) + factor, 0), 255);
                int newRgb;
                if (result.getType() == BufferedImage.TYPE_INT_ARGB) {
                    newRgb = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    newRgb = (r << 16) | (g << 8) | b;
                }
                result.setRGB(x, y, newRgb);
            }
        }

        // Actualizar imagenActual con el resultado
        imagenActual = result;
    }

    public void ajustarContraste(double factor) {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }
        if (factor <= 0) {
            throw new IllegalArgumentException("El factor de contraste debe ser mayor que 0.");
        }

        // Trabajar sobre una copia de imagenOriginal
        BufferedImage result = deepCopy(imagenOriginal);

        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                int rgb = result.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Normalizar al rango [0,1]
                double normalizedR = (r / 255.0 - 0.5) * factor + 0.5;
                double normalizedG = (g / 255.0 - 0.5) * factor + 0.5;
                double normalizedB = (b / 255.0 - 0.5) * factor + 0.5;

                // Convertir de vuelta al rango [0,255]
                r = (int) Math.min(Math.max(normalizedR * 255, 0), 255);
                g = (int) Math.min(Math.max(normalizedG * 255, 0), 255);
                b = (int) Math.min(Math.max(normalizedB * 255, 0), 255);

                int newRgb;
                if (result.getType() == BufferedImage.TYPE_INT_ARGB) {
                    newRgb = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    newRgb = (r << 16) | (g << 8) | b;
                }
                result.setRGB(x, y, newRgb);
            }
        }

        // Actualizar imagenActual con el resultado
        imagenActual = result;
    }

// OPERACIONES
    public void interpolation(int newWidth, int newHeight) {
        if (imagenActual == null) {
            return;
        }
        BufferedImage interpolatedImage = new BufferedImage(newWidth, newHeight, imagenActual.getType());

        double xRatio = (double) imagenActual.getWidth() / newWidth;
        double yRatio = (double) imagenActual.getHeight() / newHeight;

        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                int srcX = (int) (x * xRatio);
                int srcY = (int) (y * yRatio);
                interpolatedImage.setRGB(x, y, imagenActual.getRGB(srcX, srcY));
            }
        }
        imagenActual = interpolatedImage;
    }

    public void rotation(double angle) {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage rotatedImage = new BufferedImage(width, height, imagenActual.getType());

        double radians = Math.toRadians(-angle); // Cambiar el signo para rotar antihorario
        int centerX = width / 2;
        int centerY = height / 2;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newX = (int) ((x - centerX) * Math.cos(radians) - (y - centerY) * Math.sin(radians) + centerX);
                int newY = (int) ((x - centerX) * Math.sin(radians) + (y - centerY) * Math.cos(radians) + centerY);

                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    rotatedImage.setRGB(newX, newY, imagenActual.getRGB(x, y));
                }
            }
        }
        imagenActual = rotatedImage;
    }

    public void translation(int deltaX, int deltaY) {
        if (imagenActual == null) {
            return;
        }
        BufferedImage translatedImage = new BufferedImage(imagenActual.getWidth(), imagenActual.getHeight(), imagenActual.getType());

        for (int y = 0; y < imagenActual.getHeight(); y++) {
            for (int x = 0; x < imagenActual.getWidth(); x++) {
                int newX = x + deltaX;
                int newY = y + deltaY;

                if (newX >= 0 && newX < translatedImage.getWidth() && newY >= 0 && newY < translatedImage.getHeight()) {
                    translatedImage.setRGB(newX, newY, imagenActual.getRGB(x, y));
                }
            }
        }
        imagenActual = translatedImage;
    }

    public void sum(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int r = Math.min(((rgb1 >> 16) & 0xFF) + ((rgb2 >> 16) & 0xFF), 255);
                int g = Math.min(((rgb1 >> 8) & 0xFF) + ((rgb2 >> 8) & 0xFF), 255);
                int b = Math.min((rgb1 & 0xFF) + (rgb2 & 0xFF), 255);

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    nuevoRGB = (r << 16) | (g << 8) | b;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void subtraction(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int r = Math.max(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF), 0);
                int g = Math.max(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF), 0);
                int b = Math.max((rgb1 & 0xFF) - (rgb2 & 0xFF), 0);

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    nuevoRGB = (r << 16) | (g << 8) | b;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void multiplication(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int r = Math.min(((rgb1 >> 16) & 0xFF) * ((rgb2 >> 16) & 0xFF) / 255, 255);
                int g = Math.min(((rgb1 >> 8) & 0xFF) * ((rgb2 >> 8) & 0xFF) / 255, 255);
                int b = Math.min((rgb1 & 0xFF) * (rgb2 & 0xFF) / 255, 255);

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    nuevoRGB = (r << 16) | (g << 8) | b;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void division(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int r = ((rgb2 >> 16) & 0xFF) == 0 ? 0 : Math.min(((rgb1 >> 16) & 0xFF) * 255 / ((rgb2 >> 16) & 0xFF), 255);
                int g = ((rgb2 >> 8) & 0xFF) == 0 ? 0 : Math.min(((rgb1 >> 8) & 0xFF) * 255 / ((rgb2 >> 8) & 0xFF), 255);
                int b = (rgb2 & 0xFF) == 0 ? 0 : Math.min((rgb1 & 0xFF) * 255 / (rgb2 & 0xFF), 255);

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    nuevoRGB = (r << 16) | (g << 8) | b;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void not() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);

                int r = 255 - ((rgb >> 16) & 0xFF);
                int g = 255 - ((rgb >> 8) & 0xFF);
                int b = 255 - (rgb & 0xFF);

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    nuevoRGB = (r << 16) | (g << 8) | b;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void xor(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int r = ((rgb1 >> 16) & 0xFF) ^ ((rgb2 >> 16) & 0xFF);
                int g = ((rgb1 >> 8) & 0xFF) ^ ((rgb2 >> 8) & 0xFF);
                int b = (rgb1 & 0xFF) ^ (rgb2 & 0xFF);

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    nuevoRGB = (r << 16) | (g << 8) | b;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void or(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int r = ((rgb1 >> 16) & 0xFF) | ((rgb2 >> 16) & 0xFF);
                int g = ((rgb1 >> 8) & 0xFF) | ((rgb2 >> 8) & 0xFF);
                int b = (rgb1 & 0xFF) | (rgb2 & 0xFF);

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    nuevoRGB = (r << 16) | (g << 8) | b;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void and(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int r = ((rgb1 >> 16) & 0xFF) & ((rgb2 >> 16) & 0xFF);
                int g = ((rgb1 >> 8) & 0xFF) & ((rgb2 >> 8) & 0xFF);
                int b = (rgb1 & 0xFF) & (rgb2 & 0xFF);

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    nuevoRGB = (r << 16) | (g << 8) | b;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void lessThan(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int lum1 = (int) ((0.299 * ((rgb1 >> 16) & 0xFF)) + (0.587 * ((rgb1 >> 8) & 0xFF)) + (0.114 * (rgb1 & 0xFF)));
                int lum2 = (int) ((0.299 * ((rgb2 >> 16) & 0xFF)) + (0.587 * ((rgb2 >> 8) & 0xFF)) + (0.114 * (rgb2 & 0xFF)));

                int nuevoRGB;
                if (lum1 < lum2) {
                    nuevoRGB = imagenActual.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) | (0xFFFFFF) : 0xFFFFFF; // Blanco
                } else {
                    nuevoRGB = imagenActual.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) : 0x000000; // Negro
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void lessThanOrEqual(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int lum1 = (int) ((0.299 * ((rgb1 >> 16) & 0xFF)) + (0.587 * ((rgb1 >> 8) & 0xFF)) + (0.114 * (rgb1 & 0xFF)));
                int lum2 = (int) ((0.299 * ((rgb2 >> 16) & 0xFF)) + (0.587 * ((rgb2 >> 8) & 0xFF)) + (0.114 * (rgb2 & 0xFF)));

                int nuevoRGB;
                if (lum1 <= lum2) {
                    nuevoRGB = imagenActual.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) | (0xFFFFFF) : 0xFFFFFF; // Blanco
                } else {
                    nuevoRGB = imagenActual.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) : 0x000000; // Negro
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void greaterThan(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int lum1 = (int) ((0.299 * ((rgb1 >> 16) & 0xFF)) + (0.587 * ((rgb1 >> 8) & 0xFF)) + (0.114 * (rgb1 & 0xFF)));
                int lum2 = (int) ((0.299 * ((rgb2 >> 16) & 0xFF)) + (0.587 * ((rgb2 >> 8) & 0xFF)) + (0.114 * (rgb2 & 0xFF)));

                int nuevoRGB;
                if (lum1 > lum2) {
                    nuevoRGB = imagenActual.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) | (0xFFFFFF) : 0xFFFFFF; // Blanco
                } else {
                    nuevoRGB = imagenActual.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) : 0x000000; // Negro
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void greaterThanOrEqual(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int lum1 = (int) ((0.299 * ((rgb1 >> 16) & 0xFF)) + (0.587 * ((rgb1 >> 8) & 0xFF)) + (0.114 * (rgb1 & 0xFF)));
                int lum2 = (int) ((0.299 * ((rgb2 >> 16) & 0xFF)) + (0.587 * ((rgb2 >> 8) & 0xFF)) + (0.114 * (rgb2 & 0xFF)));

                int nuevoRGB;
                if (lum1 >= lum2) {
                    nuevoRGB = imagenActual.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) | (0xFFFFFF) : 0xFFFFFF; // Blanco
                } else {
                    nuevoRGB = imagenActual.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) : 0x000000; // Negro
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void equal(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int lum1 = (int) ((0.299 * ((rgb1 >> 16) & 0xFF)) + (0.587 * ((rgb1 >> 8) & 0xFF)) + (0.114 * (rgb1 & 0xFF)));
                int lum2 = (int) ((0.299 * ((rgb2 >> 16) & 0xFF)) + (0.587 * ((rgb2 >> 8) & 0xFF)) + (0.114 * (rgb2 & 0xFF)));

                int nuevoRGB;
                if (lum1 == lum2) {
                    nuevoRGB = imagenActual.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) | (0xFFFFFF) : 0xFFFFFF; // Blanco
                } else {
                    nuevoRGB = imagenActual.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) : 0x000000; // Negro
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void notEqual(BufferedImage segundaImagen) {
        if (imagenActual == null || segundaImagen == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), segundaImagen.getWidth());
        int height = Math.min(imagenActual.getHeight(), segundaImagen.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1 = imagenActual.getRGB(x, y);
                int rgb2 = segundaImagen.getRGB(x, y);

                int lum1 = (int) ((0.299 * ((rgb1 >> 16) & 0xFF)) + (0.587 * ((rgb1 >> 8) & 0xFF)) + (0.114 * (rgb1 & 0xFF)));
                int lum2 = (int) ((0.299 * ((rgb2 >> 16) & 0xFF)) + (0.587 * ((rgb2 >> 8) & 0xFF)) + (0.114 * (rgb2 & 0xFF)));

                int nuevoRGB;
                if (lum1 != lum2) {
                    nuevoRGB = imagenActual.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) | (0xFFFFFF) : 0xFFFFFF; // Blanco
                } else {
                    nuevoRGB = imagenActual.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) : 0x000000; // Negro
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

// MODIFICACIONES AL HISTOGRAMA
    public void matchHistogram(BufferedImage target) {
        if (imagenActual == null || target == null) {
            return;
        }
        int width = Math.min(imagenActual.getWidth(), target.getWidth());
        int height = Math.min(imagenActual.getHeight(), target.getHeight());
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Calcular histogramas por canal para la imagen fuente
        int[] histSrcR = new int[256];
        int[] histSrcG = new int[256];
        int[] histSrcB = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                histSrcR[r]++;
                histSrcG[g]++;
                histSrcB[b]++;
            }
        }

        // Calcular histogramas por canal para la imagen objetivo
        int[] histTgtR = new int[256];
        int[] histTgtG = new int[256];
        int[] histTgtB = new int[256];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = target.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                histTgtR[r]++;
                histTgtG[g]++;
                histTgtB[b]++;
            }
        }

        // Calcular CDF para la imagen fuente
        int[] cdfSrcR = new int[256];
        int[] cdfSrcG = new int[256];
        int[] cdfSrcB = new int[256];
        cdfSrcR[0] = histSrcR[0];
        cdfSrcG[0] = histSrcG[0];
        cdfSrcB[0] = histSrcB[0];
        for (int i = 1; i < 256; i++) {
            cdfSrcR[i] = cdfSrcR[i - 1] + histSrcR[i];
            cdfSrcG[i] = cdfSrcG[i - 1] + histSrcG[i];
            cdfSrcB[i] = cdfSrcB[i - 1] + histSrcB[i];
        }

        // Calcular CDF para la imagen objetivo
        int[] cdfTgtR = new int[256];
        int[] cdfTgtG = new int[256];
        int[] cdfTgtB = new int[256];
        cdfTgtR[0] = histTgtR[0];
        cdfTgtG[0] = histTgtG[0];
        cdfTgtB[0] = histTgtB[0];
        for (int i = 1; i < 256; i++) {
            cdfTgtR[i] = cdfTgtR[i - 1] + histTgtR[i];
            cdfTgtG[i] = cdfTgtG[i - 1] + histTgtG[i];
            cdfTgtB[i] = cdfTgtB[i - 1] + histTgtB[i];
        }

        // Crear tablas de mapeo para cada canal
        int[] mapR = new int[256];
        int[] mapG = new int[256];
        int[] mapB = new int[256];

        for (int i = 0; i < 256; i++) {
            // Mapear R
            int jR = 0;
            while (jR < 255 && cdfTgtR[jR] < cdfSrcR[i]) {
                jR++;
            }
            mapR[i] = jR;

            // Mapear G
            int jG = 0;
            while (jG < 255 && cdfTgtG[jG] < cdfSrcG[i]) {
                jG++;
            }
            mapG[i] = jG;

            // Mapear B
            int jB = 0;
            while (jB < 255 && cdfTgtB[jB] < cdfSrcB[i]) {
                jB++;
            }
            mapB[i] = jB;
        }

        // Aplicar el mapeo a la imagen fuente
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                r = Math.min(255, Math.max(0, mapR[r]));
                g = Math.min(255, Math.max(0, mapG[g]));
                b = Math.min(255, Math.max(0, mapB[b]));

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    nuevoRGB = (r << 16) | (g << 8) | b;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void equalizeHistogram() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Calcular histogramas por canal (R, G, B)
        int[] histR = new int[256];
        int[] histG = new int[256];
        int[] histB = new int[256];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                histR[r]++;
                histG[g]++;
                histB[b]++;
            }
        }

        // Calcular CDF para cada canal
        int[] cdfR = new int[256];
        int[] cdfG = new int[256];
        int[] cdfB = new int[256];
        cdfR[0] = histR[0];
        cdfG[0] = histG[0];
        cdfB[0] = histB[0];
        for (int i = 1; i < 256; i++) {
            cdfR[i] = cdfR[i - 1] + histR[i];
            cdfG[i] = cdfG[i - 1] + histG[i];
            cdfB[i] = cdfB[i - 1] + histB[i];
        }

        // Encontrar el valor mínimo no nulo de la CDF para cada canal
        int cdfMinR = 0, cdfMinG = 0, cdfMinB = 0;
        for (int i = 0; i < 256; i++) {
            if (cdfR[i] > 0 && cdfMinR == 0) {
                cdfMinR = cdfR[i];
            }
            if (cdfG[i] > 0 && cdfMinG == 0) {
                cdfMinG = cdfG[i];
            }
            if (cdfB[i] > 0 && cdfMinB == 0) {
                cdfMinB = cdfB[i];
            }
        }

        // Total de píxeles
        int totalPixels = width * height;

        // Crear tabla de igualación para cada canal
        int[] equalizedR = new int[256];
        int[] equalizedG = new int[256];
        int[] equalizedB = new int[256];
        for (int i = 0; i < 256; i++) {
            equalizedR[i] = (cdfR[i] - cdfMinR) * 255 / (totalPixels - cdfMinR);
            equalizedG[i] = (cdfG[i] - cdfMinG) * 255 / (totalPixels - cdfMinG);
            equalizedB[i] = (cdfB[i] - cdfMinB) * 255 / (totalPixels - cdfMinB);
        }

        // Aplicar la igualación a la imagen
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                r = Math.min(255, Math.max(0, equalizedR[r]));
                g = Math.min(255, Math.max(0, equalizedG[g]));
                b = Math.min(255, Math.max(0, equalizedB[b]));

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    nuevoRGB = (r << 16) | (g << 8) | b;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void scaleHistogram(int minTarget, int maxTarget) {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }
        if (minTarget < 0 || maxTarget > 255 || minTarget > maxTarget) {
            throw new IllegalArgumentException("Los valores deben cumplir: 0 <= minTarget <= maxTarget <= 255.");
        }

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Encontrar valores mínimos y máximos de cada canal en la imagen original
        int minR = 255, maxR = 0;
        int minG = 255, maxG = 0;
        int minB = 255, maxB = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenOriginal.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                minR = Math.min(minR, r);
                maxR = Math.max(maxR, r);
                minG = Math.min(minG, g);
                maxG = Math.max(maxG, g);
                minB = Math.min(minB, b);
                maxB = Math.max(maxB, b);
            }
        }

        // Aplicar reescalado al rango objetivo [minTarget, maxTarget]
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenOriginal.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Reescalar cada canal al rango [minTarget, maxTarget]
                r = (maxR == minR) ? r : (minTarget + (r - minR) * (maxTarget - minTarget) / (maxR - minR));
                g = (maxG == minG) ? g : (minTarget + (g - minG) * (maxTarget - minTarget) / (maxG - minG));
                b = (maxB == minB) ? b : (minTarget + (b - minB) * (maxTarget - minTarget) / (maxB - minB));

                // Asegurar que los valores estén en el rango [0, 255]
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));

                int newRgb = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, newRgb);
            }
        }

        imagenActual = result;
    }

    public void shiftHistogram(int shiftValue) {
        if (imagenActual == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage shiftedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int r = (int) Math.min(255, Math.max(0, ((rgb >> 16) & 0xFF) + shiftValue));
                int g = (int) Math.min(255, Math.max(0, ((rgb >> 8) & 0xFF) + shiftValue));
                int b = (int) Math.min(255, Math.max(0, (rgb & 0xFF) + shiftValue));

                shiftedImage.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        imagenActual = shiftedImage;
    }

// ECUALIZACIONES AL HISTOGRAMA
    public void equalizeUniform() {
        if (imagenActual == null) {
            return;
        }
        // Convertir a escala de grises primero
        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        int[] gray = new int[width * height];
        int[] histogram = new int[256];
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                gray[index++] = r; // ya es escala de grises, puedes tomar solo R
                histogram[r]++;
            }
        }

        float[] cdf = new float[256];
        cdf[0] = histogram[0];
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + histogram[i];
        }

        float cdfMin = cdf[0];
        float totalPixels = width * height;
        int[] equalized = new int[256];

        for (int i = 0; i < 256; i++) {
            equalized[i] = Math.round((cdf[i] - cdfMin) * 255 / (totalPixels - cdfMin));
        }

        index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int eq = equalized[gray[index++]];
                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (eq << 16) | (eq << 8) | eq;
                } else {
                    nuevoRGB = (eq << 16) | (eq << 8) | eq;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void equalizeExponential(double lambda) {
        if (imagenActual == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }
        if (lambda <= 0) {
            throw new IllegalArgumentException("Lambda debe ser mayor que 0.");
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        int[] histogram = computeGrayHistogram();
        int totalPixels = width * height;

        // Calcular la CDF (Función de Distribución Acumulada) basada en una distribución exponencial
        double[] cdf = new double[256];
        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += histogram[i];
            cdf[i] = 1 - Math.exp(-lambda * (i / 255.0)); // Transformación exponencial
            cdf[i] = (cdf[i] * 255); // Escalar a [0, 255]
        }

        // Aplicar la transformación a los píxeles
        BufferedImage equalizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                int newValue = (int) Math.min(255, Math.max(0, cdf[gray]));
                equalizedImage.setRGB(x, y, new Color(newValue, newValue, newValue).getRGB());
            }
        }
        imagenActual = equalizedImage;
    }

    public void equalizeRayleigh(double sigma) {
        if (imagenActual == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }
        if (sigma <= 0) {
            throw new IllegalArgumentException("Sigma debe ser mayor que 0.");
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        int[] histogram = computeGrayHistogram();
        int totalPixels = width * height;

        // Calcular la CDF basada en una distribución de Rayleigh
        double[] cdf = new double[256];
        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += histogram[i];
            cdf[i] = 1 - Math.exp(-Math.pow(i / 255.0, 2) / (2 * sigma * sigma)); // Transformación Rayleigh
            cdf[i] = (cdf[i] * 255); // Escalar a [0, 255]
        }

        // Aplicar la transformación a los píxeles
        BufferedImage equalizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                int newValue = (int) Math.min(255, Math.max(0, cdf[gray]));
                equalizedImage.setRGB(x, y, new Color(newValue, newValue, newValue).getRGB());
            }
        }
        imagenActual = equalizedImage;
    }

    public void equalizeHyperbolicRoots(double k) {
        if (imagenActual == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("El exponente k debe ser mayor que 0.");
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        int[] histogram = computeGrayHistogram();
        int totalPixels = width * height;

        // Calcular la CDF basada en una transformación hiperbólica (raíz k-ésima)
        double[] cdf = new double[256];
        double sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += histogram[i];
            cdf[i] = Math.pow(i / 255.0, 1.0 / k); // Transformación hiperbólica (raíz k-ésima)
            cdf[i] = (cdf[i] * 255); // Escalar a [0, 255]
        }

        // Aplicar la transformación a los píxeles
        BufferedImage equalizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                int newValue = (int) Math.min(255, Math.max(0, cdf[gray]));
                equalizedImage.setRGB(x, y, new Color(newValue, newValue, newValue).getRGB());
            }
        }
        imagenActual = equalizedImage;
    }

    public void equalizeHyperbolicLogarithmic() {
        if (imagenActual == null) {
            return;
        }
        // Convertir a escala de grises
        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        int[] gray = new int[width * height];
        int[] histogram = new int[256];
        int index = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                gray[index++] = r;
                histogram[r]++;
            }
        }

        // Calcular CDF
        float[] cdf = new float[256];
        float total = width * height;
        cdf[0] = histogram[0] / total;
        for (int i = 1; i < 256; i++) {
            cdf[i] = cdf[i - 1] + (histogram[i] / total);
        }

        // fmin y fmax
        float fmin = 1f;  // evitar división por 0
        float fmax = 255f;
        double ratio = fmax / fmin;

        // Aplicar transformación hiperbólica logarítmica
        int[] mapped = new int[256];
        for (int i = 0; i < 256; i++) {
            double val = fmin * Math.pow(ratio, cdf[i]);
            int newVal = (int) Math.round(val);
            mapped[i] = Math.min(255, Math.max(0, newVal));
        }

        // Construir imagen resultante
        index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int eq = mapped[gray[index++]];
                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (eq << 16) | (eq << 8) | eq;
                } else {
                    nuevoRGB = (eq << 16) | (eq << 8) | eq;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

// RUIDOS
    public void addGaussianNoise() {
        if (imagenActual == null) {
            return;
        }
        Random rand = new Random();

        // Paso 1: convertir a escala de grises la imagen actual (basada en la original si es necesario)
        convertirEscalaGrises();

        BufferedImage noisyImage = deepCopy(imagenActual);

        // Paso 2: aplicar ruido sobre imagen en escala de grises
        for (int y = 0; y < noisyImage.getHeight(); y++) {
            for (int x = 0; x < noisyImage.getWidth(); x++) {
                int rgb = noisyImage.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;

                int noise = (int) (rand.nextGaussian() * 20);
                int valor = Math.min(Math.max(gray + noise, 0), 255);

                int nuevoRGB = (valor << 16) | (valor << 8) | valor;
                if (noisyImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB |= 0xFF000000;
                }

                noisyImage.setRGB(x, y, nuevoRGB);
            }
        }

        // Paso 3: guardar imagen con ruido para futura comparación
        imagenConRuido = deepCopy(noisyImage);

        // Paso 4: actualizar imagen actual con la imagen ruidosa
        imagenActual = noisyImage;
    }

    public void addRayleighNoise() {
        if (imagenActual == null) {
            return;
        }
        Random rand = new Random();

        // Paso 1: convertir a escala de grises
        convertirEscalaGrises();

        BufferedImage noisyImage = deepCopy(imagenActual);

        // Paso 2: aplicar ruido Rayleigh a cada píxel gris
        for (int y = 0; y < noisyImage.getHeight(); y++) {
            for (int x = 0; x < noisyImage.getWidth(); x++) {
                int rgb = noisyImage.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;

                int noise = (int) (Math.sqrt(-2 * Math.log(rand.nextDouble())) * 30);
                int valor = Math.min(Math.max(gray + noise, 0), 255);

                int nuevoRGB = (valor << 16) | (valor << 8) | valor;
                if (noisyImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB |= 0xFF000000;
                }

                noisyImage.setRGB(x, y, nuevoRGB);
            }
        }

        // Paso 3: guardar imagen con ruido para comparación
        imagenConRuido = deepCopy(noisyImage);

        // Paso 4: actualizar imagen actual
        imagenActual = noisyImage;
    }

    public void addSaltAndPepperNoise(double probability) {
        if (imagenActual == null) {
            return;
        }
        Random rand = new Random();

        // Paso 1: convertir la imagen actual a escala de grises
        convertirEscalaGrises();

        BufferedImage noisyImage = deepCopy(imagenActual);

        // Paso 2: aplicar ruido sal y pimienta
        for (int y = 0; y < noisyImage.getHeight(); y++) {
            for (int x = 0; x < noisyImage.getWidth(); x++) {
                if (rand.nextDouble() < probability) {
                    int valor = rand.nextBoolean() ? 255 : 0; // sal o pimienta
                    int nuevoRGB = (valor << 16) | (valor << 8) | valor;
                    if (noisyImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                        nuevoRGB |= 0xFF000000;
                    }
                    noisyImage.setRGB(x, y, nuevoRGB);
                }
            }
        }

        // Paso 3: guardar imagen con ruido
        imagenConRuido = deepCopy(noisyImage);

        // Paso 4: actualizar imagen actual
        imagenActual = noisyImage;
    }

    public void addExponentialNoise() {
        if (imagenActual == null) {
            return;
        }
        Random rand = new Random();

        // Paso 1: convertir imagen actual a escala de grises
        convertirEscalaGrises();

        BufferedImage noisyImage = deepCopy(imagenActual);

        // Paso 2: aplicar ruido exponencial a cada píxel gris
        for (int y = 0; y < noisyImage.getHeight(); y++) {
            for (int x = 0; x < noisyImage.getWidth(); x++) {
                int rgb = noisyImage.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;

                int noise = (int) (-Math.log(1 - rand.nextDouble()) * 20); // Distribución exponencial
                int valor = Math.min(Math.max(gray + noise, 0), 255);

                int nuevoRGB = (valor << 16) | (valor << 8) | valor;
                if (noisyImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB |= 0xFF000000;
                }

                noisyImage.setRGB(x, y, nuevoRGB);
            }
        }

        // Paso 3: guardar imagen con ruido para comparar luego
        imagenConRuido = deepCopy(noisyImage);

        // Paso 4: actualizar imagen actual
        imagenActual = noisyImage;
    }

    public void addUniformNoise() {
        if (imagenActual == null) {
            return;
        }
        Random rand = new Random();

        // Paso 1: convertir la imagen actual a escala de grises
        convertirEscalaGrises();

        BufferedImage noisyImage = deepCopy(imagenActual);

        // Paso 2: aplicar ruido uniforme
        for (int y = 0; y < noisyImage.getHeight(); y++) {
            for (int x = 0; x < noisyImage.getWidth(); x++) {
                int rgb = noisyImage.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;

                int noise = rand.nextInt(50) - 25; // Rango [-25, +24]
                int valor = Math.min(Math.max(gray + noise, 0), 255);

                int nuevoRGB = (valor << 16) | (valor << 8) | valor;
                if (noisyImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB |= 0xFF000000;
                }

                noisyImage.setRGB(x, y, nuevoRGB);
            }
        }

        // Paso 3: guardar imagen con ruido
        imagenConRuido = deepCopy(noisyImage);

        // Paso 4: actualizar imagen actual
        imagenActual = noisyImage;
    }

    public void addGammaNoise() {
        if (imagenActual == null) {
            return;
        }
        Random rand = new Random();

        // Paso 1: convertir la imagen a escala de grises
        convertirEscalaGrises();

        BufferedImage noisyImage = deepCopy(imagenActual);

        // Paso 2: aplicar ruido gamma simulado
        for (int y = 0; y < noisyImage.getHeight(); y++) {
            for (int x = 0; x < noisyImage.getWidth(); x++) {
                int rgb = noisyImage.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;

                int noise = (int) (rand.nextGaussian() * 30 + 10); // Simulación de gamma
                int valor = Math.min(Math.max(gray + noise, 0), 255);

                int nuevoRGB = (valor << 16) | (valor << 8) | valor;
                if (noisyImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB |= 0xFF000000;
                }

                noisyImage.setRGB(x, y, nuevoRGB);
            }
        }

        // Paso 3: guardar la imagen con ruido para comparación
        imagenConRuido = deepCopy(noisyImage);

        // Paso 4: actualizar imagen actual
        imagenActual = noisyImage;
    }

    public void addCoherentNoise() {
        if (imagenActual == null) {
            return;
        }

        // Paso 1: convertir la imagen actual a escala de grises
        convertirEscalaGrises();

        BufferedImage noisyImage = deepCopy(imagenActual);

        // Paso 2: aplicar ruido coherente (variación ondulada)
        for (int y = 0; y < noisyImage.getHeight(); y++) {
            for (int x = 0; x < noisyImage.getWidth(); x++) {
                int rgb = noisyImage.getRGB(x, y);
                int gray = (rgb >> 16) & 0xFF;

                int noise = (int) (50 * Math.sin(x * 0.1)); // ruido en patrón de onda
                int valor = Math.min(Math.max(gray + noise, 0), 255);

                int nuevoRGB = (valor << 16) | (valor << 8) | valor;
                if (noisyImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB |= 0xFF000000;
                }

                noisyImage.setRGB(x, y, nuevoRGB);
            }
        }

        // Paso 3: guardar imagen con ruido para comparación
        imagenConRuido = deepCopy(noisyImage);

        // Paso 4: actualizar imagen actual
        imagenActual = noisyImage;
    }

    // Método auxiliar
    private BufferedImage deepCopy(BufferedImage img) {
        if (img == null) {
            return null;
        }
        ColorModel cm = img.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = img.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public void addSaltNoise() {
        if (imagenActual == null) {
            return;
        }
        Random rand = new Random();

        // Paso 1: convertir la imagen actual a escala de grises
        convertirEscalaGrises();

        BufferedImage noisyImage = deepCopy(imagenActual);

        // Paso 2: aplicar ruido sal
        for (int y = 0; y < noisyImage.getHeight(); y++) {
            for (int x = 0; x < noisyImage.getWidth(); x++) {
                if (rand.nextDouble() < 0.05) { // 5% de probabilidad de agregar sal
                    int nuevoRGB = 0xFFFFFFFF; // Blanco (sal)
                    if (noisyImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                        nuevoRGB |= 0xFF000000; // Asegurar que sea ARGB
                    }
                    noisyImage.setRGB(x, y, nuevoRGB);
                }
            }
        }

        // Paso 3: guardar imagen con ruido para comparación
        imagenConRuido = deepCopy(noisyImage);

        // Paso 4: actualizar imagen actual
        imagenActual = noisyImage;
    }

    public void addPepperNoise() {
        if (imagenActual == null) {
            return;
        }
        Random rand = new Random();

        // Paso 1: convertir la imagen actual a escala de grises
        convertirEscalaGrises();

        BufferedImage noisyImage = deepCopy(imagenActual);

        // Paso 2: aplicar ruido pimienta
        for (int y = 0; y < noisyImage.getHeight(); y++) {
            for (int x = 0; x < noisyImage.getWidth(); x++) {
                if (rand.nextDouble() < 0.05) { // 5% de probabilidad de agregar pimienta
                    int nuevoRGB = 0xFF000000; // Negro (pimienta)
                    if (noisyImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                        nuevoRGB |= 0xFF000000; // Asegurar que sea ARGB
                    }
                    noisyImage.setRGB(x, y, nuevoRGB);
                }
            }
        }

        // Paso 3: guardar imagen con ruido para comparación
        imagenConRuido = deepCopy(noisyImage);

        // Paso 4: actualizar imagen actual
        imagenActual = noisyImage;
    }

    // FILTROS
    public void convertirEscalaGrises() {
        BufferedImage base = (imagenActual != null) ? deepCopy(imagenActual) : deepCopy(imagenOriginal);

        if (base == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        for (int x = 0; x < base.getWidth(); x++) {
            for (int y = 0; y < base.getHeight(); y++) {
                int rgb = base.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                int gris = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                int nuevoRGB;
                if (base.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (gris << 16) | (gris << 8) | gris;
                } else {
                    nuevoRGB = (gris << 16) | (gris << 8) | gris;
                }
                base.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = base;
    }

    public void convertToYIQprocessingY() {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        BufferedImage yiqImage = deepCopy(imagenOriginal);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = yiqImage.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Conversión a YIQ (solo tomamos Y)
                int Y = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                int nuevoRGB;
                if (yiqImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (Y << 16) | (Y << 8) | Y;
                } else {
                    nuevoRGB = (Y << 16) | (Y << 8) | Y;
                }
                yiqImage.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = yiqImage;
    }

    public void convertToHSVprocessingV() {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        BufferedImage hsvImage = deepCopy(imagenOriginal);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = hsvImage.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                float[] hsv = new float[3];
                Color.RGBtoHSB(r, g, b, hsv);

                // Extraer el valor (V) del HSV
                int V = (int) (hsv[2] * 255);

                int nuevoRGB;
                if (hsvImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (V << 16) | (V << 8) | V;
                } else {
                    nuevoRGB = (V << 16) | (V << 8) | V;
                }
                hsvImage.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = hsvImage;
    }

    public void convertToHSIprocessingI() {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        BufferedImage hsiImage = deepCopy(imagenOriginal);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = hsiImage.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Intensidad en el espacio HSI
                int I = (r + g + b) / 3;

                int nuevoRGB;
                if (hsiImage.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (I << 16) | (I << 8) | I;
                } else {
                    nuevoRGB = (I << 16) | (I << 8) | I;
                }
                hsiImage.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = hsiImage;
    }

    public void extractColorChannel(String color) {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        BufferedImage result = deepCopy(imagenOriginal);
        for (int y = 0; y < result.getHeight(); y++) {
            for (int x = 0; x < result.getWidth(); x++) {
                int rgb = result.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int nuevoRGB = 0;
                switch (color.toLowerCase()) {
                    case "red":
                        nuevoRGB = result.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) | (r << 16) : (r << 16);
                        break;
                    case "green":
                        nuevoRGB = result.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) | (g << 8) : (g << 8);
                        break;
                    case "blue":
                        nuevoRGB = result.getType() == BufferedImage.TYPE_INT_ARGB ? (0xFF << 24) | b : b;
                        break;
                    default:
                        nuevoRGB = rgb; // Si el color no es válido, no cambiamos nada
                        break;
                }
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    // Métodos auxiliares para histogramas (útiles para otras funciones)
    public int[][] computeRGBHistogram() {
        return computeRGBHistogram(imagenActual);
    }

    public int[] computeGrayHistogram() {
        return computeGrayHistogram(imagenActual);
    }

    public int[][] computeRGBHistogram(BufferedImage image) {
        if (image == null) {
            return new int[3][256];
        }
        int[][] histogram = new int[3][256]; // R, G, B
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                histogram[0][r]++; // Histograma de Rojo
                histogram[1][g]++; // Histograma de Verde
                histogram[2][b]++; // Histograma de Azul
            }
        }
        return histogram;
    }

    public int[] computeGrayHistogram(BufferedImage image) {
        if (image == null) {
            return new int[256];
        }
        int[] histogram = new int[256];
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Convertir a escala de grises usando YIQ (Canal Y)
                int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);

                histogram[gray]++;
            }
        }
        return histogram;
    }

    /**
     * Devuelve las matrices de canales RGB de la imagen actual en un arreglo 3D:
     * result[0] = canal R [height][width]
     * result[1] = canal G [height][width]
     * result[2] = canal B [height][width]
     */
    public int[][][] getRGBChannelsMatrix() {
        if (imagenActual == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        int[][][] channels = new int[3][height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                channels[0][y][x] = (rgb >> 16) & 0xFF; // R
                channels[1][y][x] = (rgb >> 8) & 0xFF;  // G
                channels[2][y][x] = rgb & 0xFF;         // B
            }
        }
        return channels;
    }

    /**
     * Aplica K-means sobre los colores (RGB) de la imagen actual.
     * Genera una imagen segmentada donde cada píxel toma el color del centroide del grupo.
     * Se actualiza `imagenActual` y también devuelve la imagen resultante.
     */
    public BufferedImage kmeansColorQuantization(int k) {
        if (imagenActual == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("El número de grupos debe ser mayor que 0.");
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        int n = width * height;

        // Extraer píxeles
        int[][] pixels = new int[n][3];
        int idx = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                pixels[idx][0] = (rgb >> 16) & 0xFF;
                pixels[idx][1] = (rgb >> 8) & 0xFF;
                pixels[idx][2] = rgb & 0xFF;
                idx++;
            }
        }

        if (k > n) {
            k = n; // limite práctico
        }

        Random rnd = new Random(0); // semilla fija para reproducibilidad

        // Inicializar centroides eligiendo k píxeles aleatorios
        double[][] centroids = new double[k][3];
        for (int i = 0; i < k; i++) {
            int p = rnd.nextInt(n);
            centroids[i][0] = pixels[p][0];
            centroids[i][1] = pixels[p][1];
            centroids[i][2] = pixels[p][2];
        }

        int[] labels = new int[n];
        boolean changed = true;
        int maxIter = 25;

        for (int iter = 0; iter < maxIter && changed; iter++) {
            changed = false;

            // Asignación: cada píxel al centroide más cercano
            for (int i = 0; i < n; i++) {
                double bestDist = Double.MAX_VALUE;
                int best = 0;
                for (int c = 0; c < k; c++) {
                    double dr = pixels[i][0] - centroids[c][0];
                    double dg = pixels[i][1] - centroids[c][1];
                    double db = pixels[i][2] - centroids[c][2];
                    double dist = dr * dr + dg * dg + db * db;
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = c;
                    }
                }
                if (labels[i] != best) {
                    labels[i] = best;
                    changed = true;
                }
            }

            // Recomputar centroides
            double[][] sum = new double[k][3];
            int[] count = new int[k];
            for (int i = 0; i < n; i++) {
                int l = labels[i];
                sum[l][0] += pixels[i][0];
                sum[l][1] += pixels[i][1];
                sum[l][2] += pixels[i][2];
                count[l]++;
            }
            for (int c = 0; c < k; c++) {
                if (count[c] > 0) {
                    centroids[c][0] = sum[c][0] / count[c];
                    centroids[c][1] = sum[c][1] / count[c];
                    centroids[c][2] = sum[c][2] / count[c];
                } else {
                    // reubicar centroide vacío
                    int p = rnd.nextInt(n);
                    centroids[c][0] = pixels[p][0];
                    centroids[c][1] = pixels[p][1];
                    centroids[c][2] = pixels[p][2];
                }
            }
        }

        // Construir imagen segmentada
        BufferedImage segmented = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        idx = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int label = labels[idx++];
                int r = (int) Math.round(centroids[label][0]);
                int g = (int) Math.round(centroids[label][1]);
                int b = (int) Math.round(centroids[label][2]);
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));
                int color = (r << 16) | (g << 8) | b;
                segmented.setRGB(x, y, color);
            }
        }

        imagenActual = segmented;
        return segmented;
    }

    // Filtrar canal rojo
    public void filtrarCanalRojo() {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        BufferedImage redImage = deepCopy(imagenOriginal);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = redImage.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = 0;
                int b = 0;
                int newRGB = (r << 16) | (g << 8) | b;
                redImage.setRGB(x, y, newRGB);
            }
        }

        imagenActual = redImage;
    }

    // Filtrar canal verde
    public void filtrarCanalVerde() {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        BufferedImage greenImage = deepCopy(imagenOriginal);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = greenImage.getRGB(x, y);
                int r = 0;
                int g = (rgb >> 8) & 0xFF;
                int b = 0;
                int newRGB = (r << 16) | (g << 8) | b;
                greenImage.setRGB(x, y, newRGB);
            }
        }

        imagenActual = greenImage;
    }

    // Filtrar canal azul
    public void filtrarCanalAzul() {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        BufferedImage blueImage = deepCopy(imagenOriginal);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = blueImage.getRGB(x, y);
                int r = 0;
                int g = 0;
                int b = rgb & 0xFF;
                int newRGB = (r << 16) | (g << 8) | b;
                blueImage.setRGB(x, y, newRGB);
            }
        }

        imagenActual = blueImage;
    }

// BINARIZADOS
    public void binarizeWith1Threshold(int threshold) {
        if (imagenActual == null) {
            return;
        }
        // Convertir a YIQ para obtener el canal Y
        convertToYIQprocessingY();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = imagenActual.getRGB(x, y) & 0xFF; // Obtener el valor Y
                int binaryValue = (pixel >= threshold) ? 255 : 0; // Binarizar
                binaryImage.setRGB(x, y, (binaryValue << 16) | (binaryValue << 8) | binaryValue);
            }
        }
        imagenActual = binaryImage;
    }

    public void binarizeWithTwoThresholds(int threshold1, int threshold2) {
        if (imagenActual == null) {
            return;
        }
        // Convertir a YIQ para obtener el canal Y
        convertToYIQprocessingY();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = imagenActual.getRGB(x, y) & 0xFF; // Obtener el valor Y
                int binaryValue;
                if (pixel > threshold2) {
                    binaryValue = 255; // Blanco
                } else if (pixel > threshold1) {
                    binaryValue = 127; // Gris
                } else {
                    binaryValue = 0; // Negro
                }
                binaryImage.setRGB(x, y, (binaryValue << 16) | (binaryValue << 8) | binaryValue);
            }
        }
        imagenActual = binaryImage;
    }

    public void binarizeWithThreeThresholds(int threshold1, int threshold2, int threshold3) {
        if (imagenActual == null) {
            return;
        }
        // Convertir a YIQ para obtener el canal Y
        convertToYIQprocessingY();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage binaryImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = imagenActual.getRGB(x, y) & 0xFF; // Obtener el valor Y
                int binaryValue;
                if (pixel <= threshold1) {
                    binaryValue = 0; // Negro
                } else if (pixel <= threshold2) {
                    binaryValue = 127; // Gris claro
                } else if (pixel <= threshold3) {
                    binaryValue = 191; // Gris oscuro
                } else {
                    binaryValue = 255; // Blanco
                }
                binaryImage.setRGB(x, y, (binaryValue << 16) | (binaryValue << 8) | binaryValue);
            }
        }
        imagenActual = binaryImage;
    }

    public void invertBinarization() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage invertedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = imagenActual.getRGB(x, y) & 0xFF; // Obtener el valor binarizado
                int invertedValue = (pixel == 255) ? 0 : 255; // Invertir
                invertedImage.setRGB(x, y, (invertedValue << 16) | (invertedValue << 8) | invertedValue);
            }
        }
        imagenActual = invertedImage;
    }

    public void convertBinaryToRGB() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage rgbImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = imagenActual.getRGB(x, y) & 0xFF; // Obtener el valor Y
                int r, g, b;
                if (pixel < 85) {
                    r = 0;
                    g = 0;
                    b = 255; // Azul
                } else if (pixel < 170) {
                    r = 0;
                    g = 255;
                    b = 0; // Verde
                } else {
                    r = 255;
                    g = 0;
                    b = 0; // Rojo
                }
                rgbImg.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        imagenActual = rgbImg;
    }

// CONVERSIONES
    public void convertToLab() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage labImage = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int R = (rgb >> 16) & 0xFF;
                int G = (rgb >> 8) & 0xFF;
                int B = rgb & 0xFF;

                double Rn = R / 255.0;
                double Gn = G / 255.0;
                double Bn = B / 255.0;

                // Corrección gamma inversa
                Rn = (Rn <= 0.04045) ? (Rn / 12.92) : Math.pow((Rn + 0.055) / 1.055, 2.4);
                Gn = (Gn <= 0.04045) ? (Gn / 12.92) : Math.pow((Gn + 0.055) / 1.055, 2.4);
                Bn = (Bn <= 0.04045) ? (Bn / 12.92) : Math.pow((Bn + 0.055) / 1.055, 2.4);

                // Conversión a XYZ
                double X = Rn * 0.4124564 + Gn * 0.3575761 + Bn * 0.1804375;
                double Y = Rn * 0.2126729 + Gn * 0.7151522 + Bn * 0.0721750;
                double Z = Rn * 0.0193339 + Gn * 0.1191920 + Bn * 0.9503041;

                // Normalización respecto al punto blanco D65
                double Xn = 0.95047;
                double Yn = 1.00000;
                double Zn = 1.08883;

                double Xr = X / Xn;
                double Yr = Y / Yn;
                double Zr = Z / Zn;

                // Conversión a Lab
                double L = (Yr > 0.008856) ? (116.0 * f(Yr) - 16.0) : (903.3 * Yr);
                double a = 500.0 * (f(Xr) - f(Yr));
                double b = 200.0 * (f(Yr) - f(Zr));

                // Exagerar a y b, pero con un factor más suave (1.5 en lugar de 2)
                a *= 1.5;
                b *= 1.5;

                // Limitar a y b para evitar valores extremos
                a = Math.max(-128, Math.min(127, a));
                b = Math.max(-128, Math.min(127, b));

                // Mapear L, a, b a los canales R, G, B
                int LInt = (int) Math.round((L / 100.0) * 255.0); // L: 0-100 a 0-255
                int aInt = (int) Math.round(a + 128.0);          // a: -128 a 127 a 0-255
                int bInt = (int) Math.round(b + 128.0);          // b: -128 a 127 a 0-255

                // Asegurar que los valores estén en el rango 0-255
                LInt = Math.max(0, Math.min(255, LInt));
                aInt = Math.max(0, Math.min(255, aInt));
                bInt = Math.max(0, Math.min(255, bInt));

                // Asignar L, a, b a los canales R, G, B de la imagen
                int color;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    color = (0xFF << 24) | (LInt << 16) | (aInt << 8) | bInt;
                } else {
                    color = (LInt << 16) | (aInt << 8) | bInt;
                }
                labImage.setRGB(x, y, color);
            }
        }
        imagenActual = labImage;
    }

    public void convertLabToRGB() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage rgbImage = new BufferedImage(width, height, imagenActual.getType());

        // Punto blanco D65 (mismos valores usados en la conversión RGB a Lab)
        double Xn = 0.95047;
        double Yn = 1.00000;
        double Zn = 1.08883;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int lab = imagenActual.getRGB(x, y);
                int LInt = (lab >> 16) & 0xFF; // Canal R contiene L mapeado
                int aInt = (lab >> 8) & 0xFF;  // Canal G contiene a mapeado
                int bInt = lab & 0xFF;         // Canal B contiene b mapeado

                // Convertir de 0-255 a los rangos originales
                double L = (LInt / 255.0) * 100.0; // L: 0-255 a 0-100
                double a = (aInt - 128.0) / 1.5;   // a: 0-255 a -128-127, deshaciendo factor 1.5
                double b = (bInt - 128.0) / 1.5;   // b: 0-255 a -128-127, deshaciendo factor 1.5

                // Conversión de Lab a XYZ
                double Yr = (L > 8.0) ? Math.pow((L + 16.0) / 116.0, 3) : L / 903.3;
                double fy = (L + 16.0) / 116.0;
                double fx = (a / 500.0) + fy;
                double fz = fy - (b / 200.0);

                double Xr = (fx > 0.206897) ? Math.pow(fx, 3) : (fx - 16.0 / 116.0) / 7.787;
                double Zr = (fz > 0.206897) ? Math.pow(fz, 3) : (fz - 16.0 / 116.0) / 7.787;

                double X = Xr * Xn;
                double Y = Yr * Yn;
                double Z = Zr * Zn;

                // Conversión de XYZ a RGB lineal
                double Rn = X * 3.2404542 - Y * 1.5371385 - Z * 0.4985314;
                double Gn = -X * 0.9692660 + Y * 1.8760108 + Z * 0.0415560;
                double Bn = X * 0.0556434 - Y * 0.2040259 + Z * 1.0572252;

                // Corrección gamma
                Rn = (Rn <= 0.0031308) ? (12.92 * Rn) : (1.055 * Math.pow(Rn, 1.0 / 2.4) - 0.055);
                Gn = (Gn <= 0.0031308) ? (12.92 * Gn) : (1.055 * Math.pow(Gn, 1.0 / 2.4) - 0.055);
                Bn = (Bn <= 0.0031308) ? (12.92 * Bn) : (1.055 * Math.pow(Bn, 1.0 / 2.4) - 0.055);

                // Convertir a valores RGB 0-255
                int R = (int) Math.round(Rn * 255.0);
                int G = (int) Math.round(Gn * 255.0);
                int B = (int) Math.round(Bn * 255.0);

                // Limitar los valores al rango válido 0-255
                R = Math.max(0, Math.min(255, R));
                G = Math.max(0, Math.min(255, G));
                B = Math.max(0, Math.min(255, B));

                // Crear el color RGB y asignarlo a la imagen
                int rgb;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    rgb = (0xFF << 24) | (R << 16) | (G << 8) | B;
                } else {
                    rgb = (R << 16) | (G << 8) | B;
                }
                rgbImage.setRGB(x, y, rgb);
            }
        }
        imagenActual = rgbImage;
    }

    // Método auxiliar para la conversión a Lab
    private double f(double t) {
        return (t > 0.008856) ? Math.cbrt(t) : (t * 7.787 + 16.0 / 116.0);
    }

    public void convertToCMY() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage cmyImage = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Conversión de RGB a CMY
                int C = 255 - r; // Cian es la ausencia de Rojo
                int M = 255 - g; // Magenta es la ausencia de Verde
                int Y = 255 - b; // Amarillo es la ausencia de Azul

                // Set the CMY values in the new image
                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (C << 16) | (M << 8) | Y;
                } else {
                    nuevoRGB = (C << 16) | (M << 8) | Y;
                }
                cmyImage.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = cmyImage;
    }

    public void convertCMYtoRGB() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage rgbImage = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int cmy = imagenActual.getRGB(x, y);
                int c = (cmy >> 16) & 0xFF;
                int m = (cmy >> 8) & 0xFF;
                int yC = cmy & 0xFF;

                int r = 255 - c;
                int g = 255 - m;
                int b = 255 - yC;

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (r << 16) | (g << 8) | b;
                } else {
                    nuevoRGB = (r << 16) | (g << 8) | b;
                }
                rgbImage.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = rgbImage;
    }

    public void convertToYIQ() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage yiqImage = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Conversion from RGB to YIQ
                int Y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                int I = (int) (0.596 * r - 0.274 * g - 0.321 * b);
                int Q = (int) (0.211 * r - 0.523 * g + 0.312 * b);

                Y = Math.max(0, Math.min(255, Y));
                I = Math.max(0, Math.min(255, I + 128));
                Q = Math.max(0, Math.min(255, Q + 128));

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (Y << 16) | (I << 8) | Q;
                } else {
                    nuevoRGB = (Y << 16) | (I << 8) | Q;
                }
                yiqImage.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = yiqImage;
    }

    public void convertYIQtoRGB() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage rgbImage = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int Y = (rgb >> 16) & 0xFF;
                int I = (rgb >> 8) & 0xFF;
                int Q = rgb & 0xFF;

                int R = (int) (Y + 0.956 * (I - 128) + 0.621 * (Q - 128));
                int G = (int) (Y - 0.272 * (I - 128) - 0.647 * (Q - 128));
                int B = (int) (Y - 1.106 * (I - 128) + 1.703 * (Q - 128));

                R = Math.max(0, Math.min(255, R));
                G = Math.max(0, Math.min(255, G));
                B = Math.max(0, Math.min(255, B));

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (R << 16) | (G << 8) | B;
                } else {
                    nuevoRGB = (R << 16) | (G << 8) | B;
                }
                rgbImage.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = rgbImage;
    }

    public void convertToHSI() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage hsiImg = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int R = (rgb >> 16) & 0xFF;
                int G = (rgb >> 8) & 0xFF;
                int B = rgb & 0xFF;

                double r = R / 255.0;
                double g = G / 255.0;
                double b = B / 255.0;

                // Intensity
                double I = (r + g + b) / 3.0;
                double min = Math.min(Math.min(r, g), b);

                // Saturation
                double S = (I == 0) ? 0 : 1 - (min / I);

                // Hue
                double H;
                double num = ((r - g) + (r - b)) / 2.0;
                double den = Math.sqrt((r - g) * (r - g) + (r - b) * (g - b));
                if (den == 0) {
                    H = 0;
                } else {
                    double theta = Math.acos(num / den) * 180 / Math.PI;
                    H = (b <= g) ? theta : (360 - theta);
                }

                // Escalar a [0, 255]
                int HInt = (int) (H / 360.0 * 255);
                int SInt = (int) (S * 255);
                int IInt = (int) (I * 255);

                // Limitar valores a [0, 255]
                HInt = Math.max(0, Math.min(255, HInt));
                SInt = Math.max(0, Math.min(255, SInt));
                IInt = Math.max(0, Math.min(255, IInt));

                // Empaquetar en la imagen combinada HSI
                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (HInt << 16) | (SInt << 8) | IInt;
                } else {
                    nuevoRGB = (HInt << 16) | (SInt << 8) | IInt;
                }
                hsiImg.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = hsiImg;
    }

    public void convertHSItoRGB() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage rgbImage = new BufferedImage(width, height, imagenActual.getType());

        final int MAX_RGB_VALUE = 255;
        final double HUE_MAX = 360.0;
        final double HUE_DIVIDER = 60.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int hsi = imagenActual.getRGB(x, y);
                int H = (hsi >> 16) & 0xFF; // Hue
                int S = (hsi >> 8) & 0xFF;  // Saturation
                int I = hsi & 0xFF;         // Intensity

                double HNorm = H / (double) MAX_RGB_VALUE * HUE_MAX; // [0, 360]
                double SNorm = S / (double) MAX_RGB_VALUE;           // [0, 1]
                double INorm = I / (double) MAX_RGB_VALUE;           // [0, 1]

                double R, G, B;

                if (SNorm == 0) {
                    R = G = B = INorm;
                } else {
                    double HPrime = HNorm / HUE_DIVIDER; // [0, 6]
                    int sector = (int) HPrime;
                    double f = HPrime - sector;
                    double p = INorm * (1 - SNorm);
                    double q = INorm * (1 - SNorm * f);
                    double t = INorm * (1 - SNorm * (1 - f));

                    switch (sector) {
                        case 0: // 0°–60°
                            R = INorm;
                            G = t;
                            B = p;
                            break;
                        case 1: // 60°–120°
                            R = q;
                            G = INorm;
                            B = p;
                            break;
                        case 2: // 120°–180°
                            R = p;
                            G = INorm;
                            B = t;
                            break;
                        case 3: // 180°–240°
                            R = p;
                            G = q;
                            B = INorm;
                            break;
                        case 4: // 240°–300°
                            R = t;
                            G = p;
                            B = INorm;
                            break;
                        default: // 300°–360°
                            R = INorm;
                            G = p;
                            B = q;
                            break;
                    }
                }

                int RInt = (int) (R * MAX_RGB_VALUE);
                int GInt = (int) (G * MAX_RGB_VALUE);
                int BInt = (int) (B * MAX_RGB_VALUE);

                RInt = Math.max(0, Math.min(MAX_RGB_VALUE, RInt));
                GInt = Math.max(0, Math.min(MAX_RGB_VALUE, GInt));
                BInt = Math.max(0, Math.min(MAX_RGB_VALUE, BInt));

                int nuevoRGB;
                if (imagenActual.getType() == BufferedImage.TYPE_INT_ARGB) {
                    nuevoRGB = (0xFF << 24) | (RInt << 16) | (GInt << 8) | BInt;
                } else {
                    nuevoRGB = (RInt << 16) | (GInt << 8) | BInt;
                }
                rgbImage.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = rgbImage;
    }

    public void convertCMYtoCMYK() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage cmykImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        final int MAX_RGB_VALUE = 255;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int cmy = imagenActual.getRGB(x, y);
                int C = (cmy >> 16) & 0xFF; // Cyan
                int M = (cmy >> 8) & 0xFF;  // Magenta
                int Y = cmy & 0xFF;         // Yellow

                double CNorm = C / (double) MAX_RGB_VALUE;
                double MNorm = M / (double) MAX_RGB_VALUE;
                double YNorm = Y / (double) MAX_RGB_VALUE;

                double K = Math.min(CNorm, Math.min(MNorm, YNorm));

                double CPrime, MPrime, YPrime;

                if (K < 1) {
                    CPrime = (CNorm - K) / (1 - K);
                    MPrime = (MNorm - K) / (1 - K);
                    YPrime = (YNorm - K) / (1 - K);
                } else {
                    CPrime = 0;
                    MPrime = 0;
                    YPrime = 0;
                }

                // Scale back to [0, 255]
                int CInt = (int) (CPrime * MAX_RGB_VALUE);
                int MInt = (int) (MPrime * MAX_RGB_VALUE);
                int YInt = (int) (YPrime * MAX_RGB_VALUE);
                int KInt = (int) (K * MAX_RGB_VALUE);

                CInt = Math.max(0, Math.min(MAX_RGB_VALUE, CInt));
                MInt = Math.max(0, Math.min(MAX_RGB_VALUE, MInt));
                YInt = Math.max(0, Math.min(MAX_RGB_VALUE, YInt));
                KInt = Math.max(0, Math.min(MAX_RGB_VALUE, KInt));

                // Usar el canal alfa para K
                cmykImage.setRGB(x, y, (KInt << 24) | (CInt << 16) | (MInt << 8) | YInt);
            }
        }
        imagenActual = cmykImage;
    }

    public void convertToHSV() {
        if (imagenActual == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage hsvImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenActual.getRGB(x, y);
                int R = (rgb >> 16) & 0xFF; // Red
                int G = (rgb >> 8) & 0xFF;  // Green
                int B = rgb & 0xFF;         // Blue

                // Normalize RGB values to [0, 1]
                double RNorm = R / 255.0;
                double GNorm = G / 255.0;
                double BNorm = B / 255.0;

                // Calculate Cmax, Cmin, and delta
                double Cmax = Math.max(RNorm, Math.max(GNorm, BNorm));
                double Cmin = Math.min(RNorm, Math.min(GNorm, BNorm));
                double delta = Cmax - Cmin;

                // Calculate Hue (H)
                double H;
                if (delta == 0) {
                    H = 0;
                } else if (Cmax == RNorm) {
                    H = 60 * ((GNorm - BNorm) / delta);
                } else if (Cmax == GNorm) {
                    H = 60 * ((BNorm - RNorm) / delta + 2);
                } else {
                    H = 60 * ((RNorm - GNorm) / delta + 4);
                }

                if (H < 0) {
                    H += 360;
                }

                // Calculate Saturation (S)
                double S;
                if (Cmax == 0) {
                    S = 0;
                } else {
                    S = delta / Cmax;
                }

                // Calculate Value (V)
                double V = Cmax;

                // Convert H, S, V to integer values in range [0, 255]
                int HInt = (int) (H / 360 * 255);
                int SInt = (int) (S * 255);
                int VInt = (int) (V * 255);

                // Clamp values to [0, 255]
                HInt = Math.max(0, Math.min(255, HInt));
                SInt = Math.max(0, Math.min(255, SInt));
                VInt = Math.max(0, Math.min(255, VInt));

                // Store HSV values in the image (H as R, S as G, V as B)
                hsvImage.setRGB(x, y, new Color(HInt, SInt, VInt).getRGB());
            }
        }

        // Actualizar imagenActual con el resultado
        imagenActual = hsvImage;
    }

    public void convertHSVtoRGB() {
        if (imagenActual == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int hsv = imagenActual.getRGB(x, y);
                int H = (hsv >> 16) & 0xFF; // Hue
                int S = (hsv >> 8) & 0xFF;  // Saturation
                int V = hsv & 0xFF;         // Value

                // Normalize HSV values
                double HNorm = H / 255.0 * 360;
                double SNorm = S / 255.0;
                double VNorm = V / 255.0;

                double R, G, B;

                // If saturation is 0, the color is a shade of gray
                if (SNorm == 0) {
                    R = G = B = VNorm;
                } else {
                    // Calculate intermediate values
                    double C = VNorm * SNorm;
                    double X = C * (1 - Math.abs((HNorm / 60) % 2 - 1));
                    double m = VNorm - C;

                    // Convert based on hue sector
                    if (HNorm < 60) {
                        R = C;
                        G = X;
                        B = 0;
                    } else if (HNorm < 120) {
                        R = X;
                        G = C;
                        B = 0;
                    } else if (HNorm < 180) {
                        R = 0;
                        G = C;
                        B = X;
                    } else if (HNorm < 240) {
                        R = 0;
                        G = X;
                        B = C;
                    } else if (HNorm < 300) {
                        R = X;
                        G = 0;
                        B = C;
                    } else {
                        R = C;
                        G = 0;
                        B = X;
                    }

                    // Add offset
                    R += m;
                    G += m;
                    B += m;
                }

                // Convert to integer RGB values
                int RInt = (int) (R * 255);
                int GInt = (int) (G * 255);
                int BInt = (int) (B * 255);

                // Clamp values to [0, 255]
                RInt = Math.max(0, Math.min(255, RInt));
                GInt = Math.max(0, Math.min(255, GInt));
                BInt = Math.max(0, Math.min(255, BInt));

                // Set RGB pixel
                rgbImage.setRGB(x, y, new Color(RInt, GInt, BInt).getRGB());
            }
        }

        // Actualizar imagenActual con el resultado
        imagenActual = rgbImage;
    }

// DESPLAZAMIENTOS
    /*public void translate(int dx, int dy) {
        if (imagenActual == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage translatedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Rellenar la nueva imagen con negro (fondo)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                translatedImage.setRGB(x, y, 0); // Color negro (0x000000)
            }
        }

        // Copiar los píxeles desplazados
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newX = x + dx;
                int newY = y + dy;

                // Verificar que las nuevas coordenadas estén dentro de los límites
                if (newX >= 0 && newX < width && newY >= 0 && newY < height) {
                    translatedImage.setRGB(newX, newY, imagenActual.getRGB(x, y));
                }
            }
        }

        // Actualizar imagenActual
        imagenActual = translatedImage;
    }*/
    public void translate(int dx, int dy) {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();

        // Calcular las nuevas dimensiones del lienzo para incluir la imagen trasladada
        int newWidth = width + Math.abs(dx);  // Aumentar el ancho si dx es positivo o negativo
        int newHeight = height + Math.abs(dy); // Aumentar la altura si dy es positivo o negativo
        int offsetX = dx >= 0 ? dx : 0;       // Offset para posicionar la imagen (si dx es negativo, el offset es 0)
        int offsetY = dy >= 0 ? dy : 0;       // Offset para posicionar la imagen (si dy es negativo, el offset es 0)

        // Crear una nueva imagen con las dimensiones ajustadas
        BufferedImage translatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        // Rellenar el fondo con un color (por ejemplo, negro)
        Graphics2D g2d = translatedImage.createGraphics();
        g2d.setColor(Color.BLACK); // Color de fondo
        g2d.fillRect(0, 0, newWidth, newHeight);

        // Copiar la imagen original en la posición trasladada
        g2d.drawImage(imagenOriginal, offsetX, offsetY, null);
        g2d.dispose();

        // Actualizar imagenActual
        imagenActual = translatedImage;
    }

    public void rotate(double angle) {
        if (imagenActual == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage rotatedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Convertir el ángulo de grados a radianes
        double radians = Math.toRadians(angle);
        double cosTheta = Math.cos(radians);
        double sinTheta = Math.sin(radians);

        // Centro de la imagen
        double centerX = width / 2.0;
        double centerY = height / 2.0;

        // Rellenar la nueva imagen con negro (fondo)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                rotatedImage.setRGB(x, y, 0); // Color negro
            }
        }

        // Rotación usando transformación inversa
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Coordenadas relativas al centro
                double relX = x - centerX;
                double relY = y - centerY;

                // Aplicar la rotación inversa
                int srcX = (int) (relX * cosTheta + relY * sinTheta + centerX);
                int srcY = (int) (-relX * sinTheta + relY * cosTheta + centerY);

                // Verificar que las coordenadas de origen estén dentro de los límites
                if (srcX >= 0 && srcX < width && srcY >= 0 && srcY < height) {
                    rotatedImage.setRGB(x, y, imagenActual.getRGB(srcX, srcY));
                }
            }
        }

        // Actualizar imagenActual
        imagenActual = rotatedImage;
    }

    /*public void interpolate(double scale) {
        if (imagenActual == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }
        if (scale <= 0) {
            throw new IllegalArgumentException("El factor de escala debe ser mayor que 0.");
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        int newWidth = (int) (width * scale);
        int newHeight = (int) (height * scale);

        // Asegurarse de que las nuevas dimensiones sean al menos 1
        newWidth = Math.max(1, newWidth);
        newHeight = Math.max(1, newHeight);

        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        // Interpolación bilineal
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                // Mapear coordenadas de la imagen escalada a la imagen original
                double srcX = x / scale;
                double srcY = y / scale;

                // Coordenadas enteras y fraccionarias
                int x1 = (int) srcX;
                int y1 = (int) srcY;
                double fx = srcX - x1;
                double fy = srcY - y1;

                // Asegurarse de que las coordenadas estén dentro de los límites
                int x2 = Math.min(x1 + 1, width - 1);
                int y2 = Math.min(y1 + 1, height - 1);
                x1 = Math.max(0, x1);
                y1 = Math.max(0, y1);

                // Obtener los valores de los píxeles vecinos
                int rgb11 = imagenActual.getRGB(x1, y1);
                int rgb12 = imagenActual.getRGB(x1, y2);
                int rgb21 = imagenActual.getRGB(x2, y1);
                int rgb22 = imagenActual.getRGB(x2, y2);

                // Extraer componentes RGB
                int r11 = (rgb11 >> 16) & 0xFF;
                int g11 = (rgb11 >> 8) & 0xFF;
                int b11 = rgb11 & 0xFF;
                int r12 = (rgb12 >> 16) & 0xFF;
                int g12 = (rgb12 >> 8) & 0xFF;
                int b12 = rgb12 & 0xFF;
                int r21 = (rgb21 >> 16) & 0xFF;
                int g21 = (rgb21 >> 8) & 0xFF;
                int b21 = rgb21 & 0xFF;
                int r22 = (rgb22 >> 16) & 0xFF;
                int g22 = (rgb22 >> 8) & 0xFF;
                int b22 = rgb22 & 0xFF;

                // Interpolación bilineal
                double r = r11 * (1 - fx) * (1 - fy) + r21 * fx * (1 - fy) + r12 * (1 - fx) * fy + r22 * fx * fy;
                double g = g11 * (1 - fx) * (1 - fy) + g21 * fx * (1 - fy) + g12 * (1 - fx) * fy + g22 * fx * fy;
                double b = b11 * (1 - fx) * (1 - fy) + b21 * fx * (1 - fy) + b12 * (1 - fx) * fy + b22 * fx * fy;

                // Asegurarse de que los valores estén en el rango [0, 255]
                int rInt = (int) Math.min(255, Math.max(0, r));
                int gInt = (int) Math.min(255, Math.max(0, g));
                int bInt = (int) Math.min(255, Math.max(0, b));

                // Establecer el píxel en la imagen escalada
                scaledImage.setRGB(x, y, new Color(rInt, gInt, bInt).getRGB());
            }
        }

        // Actualizar imagenActual
        imagenActual = scaledImage;
    }*/
    public void interpolate(double scale) {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }
        if (scale <= 0) {
            throw new IllegalArgumentException("El factor de escala debe ser mayor que 0.");
        }

        // Acumular el factor de escala
        totalScale *= scale;

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        int newWidth = (int) (width * totalScale);
        int newHeight = (int) (height * totalScale);

        // Asegurarse de que las nuevas dimensiones sean al menos 1
        newWidth = Math.max(1, newWidth);
        newHeight = Math.max(1, newHeight);

        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

        // Interpolación bilineal
        for (int y = 0; y < newHeight; y++) {
            for (int x = 0; x < newWidth; x++) {
                // Mapear coordenadas de la imagen escalada a la imagen original
                double srcX = x / totalScale;
                double srcY = y / totalScale;

                // Coordenadas enteras y fraccionarias
                int x1 = (int) srcX;
                int y1 = (int) srcY;
                double fx = srcX - x1;
                double fy = srcY - y1;

                // Asegurarse de que las coordenadas estén dentro de los límites
                int x2 = Math.min(x1 + 1, width - 1);
                int y2 = Math.min(y1 + 1, height - 1);
                x1 = Math.max(0, x1);
                y1 = Math.max(0, y1);

                // Obtener los valores de los píxeles vecinos
                int rgb11 = imagenOriginal.getRGB(x1, y1);
                int rgb12 = imagenOriginal.getRGB(x1, y2);
                int rgb21 = imagenOriginal.getRGB(x2, y1);
                int rgb22 = imagenOriginal.getRGB(x2, y2);

                // Extraer componentes RGB
                int r11 = (rgb11 >> 16) & 0xFF;
                int g11 = (rgb11 >> 8) & 0xFF;
                int b11 = rgb11 & 0xFF;
                int r12 = (rgb12 >> 16) & 0xFF;
                int g12 = (rgb12 >> 8) & 0xFF;
                int b12 = rgb12 & 0xFF;
                int r21 = (rgb21 >> 16) & 0xFF;
                int g21 = (rgb21 >> 8) & 0xFF;
                int b21 = rgb21 & 0xFF;
                int r22 = (rgb22 >> 16) & 0xFF;
                int g22 = (rgb22 >> 8) & 0xFF;
                int b22 = rgb22 & 0xFF;

                // Interpolación bilineal
                double r = r11 * (1 - fx) * (1 - fy) + r21 * fx * (1 - fy) + r12 * (1 - fx) * fy + r22 * fx * fy;
                double g = g11 * (1 - fx) * (1 - fy) + g21 * fx * (1 - fy) + g12 * (1 - fx) * fy + g22 * fx * fy;
                double b = b11 * (1 - fx) * (1 - fy) + b21 * fx * (1 - fy) + b12 * (1 - fx) * fy + b22 * fx * fy;

                // Asegurarse de que los valores estén en el rango [0, 255]
                int rInt = (int) Math.min(255, Math.max(0, r));
                int gInt = (int) Math.min(255, Math.max(0, g));
                int bInt = (int) Math.min(255, Math.max(0, b));

                // Establecer el píxel en la imagen escalada
                scaledImage.setRGB(x, y, new Color(rInt, gInt, bInt).getRGB());
            }
        }

        // Actualizar imagenActual
        imagenActual = scaledImage;
    }

// EXTRACCION DE CANALES
    public BufferedImage[] getCMYChannels() {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        BufferedImage cImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage mImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage yImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenOriginal.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Calcular valores CMY
                int c = 255 - r; // Cian = 255 - R
                int m = 255 - g; // Magenta = 255 - G
                int yVal = 255 - b; // Amarillo = 255 - B

                // Representar cada canal con su color correspondiente
                cImage.setRGB(x, y, new Color(0, c, c).getRGB()); // Cian: R=0, G=C, B=C
                mImage.setRGB(x, y, new Color(m, 0, m).getRGB()); // Magenta: R=M, G=0, B=M
                yImage.setRGB(x, y, new Color(yVal, yVal, 0).getRGB()); // Amarillo: R=Y, G=Y, B=0
            }
        }

        return new BufferedImage[]{cImage, mImage, yImage};
    }

    public BufferedImage[] getYIQChannels() {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        BufferedImage yImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage iImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage qImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenOriginal.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Convertir a YIQ
                float Y = 0.299f * r + 0.587f * g + 0.114f * b;
                float I = 0.596f * r - 0.274f * g - 0.322f * b;
                float Q = 0.211f * r - 0.523f * g + 0.312f * b;

                // Normalizar I y Q para visualización
                int iVal = (int) (((I + 128) / 256.0) * 255); // Normalizar al rango [0, 255]
                int qVal = (int) (((Q + 128) / 256.0) * 255); // Normalizar al rango [0, 255]

                // Y se muestra en escala de grises
                yImage.setRGB(x, y, new Color((int) Y, (int) Y, (int) Y).getRGB());

                // I se muestra en tonos de rojo-naranja a cian
                iImage.setRGB(x, y, new Color(iVal, 128, 255 - iVal).getRGB()); // Aproximación visual

                // Q se muestra en tonos de verde a púrpura
                qImage.setRGB(x, y, new Color(128, 255 - qVal, qVal).getRGB()); // Aproximación visual
            }
        }

        return new BufferedImage[]{yImage, iImage, qImage};
    }

    public BufferedImage[] getHSIChannels() {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        BufferedImage hImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage sImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage iImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenOriginal.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Convertir a HSI
                double I = (r + g + b) / 3.0;
                double min = Math.min(Math.min(r, g), b);
                double S = (I == 0) ? 0 : 1 - (min / I);
                double theta = Math.acos((0.5 * ((r - g) + (r - b)))
                        / Math.sqrt((r - g) * (r - g) + (r - b) * (g - b) + 0.0001));
                double H = (b <= g) ? theta : (2 * Math.PI - theta);

                // Normalizar para visualización
                float hVal = (float) (H / (2 * Math.PI)); // H en [0, 1] para HSB
                int sVal = (int) (S * 255); // S en [0, 255]
                int iVal = (int) I; // I ya está en [0, 255]

                // H se muestra como color real (usando HSB)
                int hColor = Color.HSBtoRGB(hVal, 1.0f, 1.0f);
                hImage.setRGB(x, y, hColor);

                // S e I se muestran en escala de grises
                sImage.setRGB(x, y, new Color(sVal, sVal, sVal).getRGB());
                iImage.setRGB(x, y, new Color(iVal, iVal, iVal).getRGB());
            }
        }

        return new BufferedImage[]{hImage, sImage, iImage};
    }

    public BufferedImage[] getHSVChannels() {
        if (imagenOriginal == null) {
            throw new IllegalArgumentException("No hay una imagen cargada.");
        }

        int width = imagenOriginal.getWidth();
        int height = imagenOriginal.getHeight();
        BufferedImage hImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage sImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        BufferedImage vImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = imagenOriginal.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Convertir a HSV
                float[] hsv = new float[3];
                Color.RGBtoHSB(r, g, b, hsv);

                // Normalizar para visualización
                float hVal = hsv[0]; // H en [0, 1]
                int sVal = (int) (hsv[1] * 255); // S en [0, 255]
                int vVal = (int) (hsv[2] * 255); // V en [0, 255]

                // H se muestra como color real
                int hColor = Color.HSBtoRGB(hVal, 1.0f, 1.0f);
                hImage.setRGB(x, y, hColor);

                // S y V se muestran en escala de grises
                sImage.setRGB(x, y, new Color(sVal, sVal, sVal).getRGB());
                vImage.setRGB(x, y, new Color(vVal, vVal, vVal).getRGB());
            }
        }

        return new BufferedImage[]{hImage, sImage, vImage};
    }

    public void cannyEdgeDetection() {
        if (imagenActual == null) {
            return;
        }

        // Paso 1: Suavizar la imagen con un filtro Gaussiano
        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage blurred = new BufferedImage(width, height, imagenActual.getType());

        float[][] gaussianKernel = {
            {1f, 4f, 7f, 4f, 1f},
            {4f, 16f, 26f, 16f, 4f},
            {7f, 26f, 41f, 26f, 7f},
            {4f, 16f, 26f, 16f, 4f},
            {1f, 4f, 7f, 4f, 1f}
        };
        float kernelSum = 273f;

        for (int y = 2; y < height - 2; y++) {
            for (int x = 2; x < width - 2; x++) {
                float sum = 0;
                for (int ky = -2; ky <= 2; ky++) {
                    for (int kx = -2; kx <= 2; kx++) {
                        int pixel = imagenActual.getRGB(x + kx, y + ky);
                        int gray = (pixel >> 16) & 0xFF;
                        sum += gray * gaussianKernel[ky + 2][kx + 2];
                    }
                }
                int newGray = Math.min(255, Math.max(0, Math.round(sum / kernelSum)));
                int nuevoRGB = (newGray << 16) | (newGray << 8) | newGray;
                blurred.setRGB(x, y, nuevoRGB);
            }
        }

        // Paso 2: Calcular la magnitud y dirección del gradiente
        float[] gxKernel = {-1, 0, 1, -2, 0, 2, -1, 0, 1};
        float[] gyKernel = {-1, -2, -1, 0, 0, 0, 1, 2, 1};

        float[][] magnitude = new float[width][height];
        float[][] direction = new float[width][height];

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                float gx = 0, gy = 0;
                int idx = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = blurred.getRGB(x + kx, y + ky);
                        int gray = (pixel >> 16) & 0xFF;
                        gx += gray * gxKernel[idx];
                        gy += gray * gyKernel[idx];
                        idx++;
                    }
                }
                magnitude[x][y] = (float) Math.hypot(gx, gy);
                direction[x][y] = (float) Math.atan2(gy, gx);
            }
        }

        // Paso 3: Supresión no máxima
        BufferedImage suppressed = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                float angle = direction[x][y] * (180f / (float) Math.PI);
                if (angle < 0) {
                    angle += 180;
                }

                float mag = magnitude[x][y];
                float mag1 = 0, mag2 = 0;

                if ((0 <= angle && angle < 22.5) || (157.5 <= angle && angle <= 180)) {
                    mag1 = magnitude[x + 1][y];
                    mag2 = magnitude[x - 1][y];
                } else if (22.5 <= angle && angle < 67.5) {
                    mag1 = magnitude[x + 1][y - 1];
                    mag2 = magnitude[x - 1][y + 1];
                } else if (67.5 <= angle && angle < 112.5) {
                    mag1 = magnitude[x][y + 1];
                    mag2 = magnitude[x][y - 1];
                } else if (112.5 <= angle && angle < 157.5) {
                    mag1 = magnitude[x - 1][y - 1];
                    mag2 = magnitude[x + 1][y + 1];
                }

                if (mag >= mag1 && mag >= mag2) {
                    int val = Math.min(255, Math.max(0, Math.round(mag)));
                    int nuevoRGB = (val << 16) | (val << 8) | val;
                    suppressed.setRGB(x, y, nuevoRGB);
                } else {
                    suppressed.setRGB(x, y, 0);
                }
            }
        }

        // Paso 4: Umbral de histéresis (doble umbral fijo)
        int highThreshold = 40; // Nuevo ajuste
        int lowThreshold = 15;  // Nuevo ajuste

        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int pixel = (suppressed.getRGB(x, y) >> 16) & 0xFF;

                if (pixel >= highThreshold) {
                    result.setRGB(x, y, 0xFFFFFF); // Blanco
                } else if (pixel >= lowThreshold) {
                    boolean connected = false;
                    for (int ky = -1; ky <= 1 && !connected; ky++) {
                        for (int kx = -1; kx <= 1; kx++) {
                            int neighbor = (suppressed.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                            if (neighbor >= highThreshold) {
                                connected = true;
                            }
                        }
                    }
                    result.setRGB(x, y, connected ? 0xFFFFFF : 0x000000);
                } else {
                    result.setRGB(x, y, 0x000000);
                }
            }
        }

        imagenActual = result;
    }

    public void operadorHomogeneidad() {
        if (imagenActual == null) {
            return;
        }

        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int pixelCentro = (imagenActual.getRGB(x, y) >> 16) & 0xFF;

                int[] vecinos = new int[8];
                vecinos[0] = (imagenActual.getRGB(x - 1, y - 1) >> 16) & 0xFF;
                vecinos[1] = (imagenActual.getRGB(x, y - 1) >> 16) & 0xFF;
                vecinos[2] = (imagenActual.getRGB(x + 1, y - 1) >> 16) & 0xFF;
                vecinos[3] = (imagenActual.getRGB(x - 1, y) >> 16) & 0xFF;
                vecinos[4] = (imagenActual.getRGB(x + 1, y) >> 16) & 0xFF;
                vecinos[5] = (imagenActual.getRGB(x - 1, y + 1) >> 16) & 0xFF;
                vecinos[6] = (imagenActual.getRGB(x, y + 1) >> 16) & 0xFF;
                vecinos[7] = (imagenActual.getRGB(x + 1, y + 1) >> 16) & 0xFF;

                int maxDiff = 0;
                for (int i = 0; i < 8; i++) {
                    int diff = Math.abs(pixelCentro - vecinos[i]);
                    if (diff > maxDiff) {
                        maxDiff = diff;
                    }
                }

                int nuevoRGB = (maxDiff << 16) | (maxDiff << 8) | maxDiff;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void operadorDiferencia() {
        if (imagenActual == null) {
            return;
        }

        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 0; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int pixelCentro = (imagenActual.getRGB(x, y) >> 16) & 0xFF;

                int[] vecinos = new int[4];
                vecinos[0] = (imagenActual.getRGB(x - 1, y) >> 16) & 0xFF;   // izquierda
                vecinos[1] = (imagenActual.getRGB(x - 1, y + 1) >> 16) & 0xFF; // esquina inferior izquierda
                vecinos[2] = (imagenActual.getRGB(x, y + 1) >> 16) & 0xFF;   // abajo
                vecinos[3] = (imagenActual.getRGB(x + 1, y + 1) >> 16) & 0xFF; // esquina inferior derecha

                int maxDiff = 0;
                for (int i = 0; i < 4; i++) {
                    int diff = Math.abs(pixelCentro - vecinos[i]);
                    if (diff > maxDiff) {
                        maxDiff = diff;
                    }
                }

                int nuevoRGB = (maxDiff << 16) | (maxDiff << 8) | maxDiff;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void gradientePrimerOrden() {
        if (imagenActual == null) {
            return;
        }

        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Máscara para detectar cambios horizontales (Hf)
        int[][] kernelHf = {
            {-1, 0, 1},
            {-1, 0, 1},
            {-1, 0, 1}
        };

        // Máscara para detectar cambios verticales (Hc)
        int[][] kernelHc = {
            {1, 1, 1},
            {0, 0, 0},
            {-1, -1, -1}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumHf = 0;
                int sumHc = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                        sumHf += pixel * kernelHf[ky + 1][kx + 1];
                        sumHc += pixel * kernelHc[ky + 1][kx + 1];
                    }
                }

                int magnitude = Math.abs(sumHf) + Math.abs(sumHc);
                magnitude = Math.min(255, magnitude); // Limitar a máximo 255

                int nuevoRGB = (magnitude << 16) | (magnitude << 8) | magnitude;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void filtroPrewitt() {
        if (imagenActual == null) {
            return;
        }

        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Máscara horizontal Hf (filas)
        int[][] kernelHf = {
            {-1, 0, 1},
            {-1, 0, 1},
            {-1, 0, 1}
        };

        // Máscara vertical Hc (columnas)
        int[][] kernelHc = {
            {1, 1, 1},
            {0, 0, 0},
            {-1, -1, -1}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumHf = 0;
                int sumHc = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                        sumHf += pixel * kernelHf[ky + 1][kx + 1];
                        sumHc += pixel * kernelHc[ky + 1][kx + 1];
                    }
                }

                int magnitude = Math.abs(sumHf) + Math.abs(sumHc);
                magnitude = Math.min(255, magnitude);

                int nuevoRGB = (magnitude << 16) | (magnitude << 8) | magnitude;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void filtroSobel() {
        if (imagenActual == null) {
            return;
        }

        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Máscara horizontal Hf (filas)
        int[][] kernelHf = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
        };

        // Máscara vertical Hc (columnas)
        int[][] kernelHc = {
            {1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumHf = 0;
                int sumHc = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                        sumHf += pixel * kernelHf[ky + 1][kx + 1];
                        sumHc += pixel * kernelHc[ky + 1][kx + 1];
                    }
                }

                int magnitude = Math.abs(sumHf) + Math.abs(sumHc);
                magnitude = Math.min(255, magnitude);

                int nuevoRGB = (magnitude << 16) | (magnitude << 8) | magnitude;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void filtroFreiChen() {
        if (imagenActual == null) {
            return;
        }

        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Constante raíz de 2
        double sqrt2 = Math.sqrt(2);

        // Máscara horizontal Hf (filas)
        double[][] kernelHf = {
            {-1, 0, 1},
            {-sqrt2, 0, sqrt2},
            {-1, 0, 1}
        };

        // Máscara vertical Hc (columnas)
        double[][] kernelHc = {
            {1, sqrt2, 1},
            {0, 0, 0},
            {-1, -sqrt2, -1}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double sumHf = 0;
                double sumHc = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                        sumHf += pixel * kernelHf[ky + 1][kx + 1];
                        sumHc += pixel * kernelHc[ky + 1][kx + 1];
                    }
                }

                int magnitude = (int) (Math.abs(sumHf) + Math.abs(sumHc));
                magnitude = Math.min(255, magnitude);

                int nuevoRGB = (magnitude << 16) | (magnitude << 8) | magnitude;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void gradienteCompas() {
        if (imagenActual == null) {
            return;
        }

        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Definir las 8 máscaras
        int[][][] masks = {
            {{1, 1, 1}, {0, 0, 0}, {-1, -1, -1}}, // Norte
            {{0, 1, 1}, {-1, 0, 1}, {-1, -1, 0}}, // Noreste
            {{-1, 0, 1}, {-1, 0, 1}, {-1, 0, 1}}, // Este
            {{-1, -1, 0}, {-1, 0, 1}, {0, 1, 1}}, // Sureste
            {{-1, -1, -1}, {0, 0, 0}, {1, 1, 1}}, // Sur
            {{0, -1, -1}, {1, 0, -1}, {1, 1, 0}}, // Suroeste
            {{1, 0, -1}, {1, 0, -1}, {1, 0, -1}}, // Oeste
            {{1, 1, 0}, {1, 0, -1}, {0, -1, -1}} // Noroeste
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int maxResponse = 0;

                for (int m = 0; m < 8; m++) {
                    int sum = 0;
                    for (int ky = -1; ky <= 1; ky++) {
                        for (int kx = -1; kx <= 1; kx++) {
                            int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                            sum += pixel * masks[m][ky + 1][kx + 1];
                        }
                    }
                    sum = Math.abs(sum);
                    if (sum > maxResponse) {
                        maxResponse = sum;
                    }
                }

                maxResponse = Math.min(255, maxResponse);

                int nuevoRGB = (maxResponse << 16) | (maxResponse << 8) | maxResponse;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void filtroLaplaciano() {
        if (imagenActual == null) {
            return;
        }

        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        int[][] kernel = {
            {0, 1, 0},
            {1, -4, 1},
            {0, 1, 0}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sum = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                        sum += pixel * kernel[ky + 1][kx + 1];
                    }
                }

                sum = Math.abs(sum);
                sum = Math.min(255, sum);

                int nuevoRGB = (sum << 16) | (sum << 8) | sum;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void prewittSegundoOrden() {
        if (imagenActual == null) {
            return;
        }

        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Definir las 8 máscaras de Prewitt segundo orden
        int[][][] masks = {
            {{1, 1, 1}, {0, 0, 0}, {-1, -1, -1}}, // Norte
            {{0, 1, 1}, {-1, 0, 1}, {-1, -1, 0}}, // Noreste
            {{-1, 0, 1}, {-1, 0, 1}, {-1, 0, 1}}, // Este
            {{-1, -1, 0}, {-1, 0, 1}, {0, 1, 1}}, // Sureste
            {{-1, -1, -1}, {0, 0, 0}, {1, 1, 1}}, // Sur
            {{0, -1, -1}, {1, 0, -1}, {1, 1, 0}}, // Suroeste
            {{1, 0, -1}, {1, 0, -1}, {1, 0, -1}}, // Oeste
            {{1, 1, 0}, {1, 0, -1}, {0, -1, -1}} // Noroeste
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int maxResponse = 0;

                for (int m = 0; m < 8; m++) {
                    int sum = 0;
                    for (int ky = -1; ky <= 1; ky++) {
                        for (int kx = -1; kx <= 1; kx++) {
                            int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                            sum += pixel * masks[m][ky + 1][kx + 1];
                        }
                    }
                    sum = Math.abs(sum);
                    if (sum > maxResponse) {
                        maxResponse = sum;
                    }
                }

                maxResponse = Math.min(255, maxResponse);

                int nuevoRGB = (maxResponse << 16) | (maxResponse << 8) | maxResponse;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void filtroKirsch() {
        if (imagenActual == null) {
            return;
        }

        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Definir las 8 máscaras de Kirsch
        int[][][] masks = {
            {{5, 5, 5}, {-3, 0, -3}, {-3, -3, -3}}, // Norte
            {{5, 5, -3}, {5, 0, -3}, {-3, -3, -3}}, // Noreste
            {{5, -3, -3}, {5, 0, -3}, {5, -3, -3}}, // Este
            {{-3, -3, -3}, {5, 0, -3}, {5, 5, -3}}, // Sureste
            {{-3, -3, -3}, {-3, 0, -3}, {5, 5, 5}}, // Sur
            {{-3, -3, -3}, {-3, 0, 5}, {-3, 5, 5}}, // Suroeste
            {{-3, -3, 5}, {-3, 0, 5}, {-3, -3, 5}}, // Oeste
            {{-3, 5, 5}, {-3, 0, 5}, {-3, -3, -3}} // Noroeste
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int maxResponse = 0;

                for (int m = 0; m < 8; m++) {
                    int sum = 0;
                    for (int ky = -1; ky <= 1; ky++) {
                        for (int kx = -1; kx <= 1; kx++) {
                            int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                            sum += pixel * masks[m][ky + 1][kx + 1];
                        }
                    }
                    sum = Math.abs(sum);
                    if (sum > maxResponse) {
                        maxResponse = sum;
                    }
                }

                maxResponse = Math.min(255, maxResponse);

                int nuevoRGB = (maxResponse << 16) | (maxResponse << 8) | maxResponse;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void filtroRobinson() {
        if (imagenActual == null) {
            return;
        }

        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Definir las 8 máscaras de Robinson
        int[][][] masks = {
            {{1, 2, 1}, {0, 0, 0}, {-1, -2, -1}}, // Norte
            {{2, 1, 0}, {1, 0, -1}, {0, -1, -2}}, // Noreste
            {{1, 0, -1}, {2, 0, -2}, {1, 0, -1}}, // Este
            {{0, -1, -2}, {1, 0, -1}, {2, 1, 0}}, // Sureste
            {{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}}, // Sur
            {{-2, -1, 0}, {-1, 0, 1}, {0, 1, 2}}, // Suroeste
            {{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}}, // Oeste
            {{0, 1, 2}, {-1, 0, 1}, {-2, -1, 0}} // Noroeste
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int maxResponse = 0;

                for (int m = 0; m < 8; m++) {
                    int sum = 0;
                    for (int ky = -1; ky <= 1; ky++) {
                        for (int kx = -1; kx <= 1; kx++) {
                            int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                            sum += pixel * masks[m][ky + 1][kx + 1];
                        }
                    }
                    sum = Math.abs(sum);
                    if (sum > maxResponse) {
                        maxResponse = sum;
                    }
                }

                maxResponse = Math.min(255, maxResponse);

                int nuevoRGB = (maxResponse << 16) | (maxResponse << 8) | maxResponse;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void filtroPromediador() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumaR = 0, sumaG = 0, sumaB = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        sumaR += (rgb >> 16) & 0xFF;
                        sumaG += (rgb >> 8) & 0xFF;
                        sumaB += rgb & 0xFF;
                    }
                }
                int r = sumaR / 9, g = sumaG / 9, b = sumaB / 9;
                int nuevoRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void filtroGaussiano() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        int[][] kernel = {
            {1, 2, 1},
            {2, 4, 2},
            {1, 2, 1}
        };
        int factor = 16;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumaR = 0, sumaG = 0, sumaB = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        int peso = kernel[ky + 1][kx + 1];
                        sumaR += ((rgb >> 16) & 0xFF) * peso;
                        sumaG += ((rgb >> 8) & 0xFF) * peso;
                        sumaB += (rgb & 0xFF) * peso;
                    }
                }
                int r = Math.min(255, Math.max(0, sumaR / factor));
                int g = Math.min(255, Math.max(0, sumaG / factor));
                int b = Math.min(255, Math.max(0, sumaB / factor));
                int nuevoRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void filtroDefinicion() {
        if (imagenActual == null) {
            return;
        }

        convertirEscalaGrises();
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        int[][] kernel = {
            {0, -1, 0},
            {-1, 5, -1},
            {0, -1, 0}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sum = 0;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                        sum += pixel * kernel[ky + 1][kx + 1];
                    }
                }

                sum = Math.min(255, Math.max(0, sum)); // Aseguramos rango válido

                int nuevoRGB = (sum << 16) | (sum << 8) | sum;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }
    // FIltros no Lineales

    public void filtroMediana() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int[] ventanaR = new int[9];
                int[] ventanaG = new int[9];
                int[] ventanaB = new int[9];
                int idx = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        ventanaR[idx] = (rgb >> 16) & 0xFF;
                        ventanaG[idx] = (rgb >> 8) & 0xFF;
                        ventanaB[idx] = rgb & 0xFF;
                        idx++;
                    }
                }
                Arrays.sort(ventanaR);
                Arrays.sort(ventanaG);
                Arrays.sort(ventanaB);
                int r = ventanaR[4], g = ventanaG[4], b = ventanaB[4];
                int nuevoRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void filtroMinimo() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int minR = 255, minG = 255, minB = 255;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        minR = Math.min(minR, (rgb >> 16) & 0xFF);
                        minG = Math.min(minG, (rgb >> 8) & 0xFF);
                        minB = Math.min(minB, rgb & 0xFF);
                    }
                }
                int nuevoRGB = (minR << 16) | (minG << 8) | minB;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void filtroMaximo() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int maxR = 0, maxG = 0, maxB = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        maxR = Math.max(maxR, (rgb >> 16) & 0xFF);
                        maxG = Math.max(maxG, (rgb >> 8) & 0xFF);
                        maxB = Math.max(maxB, rgb & 0xFF);
                    }
                }
                int nuevoRGB = (maxR << 16) | (maxG << 8) | maxB;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void filtroPuntoMedio() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int minR = 255, minG = 255, minB = 255;
                int maxR = 0, maxG = 0, maxB = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                        minR = Math.min(minR, r);
                        maxR = Math.max(maxR, r);
                        minG = Math.min(minG, g);
                        maxG = Math.max(maxG, g);
                        minB = Math.min(minB, b);
                        maxB = Math.max(maxB, b);
                    }
                }
                int r = (minR + maxR) / 2, g = (minG + maxG) / 2, b = (minB + maxB) / 2;
                int nuevoRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void filtroAlfaRecortado() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());
        int d = 2, n = 9;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int[] ventanaR = new int[n], ventanaG = new int[n], ventanaB = new int[n];
                int idx = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        ventanaR[idx] = (rgb >> 16) & 0xFF;
                        ventanaG[idx] = (rgb >> 8) & 0xFF;
                        ventanaB[idx] = rgb & 0xFF;
                        idx++;
                    }
                }
                Arrays.sort(ventanaR);
                Arrays.sort(ventanaG);
                Arrays.sort(ventanaB);
                int sumaR = 0, sumaG = 0, sumaB = 0;
                for (int i = d; i < n - d; i++) {
                    sumaR += ventanaR[i];
                    sumaG += ventanaG[i];
                    sumaB += ventanaB[i];
                }
                int r = sumaR / (n - 2 * d), g = sumaG / (n - 2 * d), b = sumaB / (n - 2 * d);
                int nuevoRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void filtroArmonicoInferior() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double sumaInvR = 0, sumaInvG = 0, sumaInvB = 0;
                int n = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                        if (r != 0) {
                            sumaInvR += 1.0 / r;
                        }
                        if (g != 0) {
                            sumaInvG += 1.0 / g;
                        }
                        if (b != 0) {
                            sumaInvB += 1.0 / b;
                        }
                        n++;
                    }
                }
                int r = (sumaInvR != 0) ? (int) Math.round(n / sumaInvR) : 0;
                int g = (sumaInvG != 0) ? (int) Math.round(n / sumaInvG) : 0;
                int b = (sumaInvB != 0) ? (int) Math.round(n / sumaInvB) : 0;
                int nuevoRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void filtroContraArmonico() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());
        double Q = 1.5;

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double sumaSupR = 0, sumaInfR = 0, sumaSupG = 0, sumaInfG = 0, sumaSupB = 0, sumaInfB = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                        sumaSupR += Math.pow(r, Q + 1);
                        sumaInfR += Math.pow(r, Q);
                        sumaSupG += Math.pow(g, Q + 1);
                        sumaInfG += Math.pow(g, Q);
                        sumaSupB += Math.pow(b, Q + 1);
                        sumaInfB += Math.pow(b, Q);
                    }
                }
                int r = (sumaInfR != 0) ? (int) Math.round(sumaSupR / sumaInfR) : 0;
                int g = (sumaInfG != 0) ? (int) Math.round(sumaSupG / sumaInfG) : 0;
                int b = (sumaInfB != 0) ? (int) Math.round(sumaSupB / sumaInfB) : 0;
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));
                int nuevoRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void filtroGeometricoInferior() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double prodR = 1, prodG = 1, prodB = 1;
                int n = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                        prodR *= (r > 0) ? r : 1;
                        prodG *= (g > 0) ? g : 1;
                        prodB *= (b > 0) ? b : 1;
                        n++;
                    }
                }
                int r = (int) Math.round(Math.pow(prodR, 1.0 / n));
                int g = (int) Math.round(Math.pow(prodG, 1.0 / n));
                int b = (int) Math.round(Math.pow(prodB, 1.0 / n));
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));
                int nuevoRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void filtroMaximoMenosMinimo() {
        if (imagenActual == null) {
            return;
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int max = 0;
                int min = 255;

                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                        if (pixel > max) {
                            max = pixel;
                        }
                        if (pixel < min) {
                            min = pixel;
                        }
                    }
                }

                int valor = max - min;
                valor = Math.min(255, Math.max(0, valor));
                int nuevoRGB = (valor << 16) | (valor << 8) | valor;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void filtroMediaAritmetica() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumaR = 0, sumaG = 0, sumaB = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        sumaR += (rgb >> 16) & 0xFF;
                        sumaG += (rgb >> 8) & 0xFF;
                        sumaB += rgb & 0xFF;
                    }
                }
                int r = sumaR / 9, g = sumaG / 9, b = sumaB / 9;
                int nuevoRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void filtroContraGeometrico() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double prodInvR = 1, prodInvG = 1, prodInvB = 1;
                int n = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                        prodInvR *= (r != 0) ? 1.0 / r : 1;
                        prodInvG *= (g != 0) ? 1.0 / g : 1;
                        prodInvB *= (b != 0) ? 1.0 / b : 1;
                        n++;
                    }
                }
                int r = (int) Math.round(Math.pow(prodInvR, -1.0 / n));
                int g = (int) Math.round(Math.pow(prodInvG, -1.0 / n));
                int b = (int) Math.round(Math.pow(prodInvB, -1.0 / n));
                r = Math.min(255, Math.max(0, r));
                g = Math.min(255, Math.max(0, g));
                b = Math.min(255, Math.max(0, b));
                int nuevoRGB = (r << 16) | (g << 8) | b;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    public void filtroModa() {
        if (imagenActual == null) {
            return;
        }
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int[] freqR = new int[256], freqG = new int[256], freqB = new int[256];
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        int rgb = imagenActual.getRGB(x + kx, y + ky);
                        freqR[(rgb >> 16) & 0xFF]++;
                        freqG[(rgb >> 8) & 0xFF]++;
                        freqB[rgb & 0xFF]++;
                    }
                }
                int modaR = 0, modaG = 0, modaB = 0, maxR = 0, maxG = 0, maxB = 0;
                for (int i = 0; i < 256; i++) {
                    if (freqR[i] > maxR) {
                        maxR = freqR[i];
                        modaR = i;
                    }
                    if (freqG[i] > maxG) {
                        maxG = freqG[i];
                        modaG = i;
                    }
                    if (freqB[i] > maxB) {
                        maxB = freqB[i];
                        modaB = i;
                    }
                }
                int nuevoRGB = (modaR << 16) | (modaG << 8) | modaB;
                result.setRGB(x, y, nuevoRGB);
            }
        }
        imagenActual = result;
    }

    private int[][] elementoEstructurante = {
        {1, 1, 1},
        {1, 1, 1},
        {1, 1, 1}
    };

    public void setElementoEstructurante(int[][] elemento) {
        if (elemento != null && elemento.length == 3 && elemento[0].length == 3) {
            this.elementoEstructurante = elemento;
        }
    }

    // Implementacion de las morfologias de erosion, dilatacion
    public void BinaryErotion() {
        if (imagenActual == null) {
            return;
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Elemento estructurante 3x3 centrado
        int structWidth = 3;
        int structHeight = 3;
        int originX = 1;
        int originY = 1;

        // Determinar el rango de píxeles donde se puede aplicar la operación
        int xmin = Math.max(originX, 0);
        int ymin = Math.max(originY, 0);
        int xmax = Math.min(width - 1, originX + width - structWidth);
        int ymax = Math.min(height - 1, originY + height - structHeight);

        for (int y = ymin; y <= ymax; y++) {
            for (int x = xmin; x <= xmax; x++) {
                boolean hits = false;
                // Revisar si el elemento estructurante "toca" (al menos un píxel es negro)
                for (int ky = -originY; ky <= structHeight - originY - 1; ky++) {
                    for (int kx = -originX; kx <= structWidth - originX - 1; kx++) {
                        // Solo considerar las posiciones activas del elemento estructurante (valor 1)
                        if (elementoEstructurante[ky + originY][kx + originX] == 1) {
                            int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                            if (pixel == 0) { // Pixel negro (valor binario 0)
                                hits = true;
                                break;
                            }
                        }
                    }
                    if (hits) {
                        break;
                    }
                }
                int nuevoRGB = hits ? 0 : 255;
                result.setRGB(x, y, (nuevoRGB << 16) | (nuevoRGB << 8) | nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void BinaryDilation() {
        if (imagenActual == null) {
            return;
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Elemento estructurante 3x3 centrado
        int structWidth = 3;
        int structHeight = 3;
        int originX = 1;
        int originY = 1;

        // Determinar el rango de píxeles donde se puede aplicar la operación
        int xmin = Math.max(originX, 0);
        int ymin = Math.max(originY, 0);
        int xmax = Math.min(width - 1, originX + width - structWidth);
        int ymax = Math.min(height - 1, originY + height - structHeight);

        for (int y = ymin; y <= ymax; y++) {
            for (int x = xmin; x <= xmax; x++) {
                boolean fits = true;
                // Revisar si el elemento estructurante "cabe" (todos los píxeles son negros)
                for (int ky = -originY; ky <= structHeight - originY - 1; ky++) {
                    for (int kx = -originX; kx <= structWidth - originX - 1; kx++) {
                        int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                        if (elementoEstructurante[ky + originY][kx + originX] == 1) {
                            if (pixel != 0) {
                                fits = false;
                                break;
                            }
                        }
                    }
                    if (!fits) {
                        break;
                    }
                }
                int nuevoRGB = fits ? 0 : 255;
                result.setRGB(x, y, (nuevoRGB << 16) | (nuevoRGB << 8) | nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void GrayScaleErotion() {
        if (imagenActual == null) {
            return;
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Elemento estructurante 3x3 centrado
        int structWidth = 3;
        int structHeight = 3;
        int originX = 1;
        int originY = 1;

        // Determinar el rango de píxeles donde se puede aplicar la operación
        int xmin = Math.max(originX, 0);
        int ymin = Math.max(originY, 0);
        int xmax = Math.min(width - 1, originX + width - structWidth);
        int ymax = Math.min(height - 1, originY + height - structHeight);

        for (int y = ymin; y <= ymax; y++) {
            for (int x = xmin; x <= xmax; x++) {
                int max = 0;
                for (int ky = -originY; ky <= structHeight - originY - 1; ky++) {
                    for (int kx = -originX; kx <= structWidth - originX - 1; kx++) {
                        int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                        int valorElemento = elementoEstructurante[ky + originY][kx + originX];

                        int resultado = pixel + valorElemento; // ⬅ suma del lattice
                        if (resultado > max) {
                            max = resultado;
                        }
                    }
                }
                max = Math.min(max, 255); // limitar a rango válido
                int nuevoRGB = (max << 16) | (max << 8) | max;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void GrayScaleDilation() {
        if (imagenActual == null) {
            return;
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Elemento estructurante 3x3 centrado
        int structWidth = 3;
        int structHeight = 3;
        int originX = 1;
        int originY = 1;

        // Determinar el rango de píxeles donde se puede aplicar la operación
        int xmin = Math.max(originX, 0);
        int ymin = Math.max(originY, 0);
        int xmax = Math.min(width - 1, originX + width - structWidth);
        int ymax = Math.min(height - 1, originY + height - structHeight);

        for (int y = ymin; y <= ymax; y++) {
            for (int x = xmin; x <= xmax; x++) {
                int min = 255;
                for (int ky = -originY; ky <= structHeight - originY - 1; ky++) {
                    for (int kx = -originX; kx <= structWidth - originX - 1; kx++) {
                        int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                        int valorElemento = elementoEstructurante[ky + originY][kx + originX];

                        int resultado = pixel - valorElemento; // ⬅ resta del lattice
                        if (resultado < min) {
                            min = resultado;
                        }
                    }
                }
                min = Math.max(min, 0); // limitar a rango válido
                int nuevoRGB = (min << 16) | (min << 8) | min;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void hitOrAndMissTransformed() {
        if (imagenActual == null) {
            return;
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Elemento estructurante 3x3 centrado
        int structWidth = 3;
        int structHeight = 3;
        int originX = 1;
        int originY = 1;

        // Determinar el rango de píxeles donde se puede aplicar la operación
        int xmin = Math.max(originX, 0);
        int ymin = Math.max(originY, 0);
        int xmax = Math.min(width - 1, originX + width - structWidth);
        int ymax = Math.min(height - 1, originY + height - structHeight);

        for (int y = ymin; y <= ymax; y++) {
            for (int x = xmin; x <= xmax; x++) {
                boolean match = true;
                for (int ky = -originY; ky <= structHeight - originY - 1; ky++) {
                    for (int kx = -originX; kx <= structWidth - originX - 1; kx++) {
                        int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                        int valorElemento = elementoEstructurante[ky + originY][kx + originX];

                        if (valorElemento == 1 && pixel != 255) {
                            match = false;
                        } else if (valorElemento == 0 && pixel != 0) {
                            match = false;
                        }
                        // Si valorElemento == -1, ignorar (don't care)
                    }
                }
                int nuevoRGB = match ? 0xFFFFFF : 0x000000;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    public void waterSheedTransformed() {
        if (imagenActual == null) {
            return;
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        BufferedImage result = new BufferedImage(width, height, imagenActual.getType());

        // Elemento estructurante 3x3 centrado
        int structWidth = 3;
        int structHeight = 3;
        int originX = 1;
        int originY = 1;

        // Determinar el rango de píxeles donde se puede aplicar la operación
        int xmin = Math.max(originX, 0);
        int ymin = Math.max(originY, 0);
        int xmax = Math.min(width - 1, originX + width - structWidth);
        int ymax = Math.min(height - 1, originY + height - structHeight);

        for (int y = ymin; y <= ymax; y++) {
            for (int x = xmin; x <= xmax; x++) {
                boolean match = true;
                for (int ky = -originY; ky <= structHeight - originY - 1; ky++) {
                    for (int kx = -originX; kx <= structWidth - originX - 1; kx++) {
                        int pixel = (imagenActual.getRGB(x + kx, y + ky) >> 16) & 0xFF;
                        int valorElemento = elementoEstructurante[ky + originY][kx + originX];

                        if (valorElemento == 1 && pixel != 255) {
                            match = false;
                        } else if (valorElemento == 0 && pixel != 0) {
                            match = false;
                        }
                        // Si valorElemento == -1, ignorar (don't care)
                    }
                }
                int nuevoRGB = match ? 0xFFFFFF : 0x000000;
                result.setRGB(x, y, nuevoRGB);
            }
        }

        imagenActual = result;
    }

    // Etiquetado de componentes conectados (conectividad 4) con colores
    public void etiquetadoComponentes4() {
        if (imagenActual == null) {
            return;
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        int[][] labels = new int[width][height];
        int nextLabel = 1;

        // Unión-Find para etiquetas equivalentes
        int[] parent = new int[width * height / 2 + 1];
        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
        }

        // Primera pasada: asignar etiquetas y registrar equivalencias (solo vecinos arriba e izquierda)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = (imagenActual.getRGB(x, y) >> 16) & 0xFF;
                if (pixel != 0) {
                    continue; // Solo fondo negro es objeto
                }
                int labelArriba = (y > 0) ? labels[x][y - 1] : 0;
                int labelIzquierda = (x > 0) ? labels[x - 1][y] : 0;

                if (labelArriba == 0 && labelIzquierda == 0) {
                    labels[x][y] = nextLabel;
                    nextLabel++;
                } else if (labelArriba != 0 && labelIzquierda == 0) {
                    labels[x][y] = labelArriba;
                } else if (labelArriba == 0 && labelIzquierda != 0) {
                    labels[x][y] = labelIzquierda;
                } else {
                    labels[x][y] = Math.min(labelArriba, labelIzquierda);
                    int a = find(parent, labelArriba);
                    int b = find(parent, labelIzquierda);
                    if (a != b) {
                        parent[a] = b;
                    }
                }
            }
        }

        // Segunda pasada: asignar etiquetas finales
        int[] newLabels = new int[nextLabel];
        int colorCount = 1;
        for (int i = 1; i < nextLabel; i++) {
            int root = find(parent, i);
            if (newLabels[root] == 0) {
                newLabels[root] = colorCount++;
            }
            newLabels[i] = newLabels[root];
        }

        // Generar colores para cada etiqueta
        int[] colors = new int[colorCount + 1];
        colors[0] = 0x000000; // Fondo negro
        for (int i = 1; i < colors.length; i++) {
            float hue = (float) i / (float) colors.length;
            int rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            colors[i] = rgb;
        }

        // Crear imagen de salida
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int label = labels[x][y];
                int color = (label > 0) ? colors[newLabels[label]] : 0x000000;
                result.setRGB(x, y, color);
            }
        }

        imagenActual = result;
    }

    // Etiquetado de componentes conectados (conectividad 8) con colores
    public void etiquetadoComponentes8() {
        if (imagenActual == null) {
            return;
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        int[][] labels = new int[width][height];
        int nextLabel = 1;

        // Unión-Find para etiquetas equivalentes
        int[] parent = new int[width * height / 2 + 1];
        for (int i = 0; i < parent.length; i++) {
            parent[i] = i;
        }

        // Primera pasada: asignar etiquetas y registrar equivalencias (vecinos arriba, izquierda, arriba-izquierda, arriba-derecha)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = (imagenActual.getRGB(x, y) >> 16) & 0xFF;
                if (pixel != 0) {
                    continue; // Solo fondo negro es objeto
                }
                int labelArriba = (y > 0) ? labels[x][y - 1] : 0;
                int labelIzquierda = (x > 0) ? labels[x - 1][y] : 0;
                int labelArribaIzq = (x > 0 && y > 0) ? labels[x - 1][y - 1] : 0;
                int labelArribaDer = (x < width - 1 && y > 0) ? labels[x + 1][y - 1] : 0;

                int[] vecinos = {labelArriba, labelIzquierda, labelArribaIzq, labelArribaDer};
                int minLabel = 0;
                for (int l : vecinos) {
                    if (l != 0 && (minLabel == 0 || l < minLabel)) {
                        minLabel = l;
                    }
                }

                if (minLabel == 0) {
                    labels[x][y] = nextLabel;
                    nextLabel++;
                } else {
                    labels[x][y] = minLabel;
                    // Registrar equivalencias
                    for (int l : vecinos) {
                        if (l != 0 && l != minLabel) {
                            int a = find(parent, l);
                            int b = find(parent, minLabel);
                            if (a != b) {
                                parent[a] = b;
                            }
                        }
                    }
                }
            }
        }

        // Segunda pasada: asignar etiquetas finales
        int[] newLabels = new int[nextLabel];
        int colorCount = 1;
        for (int i = 1; i < nextLabel; i++) {
            int root = find(parent, i);
            if (newLabels[root] == 0) {
                newLabels[root] = colorCount++;
            }
            newLabels[i] = newLabels[root];
        }

        // Generar colores para cada etiqueta
        int[] colors = new int[colorCount + 1];
        colors[0] = 0x000000; // Fondo negro
        for (int i = 1; i < colors.length; i++) {
            float hue = (float) i / (float) colors.length;
            int rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            colors[i] = rgb;
        }

        // Crear imagen de salida
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int label = labels[x][y];
                int color = (label > 0) ? colors[newLabels[label]] : 0x000000;
                result.setRGB(x, y, color);
            }
        }

        imagenActual = result;
    }

    // Unión-Find auxiliar
    private int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    // Conteo de objetos en la imagen actual y visualización de cada objeto con un color diferente
    // El parámetro conectividad puede ser 4 u 8
    public int contarYMostrarObjetos(int conectividad) {
        if (imagenActual == null) {
            return 0;
        }
        if (conectividad != 4 && conectividad != 8) {
            throw new IllegalArgumentException("La conectividad debe ser 4 u 8.");
        }

        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        boolean[][] visitado = new boolean[width][height];
        int[][] etiquetas = new int[width][height];
        int contadorObjetos = 0;

        // Etiquetar cada objeto con un número único
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!visitado[x][y] && (imagenActual.getRGB(x, y) & 0xFFFFFF) != 0) { // No visitado y no fondo
                    contadorObjetos++;
                    etiquetarObjetoConectividad(x, y, visitado, etiquetas, contadorObjetos, conectividad);
                }
            }
        }

        // Generar colores para cada objeto
        int[] colores = new int[contadorObjetos + 1];
        colores[0] = 0x000000; // Fondo negro
        for (int i = 1; i <= contadorObjetos; i++) {
            float hue = (float) i / (float) (contadorObjetos + 1);
            colores[i] = Color.HSBtoRGB(hue, 1.0f, 1.0f);
        }

        // Crear imagen coloreada
        BufferedImage resultado = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int etiqueta = etiquetas[x][y];
                resultado.setRGB(x, y, colores[etiqueta]);
            }
        }

        imagenActual = resultado;
        return contadorObjetos;
    }

    // Etiquetar iterativamente el objeto conectado con conectividad 4 u 8 (BFS para evitar desbordamiento de pila)
    private void etiquetarObjetoConectividad(int x, int y, boolean[][] visitado, int[][] etiquetas, int etiqueta, int conectividad) {
        int width = imagenActual.getWidth();
        int height = imagenActual.getHeight();
        int[][] directions4 = {{1,0},{-1,0},{0,1},{0,-1}};
        int[][] directions8 = {{1,0},{-1,0},{0,1},{0,-1},{1,1},{-1,1},{1,-1},{-1,-1}};
        int[][] directions = (conectividad == 8) ? directions8 : directions4;

        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        queue.add(new int[]{x, y});

        while (!queue.isEmpty()) {
            int[] pos = queue.poll();
            int cx = pos[0], cy = pos[1];
            if (cx < 0 || cx >= width || cy < 0 || cy >= height) continue;
            if (visitado[cx][cy]) continue;
            if ((imagenActual.getRGB(cx, cy) & 0xFFFFFF) == 0) continue;

            visitado[cx][cy] = true;
            etiquetas[cx][cy] = etiqueta;

            for (int[] dir : directions) {
                int nx = cx + dir[0];
                int ny = cy + dir[1];
                if (nx >= 0 && nx < width && ny >= 0 && ny < height && !visitado[nx][ny]) {
                    queue.add(new int[]{nx, ny});
                }
            }
        }
    }

    public String generarResumenComparacion() {
        if (imagenOriginal == null || imagenConRuido == null || imagenActual == null) {
            return "Faltan imágenes para realizar la comparación.";
        }

        int width = Math.min(imagenOriginal.getWidth(), Math.min(imagenConRuido.getWidth(), imagenActual.getWidth()));
        int height = Math.min(imagenOriginal.getHeight(), Math.min(imagenConRuido.getHeight(), imagenActual.getHeight()));
        int totalPixeles = width * height;

        int diferentesConRuido = 0;
        int diferentesFiltrada = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int origGray = (imagenOriginal.getRGB(x, y) >> 16) & 0xFF;
                int ruidoGray = (imagenConRuido.getRGB(x, y) >> 16) & 0xFF;
                int filtradoGray = (imagenActual.getRGB(x, y) >> 16) & 0xFF;

                if (origGray != ruidoGray) {
                    diferentesConRuido++;
                }
                if (origGray != filtradoGray) {
                    diferentesFiltrada++;
                }
            }
        }

        double porcentajeRuido = 100.0 * diferentesConRuido / totalPixeles;
        double porcentajeFiltrado = 100.0 * diferentesFiltrada / totalPixeles;
        double reduccion = porcentajeRuido - porcentajeFiltrado;

        return String.format(
                """
            +-------------------------------+-------------+
            | MÉTRICA                      | VALOR       |
            +-------------------------------+-------------+
            | Total de píxeles             | %,11d |
            | Píxeles modificados (ruido)  | %,11d |
            | %% Modificados (ruido)        | %10.2f %% |
            | Píxeles modificados (filtrado)| %,11d |
            | %% Modificados (filtrado)     | %10.2f %% |
            | Reducción del error          | %10.2f %% |
            +-------------------------------+-------------+
            """,
                totalPixeles,
                diferentesConRuido,
                porcentajeRuido,
                diferentesFiltrada,
                porcentajeFiltrado,
                reduccion
        );
    }

    public void mostrarResumenComparacion() {
        String resumen = generarResumenComparacion();

        JTextArea textArea = new JTextArea(resumen);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(500, 250));

        JOptionPane.showMessageDialog(null, scrollPane, "Comparación de Resultados", JOptionPane.INFORMATION_MESSAGE);
    }

}
