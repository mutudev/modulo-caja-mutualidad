package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Services.Servicio;
import com.tenpisoft.n2w.MoneyConverters;
import javafx.application.Platform;
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
import net.synedra.validatorfx.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class TrasladoController implements Initializable {

  private static final DateTimeFormatter FORMATTER_HORA  = DateTimeFormatter.ofPattern("HH:mm:ss");
  private static final DateTimeFormatter FORMATTER_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  // Nombres de protesorero por empresa
  private static final Map<String, String> PROTESOREROS = Map.of(
          "0001", "CONT.PRIV. MARIA CARMINIA MONTERO QUINTAL",
          "0002", "CASIMIRO UITZ VILLANUEVA"
  );

  @FXML private ComboBox<String> cmbTipoTras;
  @FXML private TextField txtCantidad, txtEmpresa;
  @FXML private Button btnProcesar, btnCancelar;
  @FXML private Label lblError;

  public String empresa, usuario, turno;
  public CajeroController cajeroController;

  @Autowired public Servicio servicio;

  public Validator validator = new Validator();

  // ─── Inicialización ───────────────────────────────────────────────────────

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    if (cmbTipoTras != null) {
      ObservableList<String> opciones = FXCollections.observableArrayList("BOVEDA - CAJA");
      if (LoginController.rolusuarioLoggeado != 1) opciones.add("CAJA - BOVEDA");
      cmbTipoTras.setItems(opciones);
      cmbTipoTras.getSelectionModel().selectFirst();
    }

    validator.createCheck()
            .dependsOn("input", txtCantidad.textProperty())
            .withMethod(c -> {
              String texto = c.get("input");
              if (texto == null || texto.isBlank() || Double.parseDouble(texto) == 0) {
                c.error("Ingrese un valor mayor a cero");
                lblError.setText("Ingrese un valor mayor a cero");
              } else {
                lblError.setText("");
              }
            })
            .decorates(txtCantidad)
            .immediate();

    txtCantidad.setTextFormatter(new TextFormatter<>(change ->
            change.getControlNewText().matches("\\d*(\\.\\d*)?") ? change : null));

    Platform.runLater(() -> {
      Stage stage = (Stage) btnCancelar.getScene().getWindow();
      stage.setOnCloseRequest(this::cierreDeVentana);
    });
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
    if (event.getCode() == KeyCode.ENTER) { event.consume(); trasladar(); }
  }

  private void cerrarVentana() {
    validator = new Validator();
    ((Stage) btnCancelar.getScene().getWindow()).close();
  }

  // ─── Datos ────────────────────────────────────────────────────────────────

  public void setEmpresa(String empresa, String usuario, String turno, CajeroController controller) {
    this.cajeroController = controller;
    this.turno   = turno;
    this.usuario = usuario;
    this.empresa = empresa;
    txtEmpresa.setText(empresa);
  }

  // ─── Procesamiento ────────────────────────────────────────────────────────

  @FXML
  public void trasladar() {
    if (!validator.validate()) {
      mostrarError("ERROR AL PROCESAR EL TRASLADO",
              "ERROR AL INTENTAR LA OPERACIÓN DESEADA",
              "POR FAVOR, CUMPLA LAS VALIDACIONES DEL CAMPO.");
      return;
    }

    String nom_empresa = txtEmpresa.getText().trim();
    String cod_empresa = servicio.traerEmpresaConRS(nom_empresa).getCodigo();
    String cod_boveda  = resolverCodBoveda(nom_empresa);

    String tipoTraslado = cmbTipoTras.getSelectionModel().getSelectedItem();
    int opcion = tipoTraslado.equals("BOVEDA - CAJA") ? 6 : 7;

    double        monto_trasladar = Double.parseDouble(txtCantidad.getText().trim());
    LocalDateTime fecha           = LocalDateTime.now();
    LocalTime     hora            = fecha.toLocalTime();
    String        fechaTicket     = fecha.format(FORMATTER_FECHA);
    String        horaticket      = hora.format(FORMATTER_HORA);

    // Verificación de ajuste de sobrantes/faltantes para CAJA→BÓVEDA
    if (opcion == 7 && !confirmarAjusteSobrantes(fecha, cod_empresa)) return;

    // Primera llamada al servicio
    Map<String, Object> res = servicio.procesarTraslado(
            LoginController.usuarioLoggeado, monto_trasladar, opcion,
            cod_empresa, horaticket, turno, 0, "", 0);

    // Si el folio es "0", pedir confirmación y reintentar
    if ("0".equals(res.get("transaccion_id").toString())) {
      if (!confirmarOperacion(res.get("Resultado").toString())) return;
      res = servicio.procesarTraslado(
              LoginController.usuarioLoggeado, monto_trasladar, opcion,
              cod_empresa, horaticket, turno, 1, "", 0);
    }

    // Datos comunes del reporte
    String      nomcajero    = servicio.traerCajeroPorUsuario(LoginController.usuarioLoggeado);
    String      nombreEmpresa = servicio.traerEmpresa(cod_empresa).getRazonSocial();
    String      rfcEmpresa    = servicio.traerEmpresa(cod_empresa).getRfc();
    String      direcEmpresa  = buildDireccion(cod_empresa);
    String      folio         = res.get("transaccion_id").toString();
    String      montoStr      = NumberFormat.getCurrencyInstance(Locale.US).format(monto_trasladar);
    String      moneyAsWords  = MoneyConverters.SPANISH_BANKING_MONEY_VALUE
            .asWords(BigDecimal.valueOf(monto_trasladar)).toUpperCase() + " MXN";
    String      horaFormateada = hora.format(FORMATTER_HORA);
    InputStream isLogo        = logoStream(cod_empresa);
    String      desc          = cod_empresa.equals("0001")
            ? "NOMBRE Y FIRMA DE LA PROTESORERA"
            : "NOMBRE Y FIRMA DEL PROTESORERO";
    String      protesonom    = PROTESOREROS.get(cod_empresa);

    Stage loadingStage = crearLoadingStage();

    boolean procesar = true;
    boolean permitir = false;

    switch (res.get("Resultado").toString()) {
      case "APERTURA" -> {
        mostrarInfo("APERTURA EXITOSA", "APERTURA REALIZADA CORRECTAMENTE",
                "EL CAJERO: " + LoginController.usuarioLoggeado
                        + " AHORA ESTÁ LISTO PARA REALIZAR SUS OPERACIONES EN: " + nom_empresa);

        generarReporteAsync("/Reports/traslado_apertura.jasper",
                construirParametrosReporte(nombreEmpresa, isLogo, rfcEmpresa, direcEmpresa,
                        "REPORTE DE TRASLADO DE APERTURA", folio, fechaTicket, turno,
                        cod_boveda, "CUENTA DE CAJERO " + LoginController.usuarioLoggeado,
                        montoStr, moneyAsWords, desc, LoginController.usuarioLoggeado,
                        nomcajero, horaFormateada, protesonom,
                        buildDescripcionTraslado("APERTURA", montoStr, moneyAsWords, nomcajero, protesonom)),
                loadingStage);
        permitir = true;
      }
      case "CIERRE" -> {
        mostrarInfo("TRASLADO DE CIERRE EXITOSO", "TRASLADO DE CIERRE REALIZADO CORRECTAMENTE",
                "EL CAJERO: " + LoginController.usuarioLoggeado
                        + " AHORA ESTÁ LISTO PARA CERRAR EN: " + nom_empresa);

        generarReporteAsync("/Reports/traslado_cierre.jasper",
                construirParametrosReporte(nombreEmpresa, isLogo, rfcEmpresa, direcEmpresa,
                        "REPORTE DE TRASLADO DE CIERRE", folio, fechaTicket, turno,
                        "CUENTA DE CAJERO " + LoginController.usuarioLoggeado, cod_boveda,
                        montoStr, moneyAsWords, desc, LoginController.usuarioLoggeado,
                        nomcajero, horaFormateada, protesonom,
                        buildDescripcionTraslado("CIERRE", montoStr, moneyAsWords, nomcajero, protesonom)),
                loadingStage);
      }
      default -> {
        mostrarError("ERROR AL PROCESAR EL TRASLADO DEL CAJERO",
                "ERROR AL INTENTAR LA OPERACIÓN DESEADA",
                res.get("Resultado").toString().toUpperCase());
        procesar = false;
      }
    }

    if (procesar) {
      cajeroController.apertura = permitir;
      ((Stage) txtCantidad.getScene().getWindow()).close();
    }
  }

  // ─── Helpers de lógica ────────────────────────────────────────────────────

  private boolean confirmarAjusteSobrantes(LocalDateTime fecha, String cod_empresa) {
    List<Object[]> permisos = servicio.traerCuentaCajero(
            LoginController.usuarioLoggeado, fecha.toString(), 1, turno, cod_empresa);
    for (Object[] fila : permisos) {
      int estado = Integer.parseInt(fila[8].toString());
      if (estado == 1 || estado == 2) {
        Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION,
                "AÚN NO HA REALIZADO SU AJUSTE DE SOBRANTES Y FALTANTES.\n" +
                        "¿ESTÁ SEGURO DE QUE DESEA SEGUIR TRASLADANDO?",
                ButtonType.OK, ButtonType.CANCEL) {{
          setTitle("CONFIRMAR TRASLADO HACIA BÓVEDA");
          setHeaderText("AJUSTE DE SOBRANTES Y FALTANTES NO REALIZADO");
        }}.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return false;
      }
    }
    return true;
  }

  private boolean confirmarOperacion(String mensaje) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CONFIRMAR OPERACIÓN");
    alert.setHeaderText("¿DESEA CONTINUAR CON LA OPERACIÓN?");
    alert.setContentText(mensaje.toUpperCase());
    Optional<ButtonType> result = alert.showAndWait();
    return result.isPresent() && result.get() == ButtonType.OK;
  }


  private String resolverCodBoveda(String nom_empresa) {
    return switch (nom_empresa) {
      case "MUTUALIDAD 12 DE AGOSTO, S.C. DE R.L. DE C.V." -> "BVDA-MUT";
      case "NUEVA GENERACIÓN DE UMÁN, AC."                  -> "BVDA-NGU";
      default -> "";
    };
  }

  private String buildDireccion(String cod_empresa) {
    var emp = servicio.traerEmpresa(cod_empresa);
    return emp.getCalle() + " " + emp.getCruzamiento() + " COL. CENTRO";
  }

  private String buildDescripcionTraslado(String tipo, String monto,
                                          String moneyAsWords, String cajero, String protesonom) {
    return "TRASLADO DE " + tipo + " realizado por " + monto
            + " (" + moneyAsWords + ") efectuado en la sucursal de UMAN por "
            + cajero + " y autorizado por " + protesonom;
  }

  private InputStream logoStream(String cod_empresa) {
    String path = cod_empresa.equals("0001")
            ? "/assets/images/logo-mut.png"
            : "/assets/images/logo-ngu.jpg";
    return getClass().getResourceAsStream(path);
  }

  // ─── Reporte ──────────────────────────────────────────────────────────────

  private Map<String, Object> construirParametrosReporte(
          String nombreEmpresa, InputStream logo, String rfcEmpresa,
          String direccionEmpresa, String titulo, String folio,
          String fechaTicket, String turno, String origen, String destino,
          String monto, String montoLetras, String descProteso,
          String usuario, String nombreCajero, String hora,
          String protesonom, String descripcion) {

    Map<String, Object> pars = new HashMap<>();
    pars.put("Empresa",      nombreEmpresa);
    pars.put("Logoempresa",  logo);
    pars.put("Rfc",          rfcEmpresa);
    pars.put("Direccion",    direccionEmpresa);
    pars.put("Titulo",       titulo);
    pars.put("Id",           folio);
    pars.put("Fecha",        fechaTicket);
    pars.put("Turno",        turno);
    pars.put("Origen",       origen);
    pars.put("Destino",      destino);
    pars.put("Monto",        monto);
    pars.put("Montoletras",  montoLetras);
    pars.put("DescProteso",  descProteso);
    pars.put("Cajerouser",   usuario);
    pars.put("Cajeronom",    nombreCajero);
    pars.put("Hora",         hora);
    pars.put("Protesonom",   protesonom);
    pars.put("Descripcion",  descripcion);
    return pars;
  }

  private void generarReporteAsync(String rutaReporte, Map<String, Object> pars, Stage loadingStage) {
    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        try {
          InputStream  isRepo = getClass().getResourceAsStream(rutaReporte);
          JasperReport jr     = (JasperReport) JRLoader.loadObject(isRepo);
          JasperPrint  jp     = JasperFillManager.fillReport(jr, pars, new JREmptyDataSource());

          Platform.runLater(() -> {
            JasperViewer viewer = new JasperViewer(jp, false);
            viewer.setSize(800, 600);
            viewer.setAlwaysOnTop(true);
            viewer.setLocationRelativeTo(null);
            viewer.setTitle("REPORTE DE TRASLADO");
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
    task.setOnFailed(e    -> loadingStage.close());
    loadingStage.show();
    new Thread(task).start();
  }

  // ─── UI helpers ───────────────────────────────────────────────────────────

  private Stage crearLoadingStage() {
    ProgressIndicator pi = new ProgressIndicator();
    pi.setPrefSize(60, 60);

    Label label = new Label("Generando Traslado...");
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