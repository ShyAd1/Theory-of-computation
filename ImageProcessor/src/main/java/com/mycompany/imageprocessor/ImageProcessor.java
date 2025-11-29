/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.imageprocessor;

import modelo.ImagenModelo;
import vista.ImagenVista;
import controlador.ImagenControlador;
import javax.swing.SwingUtilities;

public class ImageProcessor {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Crear instancias del modelo, vista y controlador
                ImagenModelo modelo = new ImagenModelo();
                ImagenVista vista = new ImagenVista();
                ImagenControlador controlador = new ImagenControlador(modelo, vista);

                // Hacer visible la ventana principal
                vista.setVisible(true);
            }
        });
    }
}