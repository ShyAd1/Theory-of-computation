
package controlador;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import modelo.ImagenModelo;
import vista.ElementoEstructura;
import vista.ImagenVista;

public class ImagenControlador {
    private ImagenModelo modelo;
    private ImagenVista vista;
    
    private BufferedImage cargarSegundaImagen() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccione la segunda imagen");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Imágenes", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(vista);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                return ImageIO.read(file);
            } catch (Exception ex) {
                vista.mostrarMensajeError("Error al cargar la segunda imagen: " + ex.getMessage());
                return null;
            }
        }
        return null;
    }
    
    // Definir las clases internas antes de usarlas
    class LoadButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(vista) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    BufferedImage nuevaImagen = ImageIO.read(file);
                    modelo.cargarImagen(nuevaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (Exception ex) {
                    vista.mostrarMensajeError("Error al cargar la imagen: " + ex.getMessage());
                }
            }
        }
    }

    class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada para guardar.");
                return;
            }
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(vista) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    ImageIO.write(modelo.getImagenActual(), "png", file);
                    JOptionPane.showMessageDialog(vista, "Imagen guardada exitosamente.");
                } catch (Exception ex) {
                    vista.mostrarMensajeError("Error al guardar la imagen: " + ex.getMessage());
                }
            }
        }
    }

    class ResetButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            modelo.resetearImagen();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class GrayButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.convertirEscalaGrises();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class RedButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.filtrarCanalRojo();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class GreenButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.filtrarCanalVerde();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class BlueButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.filtrarCanalAzul();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class RgbToYIQButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Convertir la imagen principal a YIQ
            modelo.convertToYIQ();
            vista.mostrarImagen(modelo.getImagenActual());

            // Obtener los canales YIQ para mostrar en ventana emergente
            BufferedImage[] channels = modelo.getYIQChannels();
            String[] channelNames = {"Y (Luminancia)", "I (Crominancia I)", "Q (Crominancia Q)"};

            // Crear ventana emergente
            JDialog dialog = new JDialog((Frame) null, "Canales YIQ", true);
            dialog.setLayout(new GridLayout(1, 3, 10, 10));
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            // Añadir cada canal como una imagen en el diálogo
            for (int i = 0; i < channels.length; i++) {
                JLabel label = new JLabel(new ImageIcon(channels[i]));
                label.setBorder(BorderFactory.createTitledBorder(channelNames[i]));
                dialog.add(label);
            }

            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    }
    

    class YiqToRGBButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.convertYIQtoRGB();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class RgbToHSIButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Convertir la imagen principal a HSI
            modelo.convertToHSI();
            vista.mostrarImagen(modelo.getImagenActual());

            // Obtener los canales HSI para mostrar en ventana emergente
            BufferedImage[] channels = modelo.getHSIChannels();
            String[] channelNames = {"H (Tono)", "S (Saturación)", "I (Intensidad)"};

            // Crear ventana emergente
            JDialog dialog = new JDialog((Frame) null, "Canales HSI", true);
            dialog.setLayout(new GridLayout(1, 3, 10, 10));
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            // Añadir cada canal como una imagen en el diálogo
            for (int i = 0; i < channels.length; i++) {
                JLabel label = new JLabel(new ImageIcon(channels[i]));
                label.setBorder(BorderFactory.createTitledBorder(channelNames[i]));
                dialog.add(label);
            }

            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    }

    class HsiToRGBButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.convertHSItoRGB();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class RgbToHSVButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Convertir la imagen principal a HSV
            modelo.convertToHSV();
            vista.mostrarImagen(modelo.getImagenActual());

            // Obtener los canales HSV para mostrar en ventana emergente
            BufferedImage[] channels = modelo.getHSVChannels();
            String[] channelNames = {"H (Tono)", "S (Saturación)", "V (Valor)"};

            // Crear ventana emergente
            JDialog dialog = new JDialog((Frame) null, "Canales HSV", true);
            dialog.setLayout(new GridLayout(1, 3, 10, 10));
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            // Añadir cada canal como una imagen en el diálogo
            for (int i = 0; i < channels.length; i++) {
                JLabel label = new JLabel(new ImageIcon(channels[i]));
                label.setBorder(BorderFactory.createTitledBorder(channelNames[i]));
                dialog.add(label);
            }

            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    }
    
    class HsvToRGBButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.convertHSVtoRGB();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class RgbToCMYButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Convertir la imagen principal a CMY
            modelo.convertToCMY();
            vista.mostrarImagen(modelo.getImagenActual());

            // Obtener los canales CMY para mostrar en ventana emergente
            BufferedImage[] channels = modelo.getCMYChannels();
            String[] channelNames = {"C (Cian)", "M (Magenta)", "Y (Amarillo)"};

            // Crear ventana emergente
            JDialog dialog = new JDialog((Frame) null, "Canales CMY", true);
            dialog.setLayout(new GridLayout(1, 3, 10, 10));
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            // Añadir cada canal como una imagen en el diálogo
            for (int i = 0; i < channels.length; i++) {
                JLabel label = new JLabel(new ImageIcon(channels[i]));
                label.setBorder(BorderFactory.createTitledBorder(channelNames[i]));
                dialog.add(label);
            }

            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        }
    }

    class CmyToRGBButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.convertCMYtoRGB();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class CmyToCMYKButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.convertCMYtoCMYK();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class RgbToLabButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.convertToLab();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class LabToRGBButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.convertLabToRGB();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class ShowHistogramButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            int[][] histogramRGB = modelo.computeRGBHistogram();
            int[] histogramGray = modelo.computeGrayHistogram();
            vista.showHistogram(histogramRGB, histogramGray, "Histograma de Imagen Actual");
        }
    }

    class ScaleHistogramButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Calcular histogramas de la imagen original
            int[][] histogramRGBOriginal = modelo.computeRGBHistogram(modelo.getImagenActual());
            int[] histogramGrayOriginal = modelo.computeGrayHistogram(modelo.getImagenActual());

            // Solicitar el valor mínimo
            Double minTarget = vista.solicitarDouble("Ingrese el valor mínimo del rango (0-255):");
            if (minTarget == null) {
                return; // El usuario canceló
            }

            // Solicitar el valor máximo
            Double maxTarget = vista.solicitarDouble("Ingrese el valor máximo del rango (0-255):");
            if (maxTarget == null) {
                return; // El usuario canceló
            }

            // Convertir a enteros y validar
            int min = minTarget.intValue();
            int max = maxTarget.intValue();
            if (min < 0 || max > 255 || min > max) {
                vista.mostrarMensajeError("Los valores deben cumplir: 0 <= mínimo <= máximo <= 255.");
                return;
            }

            // Aplicar el reescalado del histograma
            modelo.scaleHistogram(min, max);
            vista.mostrarImagen(modelo.getImagenActual());

            // Calcular histogramas de la imagen modificada
            int[][] histogramRGBModified = modelo.computeRGBHistogram();
            int[] histogramGrayModified = modelo.computeGrayHistogram();

            // Mostrar histogramas en ventanas emergentes
            vista.showHistogram(histogramRGBOriginal, histogramGrayOriginal, "Histograma Original (Scale)");
            vista.showHistogram(histogramRGBModified, histogramGrayModified, "Histograma Modificado (Scale)");
        }
    }

    class ShiftHistogramButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Calcular histogramas de la imagen original
            int[][] histogramRGBOriginal = modelo.computeRGBHistogram(modelo.getImagenActual());
            int[] histogramGrayOriginal = modelo.computeGrayHistogram(modelo.getImagenActual());

            // Solicitar el valor de desplazamiento
            Integer shiftValue = vista.solicitarEntero("Ingrese el valor de desplazamiento (positivo para aclarar, negativo para oscurecer):");
            if (shiftValue == null) {
                return; // El usuario canceló
            }

            // Aplicar el desplazamiento del histograma
            modelo.shiftHistogram(shiftValue);
            vista.mostrarImagen(modelo.getImagenActual());

            // Calcular histogramas de la imagen modificada
            int[][] histogramRGBModified = modelo.computeRGBHistogram();
            int[] histogramGrayModified = modelo.computeGrayHistogram();

            // Mostrar histogramas en ventanas emergentes
            vista.showHistogram(histogramRGBOriginal, histogramGrayOriginal, "Histograma Original (Shift)");
            vista.showHistogram(histogramRGBModified, histogramGrayModified, "Histograma Modificado (Shift)");
        }
    }

    class MatchHistogramButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Calcular histogramas de la imagen original
            int[][] histogramRGBOriginal = modelo.computeRGBHistogram(modelo.getImagenActual());
            int[] histogramGrayOriginal = modelo.computeGrayHistogram(modelo.getImagenActual());

            // Solicitar la imagen de referencia
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(vista) == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChooser.getSelectedFile();
                    BufferedImage imagenReferencia = ImageIO.read(file);
                    modelo.matchHistogram(imagenReferencia);
                    vista.mostrarImagen(modelo.getImagenActual());

                    // Calcular histogramas de la imagen modificada
                    int[][] histogramRGBModified = modelo.computeRGBHistogram();
                    int[] histogramGrayModified = modelo.computeGrayHistogram();

                    // Mostrar histogramas en ventanas emergentes
                    vista.showHistogram(histogramRGBOriginal, histogramGrayOriginal, "Histograma Original (Match)");
                    vista.showHistogram(histogramRGBModified, histogramGrayModified, "Histograma Modificado (Match)");
                } catch (Exception ex) {
                    vista.mostrarMensajeError("Error al cargar la imagen de referencia: " + ex.getMessage());
                }
            }
        }
    }
    
    class EqualizeHistogramButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Calcular histogramas de la imagen original
            int[][] histogramRGBOriginal = modelo.computeRGBHistogram(modelo.getImagenActual());
            int[] histogramGrayOriginal = modelo.computeGrayHistogram(modelo.getImagenActual());

            // Aplicar la ecualización del histograma
            modelo.equalizeHistogram();
            vista.mostrarImagen(modelo.getImagenActual());

            // Calcular histogramas de la imagen modificada
            int[][] histogramRGBModified = modelo.computeRGBHistogram();
            int[] histogramGrayModified = modelo.computeGrayHistogram();

            // Mostrar histogramas en ventanas emergentes
            vista.showHistogram(histogramRGBOriginal, histogramGrayOriginal, "Histograma Original (Equalize)");
            vista.showHistogram(histogramRGBModified, histogramGrayModified, "Histograma Modificado (Equalize)");
        }
    }
    
    class EqualizeUniformButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Calcular histogramas de la imagen original
            int[][] histogramRGBOriginal = modelo.computeRGBHistogram(modelo.getImagenActual());
            int[] histogramGrayOriginal = modelo.computeGrayHistogram(modelo.getImagenActual());

            // Aplicar la ecualización uniforme
            modelo.equalizeUniform();
            vista.mostrarImagen(modelo.getImagenActual());

            // Calcular histogramas de la imagen modificada
            int[][] histogramRGBModified = modelo.computeRGBHistogram();
            int[] histogramGrayModified = modelo.computeGrayHistogram();

            // Mostrar histogramas en ventanas emergentes
            vista.showHistogram(histogramRGBOriginal, histogramGrayOriginal, "Histograma Original (Uniform)");
            vista.showHistogram(histogramRGBModified, histogramGrayModified, "Histograma Modificado (Uniform)");
        }
    }
    
    class EqualizeExponentialButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Calcular histogramas de la imagen original
            int[][] histogramRGBOriginal = modelo.computeRGBHistogram(modelo.getImagenActual());
            int[] histogramGrayOriginal = modelo.computeGrayHistogram(modelo.getImagenActual());

            // Solicitar lambda
            Double lambda = vista.solicitarDouble("Ingrese el valor de lambda para la ecualización exponencial (por ejemplo, 1.0):");
            if (lambda == null) {
                return; // El usuario canceló
            }

            // Aplicar la ecualización exponencial
            modelo.equalizeExponential(lambda);
            vista.mostrarImagen(modelo.getImagenActual());

            // Calcular histogramas de la imagen modificada
            int[][] histogramRGBModified = modelo.computeRGBHistogram();
            int[] histogramGrayModified = modelo.computeGrayHistogram();

            // Mostrar histogramas en ventanas emergentes
            vista.showHistogram(histogramRGBOriginal, histogramGrayOriginal, "Histograma Original (Exponential)");
            vista.showHistogram(histogramRGBModified, histogramGrayModified, "Histograma Modificado (Exponential)");
        }
    }
    
    class EqualizeRayleighButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Calcular histogramas de la imagen original
            int[][] histogramRGBOriginal = modelo.computeRGBHistogram(modelo.getImagenActual());
            int[] histogramGrayOriginal = modelo.computeGrayHistogram(modelo.getImagenActual());

            // Solicitar sigma
            Double sigma = vista.solicitarDouble("Ingrese el valor de sigma para la ecualización Rayleigh (por ejemplo, 0.5):");
            if (sigma == null) {
                return; // El usuario canceló
            }

            // Aplicar la ecualización Rayleigh
            modelo.equalizeRayleigh(sigma);
            vista.mostrarImagen(modelo.getImagenActual());

            // Calcular histogramas de la imagen modificada
            int[][] histogramRGBModified = modelo.computeRGBHistogram();
            int[] histogramGrayModified = modelo.computeGrayHistogram();

            // Mostrar histogramas en ventanas emergentes
            vista.showHistogram(histogramRGBOriginal, histogramGrayOriginal, "Histograma Original (Rayleigh)");
            vista.showHistogram(histogramRGBModified, histogramGrayModified, "Histograma Modificado (Rayleigh)");
        }
    }
    
    class EqualizeHyperbolicLogarithmicButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Calcular histogramas de la imagen original
            int[][] histogramRGBOriginal = modelo.computeRGBHistogram(modelo.getImagenActual());
            int[] histogramGrayOriginal = modelo.computeGrayHistogram(modelo.getImagenActual());

            // Aplicar la ecualización hiperbólica logarítmica
            modelo.equalizeHyperbolicLogarithmic();
            vista.mostrarImagen(modelo.getImagenActual());

            // Calcular histogramas de la imagen modificada
            int[][] histogramRGBModified = modelo.computeRGBHistogram();
            int[] histogramGrayModified = modelo.computeGrayHistogram();

            // Mostrar histogramas en ventanas emergentes
            vista.showHistogram(histogramRGBOriginal, histogramGrayOriginal, "Histograma Original (Hyperbolic Log)");
            vista.showHistogram(histogramRGBModified, histogramGrayModified, "Histograma Modificado (Hyperbolic Log)");
        }
    }
    
    class EqualizeHyperbolicRootsButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Calcular histogramas de la imagen original
            int[][] histogramRGBOriginal = modelo.computeRGBHistogram(modelo.getImagenActual());
            int[] histogramGrayOriginal = modelo.computeGrayHistogram(modelo.getImagenActual());

            // Solicitar k
            Double k = vista.solicitarDouble("Ingrese el exponente k para la ecualización hiperbólica (por ejemplo, 2.0):");
            if (k == null) {
                return; // El usuario canceló
            }

            // Aplicar la ecualización hiperbólica (raíces)
            modelo.equalizeHyperbolicRoots(k);
            vista.mostrarImagen(modelo.getImagenActual());

            // Calcular histogramas de la imagen modificada
            int[][] histogramRGBModified = modelo.computeRGBHistogram();
            int[] histogramGrayModified = modelo.computeGrayHistogram();

            // Mostrar histogramas en ventanas emergentes
            vista.showHistogram(histogramRGBOriginal, histogramGrayOriginal, "Histograma Original (Hyperbolic Roots)");
            vista.showHistogram(histogramRGBModified, histogramGrayModified, "Histograma Modificado (Hyperbolic Roots)");
        }
    }

    class Binarization1ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            Integer umbral = vista.solicitarUmbral("Ingrese el valor del umbral:");
            if (umbral != null) {
                modelo.binarizeWith1Threshold(umbral);
                vista.mostrarImagen(modelo.getImagenActual());
            }
        }
    }

    class Binarization2ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            Integer[] umbrales = vista.solicitarDosUmbrales("Ingrese el valor del umbral inferior:", "Ingrese el valor del umbral superior:");
            if (umbrales != null) {
                modelo.binarizeWithTwoThresholds(umbrales[0], umbrales[1]);
                vista.mostrarImagen(modelo.getImagenActual());
            }
        }
    }

    class Binarization3ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            Integer[] umbrales = vista.solicitarTresUmbrales("Ingrese el valor del primer umbral:", "Ingrese el valor del segundo umbral:", "Ingrese el valor del tercer umbral:");
            if (umbrales != null) {
                modelo.binarizeWithThreeThresholds(umbrales[0], umbrales[1], umbrales[2]);
                vista.mostrarImagen(modelo.getImagenActual());
            }
        }
    }

    class BinarizationToRGBButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.convertBinaryToRGB();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class InvertBinButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.invertBinarization();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class GaussianNoiseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.addGaussianNoise();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class UniformNoiseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.addUniformNoise();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class ExponentialNoiseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.addExponentialNoise();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class GammaNoiseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.addGammaNoise();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class RayleighNoiseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.addRayleighNoise();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class SaltPepperNoiseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.addSaltAndPepperNoise(0.05);
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class SaltNoiseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.addSaltNoise();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class PepperNoiseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.addPepperNoise();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class CoherentNoiseButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.addCoherentNoise();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class TranslationButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            // Crear un diálogo para ingresar los valores de traslación
            JDialog traslacionDialog = new JDialog();
            traslacionDialog.setTitle("Traslación");
            traslacionDialog.setModal(true);
            traslacionDialog.setLayout(new BorderLayout());

            // Panel para ingresar los valores de dx y dy
            JPanel valorPanel = new JPanel();
            valorPanel.setLayout(new GridLayout(2, 2, 10, 10)); // Espacio horizontal y vertical entre celdas
            valorPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Margen alrededor del panel
            JLabel dxLabel = new JLabel("Desplazamiento en X (dx):");
            JTextField dxField = new JTextField(10); // Aumentar el tamaño del campo de texto
            JLabel dyLabel = new JLabel("Desplazamiento en Y (dy):");
            JTextField dyField = new JTextField(10); // Aumentar el tamaño del campo de texto
            valorPanel.add(dxLabel);
            valorPanel.add(dxField);
            valorPanel.add(dyLabel);
            valorPanel.add(dyField);

            // Panel para los botones de aceptar y cancelar
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Centrar botones con espacio
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15)); // Margen inferior y laterales
            JButton aceptarButton = new JButton("Aceptar");
            JButton cancelarButton = new JButton("Cancelar");
            buttonPanel.add(aceptarButton);
            buttonPanel.add(cancelarButton);

            // Agregar los paneles al diálogo
            traslacionDialog.add(valorPanel, BorderLayout.CENTER);
            traslacionDialog.add(buttonPanel, BorderLayout.SOUTH);

            // Evento del botón de aceptar
            aceptarButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        int dx = Integer.parseInt(dxField.getText());
                        int dy = Integer.parseInt(dyField.getText());
                        modelo.translate(dx, dy);
                        vista.mostrarImagen(modelo.getImagenActual());
                        traslacionDialog.dispose();
                    } catch (NumberFormatException ex) {
                        vista.mostrarMensajeError("Por favor, ingrese valores numéricos válidos.");
                    }
                }
            });

            // Evento del botón de cancelar
            cancelarButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    traslacionDialog.dispose();
                }
            });

            // Ajustar el tamaño del diálogo y mostrarlo
            traslacionDialog.pack();
            traslacionDialog.setLocationRelativeTo(vista);
            traslacionDialog.setVisible(true);
        }
    }

    class RotationButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            String angleInput = JOptionPane.showInputDialog(vista, "Ingrese el ángulo de rotación (en grados):", "Rotación", JOptionPane.QUESTION_MESSAGE);
            try {
                double angle = Double.parseDouble(angleInput);
                modelo.rotate(angle);
                vista.mostrarImagen(modelo.getImagenActual());
            } catch (NumberFormatException ex) {
                vista.mostrarMensajeError("Por favor, ingrese un valor numérico válido.");
            }
        }
    }

    class InterpolationButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            String scaleInput = JOptionPane.showInputDialog(vista, "Ingrese el factor de escala (por ejemplo, 2 para duplicar el tamaño):", "Interpolación", JOptionPane.QUESTION_MESSAGE);
            try {
                double scale = Double.parseDouble(scaleInput);
                if (scale <= 0) {
                    vista.mostrarMensajeError("El factor de escala debe ser mayor que 0.");
                    return;
                }
                modelo.interpolate(scale);
                vista.mostrarImagen(modelo.getImagenActual());
            } catch (NumberFormatException ex) {
                vista.mostrarMensajeError("Por favor, ingrese un valor numérico válido.");
            }
        }
    }
    
    class BrightnessSliderListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            JSlider slider = (JSlider) e.getSource();
            int brightnessValue = slider.getValue();
            modelo.ajustarBrillo(brightnessValue);
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class ContrastSliderListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            JSlider slider = (JSlider) e.getSource();
            int contrastValue = slider.getValue();
            double contrastFactor = (contrastValue / 50.0); // 0 -> 0.0, 50 -> 1.0, 100 -> 2.0
            if (contrastFactor == 0) contrastFactor = 0.1; // Evitar factor 0
            modelo.ajustarContraste(contrastFactor);
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class SumButtonListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
        if (modelo.getImagenActual() == null) {
            vista.mostrarMensajeError("No hay una imagen cargada.");
            return;
        }
        BufferedImage segundaImagen = cargarSegundaImagen();
        if (segundaImagen != null) {
            try {
                modelo.sum(segundaImagen);
                vista.mostrarImagen(modelo.getImagenActual());
            } catch (IllegalArgumentException ex) {
                vista.mostrarMensajeError(ex.getMessage());
            }
        }
    }
}
    
    class SubtractButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            BufferedImage segundaImagen = cargarSegundaImagen();
            if (segundaImagen != null) {
                try {
                    modelo.subtraction(segundaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (IllegalArgumentException ex) {
                    vista.mostrarMensajeError(ex.getMessage());
                }
            }
        }
    }
    
    class MultiplyButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            BufferedImage segundaImagen = cargarSegundaImagen();
            if (segundaImagen != null) {
                try {
                    modelo.multiplication(segundaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (IllegalArgumentException ex) {
                    vista.mostrarMensajeError(ex.getMessage());
                }
            }
        }
    }
    
    class DivideButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            BufferedImage segundaImagen = cargarSegundaImagen();
            if (segundaImagen != null) {
                try {
                    modelo.division(segundaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (IllegalArgumentException ex) {
                    vista.mostrarMensajeError(ex.getMessage());
                }
            }
        }
    }
    
    class AndButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            BufferedImage segundaImagen = cargarSegundaImagen();
            if (segundaImagen != null) {
                try {
                    modelo.and(segundaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (IllegalArgumentException ex) {
                    vista.mostrarMensajeError(ex.getMessage());
                }
            }
        }
    }

    class OrButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            BufferedImage segundaImagen = cargarSegundaImagen();
            if (segundaImagen != null) {
                try {
                    modelo.or(segundaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (IllegalArgumentException ex) {
                    vista.mostrarMensajeError(ex.getMessage());
                }
            }
        }
    }

    class XorButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            BufferedImage segundaImagen = cargarSegundaImagen();
            if (segundaImagen != null) {
                try {
                    modelo.xor(segundaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (IllegalArgumentException ex) {
                    vista.mostrarMensajeError(ex.getMessage());
                }
            }
        }
    }

    class NotButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.not();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class LessThanButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            BufferedImage segundaImagen = cargarSegundaImagen();
            if (segundaImagen != null) {
                try {
                    modelo.lessThan(segundaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (IllegalArgumentException ex) {
                    vista.mostrarMensajeError(ex.getMessage());
                }
            }
        }
    }

    class LessOrEqualThanButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            BufferedImage segundaImagen = cargarSegundaImagen();
            if (segundaImagen != null) {
                try {
                    modelo.lessThanOrEqual(segundaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (IllegalArgumentException ex) {
                    vista.mostrarMensajeError(ex.getMessage());
                }
            }
        }
    }

    class GreaterThanButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            BufferedImage segundaImagen = cargarSegundaImagen();
            if (segundaImagen != null) {
                try {
                    modelo.greaterThan(segundaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (IllegalArgumentException ex) {
                    vista.mostrarMensajeError(ex.getMessage());
                }
            }
        }
    }

    class CannyEdgeButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.cannyEdgeDetection();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class GreaterOrEqualThanButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            BufferedImage segundaImagen = cargarSegundaImagen();
            if (segundaImagen != null) {
                try {
                    modelo.greaterThanOrEqual(segundaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (IllegalArgumentException ex) {
                    vista.mostrarMensajeError(ex.getMessage());
                }
            }
        }
    }

    class EqualButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            BufferedImage segundaImagen = cargarSegundaImagen();
            if (segundaImagen != null) {
                try {
                    modelo.equal(segundaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (IllegalArgumentException ex) {
                    vista.mostrarMensajeError(ex.getMessage());
                }
            }
        }
    }

    class NotEqualButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            BufferedImage segundaImagen = cargarSegundaImagen();
            if (segundaImagen != null) {
                try {
                    modelo.notEqual(segundaImagen);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (IllegalArgumentException ex) {
                    vista.mostrarMensajeError(ex.getMessage());
                }
            }
        }
    }
    
    class ConvertToYIQButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.convertToYIQprocessingY();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class ConvertToHSVButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.convertToHSVprocessingV();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class ConvertToHSIButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.convertToHSIprocessingI();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class HomogeneityOperatorButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.operadorHomogeneidad();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class DifferenceOperatorButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.operadorDiferencia();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class GradientFirstOrderButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.gradientePrimerOrden();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class PrewittButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.filtroPrewitt();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class SobelButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.filtroSobel();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class FreiChenButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.filtroFreiChen();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class CompassGradientButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.gradienteCompas();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class LaplacianButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.filtroLaplaciano();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    class PrewittSecondOrderButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.prewittSegundoOrden();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class KirschButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.filtroKirsch();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class RobinsonButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.filtroRobinson();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class AverageFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.filtroPromediador();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class GaussianFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.filtroGaussiano();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class SharpenFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.filtroDefinicion();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class MedianFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            modelo.filtroMediana(); // Aplica el filtro de mediana en el modelo
            vista.mostrarImagen(modelo.getImagenActual()); // Actualiza la vista
        }
    }
    
    class MinFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            modelo.filtroMinimo();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class MaxFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            modelo.filtroMaximo();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class MidpointFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            modelo.filtroPuntoMedio();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class AlphaTrimmedFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            modelo.filtroAlfaRecortado();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class HarmonicMeanFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            modelo.filtroArmonicoInferior();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class ContraHarmonicFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            modelo.filtroContraArmonico();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class GeometricMeanFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            modelo.filtroGeometricoInferior();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class MaxMinFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            modelo.filtroMaximoMenosMinimo();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class ArithmeticMeanFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            modelo.filtroMediaAritmetica();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class ContraGeometricMeanFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            modelo.filtroContraGeometrico();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class ModeFilterButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            modelo.filtroModa();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }
    
    class CompararFiltrosListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            modelo.mostrarResumenComparacion();
        }
    }
    
    private BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    class BinaryErotionButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            
            ElementoEstructura ventana = new ElementoEstructura();
            ventana.setElementoEstructuraListener(estructura -> {
                modelo.setElementoEstructurante(estructura);
                modelo.BinaryErotion();
                vista.mostrarImagen(modelo.getImagenActual());
            });
            ventana.setVisible(true);
        }
    }

    class BinaryDilationButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            
            ElementoEstructura ventana = new ElementoEstructura();
            ventana.setElementoEstructuraListener(estructura -> {
                modelo.setElementoEstructurante(estructura);
                modelo.BinaryDilation();
                vista.mostrarImagen(modelo.getImagenActual());
            });
            ventana.setVisible(true);
        }
    }

    class GrayScaleErotionButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            
            ElementoEstructura ventana = new ElementoEstructura();
            ventana.setElementoEstructuraListener(estructura -> {
                modelo.setElementoEstructurante(estructura);
                modelo.GrayScaleErotion();
                vista.mostrarImagen(modelo.getImagenActual());
            });
            ventana.setVisible(true);
        }
    }

    class GrayScaleDilationButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            
            ElementoEstructura ventana = new ElementoEstructura();
            ventana.setElementoEstructuraListener(estructura -> {
                modelo.setElementoEstructurante(estructura);
                modelo.GrayScaleDilation();
                vista.mostrarImagen(modelo.getImagenActual());
            });
            ventana.setVisible(true);
        }
    }
    
    class BinaryOpeningButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            
            ElementoEstructura ventana = new ElementoEstructura();
            ventana.setElementoEstructuraListener(estructura -> {
                modelo.setElementoEstructurante(estructura);
                BufferedImage backup = deepCopy(modelo.getImagenActual());
                modelo.BinaryErotion();
                BufferedImage afterErosion = deepCopy(modelo.getImagenActual()); // resultado tras erosión
                modelo.setImagenActual(afterErosion);
                modelo.BinaryDilation();
                vista.mostrarImagen(modelo.getImagenActual());
            });
            ventana.setVisible(true);
        }
    }
    
    class BinaryClosingButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            
            ElementoEstructura ventana = new ElementoEstructura();
            ventana.setElementoEstructuraListener(estructura -> {
                modelo.setElementoEstructurante(estructura);
                BufferedImage backup = deepCopy(modelo.getImagenActual());
                modelo.BinaryDilation();
                BufferedImage afterDilation = deepCopy(modelo.getImagenActual()); // resultado tras erosión
                modelo.setImagenActual(afterDilation);
                modelo.BinaryErotion();
                vista.mostrarImagen(modelo.getImagenActual());
            });
            ventana.setVisible(true);
        }
    }
    
    class GrayScaleOpeningButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            
            ElementoEstructura ventana = new ElementoEstructura();
            ventana.setElementoEstructuraListener(estructura -> {
                modelo.setElementoEstructurante(estructura);
                BufferedImage backup = deepCopy(modelo.getImagenActual());
                modelo.GrayScaleErotion();
                BufferedImage afterErosion = deepCopy(modelo.getImagenActual());
                modelo.setImagenActual(afterErosion);
                modelo.GrayScaleDilation();
                vista.mostrarImagen(modelo.getImagenActual());
            });
            ventana.setVisible(true);
        }
    }
    
    class GrayScaleClosingButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            
            ElementoEstructura ventana = new ElementoEstructura();
            ventana.setElementoEstructuraListener(estructura -> {
                modelo.setElementoEstructurante(estructura);
                BufferedImage backup = deepCopy(modelo.getImagenActual());
                modelo.GrayScaleDilation();
                BufferedImage afterDilation = deepCopy(modelo.getImagenActual());
                modelo.setImagenActual(afterDilation);
                modelo.GrayScaleErotion();
                vista.mostrarImagen(modelo.getImagenActual());
            });
            ventana.setVisible(true);
        }
    }
    
    class hitOrAndMissButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e){
            if(modelo.getImagenActual() == null){
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }

            ElementoEstructura ventana = new ElementoEstructura();
            ventana.setElementoEstructuraListener(estructura -> {
                modelo.setElementoEstructurante(estructura);
                BufferedImage backup = deepCopy(modelo.getImagenActual());
                modelo.hitOrAndMissTransformed();
                vista.mostrarImagen(modelo.getImagenActual());
            });
        }
    }

    class waterSheedsButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.waterSheedTransformed();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class LabeledConectivity4ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.etiquetadoComponentes4();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class LabeledConectivity8ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            modelo.etiquetadoComponentes8();
            vista.mostrarImagen(modelo.getImagenActual());
        }
    }

    class CountObjectsButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            // Preguntar al usuario por la conectividad (4 u 8)
            Object[] options = {"Conectividad 4", "Conectividad 8"};
            int seleccion = JOptionPane.showOptionDialog(
                vista,
                "Seleccione el tipo de conectividad para el conteo de objetos:",
                "Tipo de Conectividad",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );
            int conectividad = (seleccion == 1) ? 8 : 4;
            int count = modelo.contarYMostrarObjetos(conectividad);
            vista.mostrarMensajeInformativo("Número de objetos etiquetados: " + count);
        }
    }

    class KMeansButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (modelo.getImagenActual() == null) {
                vista.mostrarMensajeError("No hay una imagen cargada.");
                return;
            }
            vista.Kmeans dialog = new vista.Kmeans();
            dialog.setKmeansListener(grupos -> {
                if (grupos <= 0) {
                    vista.mostrarMensajeError("Número de grupos inválido.");
                    return;
                }
                try {
                    modelo.kmeansColorQuantization(grupos);
                    vista.mostrarImagen(modelo.getImagenActual());
                } catch (Exception ex) {
                    vista.mostrarMensajeError("Error al aplicar KMeans: " + ex.getMessage());
                }
            });
            dialog.setLocationRelativeTo(vista);
            dialog.setVisible(true);
        }
    }
    
    public ImagenControlador(ImagenModelo modelo, ImagenVista vista) {
        this.modelo = modelo;
        this.vista = vista;

        // Asignar listeners a los botones
        // Conectividad y etiquetados
        vista.getLabeled4Button().addActionListener(new LabeledConectivity4ButtonListener());
        vista.getLabeled8Button().addActionListener(new LabeledConectivity8ButtonListener());
        vista.getCountObjectsButton().addActionListener(new CountObjectsButtonListener());
        
        // Sliders
        vista.getBrightnessSlider().addChangeListener(new BrightnessSliderListener());
        vista.getContrastSlider().addChangeListener(new ContrastSliderListener());
        
        // Operaciones
        vista.getSumButton().addActionListener(new SumButtonListener());
        vista.getSubtractionButton().addActionListener(new SubtractButtonListener());
        vista.getMultiplicationButton().addActionListener(new MultiplyButtonListener());
        vista.getDivisionButton().addActionListener(new DivideButtonListener());
        vista.getAndButton().addActionListener(new AndButtonListener());
        vista.getOrButton().addActionListener(new OrButtonListener());
        vista.getXorButton().addActionListener(new XorButtonListener());
        vista.getNotButton().addActionListener(new NotButtonListener());
        vista.getLessThanButton().addActionListener(new LessThanButtonListener());
        vista.getLessThanOrEqualButton().addActionListener(new LessOrEqualThanButtonListener());
        vista.getGreaterThanButton().addActionListener(new GreaterThanButtonListener());
        vista.getGreaterThanOrEqualButton().addActionListener(new GreaterOrEqualThanButtonListener());
        vista.getEqualButton().addActionListener(new EqualButtonListener());
        vista.getNotEqualButton().addActionListener(new NotEqualButtonListener());
        
        // Carga y guardado
        vista.getLoadButton().addActionListener(new LoadButtonListener());
        vista.getSaveButton().addActionListener(new SaveButtonListener());
        vista.getResetButton().addActionListener(new ResetButtonListener());
        
        // Filtros básicos (escala de grises, canales RGB)
        vista.getGrayButton().addActionListener(new GrayButtonListener());
        vista.getRedButton().addActionListener(new RedButtonListener());
        vista.getGreenButton().addActionListener(new GreenButtonListener());
        vista.getBlueButton().addActionListener(new BlueButtonListener());
        vista.getYiqButton1().addActionListener(new ConvertToYIQButtonListener());
        vista.getHsvButton1().addActionListener(new ConvertToHSVButtonListener());
        vista.getHsiButton1().addActionListener(new ConvertToHSIButtonListener());
        vista.getCannyEdgeButton().addActionListener(new CannyEdgeButtonListener());

        // Conversiones de color
        vista.getRgbToYIQButton().addActionListener(new RgbToYIQButtonListener());
        vista.getYiqToRGBButton().addActionListener(new YiqToRGBButtonListener());
        vista.getRgbToHSIButton().addActionListener(new RgbToHSIButtonListener());
        vista.getHsiToRGBButton().addActionListener(new HsiToRGBButtonListener());
        vista.getRgbToHSVButton().addActionListener(new RgbToHSVButtonListener());
        vista.getHsvToRGBButton().addActionListener(new HsvToRGBButtonListener());
        vista.getRgbToCMYButton().addActionListener(new RgbToCMYButtonListener());
        vista.getCmyToRGBButton().addActionListener(new CmyToRGBButtonListener());
        vista.getCmyToCMYKButton().addActionListener(new CmyToCMYKButtonListener());
        vista.getRgbToLabButton().addActionListener(new RgbToLabButtonListener());
        vista.getLabToRGBButton().addActionListener(new LabToRGBButtonListener());
        
        // Histogramas
        vista.getShowHistogramButton().addActionListener(new ShowHistogramButtonListener());
        vista.getScaleHistogramButton().addActionListener(new ScaleHistogramButtonListener());
        vista.getShiftHistogramButton().addActionListener(new ShiftHistogramButtonListener());
        vista.getMatchHistogramButton().addActionListener(new MatchHistogramButtonListener());
        vista.getEqualizeHistogramButton().addActionListener(new EqualizeHistogramButtonListener());
        vista.getEqualizeUniformButton().addActionListener(new EqualizeUniformButtonListener());
        vista.getEqualizeExponentialButton().addActionListener(new EqualizeExponentialButtonListener());
        vista.getEqualizeRayleighButton().addActionListener(new EqualizeRayleighButtonListener());
        vista.getEqualizeHyperbolicLogarithmicButton().addActionListener(new EqualizeHyperbolicLogarithmicButtonListener());
        vista.getEqualizeHyperbolicRootsButton().addActionListener(new EqualizeHyperbolicRootsButtonListener());
        
        // Binarización
        vista.getBinarization1Button().addActionListener(new Binarization1ButtonListener());
        vista.getBinarization2Button().addActionListener(new Binarization2ButtonListener());
        vista.getBinarization3Button().addActionListener(new Binarization3ButtonListener());
        vista.getBinarizationToRGBButton().addActionListener(new BinarizationToRGBButtonListener());
        vista.getInvertBinButton().addActionListener(new InvertBinButtonListener());
        
        // Ruido
        vista.getGaussianNoiseButton().addActionListener(new GaussianNoiseButtonListener());
        vista.getUniformNoiseButton().addActionListener(new UniformNoiseButtonListener());
        vista.getExponentialNoiseButton().addActionListener(new ExponentialNoiseButtonListener());
        vista.getGammaNoiseButton().addActionListener(new GammaNoiseButtonListener());
        vista.getRayleighNoiseButton().addActionListener(new RayleighNoiseButtonListener());
        vista.getSaltPepperNoiseButton().addActionListener(new SaltPepperNoiseButtonListener());
        vista.getCoherentNoiseButton().addActionListener(new CoherentNoiseButtonListener());
        vista.getSaltNoiseButton().addActionListener(new SaltNoiseButtonListener());
        vista.getPepperNoiseButton().addActionListener(new PepperNoiseButtonListener());
        
        // Transformaciones geométricas
        vista.getTranslationButton().addActionListener(new TranslationButtonListener());
        vista.getRotationButton().addActionListener(new RotationButtonListener());
        vista.getInterpolationButton().addActionListener(new InterpolationButtonListener());
        
        //Filtros Lineales
        vista.getHomogeneityOperatorButton().addActionListener(new HomogeneityOperatorButtonListener());
        vista.getDifferenceOperatorButton().addActionListener(new DifferenceOperatorButtonListener());
        vista.getGradientFirstOrderButton().addActionListener(new GradientFirstOrderButtonListener());
        vista.getPrewittButton().addActionListener(new PrewittButtonListener());
        vista.getSobelButton().addActionListener(new SobelButtonListener());
        vista.getFreiChenButton().addActionListener(new FreiChenButtonListener());
        vista.getCompassGradientButton().addActionListener(new CompassGradientButtonListener());
        vista.getLaplacianButton().addActionListener(new LaplacianButtonListener());
        vista.getPrewittSecondOrderButton().addActionListener(new PrewittSecondOrderButtonListener());
        vista.getKirschButton().addActionListener(new KirschButtonListener());
        vista.getRobinsonButton().addActionListener(new RobinsonButtonListener());
        vista.getAverageFilterButton().addActionListener(new AverageFilterButtonListener());
        vista.getGaussianFilterButton().addActionListener(new GaussianFilterButtonListener());
        vista.getSharpenFilterButton().addActionListener(new SharpenFilterButtonListener());
        
        //Filtros No Lineales
        vista.getMedianFilterButton().addActionListener(new MedianFilterButtonListener());
        vista.getMinFilterButton().addActionListener(new MinFilterButtonListener());
        vista.getMaxFilterButton().addActionListener(new MaxFilterButtonListener());
        vista.getMidpointFilterButton().addActionListener(new MidpointFilterButtonListener());
        vista.getAlphaTrimmedFilterButton().addActionListener(new AlphaTrimmedFilterButtonListener());
        vista.getHarmonicMeanFilterButton().addActionListener(new HarmonicMeanFilterButtonListener());
        vista.getContraHarmonicFilterButton().addActionListener(new ContraHarmonicFilterButtonListener());
        vista.getGeometricMeanFilterButton().addActionListener(new GeometricMeanFilterButtonListener());
        vista.getMaxMinFilterButton().addActionListener(new MaxMinFilterButtonListener());
        vista.getArithmeticMeanFilterButton().addActionListener(new ArithmeticMeanFilterButtonListener());
        vista.getContraGeometricMeanFilterButton().addActionListener(new ContraGeometricMeanFilterButtonListener());
        vista.getModeFilterButton().addActionListener(new ModeFilterButtonListener());
        vista.getCompararFiltrosButton().addActionListener(new CompararFiltrosListener());

        // KMeans: abrir diálogo para ingresar número de grupos y aplicar agrupamiento por color
        vista.getKMeans().addActionListener(new KMeansButtonListener());

        // Morfología
        vista.getBinaryErotionButton().addActionListener(new BinaryErotionButtonListener());
        vista.getBinaryDilationButton().addActionListener(new BinaryDilationButtonListener());
        vista.getGrayScaleErotionButton().addActionListener(new GrayScaleErotionButtonListener());
        vista.getGrayScaleDilationButton().addActionListener(new GrayScaleDilationButtonListener());
        vista.getBinaryOpeningButton().addActionListener(new BinaryOpeningButtonListener());
        vista.getBinaryClosingButton().addActionListener(new BinaryClosingButtonListener());
        vista.getGrayScaleOpeningButton().addActionListener(new GrayScaleOpeningButtonListener());
        vista.getGrayScaleClosingButton().addActionListener(new GrayScaleClosingButtonListener());
        vista.getHitOrAndMissButton().addActionListener(new hitOrAndMissButtonListener());
        vista.getWaterSheedsButton().addActionListener(new waterSheedsButtonListener());
    }
}
