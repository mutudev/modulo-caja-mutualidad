package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Main;
import com.mutu.modulo_caja.Models.ModelCaja;
import com.mutu.modulo_caja.Models.ModelEmpresa;
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
import net.synedra.validatorfx.Validator;
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
public class CierreController implements Initializable {

  @FXML private TextField  txtSaldoFis, txtEmpresa, txtEmpresa1, txtSaldoFis1;
  @FXML private Button btnCancelar, btnLimpiar, btnLimpiar1, btnCierreMut, btnCierreNgu, btnImprimir;

  private Label lblFalt;

  public String empresa, turno, codigoEmpresa, empresa1 ;

  public BigDecimal saldoInicial, saldoFinal, saldoFisico, saldoFisico1, saldoInicialNgu, saldoFinalNgu;

  public int usuario_id = 0, codcaja =0, foliotrasladoCierreMut = 0, foliotrasladoCierreNgu = 0;

  public Validator validator = new Validator();

  ModelCaja cajaMut = null;
  ModelCaja cajaNgu = null;


  NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);

  @Autowired public Servicio servicio;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // Validador

    txtSaldoFis.setTextFormatter(
            new TextFormatter<>(
                    change -> {
                      String newText = change.getControlNewText();
                      // Permitir solo dígitos y un punto decimal
                      if (newText.matches("\\d*(\\.\\d*)?")) {
                        return change;
                      } else {
                        return null; // Rechaza el cambio
                      }
                    }));

    txtSaldoFis1.setTextFormatter(
            new TextFormatter<>(
                    change -> {
                      String newText = change.getControlNewText();
                      // Permitir solo dígitos y un punto decimal
                      if (newText.matches("\\d*(\\.\\d*)?")) {
                        return change;
                      } else {
                        return null; // Rechaza el cambio
                      }
                    }));

    Platform.runLater(
            () -> {
              Stage stage = (Stage) btnCancelar.getScene().getWindow();
              stage.setOnCloseRequest(event -> cierreDeVentana(event));
            });
    btnLimpiar.setVisible(false);
    btnLimpiar1.setVisible(false);
  }

  @FXML
  public void procesarMonto() {

    double saldofis = Double.parseDouble(txtSaldoFis.getText());
    if(saldofis <= 0){
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERRROR AL INGREDSAR DATOS");
      alert.setHeaderText("ERROR AL INGRESAR DATOS");
      alert.setContentText("POR FAVOR, ESCRIBA UN MONTO VALIDO.");
      alert.showAndWait();
      limpiar();
      return;
    }
    txtSaldoFis.setTextFormatter(null);
    txtSaldoFis.setText(formatoMoneda.format(saldofis));
    btnLimpiar.setVisible(true);
    saldoFisico = BigDecimal.valueOf(saldofis);
    btnCierreMut.setVisible(true);
    txtSaldoFis.setEditable(false);
  }

  @FXML
  public void procesarMonto1() {

    double saldofis = Double.parseDouble(txtSaldoFis1.getText());
    if(saldofis <= 0){
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERRROR AL INGREDSAR DATOS");
      alert.setHeaderText("ERROR AL INGRESAR DATOS");
      alert.setContentText("POR FAVOR, ESCRIBA UN MONTO VALIDO.");
      alert.showAndWait();
      limpiar1();
      return;
    }
    txtSaldoFis1.setTextFormatter(null);
    txtSaldoFis1.setText(formatoMoneda.format(saldofis));
    btnLimpiar1.setVisible(true);
    saldoFisico1 = BigDecimal.valueOf(saldofis);
    btnCierreNgu.setVisible(true);

    txtSaldoFis1.setEditable(false);

  }

  @FXML
  public void limpiar(){
    txtSaldoFis.setText("");
    txtSaldoFis.setTextFormatter(
            new TextFormatter<>(
                    change -> {
                      String newText = change.getControlNewText();
                      // Permitir solo dígitos y un punto decimal
                      if (newText.matches("\\d*(\\.\\d*)?")) {
                        return change;
                      } else {
                        return null; // Rechaza el cambio
                      }
                    }));

    btnLimpiar.setVisible(false);
    saldoFisico = BigDecimal.ZERO;

    btnCierreMut.setVisible(false);
    txtSaldoFis.setEditable(true);


  }

  @FXML
  public void limpiar1(){
    txtSaldoFis1.setText("");
    txtSaldoFis1.setTextFormatter(
            new TextFormatter<>(
                    change -> {
                      String newText = change.getControlNewText();
                      // Permitir solo dígitos y un punto decimal
                      if (newText.matches("\\d*(\\.\\d*)?")) {
                        return change;
                      } else {
                        return null; // Rechaza el cambio
                      }
                    }));
    btnLimpiar1.setVisible(false);
    saldoFisico1 = BigDecimal.ZERO;
    btnCierreNgu.setVisible(false);

    txtSaldoFis1.setEditable(true);
  }

  private boolean isValido(String resMutu, String resNgu) {
    boolean valido = false;
    if (resMutu.equals("CORRECTO") && resNgu.equals("CORRECTO")) {
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


  public void settearDatos(String turno, Label lblFalt) {
    this.empresa = "MUTUALIDAD DOCE DE AGOSTO S.C. DE R.L. DE C.V.";
    this.turno = turno;
    this.lblFalt = lblFalt;
    this.empresa1 = "NUEVA GENERACION DE UMAN, AC.";
    txtEmpresa.setText(empresa);
    txtEmpresa1.setText(empresa1);

    ModelEmpresa mut = servicio.traerEmpresaConRS(empresa);
    ModelEmpresa ngu = servicio.traerEmpresaConRS(empresa1);

    //Obtener id del usuario
    usuario_id = servicio.traerDatosUsuario(LoginController.usuarioLoggeado).getId();
    ModelCaja cajaMut = servicio.traerDatosCaja(usuario_id, turno, mut.getCodigo(), 1);
    ModelCaja cajaNgu = servicio.traerDatosCaja(usuario_id, turno, ngu.getCodigo(), 1);

    this.cajaNgu = cajaNgu;
    this.cajaMut = cajaMut;

    saldoInicial = BigDecimal.valueOf(cajaMut.getSal_inicial());
    saldoFinal = BigDecimal.valueOf(cajaMut.getSal_final());
    saldoInicialNgu = BigDecimal.valueOf(cajaNgu.getSal_inicial());
    saldoFinalNgu = BigDecimal.valueOf(cajaNgu.getSal_final());
  }

  @FXML
  public void cerrarNgu() {

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CIERRE DE CAJA");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR TU CAJA DE ESTA EMPRESA?");
    alert.setContentText(
            "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR.");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() != ButtonType.OK) {
      return;
    }


    DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
    LocalDateTime fecha = LocalDateTime.now();
    LocalTime hora = fecha.toLocalTime();
    String horaticket = hora.format(formatterHora);
    BigDecimal ajusteNgu = BigDecimal.valueOf(0);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String fechaTicket = fecha.format(formatter);

    //Hacer ajuste para ambas empresas
    if (saldoFinal == BigDecimal.valueOf(0)) {
      ajusteNgu = BigDecimal.valueOf(saldoInicialNgu.doubleValue() - saldoFisico1.doubleValue());
    } else {
      ajusteNgu = BigDecimal.valueOf(saldoFinalNgu.doubleValue() - saldoFisico1.doubleValue());
    }

    //TRASLADO DE CIERRE
    Map<String, Object> resNgu =
            servicio.procesarTraslado(
                    LoginController.usuarioLoggeado,
                    saldoFisico1.doubleValue(),
                    7,
                    cajaNgu.getEmpresa(),
                    horaticket,
                    turno,
                    1,
                    "",
                    0);


    String resAjusteNgu ="";
    if(ajusteNgu.doubleValue()>0){
      resAjusteNgu = servicio.procesarAjuste(LoginController.usuarioLoggeado,ajusteNgu.doubleValue(),0,
              turno,cajaNgu.getEmpresa(), saldoFisico.doubleValue(),"");
    }else if(ajusteNgu.doubleValue()<0){

      resAjusteNgu = servicio.procesarAjuste(LoginController.usuarioLoggeado,0, -1 * ajusteNgu.doubleValue(),
              turno,cajaNgu.getEmpresa(), saldoFisico.doubleValue(),"");
    }else{
      resAjusteNgu = servicio.procesarAjuste(LoginController.usuarioLoggeado,0,0,
              turno,cajaNgu.getEmpresa(), saldoFisico.doubleValue(),"");

    }

    if(!resAjusteNgu.equals("CORRECTO")){

      Alert alert2 = new Alert(Alert.AlertType.ERROR);
      alert2.setTitle("ERROR");
      alert2.setHeaderText("ERROR AL CERRAR LA EMPRESA");
      alert2.setContentText(resAjusteNgu.toString().toUpperCase());
      alert2.showAndWait();
      return;
    }



    String folioNgu = resNgu.get("transaccion_id").toString();
    if (folioNgu.equals("0")) {
      Alert alert2 = new Alert(Alert.AlertType.ERROR);
      alert2.setTitle("ERROR");
      alert2.setHeaderText("ERROR AL CERRAR LA EMPRESA");
      alert2.setContentText(resNgu.get("Resultado").toString().toUpperCase());
      alert2.showAndWait();
      return;
    } else {
      foliotrasladoCierreNgu = Integer.valueOf(folioNgu);
      Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
      alert2.setTitle("¡CORRECTO!");
      alert2.setHeaderText("CIERRE DE EMPRESA EXITOSO");
      alert2.setContentText("PUEDE PROCEDER CON SU SIGUIENTE CIERRE O A LA IMPRESIÓN.");
      alert2.showAndWait();
    }

    btnCierreNgu.setDisable(true);

    if (btnCierreMut.isDisabled()) {
      btnImprimir.setVisible(true);
    }




  }

  @FXML
  public void imprimirCierre() {
    //Verificar que ambas empresas esten cerradas

    cajaMut = servicio.traerCajaConId(cajaMut.getId());
    cajaNgu = servicio.traerCajaConId(cajaNgu.getId());


    LocalDate fecha = LocalDate.now();


    Map<String, Object> resMutu =
            servicio.pa_procesarCierre(cajaMut.getId(), 0, fecha.toString(), saldoFisico.doubleValue(), "", 0);

    Map<String, Object> resNgu =
            servicio.pa_procesarCierre(0, cajaNgu.getId(), fecha.toString(), saldoFisico1.doubleValue(), "", 0);

    boolean valido =
            isValido(resMutu.get("Resultado").toString(), resNgu.get("Resultado").toString());

    if (valido) {
      try {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("CIERRE HECHO CORRECTAMENTE");
        alert.setHeaderText("CIERRE EXITOSO");
        alert.setContentText(STR."CIERRE EXITOSO DEL CAJERO: \{LoginController.usuarioLoggeado}");
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
                servicio.traerTrasladoCajero(String.valueOf(cajaMut.getId()));
        ModelTraslado trasladoCNgu =
                servicio.traerTrasladoCajero(String.valueOf(cajaNgu.getId()));

        ModelTraslado trasladoAMut =
                servicio.traerTrasladoApertura(String.valueOf(cajaMut.getId()));
        ModelTraslado trasladoANgu =
                servicio.traerTrasladoApertura(String.valueOf(cajaNgu.getId()));

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
                STR."MUT\{resMutu.get("Cierre_id").toString()}-NGU\{resNgu.get("Cierre_id").toString()}";

        for (Object filaObj : datos) {
          if (filaObj instanceof Object[]) {
            Object[] fila = (Object[]) filaObj;

            hora = fila[15].toString();

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

        Stage ventanaActual = (Stage) btnImprimir.getScene().getWindow();
        Stage ventanaPrincipal = (Stage) lblFalt.getScene().getWindow();
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

  @FXML
  public void cerrarMut() {

    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CIERRE DE CAJA");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR TU CAJA DE ESTA EMPRESA?");
    alert.setContentText(
            "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR.");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() != ButtonType.OK) {
      return;
    }


    DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
    LocalDateTime fecha = LocalDateTime.now();
    LocalTime hora = fecha.toLocalTime();
    String horaticket = hora.format(formatterHora);
    BigDecimal ajusteMut = BigDecimal.valueOf(0);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String fechaTicket = fecha.format(formatter);

    //Hacer ajuste para ambas empresas
    if (saldoFinal == BigDecimal.valueOf(0)) {
      ajusteMut = BigDecimal.valueOf(saldoInicial.doubleValue() - saldoFisico.doubleValue());
    } else {
      ajusteMut = BigDecimal.valueOf(saldoFinal.doubleValue() - saldoFisico.doubleValue());
    }

    //TRASLADO DE CIERRE
    Map<String, Object> resMut =
            servicio.procesarTraslado(
                    LoginController.usuarioLoggeado,
                    saldoFisico.doubleValue(),
                    7,
                    cajaMut.getEmpresa(),
                    horaticket,
                    turno,
                    1,
                    "",
                    0);


    //multiplicar ajsute por negativo
    String resAjusteMUT ="";
    if(ajusteMut.doubleValue()>0){
      resAjusteMUT = servicio.procesarAjuste(LoginController.usuarioLoggeado,ajusteMut.doubleValue(),0,
              turno,cajaMut.getEmpresa(), saldoFisico.doubleValue(),"");
    }else if(ajusteMut.doubleValue()<0){

      resAjusteMUT = servicio.procesarAjuste(LoginController.usuarioLoggeado,0,-1 * ajusteMut.doubleValue(),
              turno,cajaMut.getEmpresa(), saldoFisico.doubleValue(),"");
    }else{
      resAjusteMUT = servicio.procesarAjuste(LoginController.usuarioLoggeado,0,0,
              turno,cajaMut.getEmpresa(), saldoFisico.doubleValue(),"");

    }

    if(!resAjusteMUT.equals("CORRECTO")){

      Alert alert2 = new Alert(Alert.AlertType.ERROR);
      alert2.setTitle("ERROR");
      alert2.setHeaderText("ERROR AL CERRAR LA EMPRESA");
      alert2.setContentText(resAjusteMUT.toString().toUpperCase());
      alert2.showAndWait();
      return;
    }




    String folioMut = resMut.get("transaccion_id").toString();
    if (folioMut.equals("0")) {
      Alert alert2 = new Alert(Alert.AlertType.ERROR);
      alert2.setTitle("ERROR");
      alert2.setHeaderText("ERROR AL CERRAR LA EMPRESA");
      alert2.setContentText(resMut.get("Resultado").toString().toUpperCase());
      alert2.showAndWait();
      return;
    } else {
      foliotrasladoCierreMut = Integer.valueOf(folioMut);
      Alert alert2 = new Alert(Alert.AlertType.INFORMATION);
      alert2.setTitle("¡CORRECTO!");
      alert2.setHeaderText("CIERRE DE EMPRESA EXITOSO");
      alert2.setContentText("PUEDE PROCEDER CON SU SIGUIENTE CIERRE O A LA IMPRESIÓN.");
      alert2.showAndWait();
    }

    btnCierreMut.setDisable(true);

    if (btnCierreNgu.isDisabled()) {
      btnImprimir.setVisible(true);
    }



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
    validator = new Validator();
    Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
    ventanaActual.close();
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
      validator = new Validator();
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
    validator = new Validator();
    Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
    ventanaActual.close();
  }




}
