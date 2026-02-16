package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Main;
import com.mutu.modulo_caja.Models.ModelCaja;
import com.mutu.modulo_caja.Models.ModelEmpresa;
import com.mutu.modulo_caja.Models.ModelTraslado;
import com.mutu.modulo_caja.Models.ModelUsuario;
import com.mutu.modulo_caja.Services.Servicio;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.UnaryOperator;

@Component
public class CierreController implements Initializable {

  @FXML private TextField  txtSaldoFis, txt1000, txt500, txt200, txt100, txt50, txt20, txt10, txt5, txt2,
          txt1, txt50c, txt20c, txt10c;

  @FXML private Button btnCancelar, btnLimpiar, btnCalcular, btnImprimir;


  @FXML private Label lblEmpresa1, lblEmpresa2;

  private Label cierreCajero;

  public String empresa, turno, codigoEmpresa, empresa1 ;

  public BigDecimal saldoInicial, saldoFinal, saldoFisico, saldoInicialNgu, saldoFinalNgu;

  public int usuario_id = 0, codcaja =0, foliotrasladoCierreMut = 0, foliotrasladoCierreNgu = 0;

  public double totalMandar = 0;

  public Validator validator = new Validator();

  ModelCaja cajaMut = null;
  ModelCaja cajaNgu = null;


  NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);

  @Autowired public Servicio servicio;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // Validador


    UnaryOperator<TextFormatter.Change> filter = change -> {
      String newText = change.getControlNewText();
      if (newText.matches("\\d*(\\.\\d*)?")) {
        return change;
      }
      return null;
    };

    TextField[] fields = {
            txt1000, txt500, txt200, txt100, txt50,
            txt20, txt10, txt5, txt2, txt1,
            txt50c, txt20c, txt10c
    };

    for (TextField field : fields) {
      field.setTextFormatter(new TextFormatter<>(filter));
    }



    Platform.runLater(
            () -> {
              Stage stage = (Stage) btnCancelar.getScene().getWindow();
              stage.setOnCloseRequest(event -> cierreDeVentana(event));
            });
    btnLimpiar.setVisible(false);


    lblEmpresa1.setText("MUTUALIDAD DOCE DE AGOSTO S.C. DE R.L. DE C.V.");
    lblEmpresa2.setText( "NUEVA GENERACION DE UMAN, AC.");

  }

  @FXML
  public void calcularMonto() {

    double n1000 = getValue(txt1000, 1000);
    double n500  = getValue(txt500, 500);
    double n200  = getValue(txt200, 200);
    double n100  = getValue(txt100, 100);
    double n50   = getValue(txt50, 50);
    double n20   = getValue(txt20, 20);
    double n10   = getValue(txt10, 10);
    double n5    = getValue(txt5, 5);
    double n2    = getValue(txt2, 2);
    double n1    = getValue(txt1, 1);
    double n50c  = getValue(txt50c, 0.5);
    double n20c  = getValue(txt20c, 0.2);
    double n10c  = getValue(txt10c, 0.1);

    txt1000.setDisable(true);
    txt500.setDisable(true);
    txt200.setDisable(true);
    txt100.setDisable(true);
    txt50.setDisable(true);
    txt20.setDisable(true);
    txt10.setDisable(true);
    txt5.setDisable(true);
    txt2.setDisable(true);
    txt1.setDisable(true);
    txt50c.setDisable(true);
    txt20c.setDisable(true);
    txt10c.setDisable(true);



     totalMandar = n1000 + n500 + n200 + n100 + n50 +
            n20 + n10 + n5 + n2 + n1 +
            n50c + n20c + n10c;
    String totalFormateado = formatoMoneda.format(totalMandar);
    txtSaldoFis.setText(totalFormateado);
    btnLimpiar.setVisible(true);

  }


  @FXML
  public void limpiar(){
    txtSaldoFis.setText("");
    txt1000.setDisable(false);
    txt500.setDisable(false);
    txt200.setDisable(false);
    txt100.setDisable(false);
    txt50.setDisable(false);
    txt20.setDisable(false);
    txt10.setDisable(false);
    txt5.setDisable(false);
    txt2.setDisable(false);
    txt1.setDisable(false);
    txt50c.setDisable(false);
    txt20c.setDisable(false);
    txt10c.setDisable(false);
    txt1000.setText("");
    txt500.setText("");
    txt200.setText("");
    txt100.setText("");
    txt50.setText("");
    txt20.setText("");
    txt10.setText("");
    txt5.setText("");
    txt2.setText("");
    txt1.setText("");
    txt50c.setText("");
    txt20c.setText("");
    txt10c.setText("");
    txtSaldoFis.setText("");
    btnLimpiar.setVisible(false);
  }


  private double getValue(TextField field, double multiplier) {
    if (field.getText().isEmpty()) {
      field.setText("0");
      return 0;
    }
    return Double.parseDouble(field.getText()) * multiplier;
  }


  public void settearDatos(Label lblCierreCajero, String turno) {
    this.cierreCajero = lblCierreCajero;
    this.turno = turno;
    ModelUsuario usuario = servicio.traerDatosUsuario(LoginController.usuarioLoggeado);
    ModelCaja cajaMut = servicio.traerDatosCaja(usuario.getId(), turno, "0001", 1);
    ModelCaja cajaNgu = servicio.traerDatosCaja(usuario.getId(), turno, "0002", 1);

    this.cajaMut = cajaMut;
    this.cajaNgu = cajaNgu;

  }

  @FXML
  public void imprimirCierre() {

    //Verificar que ambas empresas esten cerradas y obtener los ID's de cajas
    cajaMut = servicio.traerCajaConId(cajaMut.getId());
    cajaNgu = servicio.traerCajaConId(cajaNgu.getId());

    if (cajaMut == null || cajaNgu == null) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR");
      alert.setHeaderText("ERROR AL GENERAR EL CIERRE");
      alert.setContentText("NO SE ENCONTRÓ REGISTROS DE ALGUNA DE LAS CAJAS DEL USUARIO.");
      alert.showAndWait();
      return;
    }

    if (cajaNgu.getAjuste() != 1 || cajaMut.getAjuste() != 1) {
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR");
      alert.setHeaderText("ERROR AL GENERAR EL CIERRE");
      alert.setContentText("ALGUNA DE LAS CAJAS DEL USUARIO NO SE ENCUENTRA CON ACCESO.");
      alert.showAndWait();
      return;
    }

    LocalDate fecha = LocalDate.now();

    //Declarar la pantalla de carga
    Stage loadingStage = new Stage();
    loadingStage.initModality(Modality.APPLICATION_MODAL);
    loadingStage.initStyle(StageStyle.UNDECORATED);
    loadingStage.setAlwaysOnTop(true);

    VBox loadingPane = new VBox(20);
    loadingPane.setAlignment(Pos.CENTER);
    loadingPane.setPadding(new Insets(30));
    loadingPane.setStyle("-fx-background-color: white; -fx-border-color: #185754; -fx-border-width: 2;");

    ProgressIndicator progressIndicator = new ProgressIndicator();
    progressIndicator.setPrefSize(60, 60);

    Label loadingLabel = new Label("Generando Cierre...");
    loadingLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
    loadingLabel.setTextFill(Color.web("#39577c"));

    loadingPane.getChildren().addAll(progressIndicator, loadingLabel);

    Scene loadingScene = new Scene(loadingPane, 300, 150);
    loadingStage.setScene(loadingScene);
    loadingStage.centerOnScreen();

    Map<String, Object> res = servicio.pa_procesarCierre(cajaMut.getId(), cajaNgu.getId(), fecha.toString(), totalMandar, LoginController.usuarioLoggeado, "", 0, 0);
    //Verificar que haya sido válido el cierre
    if (res.get("Resultado").toString().equalsIgnoreCase("CORRECTO")){
      //Seguimos con el cierre
      Alert alert = new Alert(Alert.AlertType.INFORMATION);
      alert.setTitle("CIERRE HECHO CORRECTAMENTE");
      alert.setHeaderText("CIERRE EXITOSO");
      alert.setContentText("CIERRE EXITOSO DEL CAJERO: "+ LoginController.usuarioLoggeado);
      alert.showAndWait();

      //Obtener los id de cierre de mut y NGU que se retornan
      int cierreMut = Integer.valueOf(res.get("Cierre_id_mut").toString());
      int cierreNgu = Integer.valueOf(res.get("Cierre_id_ngu").toString());

      // Obtenemos los datos para el reporte
      Task<Void> task =
          new Task<>() {
            @Override
            protected Void call() {
              try {

                String mutNombre = lblEmpresa1.getText().trim();
                String nguNombre = lblEmpresa2.getText().trim();

                Object[] datos = servicio.traerCierreCajero(cierreMut);
                Object[] datosNGU = servicio.traerCierreCajero(cierreNgu);

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
                String total = "";
                double totalcant = 0;
                String saldoFisicoMut = "";
                double cantSaldoFisicoMut = 0;
                String sobranteMUT = "";
                double sobranteCantMUT = 0;
                String faltanteMUT = "";
                double faltanteCantMUT = 0;

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
                String totalNGU = "";
                double totalcantNGU = 0;
                String saldoFisicoNgu = "";
                double cantSaldoFisicoNgu = 0;
                String sobranteNGU = "";
                double sobranteCantNGU = 0;
                String faltanteNGU = "";
                double faltanteCantNGU = 0;

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

                folio = "MUT" + String.valueOf(cierreMut) + "-NGU" + String.valueOf(cierreNgu);

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

                    sobranteCantMUT = Double.parseDouble(String.valueOf(fila[10]));
                    sobranteMUT = formatoMoneda.format(sobranteCantMUT);

                    faltanteCantMUT = Double.parseDouble(String.valueOf(fila[11]));
                    faltanteMUT = formatoMoneda.format(faltanteCantMUT);

                    cantSaldoFisicoMut = Double.parseDouble(String.valueOf(fila[12]));
                    saldoFisicoMut = formatoMoneda.format(cantSaldoFisicoMut);

                    totalcant =
                        creditosCant
                            + ahorrosCant
                            - retirosCant
                            - desembolsosCant
                            + csCant
                            + presocCant
                            + aperturaCant;
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
                            + aperturaCantNGU;
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

                cierreCantTOT = totalcant + totalcantNGU;
                cierreTOT = formatoMoneda.format(cierreCantTOT);

                sobranteCantTOT = sobranteCantMUT;
                sobranteTOT = formatoMoneda.format(sobranteCantTOT);

                faltanteCantTOT = faltanteCantMUT;
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
                pars.put("DEP_TRASLADO_C_MUT", cierre);
                pars.put("TOTAL_MUT", total);

                pars.put("DEP_AHORRO_NGU", ahorrosNGU);
                pars.put("DEP_CREDITO_NGU", creditosNGU);
                pars.put("DEP_CS_NGU", capitalNGU);
                pars.put("DEP_RETIROS_NGU", retirosNGU);
                pars.put("DEP_DESEMBOLSO_NGU", desembolsosNGU);
                pars.put("DEP_PRESOC_NGU", presocNGU);
                pars.put("DEP_TRASLADO_A_NGU", aperturaNGU);
                pars.put("DEP_TRASLADO_C_NGU", cierreNGU);
                pars.put("TOTAL_NGU", totalNGU);

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

                pars.put("TOTAL_FISI_TOT", totSaldoFisicoAmbas);

                InputStream isRepo = getClass().getResourceAsStream("/Reports/cierrefinal.jasper");
                JasperReport jrRepo = (JasperReport) JRLoader.loadObject(isRepo);
                JasperPrint jpRepo =
                    JasperFillManager.fillReport(jrRepo, pars, new JREmptyDataSource());

                Platform.runLater(
                    () -> {
                      JasperViewer viewer = new JasperViewer(jpRepo, false);
                      viewer.setAlwaysOnTop(true);
                      viewer.setSize(800, 600);
                      viewer.setLocationRelativeTo(null);
                      viewer.setTitle("REPORTE DE CIERRE DE CAJERO");
                      viewer.setVisible(true);

                      try {
                        Stage ventanaActual = (Stage) btnImprimir.getScene().getWindow();
                        Stage ventanaPrincipal = (Stage) cierreCajero.getScene().getWindow();
                        Stage nuevaVentana = new Stage();
                        FXMLLoader fxml =
                            new FXMLLoader(getClass().getResource("/com/java/fx/login.fxml"));
                        fxml.setControllerFactory(Main.context::getBean);
                        Scene nuevaEscena = new Scene(fxml.load());
                        nuevaEscena
                            .getStylesheets()
                            .add(
                                getClass().getResource("/assets/css/estilos.css").toExternalForm());
                        nuevaVentana.setTitle("AUTENTICACIÓN DE USUARIO");
                        Image icon =
                            new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
                        nuevaVentana.getIcons().add(icon);
                        nuevaVentana.setScene(nuevaEscena);
                        nuevaVentana.setResizable(false);
                        nuevaVentana.centerOnScreen();
                        nuevaVentana.show();
                        ventanaActual.close();
                        ventanaPrincipal.close();
                      } catch (IOException e) {

                      }
                    });

              } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(
                    () -> {
                      Alert alert = new Alert(Alert.AlertType.ERROR);
                      alert.setTitle("ERROR");
                      alert.setHeaderText("ERROR AL GENERAR EL REPORTE");
                      alert.setContentText("OCURRIÓ UN ERROR: " + e.getMessage());
                      alert.showAndWait();
                    });
              }
              return null;
            }
          };

      task.setOnSucceeded(e -> loadingStage.close());
      task.setOnFailed(e -> loadingStage.close());

      loadingStage.show();
      new Thread(task).start();


    } else {
      //Mostramos el error
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("ERROR AL REALIZAR EL CIERRE DE CAJERO");
      alert.setHeaderText("ERROR EN EL CIERRE");
      alert.setContentText("ERROR: " + res.get("Resultado").toString().toUpperCase());
      alert.showAndWait();
    }








  }


  public void cierreDeVentana(Event event) {
    event.consume();
    validator = new Validator();
    Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
    ventanaActual.close();
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ESCAPE)) {
      validator = new Validator();
      Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
      ventanaActual.close();
    }
  }

  @FXML
  public void cerrarConBoton() {
    validator = new Validator();
    Stage ventanaActual = (Stage) btnCancelar.getScene().getWindow();
    ventanaActual.close();
  }




}
