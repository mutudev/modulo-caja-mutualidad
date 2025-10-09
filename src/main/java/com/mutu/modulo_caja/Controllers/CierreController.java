package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Main;
import com.mutu.modulo_caja.Models.ModelTraslado;
import com.mutu.modulo_caja.Services.Servicio;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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

import java.io.InputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class CierreController implements Initializable {

  @FXML private TextField txtSaldoMut, txtSaldoNgu;
  @FXML private Button btnCerrar, btnCancelar;
  private Label lblParaCerrarLaPrincipal;

  @FXML private Label lblListo;

  @Autowired public Servicio servicio;

  public int cuentCajaMutu = 0;
  public int cuentaCajaNgu = 0;
  double salfisicoMUT = 0;
  double salfisicoNGU = 0;
  String turno = "";
  List<Object[]> cuentaMUT1;
  List<Object[]> cuentaNGU1;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Platform.runLater(
        () -> {
          Stage stage = (Stage) btnCancelar.getScene().getWindow();
          stage.setOnCloseRequest(event -> cierreDeVentana(event));
        });
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

  public void setDatos(
      List<Object[]> cuentaMUT, List<Object[]> cuentaNGU, Label lbl, String turno) {
    this.lblParaCerrarLaPrincipal = lbl;
    this.turno = turno;
    this.cuentaMUT1 = cuentaMUT;
    this.cuentaNGU1 = cuentaNGU;

    NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);

    if (cuentaMUT.isEmpty() && cuentaNGU.isEmpty()) {
      txtSaldoNgu.setDisable(true);
      txtSaldoMut.setDisable(true);
      lblListo.setText("EL CAJERO NO CUENTA CON CUENTAS DE CAJA PARA CERRAR");
      btnCerrar.setDisable(true);
    } else if (cuentaMUT.size() == 1 && cuentaNGU.size() == 1) {
      ModelTraslado trasladoMut = servicio.traerTrasladoCajero(cuentaMUT.get(0)[0].toString());
      ModelTraslado trasladoNgu = servicio.traerTrasladoCajero(cuentaNGU.get(0)[0].toString());

      double montoEnCuentaMut =
          Double.parseDouble(cuentaMUT.get(0)[10].toString()) - trasladoMut.getMonto();
      double montoEnCuentaNgu =
          Double.parseDouble(cuentaNGU.get(0)[10].toString()) - trasladoNgu.getMonto();
      if (montoEnCuentaMut != 0 || montoEnCuentaNgu != 0) {
        lblListo.setText("EL CAJERO AÚN NO HA TRASALADO TODO SU SALDO EN SUS CUENTAS DE CAJA");
        btnCerrar.setDisable(true);
      } else {
        lblListo.setText("EL CAJERO SE ENCUENTRA LISTO PARA CERRAR SUS CUENTAS DE CAJA");
        cuentCajaMutu = Integer.parseInt(trasladoMut.getCuentaOrigen());
        cuentaCajaNgu = Integer.parseInt(trasladoNgu.getCuentaOrigen());
      }
      txtSaldoMut.setText(formatoMoneda.format(montoEnCuentaMut));
      salfisicoMUT = Double.parseDouble(cuentaMUT.get(0)[10].toString());
      salfisicoNGU = Double.parseDouble(cuentaNGU.get(0)[10].toString());
      txtSaldoNgu.setText(formatoMoneda.format(montoEnCuentaNgu));

    } else if (cuentaMUT.isEmpty() && cuentaNGU.size() == 1) {

      ModelTraslado trasladoNgu = servicio.traerTrasladoCajero(cuentaNGU.get(0)[0].toString());
      double montoEnCuentaNgu =
          Double.parseDouble(cuentaNGU.get(0)[10].toString()) - trasladoNgu.getMonto();
      if (montoEnCuentaNgu != 0) {
        lblListo.setText("EL CAJERO AÚN NO HA TRASALADO TODO SU SALDO EN SUS CUENTAS DE CAJA");
        btnCerrar.setDisable(true);
      } else {
        lblListo.setText("EL CAJERO SE ENCUENTRA LISTO PARA CERRAR SUS CUENTAS DE CAJA");
        cuentaCajaNgu = Integer.parseInt(trasladoNgu.getCuentaOrigen());
      }
      txtSaldoNgu.setText(formatoMoneda.format(montoEnCuentaNgu));
      salfisicoNGU = Double.parseDouble(cuentaNGU.get(0)[10].toString());
    } else {
      ModelTraslado trasladoMut = servicio.traerTrasladoCajero(cuentaMUT.get(0)[0].toString());

      double montoEnCuentaMut =
          Double.parseDouble(cuentaMUT.get(0)[10].toString()) - trasladoMut.getMonto();
      if (montoEnCuentaMut != 0) {
        lblListo.setText("EL CAJERO AÚN NO HA TRASALADO TODO SU SALDO EN SUS CUENTAS DE CAJA");
        btnCerrar.setDisable(true);
      } else {
        lblListo.setText("EL CAJERO SE ENCUENTRA LISTO PARA CERRAR SUS CUENTAS DE CAJA");
        cuentCajaMutu = Integer.parseInt(trasladoMut.getCuentaOrigen());
      }
      txtSaldoMut.setText(formatoMoneda.format(montoEnCuentaMut));
      salfisicoMUT = Double.parseDouble(cuentaMUT.get(0)[10].toString());
    }
  }

  @FXML
  public void cerrar() {
    if (lblListo.getText().equals("EL CAJERO SE ENCUENTRA LISTO PARA CERRAR SUS CUENTAS DE CAJA")) {

      LocalDate fecha = LocalDate.now();

      Map<String, Object> resMutu =
          servicio.pa_procesarCierre(cuentCajaMutu, 0, fecha.toString(), salfisicoMUT, "", 0);

      Map<String, Object> resNgu =
          servicio.pa_procesarCierre(0, cuentaCajaNgu, fecha.toString(), salfisicoNGU, "", 0);

      boolean valido =
          isValido(resMutu.get("Resultado").toString(), resNgu.get("Resultado").toString());

      if (valido) {
        try {
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("CIERRE HECHO CORRECTAMENTE");
          alert.setHeaderText("CIERRE EXITOSO");
          alert.setContentText("CIERRE EXITOSO DEL CAJERO: " + LoginController.usuarioLoggeado);
          alert.showAndWait();

          String mutNombre = "MUTUALIDAD DOCE DE AGOSTO S.C. DE R.L. DE C.V.";
          String nguNombre = "NUEVA GENERACIÓN DE UMÁN. AC.";

          Object[] datos =
              servicio.traerCierreCajero(Integer.parseInt(resMutu.get("Cierre_id").toString()));
          Object[] datosNGU =
              servicio.traerCierreCajero(Integer.parseInt(resNgu.get("Cierre_id").toString()));
          String folio = "";
          String fechad = "";
          String hora = "";
          String empresa = "";
          String rfcEmpresa = "";
          String direcEmpresa = "";
          InputStream isLogo = null;
          InputStream isLogo2 = null;

          ModelTraslado trasladoCMut =
              servicio.traerTrasladoCajero(cuentaMUT1.get(0)[0].toString());
          ModelTraslado trasladoCNgu =
              servicio.traerTrasladoCajero(cuentaNGU1.get(0)[0].toString());
          ModelTraslado trasladoAMut =
              servicio.traerTrasladoApertura(cuentaMUT1.get(0)[0].toString());
          ModelTraslado trasladoANgu =
              servicio.traerTrasladoApertura(cuentaNGU1.get(0)[0].toString());

          String nomcajero = "";
          String ahorros = "";
          double ahorrosCant = 0;
          String creditos = "";
          double creditosCant = 0;
          String retiros = "";
          double retirosCant = 0;
          String desembolsos = "";
          double desembolsosCant = 0;
          String capital = "";
          double csCant = 0;
          String presoc = "";
          double presocCant = 0;
          String apertura = "";
          double aperturaCant = 0;
          String cierre = "";
          double cierreCant = 0;
          String sobrante = "";
          double sobranteCant = 0;
          String faltante = "";
          double faltanteCant = 0;
          String total = "";
          double totalcant = 0;
          String saldoFisicoMut = "";
          double cantSaldoFisicoMut = 0;

          // NGU var
          String ahorrosNGU = "";
          double ahorrosCantNGU = 0;
          String creditosNGU = "";
          double creditosCantNGU = 0;
          String retirosNGU = "";
          double retirosCantNGU = 0;
          String desembolsosNGU = "";
          double desembolsosCantNGU = 0;
          String capitalNGU = "";
          double csCantNGU = 0;
          String presocNGU = "";
          double presocCantNGU = 0;
          String aperturaNGU = "";
          double aperturaCantNGU = 0;
          String cierreNGU = "";
          double cierreCantNGU = 0;
          String sobranteNGU = "";
          double sobranteCantNGU = 0;
          String faltanteNGU = "";
          double faltanteCantNGU = 0;
          String totalNGU = "";
          double totalcantNGU = 0;
          String saldoFisicoNgu = "";
          double cantSaldoFisicoNgu = 0;

          // Totales
          String ahorrosTOT = "";
          double ahorrosCantTOT = 0;
          String creditosTOT = "";
          double creditosCantTOT = 0;
          String retirosTOT = "";
          double retirosCantTOT = 0;
          String desembolsosTOT = "";
          double desembolsosCantTOT = 0;
          String capitalTOT = "";
          double csCantTOT = 0;
          String presocTOT = "";
          double presocCantTOT = 0;
          String aperturaTOT = "";
          double aperturaCantTOT = 0;
          String cierreTOT = "";
          double cierreCantTOT = 0;
          String sobranteTOT = "";
          double sobranteCantTOT = 0;
          String faltanteTOT = "";
          double faltanteCantTOT = 0;
          String totalTOT = "";
          double totalcantTOT = 0;
          String totSaldoFisicoAmbas = "";
          double cantTotSaldoFisicoAmbas = 0;

          NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);

          folio =
              "MUT"
                  + resMutu.get("Cierre_id").toString()
                  + "-"
                  + "NGU"
                  + resNgu.get("Cierre_id").toString();

          for (Object filaObj : datos) {
            if (filaObj instanceof Object[]) {
              Object[] fila = (Object[]) filaObj;

              hora = fila[14].toString();

              nomcajero = servicio.traerCajeroPorUsuario(LoginController.usuarioLoggeado);
              ahorrosCant = Double.parseDouble(String.valueOf(fila[2]));
              ahorros = formatoMoneda.format(ahorrosCant);

              creditosCant = Double.parseDouble(String.valueOf(fila[3]));
              creditos = formatoMoneda.format(creditosCant);

              retirosCant = Double.parseDouble(String.valueOf(fila[4]));
              retiros = formatoMoneda.format(retirosCant);

              desembolsosCant = Double.parseDouble(String.valueOf(fila[5]));
              desembolsos = formatoMoneda.format(desembolsosCant);

              csCant = Double.parseDouble(String.valueOf(fila[6]));
              capital = formatoMoneda.format(csCant);

              presocCant = Double.parseDouble(String.valueOf(fila[7]));
              presoc = formatoMoneda.format(presocCant);

              aperturaCant = Double.parseDouble(String.valueOf(fila[8]));
              apertura = formatoMoneda.format(aperturaCant);

              cierreCant = Double.parseDouble(String.valueOf(fila[9]));
              cierre = formatoMoneda.format(cierreCant);

              sobranteCant = Double.parseDouble(String.valueOf(fila[10]));
              sobrante = formatoMoneda.format(sobranteCant);

              faltanteCant = Double.parseDouble(String.valueOf(fila[11]));
              faltante = formatoMoneda.format(faltanteCant);

              cantSaldoFisicoMut = Double.parseDouble(String.valueOf(fila[12]));
              saldoFisicoMut = formatoMoneda.format(cantSaldoFisicoMut);

              totalcant =
                  creditosCant
                      + ahorrosCant
                      - retirosCant
                      - desembolsosCant
                      + csCant
                      + presocCant
                      + aperturaCant
                      + sobranteCant
                      - faltanteCant;
              total = formatoMoneda.format(totalcant);
            }
          }

          for (Object filaObj : datosNGU) {
            if (filaObj instanceof Object[]) {
              Object[] fila = (Object[]) filaObj;

              ahorrosCantNGU = Double.parseDouble(String.valueOf(fila[2]));
              ahorrosNGU = formatoMoneda.format(ahorrosCantNGU);

              creditosCantNGU = Double.parseDouble(String.valueOf(fila[3]));
              creditosNGU = formatoMoneda.format(creditosCantNGU);

              retirosCantNGU = Double.parseDouble(String.valueOf(fila[4]));
              retirosNGU = formatoMoneda.format(retirosCantNGU);

              desembolsosCantNGU = Double.parseDouble(String.valueOf(fila[5]));
              desembolsosNGU = formatoMoneda.format(desembolsosCantNGU);

              csCantNGU = Double.parseDouble(String.valueOf(fila[6]));
              capitalNGU = formatoMoneda.format(csCantNGU);

              presocCantNGU = Double.parseDouble(String.valueOf(fila[7]));
              presocNGU = formatoMoneda.format(presocCantNGU);

              aperturaCantNGU = Double.parseDouble(String.valueOf(fila[8]));
              aperturaNGU = formatoMoneda.format(aperturaCantNGU);

              cierreCantNGU = Double.parseDouble(String.valueOf(fila[9]));
              cierreNGU = formatoMoneda.format(cierreCantNGU);

              sobranteCantNGU = Double.parseDouble(String.valueOf(fila[10]));
              sobranteNGU = formatoMoneda.format(sobranteCantNGU);

              faltanteCantNGU = Double.parseDouble(String.valueOf(fila[11]));
              faltanteNGU = formatoMoneda.format(faltanteCantNGU);

              cantSaldoFisicoNgu = Double.parseDouble(String.valueOf(fila[12]));
              saldoFisicoNgu = formatoMoneda.format(cantSaldoFisicoNgu);

              totalcantNGU =
                  creditosCantNGU
                      + ahorrosCantNGU
                      - retirosCantNGU
                      - desembolsosCantNGU
                      + csCantNGU
                      + presocCantNGU
                      + aperturaCantNGU
                      + sobranteCantNGU
                      - faltanteCantNGU;
              totalNGU = formatoMoneda.format(totalcantNGU);
            }
          }

          ahorrosCantTOT = ahorrosCant + ahorrosCantNGU;
          ahorrosTOT = formatoMoneda.format(ahorrosCantTOT);

          creditosCantTOT = creditosCant + creditosCantNGU;
          creditosTOT = formatoMoneda.format(creditosCantTOT);

          retirosCantTOT = retirosCant + retirosCantNGU;
          retirosTOT = formatoMoneda.format(retirosCantTOT);

          desembolsosCantTOT = desembolsosCant + desembolsosCantNGU;
          desembolsosTOT = formatoMoneda.format(desembolsosCantTOT);

          csCantTOT = csCant + csCantNGU;
          capitalTOT = formatoMoneda.format(csCantTOT);

          presocCantTOT = presocCant + presocCantNGU;
          presocTOT = formatoMoneda.format(presocCantTOT);

          aperturaCantTOT = aperturaCant + aperturaCantNGU;
          aperturaTOT = formatoMoneda.format(aperturaCantTOT);

          cierreCantTOT = cierreCant + cierreCantNGU;
          cierreTOT = formatoMoneda.format(cierreCantTOT);

          sobranteCantTOT = sobranteCant + sobranteCantNGU;
          sobranteTOT = formatoMoneda.format(sobranteCantTOT);

          faltanteCantTOT = faltanteCant + faltanteCantNGU;
          faltanteTOT = formatoMoneda.format(faltanteCantTOT);

          cantTotSaldoFisicoAmbas = cantSaldoFisicoMut + cantSaldoFisicoNgu;
          totSaldoFisicoAmbas = formatoMoneda.format(cantTotSaldoFisicoAmbas);

          totalcantTOT =
              creditosCantTOT
                  + ahorrosCantTOT
                  - retirosCantTOT
                  - desembolsosCantTOT
                  + csCantTOT
                  + presocCantTOT
                  + aperturaCantTOT
                  + sobranteCantTOT
                  - faltanteCantTOT;
          totalTOT = formatoMoneda.format(totalcantTOT);

          isLogo = getClass().getResourceAsStream("/assets/images/logo-mut.png");
          isLogo2 = getClass().getResourceAsStream("/assets/images/logo-ngu.jpg");
          DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
          DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

          try {
            Map pars = new HashMap<>();

            pars.put("LOGOMUT", isLogo);
            pars.put("LOGO_NGU", isLogo2);

            pars.put("EMPRESA_1", mutNombre);
            pars.put("EMPRESA_2", nguNombre);
            pars.put("Titulo", "REPORTE DE CIERRE DE CAJERO");
            pars.put("USUARIO", LoginController.usuarioLoggeado);
            pars.put("NOMBRE_USER", nomcajero);
            pars.put("TURNO", turno);
            pars.put("Folio", folio);
            pars.put("Fecha", fecha.format(formatter));
            pars.put("Hora", hora);

            pars.put("DEP_AHORRO_MUT", ahorros);
            pars.put("DEP_CREDITO_MUTU", creditos);
            pars.put("DEP_CS_MUT", capital);
            pars.put("DEP_RETIROS_MUT", retiros);
            pars.put("DEP_DESEMBOLSO_MUT", desembolsos);
            pars.put("DEP_PRESOC_MUT", presoc);
            pars.put("DEP_TRASLADO_A_MUT", apertura);
            pars.put("DEP_TRASLADO_C_MUT", total);
            pars.put("SOBRANTE_MUT", sobrante);
            pars.put("FALTANTE_MUT", faltante);
            pars.put("TOTAL_MUT", cierre);

            pars.put("DEP_AHORRO_NGU", ahorrosNGU);
            pars.put("DEP_CREDITO_NGU", creditosNGU);
            pars.put("DEP_CS_NGU", capitalNGU);
            pars.put("DEP_RETIROS_NGU", retirosNGU);
            pars.put("DEP_DESEMBOLSO_NGU", desembolsosNGU);
            pars.put("DEP_PRESOC_NGU", presocNGU);
            pars.put("DEP_TRASLADO_A_NGU", aperturaNGU);
            pars.put("DEP_TRASLADO_C_NGU", totalNGU);
            pars.put("SOBRANTE_NGU", sobranteNGU);
            pars.put("FALTANTE_NGU", faltanteNGU);
            pars.put("TOTAL_NGU", cierreNGU);

            pars.put("DEP_AHORRO", ahorrosTOT);
            pars.put("DEP_CREDITO_TOT", creditosTOT);
            pars.put("DEP_CS_TOT", capitalTOT);
            pars.put("DEP_RETIROS_TOT", retirosTOT);
            pars.put("DEP_DESEMBOLSO_TOT", desembolsosTOT);
            pars.put("DEP_PRESOC_TOT", presocTOT);
            pars.put("DEP_TRASLADO_A_TOT", aperturaTOT);
            pars.put("DEP_TRASLADO_C_TOT", totalTOT);
            pars.put("SOBRANTE_TOT", sobranteTOT);
            pars.put("FALTANTE_TOT", faltanteTOT);
            pars.put("TOTAL_F", cierreTOT);

            // pars.put("TOTAL_E" , cierreTOT);
            pars.put("TOTAL_FISI", saldoFisicoMut);
            pars.put("TOTAL_FISI_NGU", saldoFisicoNgu);
            pars.put("TOTAL_FISI_TOT", totSaldoFisicoAmbas);

            InputStream isRepo = getClass().getResourceAsStream("/Reports/cierrefinal.jasper");
            JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
            JasperPrint jpRepo =
                JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

            JasperViewer viewer = new JasperViewer(jpRepo, false);

            viewer.setSize(800, 600);
            viewer.setLocationRelativeTo(null);
            viewer.setTitle("REPORTE DE CIERRE DE CAJERO");
            viewer.setVisible(true);

          } catch (Exception e) {
            e.printStackTrace();
          }

          Stage ventanaActual = (Stage) btnCerrar.getScene().getWindow();
          Stage ventanaPrincipal = (Stage) lblParaCerrarLaPrincipal.getScene().getWindow();
          Stage nuevaVentana = new Stage();
          FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/login.fxml"));
          fxml.setControllerFactory(Main.context::getBean);
          Scene nuevaEscena = new Scene(fxml.load());
          nuevaEscena
              .getStylesheets()
              .add(getClass().getResource("/assets/css/estilos.css").toExternalForm());
          nuevaVentana.setTitle("AUTENTICACIÓN DE USUARIO");
          Image icon = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
          nuevaVentana.getIcons().add(icon);
          nuevaVentana.setScene(nuevaEscena);
          nuevaVentana.setResizable(false);
          nuevaVentana.centerOnScreen();
          nuevaVentana.show();
          ventanaActual.close();
          ventanaPrincipal.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  private boolean isValido(String resMutu, String resNgu) {
    boolean valido = false;
    if (resMutu.equals("CORRECTO") && resNgu.equals("CORRECTO")) {
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("CIERRE DE CAJERO REALIZADO CORRECTAMENTE");
      alert.setHeaderText("CIERRE EXITOSO");
      alert.setContentText(
          "EL CIERRE DEL CAJERO: " + LoginController.usuarioLoggeado + " FUE EXITOSO.");
      alert.showAndWait();
      valido = true;
    } else {
      String error = "";
      if (resMutu.equals("CORRECTO") && !resNgu.equals("CORRECTO")) {
        error = resNgu.toUpperCase();
      } else if (!resMutu.equals("CORRECTO") && resNgu.equals("CORRECTO")) {
        error = resMutu.toUpperCase();
      } else {
        error = resMutu.toUpperCase();
      }
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR AL REALIZAR EL CIERRE DE CAJERO");
      alert.setHeaderText("ERROR EN EL CIERRE");
      alert.setContentText("ERROR: " + error);
      alert.showAndWait();
    }
    return valido;
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.CONTROL)) {
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
}
