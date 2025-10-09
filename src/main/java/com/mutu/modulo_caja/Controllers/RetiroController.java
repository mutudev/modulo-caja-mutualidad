package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Models.ModelAhorro;
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
import org.springframework.boot.Banner;
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
public class RetiroController implements Initializable {

  @FXML private Button btnCancelar, btnProcesar;

  @FXML private TableView tableRetiros;

  @FXML private TableColumn<Object[], String> colSocio, colNombre, colMonto;
  @FXML private TextField txtAhorroAnt, txtAhorroDesp, txtIdentificador, txtEstado;

  public int socio;
  public String usuario;
  public String turno;
  public String empresa;

  @Autowired public Servicio servicio;

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
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[6])));
    colMonto.setCellValueFactory(
        cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[3])));
  }

  public void cierreDeVentana(Event event) {
    event.consume();
    //    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    //    alert.setTitle("CIERRE DE VENTANA");
    //    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
    //    alert.setContentText(
    //        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
    //            + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");
    //
    //    Optional<ButtonType> result = alert.showAndWait();
    //    if (result.isPresent() && result.get() == ButtonType.OK) {
    //
    //    }
    Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
    ventanaActual.close();
  }

  public void setDatos(int socio, String usuario, String turno) {
    this.socio = socio;
    this.usuario = usuario;
    this.turno = turno;
    traerRetiro(socio);
  }

  public void traerRetiro(int socio) {
    List<Object[]> resultados = servicio.retirosPendientes(socio);

    ObservableList<Object[]> retiros = FXCollections.observableArrayList();

    for (Object[] resultado : resultados) {
      retiros.add(resultado);
    }

    tableRetiros.setItems(retiros);

    if (!retiros.isEmpty()) {
      Object[] primeraFila = retiros.get(0);
      txtAhorroAnt.setText(String.valueOf(primeraFila[1]));
      txtAhorroDesp.setText(String.valueOf(primeraFila[2]));
      txtEstado.setText(String.valueOf(primeraFila[5]));
      txtIdentificador.setText(String.valueOf(primeraFila[4]));
      empresa = primeraFila[7].toString();
    }
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ESCAPE)) {
      //      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      //      alert.setTitle("CIERRE DE VENTANA");
      //      alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
      //      alert.setContentText(
      //          "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
      //              + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");
      //
      //      Optional<ButtonType> result = alert.showAndWait();
      //      if (result.isPresent() && result.get() == ButtonType.OK) {
      //
      //      }
      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();
    }
    if (event.getCode().equals(KeyCode.ENTER)) {
      procesarRetiro();
    }
  }

  @FXML
  public void cerrarConBoton() {
    //    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    //    alert.setTitle("CIERRE DE VENTANA");
    //    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
    //    alert.setContentText(
    //        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
    //            + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");
    //
    //    Optional<ButtonType> result = alert.showAndWait();
    //    if (result.isPresent() && result.get() == ButtonType.OK) {
    //
    //    }
    Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
    ventanaActual.close();
  }

  @FXML
  public void procesarRetiro() {

    if (!tableRetiros.getItems().isEmpty() && tableRetiros.getItems().size() == 1) {
      Object[] primeraFila = (Object[]) tableRetiros.getItems().get(0);

      String montoString = String.valueOf(primeraFila[3]);

      montoString = montoString.replaceAll("[\\$,]", "");
      LocalDateTime fecha = LocalDateTime.now();
      LocalTime hora = fecha.toLocalTime();
      DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
      String fechaTicket = fecha.format(formatter);

      double monto = Double.parseDouble(montoString);

      int id = Integer.parseInt(txtIdentificador.getText().trim());
      Map<String, Object> result =
          servicio.ProcesarRetiro(
              id,
              socio,
              LoginController.usuarioLoggeado,
              monto,
              empresa,
              fechaTicket,
              turno,
              0,
              "");

      if (result.get("Resultado").toString().equals("CORRECTO")) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("RETIRO PROCESADO CON ÉXITO");
        alert.setHeaderText("RETIRO PROCESADO CON ÉXITO");
        alert.setContentText(
            "RETIRO DEL SOCIO: " + socio + " POR: " + montoString + " HECHO CON EXITO");
        alert.showAndWait();

        try {
          InputStream isLogo = null;
          ModelAhorro ahororoactual = servicio.traerCuentaAhorroPorSocio(socio);
          String horaFormateada = hora.format(formatterHora);
          String nombreEmpresa = servicio.traerEmpresa(empresa).getRazonSocial();
          String rfcEmpresa = servicio.traerEmpresa(empresa).getRfc();
          String direcEmpresa =
              servicio.traerEmpresa(empresa).getCalle()
                  + " "
                  + servicio.traerEmpresa(empresa).getCruzamiento()
                  + " COL. CENTRO";
          String numcuenta = ahororoactual.getNum_cuenta();
          String nomsocio = String.valueOf(primeraFila[6]);
          double montores = ahororoactual.getSaldo();
          String montoanterior = String.valueOf(primeraFila[1]);
          String folio = result.get("transaccion_id").toString();

          NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
          String montoactual = String.valueOf(primeraFila[2]);
          String montoretirado = String.valueOf(primeraFila[3]);
          MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
          String moneyAsWords = converter.asWords(BigDecimal.valueOf(monto)).toUpperCase() + " MXN";
          if (empresa.equals("0001")) {
            isLogo = getClass().getResourceAsStream("/assets/images/logo-mut.png");
          } else {
            isLogo = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
          }

          Map pars = new HashMap<>();
          pars.put("Empresa", nombreEmpresa);
          pars.put("Logo", isLogo);
          pars.put("Rfc", rfcEmpresa);
          pars.put("Direccion", direcEmpresa);
          pars.put("Titulo", "REPORTE DE RETIRO DE AHORROS");
          pars.put("Montoanterior", montoanterior);
          pars.put("Fecha", fechaTicket);
          pars.put("Id", folio);
          pars.put("Numsocio", String.valueOf(socio));
          pars.put("Nombresocio", nomsocio);
          pars.put("Numcuenta", numcuenta);
          pars.put("Montoretirado", montoretirado);
          pars.put("Montorestante", montoactual);
          pars.put("Montoletras", moneyAsWords);
          pars.put("Cajero", LoginController.usuarioLoggeado);
          pars.put("Hora", horaFormateada);
          if (empresa.equals("0001")) {
            pars.put(
                "Descripcion",
                "Recibí de la "
                    + nombreEmpresa
                    + " la cantidad de "
                    + montoretirado
                    + " ("
                    + moneyAsWords
                    + ") por concepto de RETIRO DE CUENTA DE AHORRO.");
          } else {
            pars.put(
                "Descripcion",
                "Recibí de "
                    + nombreEmpresa
                    + " la cantidad de "
                    + montoretirado
                    + " ("
                    + moneyAsWords
                    + ") por concepto de RETIRO DE CUENTA DE AHORRO.");
          }

          InputStream isRepo = getClass().getResourceAsStream("/Reports/retiro.jasper");
          JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
          JasperPrint jpRepo = JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

          JasperViewer viewer = new JasperViewer(jpRepo, false);

          viewer.setAlwaysOnTop(true);
          viewer.setSize(800, 600);
          viewer.setLocationRelativeTo(null);
          viewer.setTitle("REPORTE DE RETIRO");
          viewer.setVisible(true);

        } catch (Exception e) {
          e.printStackTrace();
        }

        Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
        ventanaActual.close();
      } else {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("ERROR AL QUERER PROCESAR EL RETIRAR");
        alert.setHeaderText("ERROR EN EL RETIRO");
        alert.setContentText(result.get("Resultado").toString().toUpperCase());
        alert.showAndWait();
      }
    } else {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("NO TIENE RETIROS PENDIENTES");
      alert.setHeaderText("NO TIENE RETIROS PENDIENTES");
      alert.setContentText("NO TIENE RETIROS PENDIENTES");
      alert.showAndWait();
    }
  }
}
