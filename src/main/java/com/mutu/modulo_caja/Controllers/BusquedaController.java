package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Services.Servicio;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import net.synedra.validatorfx.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class BusquedaController implements Initializable {

  @FXML private TextField txtNombreSocio;

  @FXML private Label lblError;

  @FXML private TableView tableSocios;

  @FXML private TableColumn<Object[], String> colSocio;

  @FXML private TableColumn<Object[], String> colNumero;

  @FXML private TableColumn<Object[], String> colTipo;

  @FXML private TableColumn<Object[], String> colEmpresa;

  public Validator validator = new Validator();

  @Autowired public Servicio busquedaServicio;

  public CajeroController cajeroController;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // Validación del usuario
    validator
        .createCheck()
        .dependsOn("input", txtNombreSocio.textProperty())
        .withMethod(
            c -> {
              String texto = c.get("input");
              if (texto == null || texto.isEmpty()) {
                c.error("El campo no puede estar vacío");
                lblError.setText("El campo no puede estar vacío");
              } else if (texto.matches(".*\\d.*")) {
                c.error("No se permiten números en este campo");
                lblError.setText("No se permiten números en este campo");
              } else if (texto.length() < 3) {
                c.error("El texto debe tener al menos 3 caracteres");
                lblError.setText("El texto debe tener al menos 3 caracteres");
              } else {
                lblError.setText("");
              }
            })
        .decorates(txtNombreSocio)
        .immediate();

    txtNombreSocio.setTextFormatter(
        new TextFormatter<>(
            change -> {
              change.setText(change.getText().toUpperCase());
              if (change.getText().matches("[0-9]")) {
                change.setText("");
              }
              return change;
            }));

    colSocio.setCellValueFactory(
        cellData -> new SimpleStringProperty((String) cellData.getValue()[0]));
    colNumero.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[1])));
    colTipo.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[2])));
    colEmpresa.setCellValueFactory(
        cellData -> new SimpleStringProperty((String) cellData.getValue()[3]));
  }

  public void setCajeroController(CajeroController controller) {
    this.cajeroController = controller;
  }

  @FXML
  public void cerrarModal(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ESCAPE)) {
      validator = new Validator();
      Stage ventanaActual = (Stage) txtNombreSocio.getScene().getWindow();
      ventanaActual.close();
    } else if (event.getCode().equals((KeyCode.ENTER)) && !txtNombreSocio.getText().isEmpty()) {
      traerCoincidencias();
    }
  }

  @FXML
  public void traerCoincidencias() {
    String socioBuscar = txtNombreSocio.getText().trim();
    List<Object[]> resultados = busquedaServicio.buscarSocioPorNombre(socioBuscar);

    ObservableList<Object[]> socios = FXCollections.observableArrayList();

    for (Object[] resultado : resultados) {
      socios.add(resultado);
    }

    tableSocios.setItems(socios);
  }

  public void cargarSocio(MouseEvent event) {
    if (event.getClickCount() == 2) {
      Object[] selectedRow = (Object[]) tableSocios.getSelectionModel().getSelectedItem();

      if (selectedRow != null) {
        String nombre = (String) selectedRow[0];
        String numSocio = String.valueOf(selectedRow[1]);
        String tipo = String.valueOf(selectedRow[2]);

        cajeroController.cargarSocioPorNombre(nombre, numSocio, tipo);

        // Cierra la ventana actual
        validator = new Validator();
        Stage ventanaActual = (Stage) txtNombreSocio.getScene().getWindow();
        ventanaActual.close();
      }
    }
  }
}
