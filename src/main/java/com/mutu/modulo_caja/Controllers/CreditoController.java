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
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.FileInputStream;
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
      lblCodigo,
      lblInmediatas;

  @FXML private TableView<Object[]> tablaCuotas;

  @Autowired public Servicio servicio;

  @FXML
  private TableColumn<Object[], String> cuota, fecha, colcap, ord, colmora, coliva, bonif, tot;


  // Variables de datos básicos
  String numSocio = "";
  String nomSocio = "";
  String numCredito = "";
  String plazos = "";
  String tasa = "";
  String mora = "";
  String iva = "";
  String tipoCredito = "";
  String codigoSistema = "";
  String fechaDesembolso = "";
  String turno = "";

  // Variables de cálculo
  double tasaInteres = 0;
  double ivaaplicar = 0;
  double moraaplicar = 0;
  double capitalInicial = 0;
  double interesBuffer = 0;
  boolean bonifAplicable;
  int cuotasAprocesar = 0;
  BigDecimal totalCuotas = BigDecimal.valueOf(0);
  BigDecimal devolver = BigDecimal.valueOf(0);

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

    fecha.setCellFactory(
        column ->
            new TableCell<Object[], String>() {
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
                  String fechaFormateada =
                      itemDividido[2] + "-" + itemDividido[1] + "-" + itemDividido[0];
                  LocalDate fechaTabla = LocalDate.parse(fechaFormateada, formatter);
                  long diasDiferencia = ChronoUnit.DAYS.between(fechaTabla, fechaHoy);
                  if (diasDiferencia > 90) {
                    // Más de 90 días, está vencida
                    setStyle("-fx-background-color: yellow;");
                  } else if (diasDiferencia > 0) {
                    // Está atrasada, pero con menos de 90 días
                    setStyle("-fx-background-color: lightgreen;");
                  } else {
                    // Todavía no vence o es hoy
                    setStyle("");
                  }
                }
              }
            });

    cuota.setCellFactory(
        column ->
            new TableCell<Object[], String>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty
                    || item == null
                    || getTableRow() == null
                    || getTableRow().getItem() == null) {
                  setText(null);
                  setStyle("");
                } else {
                  setText(item);
                  Object[] rowData = (Object[]) getTableRow().getItem();
                  LocalDate fechaHoy = LocalDate.now();
                  LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
                  long diasDiferencia = ChronoUnit.DAYS.between(fechaVencimiento, fechaHoy);
                  if (diasDiferencia > 90) {
                    // Más de 90 días, está vencida
                    setStyle("-fx-background-color: yellow;");
                  } else if (diasDiferencia > 0) {
                    // Está atrasada, pero con menos de 90 días
                    setStyle("-fx-background-color: lightgreen;");
                  } else {
                    // Todavía no vence o es hoy
                    setStyle("");
                  }
                }
              }
            });

    colcap.setCellValueFactory(
        cellData -> new SimpleStringProperty((String) cellData.getValue()[4]));

    colcap.setCellFactory(
        column ->
            new TableCell<Object[], String>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty
                    || item == null
                    || getTableRow() == null
                    || getTableRow().getItem() == null) {
                  setText(null);
                  setStyle("");
                } else {
                  setText(item);
                  Object[] rowData = (Object[]) getTableRow().getItem();
                  LocalDate fechaHoy = LocalDate.now();
                  LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
                  long diasDiferencia = ChronoUnit.DAYS.between(fechaVencimiento, fechaHoy);
                  if (diasDiferencia > 90) {
                    // Más de 90 días, está vencida
                    setStyle("-fx-background-color: yellow;");
                  } else if (diasDiferencia > 0) {
                    // Está atrasada, pero con menos de 90 días
                    setStyle("-fx-background-color: lightgreen;");
                  } else {
                    // Todavía no vence o es hoy
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

    ord.setCellFactory(
        column ->
            new TableCell<Object[], String>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty
                    || item == null
                    || getTableRow() == null
                    || getTableRow().getItem() == null) {
                  setText(null);
                  setStyle("");
                } else {
                  setText(item);
                  Object[] rowData = (Object[]) getTableRow().getItem();
                  LocalDate fechaHoy = LocalDate.now();
                  LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
                  long diasDiferencia = ChronoUnit.DAYS.between(fechaVencimiento, fechaHoy);
                  if (diasDiferencia > 90) {
                    // Más de 90 días, está vencida
                    setStyle("-fx-background-color: yellow;");
                  } else if (diasDiferencia > 0) {
                    // Está atrasada, pero con menos de 90 días
                    setStyle("-fx-background-color: lightgreen;");
                  } else {
                    // Todavía no vence o es hoy
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

    colmora.setCellFactory(
        column ->
            new TableCell<Object[], String>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty
                    || item == null
                    || getTableRow() == null
                    || getTableRow().getItem() == null) {
                  setText(null);
                  setStyle("");
                } else {
                  setText(item);
                  Object[] rowData = (Object[]) getTableRow().getItem();
                  LocalDate fechaHoy = LocalDate.now();
                  LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
                  long diasDiferencia = ChronoUnit.DAYS.between(fechaVencimiento, fechaHoy);
                  if (diasDiferencia > 90) {
                    // Más de 90 días, está vencida
                    setStyle("-fx-background-color: yellow;");
                  } else if (diasDiferencia > 0) {
                    // Está atrasada, pero con menos de 90 días
                    setStyle("-fx-background-color: lightgreen;");
                  } else {
                    // Todavía no vence o es hoy
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

    coliva.setCellFactory(
        column ->
            new TableCell<Object[], String>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty
                    || item == null
                    || getTableRow() == null
                    || getTableRow().getItem() == null) {
                  setText(null);
                  setStyle("");
                } else {
                  setText(item);
                  Object[] rowData = (Object[]) getTableRow().getItem();
                  LocalDate fechaHoy = LocalDate.now();
                  LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
                  long diasDiferencia = ChronoUnit.DAYS.between(fechaVencimiento, fechaHoy);
                  if (diasDiferencia > 90) {
                    // Más de 90 días, está vencida
                    setStyle("-fx-background-color: yellow;");
                  } else if (diasDiferencia > 0) {
                    // Está atrasada, pero con menos de 90 días
                    setStyle("-fx-background-color: lightgreen;");
                  } else {
                    // Todavía no vence o es hoy
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

    bonif.setCellFactory(
        column ->
            new TableCell<Object[], String>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty
                    || item == null
                    || getTableRow() == null
                    || getTableRow().getItem() == null) {
                  setText(null);
                  setStyle("");
                } else {
                  setText(item);
                  Object[] rowData = (Object[]) getTableRow().getItem();
                  LocalDate fechaHoy = LocalDate.now();
                  LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
                  long diasDiferencia = ChronoUnit.DAYS.between(fechaVencimiento, fechaHoy);
                  if (diasDiferencia > 90) {
                    // Más de 90 días, está vencida
                    setStyle("-fx-background-color: yellow;");
                  } else if (diasDiferencia > 0) {
                    // Está atrasada, pero con menos de 90 días
                    setStyle("-fx-background-color: lightgreen;");
                  } else {
                    // Todavía no vence o es hoy
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

    tot.setCellFactory(
        column ->
            new TableCell<Object[], String>() {
              @Override
              protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty
                    || item == null
                    || getTableRow() == null
                    || getTableRow().getItem() == null) {
                  setText(null);
                  setStyle("");
                } else {
                  setText(item);
                  Object[] rowData = (Object[]) getTableRow().getItem();
                  LocalDate fechaHoy = LocalDate.now();
                  LocalDate fechaVencimiento = LocalDate.parse((String) rowData[3], formatter);
                  long diasDiferencia = ChronoUnit.DAYS.between(fechaVencimiento, fechaHoy);
                  if (diasDiferencia > 90) {
                    // Más de 90 días, está vencida
                    setStyle("-fx-background-color: yellow;");
                  } else if (diasDiferencia > 0) {
                    // Está atrasada, pero con menos de 90 días
                    setStyle("-fx-background-color: lightgreen;");
                  } else {
                    // Todavía no vence o es hoy
                    setStyle("");
                  }
                }
              }
            });
  }

  private double calcularInteresOrdinario(Object[] cuota) {
    int numCuotaActual = Integer.parseInt(String.valueOf(cuota[2]));
    LocalDate fechaVencimiento = LocalDate.parse((String) cuota[3], formatter);
    LocalDate fechaHoy = LocalDate.now();
    LocalDate fechaDesembolsoDate = LocalDate.parse(fechaDesembolso, formatter);
    long dias = 0;
    double interesAcumulado = 0, capital = 0;
    LocalDate fechaPagoDate = null;
    Object[] cuotaAnterior = null;
    Object[] cuotaReferencia = null;
    boolean validador = false;

    // Primer caso, no he pagado nada de ninguna
    if (numCuotaActual != 1) {
      cuotaAnterior =
          servicio.traerUltimaCuotaConNum(Integer.parseInt(numCredito), 2, (numCuotaActual - 1));
      cuotaReferencia = servicio.traerUltimaCuotaConNum(Integer.parseInt(numCredito), 2, 1);

      if (cuotaAnterior.length != 0) {
        if (cuotaAnterior[0] instanceof Object[]) {
          Object[] filaCuota = (Object[]) cuotaAnterior[0];
          fechaPagoDate = LocalDate.parse((String) filaCuota[3], formatter);
          if (filaCuota[12] != null) {
            LocalDate FechaPague = LocalDate.parse(filaCuota[12].toString(), formatter);
            capital = parseMoneda(filaCuota[10].toString());
            dias = ChronoUnit.DAYS.between(FechaPague, fechaHoy);
            interesAcumulado = Double.parseDouble(cuota[15].toString());
            validador = true;
          } else if (cuotaReferencia.length == 0 && filaCuota[12] == null) {
            LocalDate FechaPague = null;
            cuotaReferencia = servicio.traerUltimaCuotaPagada(Integer.parseInt(numCredito), 0);
            if (cuotaReferencia[0] instanceof Object[]) {
              Object[] filaCuotaReferencia = (Object[]) cuotaReferencia[0];
              FechaPague = LocalDate.parse(filaCuotaReferencia[12].toString(), formatter);
              capital = parseMoneda(filaCuotaReferencia[10].toString());
            }
            dias = ChronoUnit.DAYS.between(FechaPague, fechaHoy);
            interesAcumulado = Double.parseDouble(cuota[15].toString());
            validador = true;
          } else {
            capital = capitalInicial;
            dias = ChronoUnit.DAYS.between(fechaPagoDate, fechaHoy);
          }
        }

        double factordiario = (((tasaInteres / 100) * 12) / 360) * capital;
        // Si está en el rango del mes de la cuota calcula, sino, no calcules y retorna 0 (NO TOCAR
        // YA LISTO)
        if (fechaHoy.isAfter(fechaPagoDate)) {
          if (validador) {
            devolver = BigDecimal.valueOf(interesAcumulado + (dias * factordiario)).setScale(2, RoundingMode.HALF_UP);

            return (devolver.doubleValue());
          }
          devolver = BigDecimal.valueOf(interesAcumulado + (dias * factordiario)).setScale(2, RoundingMode.HALF_UP);
          return devolver.doubleValue() ;
        } else {
          return 0;
        }

      } else {
        // Cuando ya pagué la anterior a la inmediata empezando desde la 2
        // Checar si existe una con estatus 0
        cuotaAnterior =
            servicio.traerUltimaCuotaConNum(Integer.parseInt(numCredito), 0, (numCuotaActual - 1));

        if (cuotaAnterior.length != 0) {
          if (cuotaAnterior[0] instanceof Object[]) {
            Object[] filaCuota = (Object[]) cuotaAnterior[0];
            capital = parseMoneda(filaCuota[10].toString());
            fechaPagoDate = LocalDate.parse(filaCuota[12].toString(), formatter);
          }
        }

        if (fechaPagoDate == null) {
          cuotaAnterior = servicio.traerUltimaCuotaPagada(Integer.parseInt(numCredito), 0);
          if (cuotaAnterior.length != 0) {
            if (cuota[12] == null) {
              if (cuotaAnterior[0] instanceof Object[]) {
                Object[] filaCuota = (Object[]) cuotaAnterior[0];
                capital = parseMoneda(filaCuota[10].toString());
                fechaPagoDate = LocalDate.parse(filaCuota[12].toString(), formatter);
              }
              interesAcumulado = Double.parseDouble(cuota[15].toString());
              dias = ChronoUnit.DAYS.between(fechaPagoDate, fechaHoy);
              double factordiario = (((tasaInteres / 100) * 12) / 360) * capital;
              devolver = BigDecimal.valueOf((interesAcumulado + (dias * factordiario))).setScale(2, RoundingMode.HALF_UP);
              return devolver.doubleValue();
            }
          }
        }

        interesAcumulado = Double.parseDouble(cuota[15].toString());
        dias = ChronoUnit.DAYS.between(fechaPagoDate, fechaHoy);
        double factordiario = (((tasaInteres / 100) * 12) / 360) * capital;
        devolver = BigDecimal.valueOf((interesAcumulado + (dias * factordiario))).setScale(2, RoundingMode.HALF_UP);
        return devolver.doubleValue();
      }
    } else {
      if (cuota[12] != null) {
        fechaPagoDate = LocalDate.parse(cuota[12].toString(), formatter);
        // Tengo intereses acumulados de pagos anteriores
        if (Boolean.parseBoolean(cuota[14].toString())) {
          interesAcumulado = Double.parseDouble(cuota[15].toString());
          capital = parseMoneda(cuota[10].toString());
          dias = ChronoUnit.DAYS.between(fechaPagoDate, fechaHoy);
          devolver = BigDecimal.valueOf(calcularInteresPorDias(capital, dias) + interesAcumulado).setScale(2, RoundingMode.HALF_UP);
          return devolver.doubleValue();
        } else {
          capital = parseMoneda(cuota[10].toString());
          dias = ChronoUnit.DAYS.between(fechaPagoDate, fechaHoy);
          devolver = BigDecimal.valueOf(calcularInteresPorDias(capital, dias)).setScale(2, RoundingMode.HALF_UP);
          return devolver.doubleValue();
        }
      } else {
        dias = ChronoUnit.DAYS.between(fechaDesembolsoDate, fechaHoy);
        devolver = BigDecimal.valueOf(calcularInteresPorDias(capitalInicial, dias)).setScale(2, RoundingMode.HALF_UP);
        return devolver.doubleValue();
      }
    }
  }

  private double calcularInteresPorDias(double saldoCredito, long dias) {
    devolver = BigDecimal.valueOf((((tasaInteres / 100) * 12) / 360) * dias * saldoCredito).setScale(2, RoundingMode.HALF_UP);
    return devolver.doubleValue();
  }

  private double aplicarBonificacion(
      double interesOrdinario, LocalDate fechaHoy, LocalDate fechaVencimiento) {

    if (!bonifAplicable) {
      return interesOrdinario;
    }

    double resultado;
    if (fechaHoy.isBefore(fechaVencimiento)) {
      double factorBonificacion = (tasaInteres == 1.1) ? 0.7 : 0.9;
      devolver = BigDecimal.valueOf((interesOrdinario * factorBonificacion)).setScale(2, RoundingMode.HALF_UP);
      resultado = devolver.doubleValue();
    } else if (fechaHoy.isEqual(fechaVencimiento)) {
      double factorBonificacion = (tasaInteres == 1.1) ? 0.8 : 0.99;
      devolver = BigDecimal.valueOf((interesOrdinario * factorBonificacion)).setScale(2, RoundingMode.HALF_UP);
      resultado = devolver.doubleValue();
    } else {
      resultado = interesOrdinario;
    }

    return resultado;
  }

  private double calcularMora(Object[] cuota) {
    try {
      LocalDate fechaHoy = LocalDate.now();
      LocalDate fechaVencimiento = LocalDate.parse((String) cuota[3], formatter);

      String fechaPagoActualizado = "";

      if (cuota[12] != null) {
        fechaPagoActualizado = cuota[12].toString();
      }

      if (fechaPagoActualizado == "") {
        if (fechaHoy.isAfter(fechaVencimiento.plusDays(29))) {
          long diasMora = ChronoUnit.DAYS.between(fechaVencimiento, fechaHoy);
          double interesOrdinario = calcularInteresOrdinario(cuota);
          devolver = BigDecimal.valueOf((((moraaplicar) * 12) / 360) * diasMora * interesOrdinario).setScale(2, RoundingMode.HALF_UP);
          return devolver.doubleValue();

        }
      } else {
        LocalDate nuevaFechaPago = LocalDate.parse(fechaPagoActualizado, formatter);
        if (fechaHoy.isAfter(nuevaFechaPago.plusDays(29))) {
          long diasMora = ChronoUnit.DAYS.between(nuevaFechaPago, fechaHoy);
          double interesOrdinario = calcularInteresOrdinario(cuota);
          devolver = BigDecimal.valueOf((((moraaplicar) * 12) / 360) * diasMora * interesOrdinario).setScale(2, RoundingMode.HALF_UP);

          return devolver.doubleValue();
        }
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
      double bonif = calcularBonificacion(cuota);
      interesOrdinario -= bonif;
      double ivaCubierto = 0;
      boolean existenActuales = false;
      if (Boolean.parseBoolean(String.valueOf(cuota[14]))) {
        existenActuales = true;
      }

      if (existenActuales) {

        ivaCubierto = Double.parseDouble(String.valueOf(cuota[17]));
      }
      devolver = BigDecimal.valueOf((interesOrdinario * ivaaplicar) -ivaCubierto ).setScale(2, RoundingMode.HALF_UP);

      return devolver.doubleValue();
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
      devolver = BigDecimal.valueOf(interesOrdinario - interesConBonificacion).setScale(2, RoundingMode.HALF_UP);
      if (devolver.doubleValue() < 0) {
        return 0;
      }

      return devolver.doubleValue();
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  private double calcularTotal(Object[] cuota) {
    try {
      double capital = parseMoneda((String) cuota[4]);
      double interes = calcularInteresOrdinario(cuota);
      System.out.println("Interes: " + interes);
      System.out.println("Capital: " + capital);

      LocalDate fechaHoy = LocalDate.now();
      LocalDate fechaVencimiento = LocalDate.parse((String) cuota[3], formatter);

      double interesConBonificacion = aplicarBonificacion(interes, fechaHoy, fechaVencimiento);
      System.out.println("Int bonif: " + interesConBonificacion);

      double mora = calcularMora(cuota);
      System.out.println("Mora: " + mora);

      double iva  = BigDecimal.valueOf(interesConBonificacion * ivaaplicar).setScale(2, RoundingMode.HALF_UP).doubleValue();
      System.out.println("iva: " + iva);


      devolver = BigDecimal.valueOf(capital + interesConBonificacion + mora + iva).setScale(2, RoundingMode.HALF_UP);

      System.out.println("Resultado: " + devolver.doubleValue());

      return devolver.doubleValue();
    } catch (Exception e) {
      e.printStackTrace();
      return 0;
    }
  }

  private void calcularTresCuotasInmediatas() {
    if (cuotas != null && !cuotas.isEmpty()) {
      BigDecimal totalTresCuotas = BigDecimal.ZERO;
      int cuotasAProcesar = Math.min(cuotasAprocesar, cuotas.size());

      for (int i = 0; i < cuotasAProcesar; i++) {
        Object[] cuota = cuotas.get(i);
        try {
          double capital = parseMoneda((String) cuota[4]);
          double interes = calcularInteresOrdinario(cuota);
          LocalDate fechaHoy = LocalDate.now();
          LocalDate fechaVencimiento = LocalDate.parse((String) cuota[3], formatter);

          double interesConBonificacion = aplicarBonificacion(interes, fechaHoy, fechaVencimiento);
          double mora = calcularMora(cuota);
          devolver = BigDecimal.valueOf(interesConBonificacion * ivaaplicar).setScale(2, RoundingMode.HALF_UP);



          // Usar BigDecimal para cálculos precisos
          BigDecimal totalCuotaIndividual =
              BigDecimal.valueOf(capital)
                  .add(BigDecimal.valueOf(interesConBonificacion))
                  .add(BigDecimal.valueOf(mora))
                  .add(BigDecimal.valueOf(devolver.doubleValue()))
                  .setScale(2, RoundingMode.HALF_UP);

          totalTresCuotas = totalTresCuotas.add(totalCuotaIndividual);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }

      txtInmediatas.setText(formatoMoneda.format(totalTresCuotas.doubleValue()));
    }
  }

//AQUI ME QUEDE


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

    if (event.getCode().equals(KeyCode.ESCAPE)) {
      mostrarDialogoCierre();
    }

    if ((event.getCode() == KeyCode.F1 || event.getCode() == KeyCode.F2)
        && txtMonto.getText().isEmpty()) {

      return;
    }
    boolean validador = false;

    switch (event.getCode()) {
      case F1:
        // Pago normal y aparte el interés
        validador = calcularPago(1);
        if (validador) {
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("PAGO EXITOSO");
          alert.setHeaderText("PAGO APARTE EL INTERES APLICADO CORRECTAMENTE");
          alert.setContentText(
              "ABONO DE CRÉDITO AL SOCIO: " + lblNumSocio.getText() + " HECHO CORRECTAMENTE.");
          alert.showAndWait();
          Stage ventanaActual = (Stage) txtMonto.getScene().getWindow();
          ventanaActual.close();
        }else{
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("ERROR");
          alert.setHeaderText("NO SE PUEDE PAGAR MAS DEL TOTAL DEL CREDITO");
          alert.setContentText("INGRESE UN VALOR MENOR AL TOTAL.");
          alert.showAndWait();
        }
        break;
      case F2:
        // Pago normal con todo y el interes
        validador = calcularPago(2);
        if (!validador) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("ERROR");
          alert.setHeaderText("NO SE PUEDE PAGAR MAS DEL TOTAL DEL CREDITO");
          alert.setContentText("INGRESE UN VALOR MENOR AL TOTAL.");
          alert.showAndWait();
        }else{
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("PAGO EXITOSO");
          alert.setHeaderText("PAGO CON TODO Y EL INTERES APLICADO CORRECTAMENTE");
          alert.setContentText(
                  "ABONO DE CRÉDITO AL SOCIO: " + lblNumSocio.getText() + " HECHO CORRECTAMENTE.");
          alert.showAndWait();
          Stage ventanaActual = (Stage) txtMonto.getScene().getWindow();
          ventanaActual.close();
        }
        break;
      case F9: // Para saldar toda la cuota inmediata
        int filaSeleccionada = 0;
        txtMonto.setText(String.valueOf(parseMoneda(tot.getCellData(filaSeleccionada))));
        break;
      case F10:
        txtMonto.setText(String.valueOf(parseMoneda(txtInmediatas.getText().trim())));
        break;
    }
  }

  private boolean calcularPago(int opcion) {
    int filaSeleccionada = 0;
    double montoPagado = Double.parseDouble(txtMonto.getText().trim());

    if (montoPagado == 0) {
      return false;
    }else if(montoPagado > parseMoneda(txtSaldoCredito.getText())){
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

    double capital = 0;
    double capcuota = parseMoneda(colcap.getCellData(filaSeleccionada));
    switch (opcion) {

      case 1:
        {
          abonoTotal = montoPagado + pagoAntesCapital;

          if (capcuota < montoPagado) {
            montoPagado = capcuota;
          }
          if(abonoTotal > totalCuotas.doubleValue()){

            return false;
          }
          res =
              servicio.pa_PagarCredito(
                  1,
                  montoPagado,
                  intereses,
                  mora,
                  iva,
                  cuota_id,
                  Integer.parseInt(numCredito),
                  totalCuota,
                  numeroCuota,
                  bonificacion,
                  abonoTotal,
                  Integer.parseInt(plazos),
                  LoginController.usuarioLoggeado,
                  empresaCod,
                  horaFormateada,
                  Integer.parseInt(numSocio),
                  turno,
                  0,
                  "",
                  "",
                  "",
                  "",
                  "",
                  "");
          CajeroController.bufferOperaciones += abonoTotal;
          break;
        }
      case 2:
        {
          //          capital = parseMoneda(colcap.getCellData(filaSeleccionada));


          if (totalCuota < montoPagado) {
            capital = capcuota;
          }else {
            capital = montoPagado - intereses - mora - iva;
          }
          if (capital < 0) {
            capital = 0;
          }

          if(montoPagado > totalCuotas.doubleValue()){
            return  false;
          }


          res =
              servicio.pa_PagarCredito(
                  1,
                  capital,
                  intereses,
                  mora,
                  iva,
                  cuota_id,
                  Integer.parseInt(numCredito),
                  totalCuota,
                  numeroCuota,
                  bonificacion,
                  montoPagado,
                  Integer.parseInt(plazos),
                  LoginController.usuarioLoggeado,
                  empresaCod,
                  horaFormateada,
                  Integer.parseInt(numSocio),
                  turno,
                  0,
                  "",
                  "",
                  "",
                  "",
                  "",
                  "");
          CajeroController.bufferOperaciones += montoPagado;
          break;
        }
      default:
        return false;
    }

    if (res != null && res.get("Resultado").toString().equals("CORRECTO")) {

      double psngu = 0, psmut = 0;
      String capenviar = "", ivaenviar = "", moraenviar = "", interesenviar = "";
      String nombreEmpresa = servicio.traerEmpresa(empresaCod).getRazonSocial();
      String rfcEmpresa = servicio.traerEmpresa(empresaCod).getRfc();
      String direcEmpresa =
          servicio.traerEmpresa(empresaCod).getCalle()
              + " "
              + servicio.traerEmpresa(empresaCod).getCruzamiento()
              + " COL. CENTRO";

      if (servicio.traerCuentasCS(Integer.parseInt(numSocio)).size() == 1) {
        psmut = servicio.traerCuentasCS(Integer.parseInt(numSocio)).getFirst().getMonto_cubierto();
      } else {
        psmut = servicio.traerCuentasCS(Integer.parseInt(numSocio)).getFirst().getMonto_cubierto();
        psngu = servicio.traerCuentasCS(Integer.parseInt(numSocio)).get(1).getMonto_cubierto();
      }

      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
      String fechaTicket = fecha.format(formatter);
      String socio = lblNumSocio.getText();
      String folio = res.get("transaccion_id").toString();

      if (res.get("capital_devueltos").toString().equals("")) {
        capenviar = formatoMoneda.format(capital);

      } else {
        capenviar = res.get("capital_devueltos").toString();
      }
      if (res.get("iva_devueltos").toString().equals("")) {
        ivaenviar = formatoMoneda.format(iva);
      } else {
        ivaenviar = res.get("iva_devueltos").toString();
      }

      if (res.get("mora_devueltos").toString().equals("")) {
        moraenviar = formatoMoneda.format(mora);
      } else {
        moraenviar = res.get("mora_devueltos").toString();
      }

      String interessinbonfienviar = "";
      if (res.get("intereses_devueltos").toString().equals("")) {
        interesenviar = formatoMoneda.format(intereses);
      } else {
        interesenviar = res.get("intereses_devueltos").toString();
        interessinbonfienviar =
            formatoMoneda.format(
                parseMoneda(res.get("intereses_devueltos").toString()) + bonificacion);
      }

      String bonifenviar = formatoMoneda.format(bonificacion);

      String psnguenviar = formatoMoneda.format(psngu);
      String psmutenviar = formatoMoneda.format(psmut);
      String saldoenviar = res.get("saldo_ticket").toString();

      NumberFormat formatoMoneda = NumberFormat.getCurrencyInstance(Locale.US);
      String totalCuotaEnviar = formatoMoneda.format(abonoTotal);

      MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
      String moneyAsWords = "";
      if (opcion == 1) {
        capenviar = formatoMoneda.format(montoPagado);
        moneyAsWords =
            converter
                    .asWords(BigDecimal.valueOf(abonoTotal).setScale(2, RoundingMode.HALF_UP))
                    .toUpperCase()
                + " MXN"; // REDONDEAR A DOS DECIMALES

      } else if (opcion == 2) {

        moneyAsWords =
            converter
                    .asWords(BigDecimal.valueOf(abonoTotal).setScale(2, RoundingMode.HALF_UP))
                    .toUpperCase()
                + " MXN";
      }

      PrintJob impresion = new PrintJob();
      PrinterMatrix printer =
          impresion.imprimirAbonoACredito(
              nombreEmpresa,
              rfcEmpresa,
              direcEmpresa,
              numSocio,
              folio,
              nomSocio,
              fechaTicket,
              horaFormateada,
              LoginController.usuarioLoggeado,
              capenviar,
              moneyAsWords,
              totalCuotaEnviar,
              codigoSistema,
              moraenviar,
              interesenviar,
              bonifenviar,
              ivaenviar,
              psnguenviar,
              psmutenviar,
              saldoenviar,
              interessinbonfienviar);
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
      boolean bonifAplicable,
      int cuotasAprocesar,
      String turno) {

    this.numSocio = numSocio;
    this.nomSocio = nomSocio;
    this.numCredito = numCredito;
    this.plazos = plazos;
    this.tasa = tasa;
    this.cuotasAprocesar = cuotasAprocesar;
    this.mora = mora;
    this.turno = turno;
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
    lblInmediatas.setText("Total " + cuotasAprocesar + " Cuotas Inmediatas:");

    //cuotas = servicio.traerCuotasxCredito(Integer.parseInt(numCredito), 2);
    int plazo = servicio.obtenerPlazoCredito(Integer.parseInt(numCredito));
    PageRequest limit = PageRequest.of(0, plazo);
    cuotas = servicio.traerCuotasPrueba(Integer.parseInt(numCredito), 2, limit);
    ObservableList<Object[]> datosCuotas = FXCollections.observableArrayList();
    NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.US);


    //Sttear todas las cuotas por pagar primero
    for (Object[] resultado : cuotas) {
      datosCuotas.add(resultado);
    }

    tablaCuotas.setItems(datosCuotas);

    double totalDouble = 0;

    for (int i = 0; i < tablaCuotas.getItems().size(); i++) {
      totalDouble += parseMoneda(tot.getCellData(i));
    }

     totalCuotas =
            BigDecimal.valueOf(totalDouble).setScale(2, RoundingMode.HALF_UP);

    tablaCuotas.getItems().clear();

    //Faltar consultar directo desde bd
    limit = PageRequest.of(0, 3);
    cuotas = servicio.traerCuotasPrueba(Integer.parseInt(numCredito), 2, limit);


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

//    int plazoCopia = servicio.obtenerPlazoCredito(Integer.parseInt(numCredito));
//    PageRequest limitCopia = PageRequest.of(0, plazoCopia);
//    cuotas = servicio.traerCuotasPrueba(Integer.parseInt(numCredito), 2, limitCopia);
//    for (Object[] resultado : cuotas) {
//      datosCuotasCopia.add(resultado);
//    }
//
//    tablaCuotasCopia.setItems(datosCuotasCopia);
    tablaCuotas.setItems(datosCuotas);
    calcularTresCuotasInmediatas();
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
    Stage ventanaActual = (Stage) txtMonto.getScene().getWindow();
    ventanaActual.close();
  }

  @FXML
  public void cerrarConTecla(KeyEvent event) {
    if (event.getCode().equals(KeyCode.ESCAPE)) {
      mostrarDialogoCierre();
    } else if (event.getCode().equals(KeyCode.F12)) {
      System.out.println(totalCuotas);
    }
  }

  @FXML
  public void cerrarConBoton() {
    mostrarDialogoCierre();
  }

  private void mostrarDialogoCierre() {
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
    Stage ventanaActual = (Stage) txtMonto.getScene().getWindow();
    ventanaActual.close();
  }
}
