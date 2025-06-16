package com.mutu.modulo_caja.utils;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;

import javax.swing.*;
import java.io.InputStream;

public class Report {
    public void mostrarReporte() {
        // Ejemplo de cómo usar SwingUtilities
        SwingUtilities.invokeLater(() -> {
            try {
                // Ruta al archivo .jrxml
                String jrxmlFile = "Reports/desembolso.jrxml";


                // Compilar el archivo JRXML a un archivo .jasper
                InputStream jrxmlStream = Class.class.getResourceAsStream(jrxmlFile);
                JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);

                // Rellenar el reporte con datos
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, new JREmptyDataSource());

                // Visualizar el reporte
                JasperViewer viewer = new JasperViewer(jasperPrint, false);
                viewer.setVisible(true);

            } catch (Exception e) {
                e.printStackTrace(); // Para obtener más detalles del error
                throw new RuntimeException("Error al generar el reporte", e);
            }
        });
    }
}
