package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Models.ModelAhorro;
import com.mutu.modulo_caja.Models.ModelCredito;
import com.mutu.modulo_caja.Models.ModelEmpresa;
import com.mutu.modulo_caja.Services.Servicio;
import com.tenpisoft.n2w.MoneyConverters;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class DesembolsoController implements Initializable {

  @FXML private Button btnCancelar;

  @FXML private TableView tableDesembolsos;

  @FXML private TableColumn<Object[], String> colSocio, colNombre, colMonto, colEstado, colCredito;

  @Autowired public Servicio servicio;

  public int socio;
  public String usuario;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Platform.runLater(
        () -> {
          Stage stage = (Stage) btnCancelar.getScene().getWindow();
          stage.setOnCloseRequest(event -> cierreDeVentana(event));
        });

    colSocio.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[0])));
    colNombre.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[3])));
    colMonto.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[1])));
    colEstado.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[2])));
    colCredito.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[4])));
  }

  public void setDatos(int socio, String usuario) {
    this.socio = socio;
    this.usuario = usuario;
    desembolsosPendientes(socio);
  }

  public void desembolsosPendientes(int socio) {
    List<Object[]> resultados = servicio.desembolsosPendientes(socio);

    ObservableList<Object[]> desembolsos = FXCollections.observableArrayList();

    for (Object[] resultado : resultados) {
      desembolsos.add(resultado);
    }

    tableDesembolsos.setItems(desembolsos);
  }

  public void cierreDeVentana(Event event) {
    event.consume();
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CIERRE DE VENTANA");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
    alert.setContentText(
        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
            + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();
    }
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.CONTROL)) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("CIERRE DE VENTANA");
      alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
      alert.setContentText(
          "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
              + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");

      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
        ventanaActual.close();
      }
    }
    if (event.getCode().equals(KeyCode.ENTER)) {
      procesarDesembolso();
    }
  }

  @FXML
  public void cerrarConBoton() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CIERRE DE VENTANA");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
    alert.setContentText(
        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
            + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();
    }
  }

  @FXML
  public void procesarDesembolso() {
    if (!tableDesembolsos.getItems().isEmpty() && tableDesembolsos.getItems().size() == 1) {
      Object[] primeraFila = (Object[]) tableDesembolsos.getItems().get(0);

      String montoString = String.valueOf(primeraFila[1]);

      montoString = montoString.replaceAll("[\\$,]", "");
      double monto = Double.parseDouble(montoString);

      String colCredito = String.valueOf(primeraFila[4]);

      String socio = String.valueOf(primeraFila[0]);
      String nomsocio = String.valueOf(primeraFila[3]);
      LocalDateTime fecha = LocalDateTime.now();
      LocalDateTime fechaVenc = fecha.plusYears(1);
      LocalTime hora = fecha.toLocalTime();
      DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
      String fechaTicket = fecha.format(formatter);
      String fechaVencticket = fechaVenc.format(formatter);
      String horaticket = hora.format(formatterHora);

      Map<String, Object> result =
          servicio.ProcesarDesembolso(Integer.parseInt(colCredito), usuario, monto, horaticket, "",0);

      if  (result.get("Resultado").toString().equals("CORRECTO")) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("DESEMBOLSO PROCESADO CON ÉXITO");
        alert.setHeaderText("DESEMBOLSO PROCESADO CON ÉXITO");
        alert.setContentText(
            "DESEMBOLSO DEL SOCIO: " + socio + " POR: " + montoString + " HECHO CON EXITO");
          alert.showAndWait();
        String empresa = servicio.traerEmpresa("0002").getCodigo();

        try {
          InputStream isLogo = null;
          String nombreEmpresa = servicio.traerEmpresa(empresa).getRazonSocial();
          String rfcEmpresa = servicio.traerEmpresa(empresa).getRfc();
          String direcEmpresa =
              servicio.traerEmpresa(empresa).getCalle()
                  + " "
                  + servicio.traerEmpresa(empresa).getCruzamiento()
                  + " COL. CENTRO";
          String folio = result.get("transaccion_id").toString();

          NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
          String montoenviar = formatoMoneda.format(monto);
          MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
          String moneyAsWords = converter.asWords(BigDecimal.valueOf(monto)).toUpperCase() + " MXN";
          isLogo = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
          ModelCredito credito = servicio.traerDatosCredito(Integer.parseInt(colCredito));
          String ord = String.valueOf(credito.getOrdinarios()) + " %";
          String mor = String.valueOf(credito.getMoratorios()) + " %";
          String fechvenc = String.valueOf(credito.getFecha_venci());
          isLogo = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
          Map pars = new HashMap<>();
          pars.put("Empresa", nombreEmpresa);
          pars.put("Logo", isLogo);
          pars.put("RFC", rfcEmpresa);
          pars.put("Direccion", direcEmpresa);
          pars.put("Numcredito", colCredito);
          pars.put("Titulo", "REPORTE DE DESEMBOLSO DE CRÉDITO");
          pars.put("Fecha", fechaTicket);
          pars.put("Folio", folio);
          pars.put("NumSocio", String.valueOf(socio));
          pars.put("NombreSocio", nomsocio);

          pars.put("IntOrd", ord);
          pars.put("IntMora", mor);
          pars.put("Vencimiento", fechaVencticket);

          pars.put("Monto", montoenviar);
          pars.put("MontoLetras", moneyAsWords);
          pars.put("Cajero", LoginController.usuarioLoggeado);
          pars.put("Hora", horaticket);
          pars.put("LogoImg", isLogo);
          pars.put(
              "Descripcion",
              "Recibí de " + nombreEmpresa + " la cantidad de "+ montoenviar +
                      " ("+moneyAsWords +
                      ") recibido en efectivo a mi entera satisfacción. Así mismo, manifiesto conocer y apegarme al " +
                      "cumplimiento del acuerdo 2 de la Asamblea General efectuada el 22 de Julio de 2011," +
                      " el cual menciona que todo socio que realice un crédito por sus ahorros o menos y que " +
                      "en seis meses consecutivos no realice abono alguno a su crédito, será dado de baja con el fin " +
                      "de evitar el incremento de su deuda y la cartera vencida.");

          InputStream isRepo = getClass().getResourceAsStream("/Reports/desembolso.jasper");
          JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
          JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

          JasperViewer viewer = new JasperViewer(jpRepo, false);

          viewer.setAlwaysOnTop(true);
          viewer.setSize(800, 600);
          viewer.setLocationRelativeTo(null);
          viewer.setTitle("REPORTE DE DESEMBOLSO");
          viewer.setVisible(true);

        } catch (Exception e) {
          e.printStackTrace();
        }

        Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
        ventanaActual.close();
      } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR AL QUERER PROCESAR EL DESEMBOLSO");
        alert.setHeaderText("ERROR EN EL DESEMBOLSO");
        alert.setContentText(result.get("Resultado").toString().toUpperCase());
        alert.showAndWait();
      }
    } else {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("NO HAY DESEMBOLSO PENDIENTE");
      alert.setHeaderText("NO HAY DESEMBOLSO PENDIENTE");
      alert.setContentText("NO HAY DESEMBOLSO PENDIENTE");
      alert.showAndWait();
    }
  }
}
