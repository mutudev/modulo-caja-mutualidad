package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Models.ModelCredito;
import com.mutu.modulo_caja.Models.ModelEmpresa;
import com.mutu.modulo_caja.Services.Servicio;
import com.tenpisoft.n2w.MoneyConverters;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.view.JasperViewer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
  @FXML private TableView<Object[]> tableDesembolsos;
  @FXML private TableColumn<Object[], String> colSocio, colNombre, colMonto, colEstado, colCredito;

  @Autowired public Servicio servicio;

  public int socio;
  public String usuario;

  private static final DateTimeFormatter FORMATTER_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final DateTimeFormatter FORMATTER_HORA  = DateTimeFormatter.ofPattern("HH:mm:ss");

  // ─── Initializable ────────────────────────────────────────────────────────

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Platform.runLater(() -> {
      Stage stage = (Stage) btnCancelar.getScene().getWindow();
      stage.setOnCloseRequest(this::cierreDeVentana);
    });

    colSocio.setCellValueFactory(  c -> new SimpleStringProperty(String.valueOf(c.getValue()[0])));
    colNombre.setCellValueFactory( c -> new SimpleStringProperty(String.valueOf(c.getValue()[3])));
    colMonto.setCellValueFactory(  c -> new SimpleStringProperty(String.valueOf(c.getValue()[1])));
    colEstado.setCellValueFactory( c -> new SimpleStringProperty(String.valueOf(c.getValue()[2])));
    colCredito.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[4])));
  }

  // ─── Datos ────────────────────────────────────────────────────────────────

  public void setDatos(int socio, String usuario) {
    this.socio   = socio;
    this.usuario = usuario;
    desembolsosPendientes(socio);
  }

  public void desembolsosPendientes(int socio) {
    List<Object[]> resultados = servicio.desembolsosPendientes(socio);
    ObservableList<Object[]> desembolsos = FXCollections.observableArrayList(resultados);
    tableDesembolsos.setItems(desembolsos);
  }

  // ─── Cierre de ventana ────────────────────────────────────────────────────

  public void cierreDeVentana(Event event) {
    event.consume();
    cerrarVentanaActual();
  }

  @FXML
  public void cerrarConBoton() {
    cerrarVentanaActual();
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) cerrarVentanaActual();
    if (event.getCode() == KeyCode.ENTER)  procesarDesembolso();
  }

  private void cerrarVentanaActual() {
    ((Stage) btnCancelar.getScene().getWindow()).close();
  }

  // ─── Desembolso ───────────────────────────────────────────────────────────

  @FXML
  public void procesarDesembolso() {
    if (tableDesembolsos.getItems().isEmpty() || tableDesembolsos.getItems().size() != 1) {
      mostrarError("NO HAY DESEMBOLSO PENDIENTE", "NO HAY DESEMBOLSO PENDIENTE", "NO HAY DESEMBOLSO PENDIENTE");
      return;
    }



    if (tableDesembolsos.getSelectionModel().getSelectedItem() == null) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR AL INTENTAR DESEMBOLSAR");
      alert.setHeaderText("ERROR");
      alert.setContentText("POR FAVOR, SELECCIONE UN DESEMBOLSO");
      alert.showAndWait();
      return;
    }

    int indice = tableDesembolsos.getSelectionModel().getSelectedIndex();

    Object[] fila      = tableDesembolsos.getItems().get(indice);
    String creditoId   = String.valueOf(fila[4]);
    String socioStr    = String.valueOf(fila[0]);
    String nomSocio    = String.valueOf(fila[3]);
    double monto       = parseMonto(String.valueOf(fila[1]));

    LocalDate fecha     = servicio.traerFechaHoy();
    String fechaTicket      = fecha.format(FORMATTER_FECHA);
    String fechaVencTicket  = fecha.plusYears(1).format(FORMATTER_FECHA);
    String horaTicket       = LocalTime.now().format(FORMATTER_HORA);


    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("DESEMBOLSO");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA DESEMBOLSAR EL CRÉDITO DEL SOCIO: " + socioStr + "?");
    alert.setContentText(
            "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

    Optional<ButtonType> resultAlert = alert.showAndWait();
    if (resultAlert.isPresent() && resultAlert.get() != ButtonType.OK) {
      return;
    }


    Map<String, Object> result = servicio.ProcesarDesembolso(
            Integer.parseInt(creditoId), usuario, monto, horaTicket, "", 0);

    if (!"CORRECTO".equals(result.get("Resultado").toString())) {
      mostrarError("ERROR AL QUERER PROCESAR EL DESEMBOLSO", "ERROR EN EL DESEMBOLSO",
              result.get("Resultado").toString().toUpperCase());
      return;
    }

    mostrarInfo("DESEMBOLSO PROCESADO CON ÉXITO", "DESEMBOLSO PROCESADO CON ÉXITO",
            "DESEMBOLSO DEL SOCIO: " + socioStr + " POR: " + monto + " HECHO CON EXITO");

    String folio         = result.get("transaccion_id").toString();
    String codigoEmpresa = servicio.traerEmpresa("0002").getCodigo();

    try {
      Map<String, Object> pars = construirParametros(
              codigoEmpresa, creditoId, socioStr, nomSocio,
              monto, folio, fechaTicket, fechaVencTicket, horaTicket);

      mostrarReporteConCarga(pars);
    } catch (Exception e) {
      e.printStackTrace();
    }

    cerrarVentanaActual();
  }

  // ─── Helpers privados ─────────────────────────────────────────────────────

  private double parseMonto(String raw) {
    return Double.parseDouble(raw.replaceAll("[\\$,]", ""));
  }

  private Map<String, Object> construirParametros(
          String codigoEmpresa, String creditoId, String socioStr, String nomSocio,
          double monto, String folio, String fechaTicket, String fechaVencTicket, String horaTicket) {

    ModelEmpresa empresa     = servicio.traerEmpresa(codigoEmpresa);
    String nomEmp   = empresa.getRazonSocial();
    String rfcEmp   = empresa.getRfc();
    String dirEmp   = empresa.getCalle() + " " + empresa.getCruzamiento() + " COL. CENTRO";

    ModelCredito credito = servicio.traerDatosCredito(Integer.parseInt(creditoId));
    String asesor        = credito.getAsesor();
    String intOrd        = credito.getTasa() + " %";
    String intMora       = credito.getMora() + " %";

    NumberFormat fmt     = NumberFormat.getCurrencyInstance(Locale.US);
    String montoFmt      = fmt.format(monto);
    String montoLetras   = MoneyConverters.SPANISH_BANKING_MONEY_VALUE
            .asWords(BigDecimal.valueOf(monto)).toUpperCase() + " MXN";

    InputStream logo = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");

    String descripcion =
            "Recibí de " + nomEmp + " la cantidad de " + montoFmt +
                    " (" + montoLetras + ") recibido en efectivo a mi entera satisfacción. " +
                    "Así mismo, manifiesto conocer y apegarme al cumplimiento del acuerdo 2 de la Asamblea General " +
                    "efectuada el 22 de Julio de 2011, el cual menciona que todo socio que realice un crédito por sus " +
                    "ahorros o menos y que en seis meses consecutivos no realice abono alguno a su crédito, será dado " +
                    "de baja con el fin de evitar el incremento de su deuda y la cartera vencida.";

    Map<String, Object> pars = new HashMap<>();
    pars.put("Empresa",      nomEmp);
    pars.put("Logo",         logo);
    pars.put("LogoImg",      logo);
    pars.put("RFC",          rfcEmp);
    pars.put("Direccion",    dirEmp);
    pars.put("Numcredito",   creditoId);
    pars.put("Titulo",       "REPORTE DE DESEMBOLSO DE CRÉDITO");
    pars.put("Fecha",        fechaTicket);
    pars.put("Folio",        folio);
    pars.put("NumSocio",     socioStr);
    pars.put("NombreSocio",  nomSocio);
    pars.put("ASESOR",       asesor);
    pars.put("IntOrd",       intOrd);
    pars.put("IntMora",      intMora);
    pars.put("Vencimiento",  fechaVencTicket);
    pars.put("Monto",        montoFmt);
    pars.put("MontoLetras",  montoLetras);
    pars.put("Cajero",       LoginController.usuarioLoggeado);
    pars.put("Hora",         horaTicket);
    pars.put("Descripcion",  descripcion);
    return pars;
  }

  private void mostrarReporteConCarga(Map<String, Object> pars) {
    Stage loadingStage = crearVentanaCarga();

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        try {
          InputStream isRepo = getClass().getResourceAsStream("/Reports/desembolso.jasper");
          JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
          JasperPrint  jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

          Platform.runLater(() -> {
            JasperViewer viewer = new JasperViewer(jpRepo, false);
            viewer.setAlwaysOnTop(true);
            viewer.setSize(800, 600);
            viewer.setLocationRelativeTo(null);
            viewer.setTitle("REPORTE DE DESEMBOLSO");
            viewer.setVisible(true);
          });
        } catch (Exception e) {
          e.printStackTrace();
          Platform.runLater(() -> mostrarError("ERROR", "ERROR AL GENERAR EL REPORTE",
                  "OCURRIÓ UN ERROR: " + e.getMessage()));
        }
        return null;
      }
    };

    task.setOnSucceeded(e -> loadingStage.close());
    task.setOnFailed(   e -> loadingStage.close());

    loadingStage.show();
    new Thread(task).start();
  }

  private Stage crearVentanaCarga() {
    ProgressIndicator spinner = new ProgressIndicator();
    spinner.setPrefSize(60, 60);

    Label label = new Label("Generando Desembolso...");
    label.setFont(Font.font("System", FontWeight.BOLD, 16));
    label.setTextFill(Color.web("#39577c"));

    VBox pane = new VBox(20, spinner, label);
    pane.setAlignment(Pos.CENTER);
    pane.setPadding(new Insets(30));
    pane.setStyle("-fx-background-color: white; -fx-border-color: #185754; -fx-border-width: 2;");

    Stage stage = new Stage();
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.initStyle(StageStyle.UNDECORATED);
    stage.setAlwaysOnTop(true);
    stage.setScene(new Scene(pane, 300, 150));
    stage.centerOnScreen();
    return stage;
  }

  private void mostrarError(String titulo, String header, String contenido) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(titulo);
    alert.setHeaderText(header);
    alert.setContentText(contenido);
    alert.showAndWait();
  }

  private void mostrarInfo(String titulo, String header, String contenido) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(titulo);
    alert.setHeaderText(header);
    alert.setContentText(contenido);
    alert.showAndWait();
  }
}