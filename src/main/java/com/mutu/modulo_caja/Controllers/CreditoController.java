package com.mutu.modulo_caja.Controllers;

import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Services.Servicio;
import com.mutu.modulo_caja.utils.PrintJob;
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
import net.synedra.validatorfx.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class CreditoController implements Initializable {

  public final Validator validator = new Validator();

  @FXML private TextField txtMonto, txtInmediatas, txtSaldoCredito;
  @FXML
  private Label lblError,
      lblNumSocio,
      lblNombre,
      lblCredito,
      lblPlazo,
      lblTasa,
      lblMora,
      lblTipo,
      lblCodigo;

  @FXML private TableView<Object[]> tablaCuotas;

  @Autowired public Servicio servicio;

  @FXML
  private TableColumn<Object[], String> cuota, fecha, capital, ord, colmora, coliva, bonif, tot;

  // Variables de datos básicos
  String numSocio;
  String nomSocio;
  String numCredito;
  String plazos;
  String tasa;
  String mora;
  String iva;
  String tipoCredito;
  String codigoSistema;
  String fechaDesembolso = "";

  // Variables de cálculo
  double tasaInteres = 0;
  double ivaaplicar = 0;
  double moraaplicar = 0;
  double capitalInicial = 0;
  double interesBuffer = 0;
  boolean bonifAplicable;

  // Formatters
  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);

  // Lista de cuotas
  List<Object[]> cuotas = null;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // Validador
    validator
        .createCheck()
        .dependsOn("input", txtMonto.textProperty())
        .withMethod(
            c -> {
              String texto = c.get("input");
              try {
                if (texto == null || texto.isEmpty() || Double.parseDouble(texto) <= 0) {
                  c.error("Ingrese un valor mayor a cero");
                  lblError.setText("Ingrese un valor mayor a cero");
                } else {
                  lblError.setText("");
                }
              } catch (NumberFormatException e) {
                c.error("Ingrese un número válido");
                lblError.setText("Ingrese un número válido");
              }
            })
        .decorates(txtMonto)
        .immediate();

    txtMonto.setTextFormatter(
        new TextFormatter<>(
            change -> {
              String nuevoTexto = change.getControlNewText();

              // Permitir solo números decimales válidos (opcionalmente solo un punto decimal)
              if (nuevoTexto.matches("\\d*(\\.\\d{0,2})?")) {
                return change;
              } else {
                return null; // rechaza el cambio
              }
            }));

    Platform.runLater(
        () -> {
          Stage stage = (Stage) txtMonto.getScene().getWindow();
          stage.setOnCloseRequest(event -> cierreDeVentana(event));
        });

    // Configuración de columnas
    configurarColumnas();
  }

  private void configurarColumnas() {
    cuota.setCellValueFactory(
            cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue()[2])));

    fecha.setCellValueFactory(
            cellData -> {
              LocalDate fecha = LocalDate.parse((String) cellData.getValue()[3], formatter);
              DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
              return new SimpleStringProperty(fecha.format(outputFormatter));
            });

    fecha.setCellFactory(column -> new TableCell<Object[], String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
          setText(null);
          setStyle("");
        } else {
          setText(item);
          LocalDate fechaHoy = LocalDate.now();
          String[] itemDividido = item.split("/");
          String fechaFormateada = itemDividido[2] + "-" + itemDividido[1] + "-" + itemDividido[0];
          LocalDate fechaTabla = LocalDate.parse(fechaFormateada, formatter);
          if (fechaHoy.isAfter(fechaTabla)) {
            setStyle("-fx-background-color: lightgreen;");
          } else {
            setStyle("");
          }
        }
      }
    });

    cuota.setCellFactory(column -> new TableCell<Object[], String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
          setText(null);
          setStyle("");
        } else {
          setText(item);
          Object[] rowData = (Object[]) getTableRow().getItem();
          LocalDate fechaHoy = LocalDate.now();
          LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
          if (fechaHoy.isAfter(fechaVencimiento)) {
            setStyle("-fx-background-color: lightgreen;");
          } else {
            setStyle("");
          }
        }
      }
    });

    capital.setCellValueFactory(
            cellData -> new SimpleStringProperty((String) cellData.getValue()[4]));

    capital.setCellFactory(column -> new TableCell<Object[], String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
          setText(null);
          setStyle("");
        } else {
          setText(item);
          Object[] rowData = (Object[]) getTableRow().getItem();
          LocalDate fechaHoy = LocalDate.now();
          LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
          if (fechaHoy.isAfter(fechaVencimiento)) {
            setStyle("-fx-background-color: lightgreen;");
          } else {
            setStyle("");
          }
        }
      }
    });

    ord.setCellValueFactory(
            cellData -> {
              double interesOrdinario = calcularInteresOrdinario(cellData.getValue());
              LocalDate fechaHoy = LocalDate.now();
              LocalDate fechaVencimiento = LocalDate.parse((String) cellData.getValue()[3], formatter);

              double interesFinal = aplicarBonificacion(interesOrdinario, fechaHoy, fechaVencimiento);
              interesBuffer = interesFinal;
              return new SimpleStringProperty(formatoMoneda.format(interesFinal));
            });

    ord.setCellFactory(column -> new TableCell<Object[], String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
          setText(null);
          setStyle("");
        } else {
          setText(item);
          Object[] rowData = (Object[]) getTableRow().getItem();
          LocalDate fechaHoy = LocalDate.now();
          LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
          if (fechaHoy.isAfter(fechaVencimiento)) {
            setStyle("-fx-background-color: lightgreen;");
          } else {
            setStyle("");
          }
        }
      }
    });

    colmora.setCellValueFactory(
            cellData -> {
              double moraaplicada = calcularMora(cellData.getValue());
              return new SimpleStringProperty(formatoMoneda.format(moraaplicada));
            });

    colmora.setCellFactory(column -> new TableCell<Object[], String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
          setText(null);
          setStyle("");
        } else {
          setText(item);
          Object[] rowData = (Object[]) getTableRow().getItem();
          LocalDate fechaHoy = LocalDate.now();
          LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
          if (fechaHoy.isAfter(fechaVencimiento)) {
            setStyle("-fx-background-color: lightgreen;");
          } else {
            setStyle("");
          }
        }
      }
    });

    coliva.setCellValueFactory(
            cellData -> {
              double ivaaplicada = calcularIva(cellData.getValue());
              return new SimpleStringProperty(formatoMoneda.format(ivaaplicada));
            });

    coliva.setCellFactory(column -> new TableCell<Object[], String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
          setText(null);
          setStyle("");
        } else {
          setText(item);
          Object[] rowData = (Object[]) getTableRow().getItem();
          LocalDate fechaHoy = LocalDate.now();
          LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
          if (fechaHoy.isAfter(fechaVencimiento)) {
            setStyle("-fx-background-color: lightgreen;");
          } else {
            setStyle("");
          }
        }
      }
    });

    bonif.setCellValueFactory(
            cellData -> {
              double bonifAplicada = calcularBonificacion(cellData.getValue());
              return new SimpleStringProperty(formatoMoneda.format(bonifAplicada));
            });

    bonif.setCellFactory(column -> new TableCell<Object[], String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
          setText(null);
          setStyle("");
        } else {
          setText(item);
          Object[] rowData = (Object[]) getTableRow().getItem();
          LocalDate fechaHoy = LocalDate.now();
          LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
          if (fechaHoy.isAfter(fechaVencimiento)) {
            setStyle("-fx-background-color: lightgreen;");
          } else {
            setStyle("");
          }
        }
      }
    });

    tot.setCellValueFactory(
            cellData -> {
              double total = calcularTotal(cellData.getValue());
              return new SimpleStringProperty(formatoMoneda.format(total));
            });

    tot.setCellFactory(column -> new TableCell<Object[], String>() {
      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null || getTableRow() == null || getTableRow().getItem() == null) {
          setText(null);
          setStyle("");
        } else {
          setText(item);
          Object[] rowData = (Object[]) getTableRow().getItem();
          LocalDate fechaHoy = LocalDate.now();
          LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
          if (fechaHoy.isAfter(fechaVencimiento)) {
            setStyle("-fx-background-color: lightgreen;");
          } else {
            setStyle("");
          }
        }
      }
    });
  }

  private double calcularInteresOrdinario(Object[] cuota) {
    try {
      int numeroCuota = Integer.parseInt(String.valueOf(cuota[2]));
      LocalDate fechaVencimiento = LocalDate.parse((String) cuota[3], formatter);
      LocalDate fechaHoy = LocalDate.now();
      LocalDate fechaDesembolsoDate = LocalDate.parse(fechaDesembolso, formatter);

      double saldoCredito = parseMoneda((String) cuota[10]);

      if (numeroCuota == 1) {
        LocalDate fechaUltimaPago = null;
        long dias = 0;
        if (cuota[12] != null) {
          fechaUltimaPago = LocalDate.parse(cuota[12].toString(), formatter);
          dias = ChronoUnit.DAYS.between(fechaUltimaPago, fechaHoy);
          return Math.round(calcularInteresPorDias(saldoCredito, dias) * 100.0) / 100.0;
        } else {
          dias = ChronoUnit.DAYS.between(fechaDesembolsoDate, fechaHoy);
          return Math.round(calcularInteresPorDias(capitalInicial, dias) * 100.0) / 100.0;
        }

      } else {

        Object[] ultimaCuotaPagada = null;
        if (cuota[12] == null) {
          ultimaCuotaPagada =
                  servicio.traerUltimaCuotaPagada(Integer.parseInt(numCredito), 0);
        } else {
          ultimaCuotaPagada = cuota;
        }


        if (ultimaCuotaPagada.length == 0) {
          if (fechaHoy.isBefore(fechaVencimiento)) {
            return 0;
          } else {
            long diasVencidos = ChronoUnit.DAYS.between(fechaVencimiento, fechaHoy);
            return Math.round(calcularInteresPorDias(capitalInicial, diasVencidos) * 100.0) / 100.0;
          }
        } else {
          String fechaUltimaCuota = "";
          double saldoUltimaCuota = 0;
          int numCuotaReferencia = 0;

          // Manejar si ultimaCuotaPagada es un array de arrays o un array simple
          if (ultimaCuotaPagada[0] instanceof Object[]) {
            Object[] filaCuota = (Object[]) ultimaCuotaPagada[0];
            fechaUltimaCuota = filaCuota[12].toString();
            saldoUltimaCuota = parseMoneda(filaCuota[10].toString());
            numCuotaReferencia = Integer.parseInt(String.valueOf(filaCuota[2]));
          } else {
            fechaUltimaCuota = ultimaCuotaPagada[12].toString();
            saldoUltimaCuota = parseMoneda(ultimaCuotaPagada[10].toString());
            numCuotaReferencia = Integer.parseInt(ultimaCuotaPagada[2].toString());
          }

          if (numeroCuota == (numCuotaReferencia + 1) || numeroCuota == numCuotaReferencia) {
            LocalDate fechaUltimaCuotaDate = LocalDate.parse(fechaUltimaCuota, formatter);
            long dias = ChronoUnit.DAYS.between(fechaUltimaCuotaDate, fechaHoy);
            return Math.round(calcularInteresPorDias(saldoUltimaCuota, dias) * 100.0) / 100.0;
          } else {
            return 0;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  private double calcularInteresPorDias(double saldoCredito, long dias) {
    double resultado = (((tasaInteres / 100) * 12) / 360) * dias * saldoCredito;
    return Math.round(resultado * 100.0) / 100.0;
  }

  private double aplicarBonificacion(
          double interesOrdinario, LocalDate fechaHoy, LocalDate fechaVencimiento) {

    if (!bonifAplicable) {
      return Math.round(interesOrdinario * 100.0) / 100.0;
    }

    double resultado;
    if (fechaHoy.isBefore(fechaVencimiento)) {
      double factorBonificacion = (tasaInteres == 1.1) ? 0.7 : 0.9;
      resultado = interesOrdinario * factorBonificacion;
    } else if (fechaHoy.isEqual(fechaVencimiento)) {
      double factorBonificacion = (tasaInteres == 1.1) ? 0.8 : 0.99;
      resultado = interesOrdinario * factorBonificacion;
    } else {
      resultado = interesOrdinario;
    }

    return Math.round(resultado * 100.0) / 100.0;
  }

  private double calcularMora(Object[] cuota) {
    try {
      LocalDate fechaHoy = LocalDate.now();
      LocalDate fechaVencimiento = LocalDate.parse((String) cuota[3], formatter);

      if (fechaHoy.isAfter(fechaVencimiento.plusDays(29))) {
        long diasMora = ChronoUnit.DAYS.between(fechaVencimiento, fechaHoy);
        double interesOrdinario = calcularInteresOrdinario(cuota);
        double resultado = (((moraaplicar) * 12) / 360) * diasMora * interesOrdinario;
        return Math.round(resultado * 100.0) / 100.0;
      }
      return 0;
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  private double calcularIva(Object[] cuota) {
    try {
      double interesOrdinario = calcularInteresOrdinario(cuota);
      double resultado = interesOrdinario * ivaaplicar;
      return Math.round(resultado * 100.0) / 100.0;
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  private double calcularBonificacion(Object[] cuota) {
    try {

      if (!bonifAplicable) {
        return 0;
      }

      LocalDate fechaHoy = LocalDate.now();
      LocalDate fechaVencimiento = LocalDate.parse((String) cuota[3], formatter);

      double interesOrdinario = calcularInteresOrdinario(cuota);
      double interesConBonificacion =
              aplicarBonificacion(interesOrdinario, fechaHoy, fechaVencimiento);

      double resultado = interesOrdinario - interesConBonificacion;
      return Math.round(resultado * 100.0) / 100.0;
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  private double calcularTotal(Object[] cuota) {
    try {
      double capital = parseMoneda((String) cuota[4]);
      double interes = calcularInteresOrdinario(cuota);
      LocalDate fechaHoy = LocalDate.now();
      LocalDate fechaVencimiento = LocalDate.parse((String) cuota[3], formatter);

      double interesConBonificacion = aplicarBonificacion(interes, fechaHoy, fechaVencimiento);
      double mora = calcularMora(cuota);
      double iva = interesConBonificacion * ivaaplicar;

      // Redondear el total final
      double resultado = capital + interesConBonificacion + mora + iva;
      return Math.round(resultado * 100.0) / 100.0;
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }



  private void calcularTresCuotasInmediatas() {
    if (cuotas != null && !cuotas.isEmpty()) {
      BigDecimal totalTresCuotas = BigDecimal.ZERO;
      int cuotasAProcesar = Math.min(3, cuotas.size());

      for (int i = 0; i < cuotasAProcesar; i++) {
        Object[] cuota = cuotas.get(i);
        try {
          double capital = parseMoneda((String) cuota[4]);
          double interes = calcularInteresOrdinario(cuota);
          LocalDate fechaHoy = LocalDate.now();
          LocalDate fechaVencimiento = LocalDate.parse((String) cuota[3], formatter);

          double interesConBonificacion = aplicarBonificacion(interes, fechaHoy, fechaVencimiento);
          double mora = calcularMora(cuota);
          double iva = interesConBonificacion * ivaaplicar;

          // Usar BigDecimal para cálculos precisos
          BigDecimal totalCuotaIndividual = BigDecimal.valueOf(capital)
                  .add(BigDecimal.valueOf(interesConBonificacion))
                  .add(BigDecimal.valueOf(mora))
                  .add(BigDecimal.valueOf(iva))
                  .setScale(2, RoundingMode.HALF_UP);

          totalTresCuotas = totalTresCuotas.add(totalCuotaIndividual);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      txtInmediatas.setText(formatoMoneda.format(totalTresCuotas.doubleValue()));
    }
  }

  private double parseMoneda(String moneda) {
    try {
      Number numero = formatoMoneda.parse(moneda);
      return numero.doubleValue();
    } catch (ParseException e) {
      e.printStackTrace();
      return 0;
    }
  }

  @FXML
  public void pagarCredito(KeyEvent event) {

    if (txtMonto.getText().isEmpty()) {
      return;
    }

    boolean validador = false;

    switch (event.getCode()) {
      case F1:
        // Pago normal con todo y el interés
        validador = calcularPago(1);
        if (validador) {
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("PAGO EXITOSO");
          alert.setHeaderText("PAGO APARTE EL INTERES APLICADO CORRECTAMENTE");
          alert.setContentText("ABONO DE CRÉDITO AL SOCIO: " + lblNumSocio.getText() + " HECHO CORRECTAMENTE.");
          alert.showAndWait();
          Stage ventanaActual = (Stage) txtMonto.getScene().getWindow();
          ventanaActual.close();
        }
        break;
      case F2:
        // Pago normal y aparte el interés
        validador = calcularPago(2);
        if(!validador){
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("ERROR");
          alert.setHeaderText("EL INTERES ES MAYOR A LO QUE DESEA PAGAR");
          alert.setContentText("INGRESE UN VALOR QUE SALDE LOS INTERESES.");
          alert.showAndWait();

        }else {
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("PAGO EXITOSO");
          alert.setHeaderText("PAGO CON TODO Y EL INTERÉS APLICADO CORRECTAMENTE");
          alert.setContentText("ABONO DE CRÉDITO AL SOCIO: " + lblNumSocio.getText() + " HECHO CORRECTAMENTE.");
          alert.showAndWait();
          Stage ventanaActual = (Stage) txtMonto.getScene().getWindow();
          ventanaActual.close();
      }
        break;
      case F3:
        // Pago si está atrasado y no tiene interés
        calcularPago(3);
        break;
      case F4:
        // Pago si está atrasado y con todo y el interés
        calcularPago(4);
        break;
      case F5:
        // Pago si está atrasado y con todo y el interés
        calcularPago(5);
        break;
      default:
        break;
    }



  }

  private boolean calcularPago(int opcion) {
    int filaSeleccionada = 0;
    double montoPagado = Double.parseDouble(txtMonto.getText().trim());

    if (montoPagado == 0) {
      return false;
    }

    double intereses = parseMoneda(ord.getCellData(filaSeleccionada));
    double mora = parseMoneda(colmora.getCellData(filaSeleccionada));
    double iva = parseMoneda(coliva.getCellData(filaSeleccionada));
    double pagoAntesCapital = intereses + mora + iva;
    double abonoTotal = 0;
    double totalCuota = parseMoneda(tot.getCellData(filaSeleccionada));
    double bonificacion = parseMoneda(bonif.getCellData(filaSeleccionada));
    int numeroCuota = Integer.parseInt(cuota.getCellData(filaSeleccionada));
    int cuota_id = 0;

    for (Object[] resultado : cuotas) {
      cuota_id = Integer.parseInt(resultado[0].toString());
      break;
    }

    Map<String, Object> res = null;

    String empresaCod = (Integer.parseInt(numSocio) >= 8543) ? "0002" : "0001";

    LocalDateTime fecha = LocalDateTime.now();
    LocalTime hora = fecha.toLocalTime();
    DateTimeFormatter formatterHora = DateTimeFormatter.ofPattern("HH:mm:ss");
    String horaFormateada = hora.format(formatterHora);
    double interesbonif = intereses - bonificacion;
    double capital =0;
    switch (opcion) {
      case 1: {
        abonoTotal = montoPagado + pagoAntesCapital;
        res = servicio.pa_PagarCredito(1, montoPagado, intereses, mora, iva, cuota_id,
                Integer.parseInt(numCredito), totalCuota, numeroCuota, bonificacion,
                abonoTotal, Integer.parseInt(plazos), LoginController.usuarioLoggeado,
                empresaCod, horaFormateada, Integer.parseInt(numSocio), 0, "", "");
        break;
      }
      case 2: {
        if (pagoAntesCapital > montoPagado) {
          return false;
        }
        capital = montoPagado - pagoAntesCapital;
        res = servicio.pa_PagarCredito(1, capital, intereses, mora, iva, cuota_id,
                Integer.parseInt(numCredito), totalCuota, numeroCuota, bonificacion,
                montoPagado, Integer.parseInt(plazos), LoginController.usuarioLoggeado,
                empresaCod, horaFormateada, Integer.parseInt(numSocio), 0, "", "");
        abonoTotal = montoPagado;
        break;
      }
      default:
        return false;
    }

    if (res != null && res.get("Resultado").equals("CORRECTO")) {
      double psngu = 0, psmut = 0, ahorro = 0;

      String nombreEmpresa = servicio.traerEmpresa(empresaCod).getRazonSocial();
      String rfcEmpresa = servicio.traerEmpresa(empresaCod).getRfc();
      String direcEmpresa = servicio.traerEmpresa(empresaCod).getCalle() + " "
              + servicio.traerEmpresa(empresaCod).getCruzamiento() + " COL. CENTRO";

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
      String fechaTicket = fecha.format(formatter);
      String socio = lblNumSocio.getText();
      String folio = res.get("transaccion_id").toString();
      String capenviar = formatoMoneda.format(capital);
      String ivaenviar = formatoMoneda.format(iva);
      String moraenviar = formatoMoneda.format(mora);
      String interesenviar = formatoMoneda.format(intereses);
      String bonifenviar = formatoMoneda.format(bonificacion);
      String interesbonfienviar = formatoMoneda.format(interesbonif);
      String ahorroenviar = formatoMoneda.format(ahorro);
      String psnguenviar = formatoMoneda.format(psngu);
      String psmutenviar = formatoMoneda.format(psmut);
      String saldoenviar = res.get("saldo_ticket").toString();


      NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
      String totalCuotaEnviar = formatoMoneda.format(abonoTotal);

      MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
      String moneyAsWords = "";
      if(opcion == 1 ){
        capenviar = formatoMoneda.format(montoPagado);
        moneyAsWords = converter.asWords(BigDecimal.valueOf(montoPagado)).toUpperCase() + " MXN";


      }else if(opcion==2){
        capenviar = formatoMoneda.format(capital);
        moneyAsWords = converter.asWords(BigDecimal.valueOf(capital)).toUpperCase() + " MXN";
      }


      PrintJob impresion = new PrintJob();
      PrinterMatrix printer = impresion.imprimirAbonoACredito(nombreEmpresa,rfcEmpresa,direcEmpresa,numSocio,folio,nomSocio,
      fechaTicket, horaFormateada, LoginController.usuarioLoggeado, capenviar,moneyAsWords,totalCuotaEnviar, codigoSistema,
              moraenviar, interesenviar ,bonifenviar, ivaenviar, ahorroenviar, psnguenviar, psmutenviar, saldoenviar,
              interesbonfienviar);
      printer.toFile("impresion_Abono_Credito.txt");

      try (InputStream inputStream = new FileInputStream("impresion_Abono_Credito.txt")) {
        DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
        Doc document = new SimpleDoc(inputStream, docFormat, null);
        PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();
        PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();

        if (defaultPrintService != null) {
          DocPrintJob printJob = defaultPrintService.createPrintJob();
          try {
            printJob.print(document, attributeSet);
          } catch (PrintException b) {
            Alert alert2 = new Alert(Alert.AlertType.ERROR);
            alert2.setTitle("ERROR IMPRIMIENDO");
            alert2.setHeaderText("ERROR IMPRIMIENDO");
            alert2.setContentText(b.getMessage());
            alert2.showAndWait();
          }
          return true;
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return false;
  }


  public void setDatos(
      String numSocio,
      String nomSocio,
      String numCredito,
      String plazos,
      String tasa,
      String mora,
      String iva,
      String tipoCredito,
      String codigoSistema,
      String fechaDesembolso,
      String capitalMonto1,
      boolean bonifAplicable) {

    this.numSocio = numSocio;
    this.nomSocio = nomSocio;
    this.numCredito = numCredito;
    this.plazos = plazos;
    this.tasa = tasa;
    this.mora = mora;
    this.iva = iva;
    this.tipoCredito = tipoCredito;
    this.codigoSistema = codigoSistema;
    this.fechaDesembolso = fechaDesembolso;
    this.capitalInicial = Double.parseDouble(capitalMonto1);
    this.bonifAplicable = bonifAplicable;
    this.tasaInteres = Double.parseDouble(tasa);
    this.ivaaplicar = Double.parseDouble(iva) / 100;
    this.moraaplicar = Double.parseDouble(mora) / 100;

    lblNombre.setText(nomSocio);
    lblNumSocio.setText(numSocio);
    lblCredito.setText(numCredito);
    lblPlazo.setText("Plazo: " + plazos + " meses");
    lblTasa.setText("Tasa Ordinaria: " + tasa + "%");
    lblMora.setText("Tasa Moratoria: " + mora + "%");
    lblTipo.setText("Tipo: " + tipoCredito);
    lblCodigo.setText("Código Sistema: " + codigoSistema);

    cuotas = servicio.traerCuotasxCredito(Integer.parseInt(numCredito), 2);
    ObservableList<Object[]> datosCuotas = FXCollections.observableArrayList();
    NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.US);
    int indice = 0;
    for (Object[] resultado : cuotas) {
      if (indice == 0) {
        int numeroCuota = Integer.parseInt(resultado[2].toString());
        if (numeroCuota == 1) {
          if (resultado[12] != null) {
            txtSaldoCredito.setText(resultado[10].toString());
          } else {
            txtSaldoCredito.setText(formato.format(Double.parseDouble(capitalMonto1)));
          }

        } else {

          if (resultado[12] != null) {
            txtSaldoCredito.setText(resultado[10].toString());
          } else {
            Object[] ultimaCuotaPagada =
                    servicio.traerUltimaCuotaPagada(Integer.parseInt(numCredito), 0);
            if (ultimaCuotaPagada.length != 0) {
              if (ultimaCuotaPagada[0] instanceof Object[]) {
                Object[] filaCuota = (Object[]) ultimaCuotaPagada[0];
                txtSaldoCredito.setText(filaCuota[10].toString());
              } else {
                txtSaldoCredito.setText(ultimaCuotaPagada[10].toString());
              }
            }
          }



        }
        indice++;
      }
      datosCuotas.add(resultado);
    }
    tablaCuotas.setItems(datosCuotas);
    calcularTresCuotasInmediatas();
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
      Stage ventanaActual = (Stage) txtMonto.getScene().getWindow();
      ventanaActual.close();
    }
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.CONTROL)) {
      mostrarDialogoCierre();
    }
  }

  @FXML
  public void cerrarConBoton() {
    mostrarDialogoCierre();
  }

  private void mostrarDialogoCierre() {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("CIERRE DE VENTANA");
    alert.setHeaderText("¿ESTÁ SEGURO QUE DESEA CERRAR LA VENTANA?");
    alert.setContentText(
        "EN CASO DE QUE SÍ, PRESIONE ACEPTAR, EN CASO CONTRARIO PRESIONE CANCELAR"
            + ". LOS CAMBIOS NO PROCESADOS NO SE GUARDARÁN.");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      Stage ventanaActual = (Stage) txtMonto.getScene().getWindow();
      ventanaActual.close();
    }
  }
}
