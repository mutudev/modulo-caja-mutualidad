package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Models.ModelAhorro;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class RetiroController implements Initializable {

  private static final DateTimeFormatter FORMATTER_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final DateTimeFormatter FORMATTER_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  @FXML private Button btnCancelar, btnProcesar;
  @FXML private TableView tableRetiros;
  @FXML private TableColumn<Object[], String> colSocio, colNombre, colMonto;
  @FXML private TextField txtAhorroAnt, txtAhorroDesp, txtIdentificador, txtEstado;

  public int socio;
  public String usuario, turno, empresa;

  @Autowired public Servicio servicio;

  // ─── Inicialización ───────────────────────────────────────────────────────

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Platform.runLater(() -> {
      Stage stage = (Stage) btnCancelar.getScene().getWindow();
      stage.setOnCloseRequest(this::cierreDeVentana);
    });

    colSocio.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[0])));
    colNombre.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[6])));
    colMonto.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue()[3])));
  }

  // ─── Cierre de ventana ────────────────────────────────────────────────────

  public void cierreDeVentana(Event event) {
    event.consume();
    cerrarVentana();
  }

  @FXML
  public void cerrarConBoton() {
    cerrarVentana();
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) cerrarVentana();
    if (event.getCode() == KeyCode.ENTER)  procesarRetiro();
  }

  private void cerrarVentana() {
    ((Stage) btnCancelar.getScene().getWindow()).close();
  }

  // ─── Datos ────────────────────────────────────────────────────────────────

  public void setDatos(int socio, String usuario, String turno) {
    this.socio   = socio;
    this.usuario = usuario;
    this.turno   = turno;
    traerRetiro(socio);
  }

  public void traerRetiro(int socio) {
    List<Object[]> resultados = servicio.retirosPendientes(socio);
    ObservableList<Object[]> retiros = FXCollections.observableArrayList(resultados);
    tableRetiros.setItems(retiros);

    if (!retiros.isEmpty()) {
      Object[] primera = retiros.get(0);
      txtAhorroAnt.setText(String.valueOf(primera[1]));
      txtAhorroDesp.setText(String.valueOf(primera[2]));
      txtEstado.setText(String.valueOf(primera[5]));
      txtIdentificador.setText(String.valueOf(primera[4]));
      empresa = primera[7].toString();
    }
  }

  // ─── Procesamiento ────────────────────────────────────────────────────────

  @FXML
  public void procesarRetiro() {
    if (tableRetiros.getItems().isEmpty() || tableRetiros.getItems().size() != 1) {
      mostrarError("NO TIENE RETIROS PENDIENTES", "NO TIENE RETIROS PENDIENTES", "NO TIENE RETIROS PENDIENTES");
      return;
    }

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("RETIRO");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA PROCESAR EL RETIRO DEL SOCIO: " + socio  + "?");
    alert.setContentText(
            "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR");

    Optional<ButtonType> resultAlert = alert.showAndWait();
    if (resultAlert.isPresent() && resultAlert.get() != ButtonType.OK) {
      return;
    }

    Object[] fila      = (Object[]) tableRetiros.getItems().get(0);
    double   monto     = parseMonto(String.valueOf(fila[3]));
    int      id        = Integer.parseInt(txtIdentificador.getText().trim());
    LocalDateTime fecha = LocalDateTime.now();
    String   fechaTicket = fecha.format(FORMATTER_FECHA);

    Map<String, Object> result = servicio.ProcesarRetiro(
            id, socio, LoginController.usuarioLoggeado,
            monto, empresa, fechaTicket, turno, 0, "");

    if ("CORRECTO".equals(result.get("Resultado").toString())) {
      mostrarInfo("RETIRO PROCESADO CON ÉXITO", "RETIRO PROCESADO CON ÉXITO",
              "RETIRO DEL SOCIO: " + socio + " POR: " + monto + " HECHO CON EXITO");

      Stage loadingStage = crearLoadingStage();
      generarReporteAsync(fila, monto, fecha.toLocalTime(), fechaTicket, result, loadingStage);
      cerrarVentana();
    } else {
      mostrarError("ERROR AL QUERER PROCESAR EL RETIRAR", "ERROR EN EL RETIRO",
              result.get("Resultado").toString().toUpperCase());
    }
  }

  // ─── Helpers ──────────────────────────────────────────────────────────────

  private double parseMonto(String raw) {
    return Double.parseDouble(raw.replaceAll("[\\$,]", ""));
  }

  private Stage crearLoadingStage() {
    ProgressIndicator pi = new ProgressIndicator();
    pi.setPrefSize(60, 60);

    Label label = new Label("Generando Retiro...");
    label.setFont(Font.font("System", FontWeight.BOLD, 16));
    label.setTextFill(Color.web("#39577c"));

    VBox pane = new VBox(20, pi, label);
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

  private void generarReporteAsync(Object[] fila, double monto, LocalTime hora,
                                   String fechaTicket, Map<String, Object> result,
                                   Stage loadingStage) {
    try {
      ModelAhorro ahorro       = servicio.traerCuentaAhorroPorSocio(socio);
      var         emp          = servicio.traerEmpresa(empresa);
      String nombreEmpresa     = emp.getRazonSocial();
      String rfcEmpresa        = emp.getRfc();
      String direcEmpresa      = emp.getCalle() + " " + emp.getCruzamiento() + " COL. CENTRO";
      String moneyAsWords      = MoneyConverters.SPANISH_BANKING_MONEY_VALUE
              .asWords(BigDecimal.valueOf(monto)).toUpperCase() + " MXN";
      String logoPath          = empresa.equals("0001")
              ? "/assets/images/logo-mut.png"
              : "/assets/images/logo-ngu.jpg";
      String descripcion       = buildDescripcion(empresa, nombreEmpresa,
              String.valueOf(fila[3]), moneyAsWords);

      Map<String, Object> pars = new HashMap<>();
      pars.put("Empresa",        nombreEmpresa);
      pars.put("Logo",           getClass().getResourceAsStream(logoPath));
      pars.put("Rfc",            rfcEmpresa);
      pars.put("Direccion",      direcEmpresa);
      pars.put("Titulo",         "REPORTE DE RETIRO DE AHORROS");
      pars.put("Montoanterior",  String.valueOf(fila[1]));
      pars.put("Fecha",          fechaTicket);
      pars.put("Id",             result.get("transaccion_id").toString());
      pars.put("Numsocio",       String.valueOf(socio));
      pars.put("Nombresocio",    String.valueOf(fila[6]));
      pars.put("Numcuenta",      ahorro.getNum_cuenta());
      pars.put("forma",          "Efectivo");
      pars.put("Montoretirado",  String.valueOf(fila[3]));
      pars.put("Montorestante",  String.valueOf(fila[2]));
      pars.put("Montoletras",    moneyAsWords);
      pars.put("Cajero",         LoginController.usuarioLoggeado);
      pars.put("Hora",           hora.format(FORMATTER_HORA));
      pars.put("Descripcion",    descripcion);

      Task<Void> task = new Task<>() {
        @Override
        protected Void call() {
          try {
            InputStream   isRepo = getClass().getResourceAsStream("/Reports/retiro.jasper");
            JasperReport  jr     = (JasperReport) JRLoader.loadObject(isRepo);
            JasperPrint   jp     = JasperFillManager.fillReport(jr, pars, new JREmptyDataSource());

            Platform.runLater(() -> {
              JasperViewer viewer = new JasperViewer(jp, false);
              viewer.setAlwaysOnTop(true);
              viewer.setSize(800, 600);
              viewer.setLocationRelativeTo(null);
              viewer.setTitle("REPORTE DE RETIRO");
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
      task.setOnFailed(e -> loadingStage.close());

      loadingStage.show();
      new Thread(task).start();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private String buildDescripcion(String empresa, String nombreEmpresa,
                                  String monto, String moneyAsWords) {
    String prefijo = empresa.equals("0001") ? "la " : "";
    return "Recibí de " + prefijo + nombreEmpresa
            + " la cantidad de " + monto
            + " (" + moneyAsWords + ") por concepto de RETIRO DE CUENTA DE AHORRO.";
  }

  private void mostrarInfo(String titulo, String header, String contenido) {
    Alert a = new Alert(Alert.AlertType.INFORMATION);
    a.setTitle(titulo); a.setHeaderText(header); a.setContentText(contenido);
    a.showAndWait();
  }

  private void mostrarError(String titulo, String header, String contenido) {
    Alert a = new Alert(Alert.AlertType.ERROR);
    a.setTitle(titulo); a.setHeaderText(header); a.setContentText(contenido);
    a.showAndWait();
  }
}