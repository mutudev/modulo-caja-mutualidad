package com.mutu.modulo_caja.Controllers;

import com.mutu.modulo_caja.Services.Servicio;
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

import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

@Component
public class CreditoController implements Initializable {

    public final Validator validator = new Validator();

    @FXML private TextField txtMonto, txtInmediatas;

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
    double capitalTresInmediatas = 0;
    int contadorTotales = 0;

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
                            if (texto == null || texto.equals("") || Integer.parseInt(texto) == 0) {
                                c.error("Ingrese un valor mayor a cero");
                                lblError.setText("Ingrese un valor mayor a cero");
                            } else {
                                lblError.setText("");
                            }
                        })
                .decorates(txtMonto)
                .immediate();

        txtMonto.setTextFormatter(
                new TextFormatter<>(
                        change -> {
                            change.setText(change.getText().replaceAll("[^0-9]", ""));
                            return change;
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

        capital.setCellValueFactory(
                cellData -> new SimpleStringProperty((String) cellData.getValue()[4]));

        ord.setCellValueFactory(cellData -> {
            double interesOrdinario = calcularInteresOrdinario(cellData.getValue());
            LocalDate fechaHoy = LocalDate.now();
            LocalDate fechaVencimiento = LocalDate.parse((String) cellData.getValue()[3], formatter);

            double interesFinal = aplicarBonificacion(interesOrdinario, fechaHoy, fechaVencimiento);
            interesBuffer = interesFinal;
            return new SimpleStringProperty(formatoMoneda.format(interesFinal));
        });

        colmora.setCellValueFactory(cellData -> {
            double moraaplicada = calcularMora(cellData.getValue());
            return new SimpleStringProperty(formatoMoneda.format(moraaplicada));
        });

        coliva.setCellValueFactory(cellData -> {
            double ivaaplicada = calcularIva(cellData.getValue());
            return new SimpleStringProperty(formatoMoneda.format(ivaaplicada));
        });

        bonif.setCellValueFactory(cellData -> {
            double bonifAplicada = calcularBonificacion(cellData.getValue());
            return new SimpleStringProperty(formatoMoneda.format(bonifAplicada));
        });

        tot.setCellValueFactory(cellData -> {
            double total = calcularTotal(cellData.getValue());
            return new SimpleStringProperty(formatoMoneda.format(total));
        });


    }

    private double calcularInteresOrdinario(Object[] cuota) {
        try {
            int numeroCuota = Integer.parseInt(String.valueOf(cuota[2]));
            LocalDate fechaVencimiento = LocalDate.parse((String) cuota[3], formatter);
            LocalDate fechaHoy = LocalDate.now();
            LocalDate fechaDesembolsoDate = LocalDate.parse(fechaDesembolso, formatter);

            // Obtener el saldo del crédito para esta cuota (columna 10)
            double saldoCredito = parseMoneda((String) cuota[10]);

            if (numeroCuota == 1) {
                // Primera cuota: desde desembolso hasta hoy (calcular por días)
                long dias = ChronoUnit.DAYS.between(fechaDesembolsoDate, fechaHoy);
                return calcularInteresPorDias(capitalInicial, dias);
            } else {
                // Para cuotas siguientes: verificar si hay cuotas pagadas
                Object[] ultimaCuotaPagada = servicio.traerUltimaCuotaPagada(Integer.parseInt(numCredito), 0);

                if (ultimaCuotaPagada.length == 0) {
                    // No hay cuotas pagadas
                    if (fechaHoy.isBefore(fechaVencimiento)) {
                        // Cuota futura no vencida: interés = 0 (solo capital)
                        return 0;
                    } else {
                        // Cuota vencida sin pagos previos: calcular interés por días vencidos
                        long diasVencidos = ChronoUnit.DAYS.between(fechaVencimiento, fechaHoy);
                        return calcularInteresPorDias(saldoCredito, diasVencidos);
                    }
                } else {
                    // Hay cuotas pagadas: calcular desde última cuota pagada hasta hoy
                    String fechaUltimaCuota = "";
                    double saldoUltimaCuota = 0;

                    // Manejar si ultimaCuotaPagada es un array de arrays o un array simple
                    if (ultimaCuotaPagada[0] instanceof Object[]) {
                        Object[] filaCuota = (Object[]) ultimaCuotaPagada[0];
                        fechaUltimaCuota = filaCuota[3].toString();
                        saldoUltimaCuota = parseMoneda(filaCuota[10].toString());
                    } else {
                        fechaUltimaCuota = ultimaCuotaPagada[3].toString();
                        saldoUltimaCuota = parseMoneda(ultimaCuotaPagada[10].toString());
                    }

                    LocalDate fechaUltimaCuotaDate = LocalDate.parse(fechaUltimaCuota, formatter);
                    long dias = ChronoUnit.DAYS.between(fechaUltimaCuotaDate, fechaHoy);
                    return calcularInteresPorDias(saldoUltimaCuota, dias);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private LocalDate obtenerFechaCuotaAnterior(int numeroCuota) {
        for (Object[] cuota : cuotas) {
            if (Integer.parseInt(String.valueOf(cuota[2])) == numeroCuota - 1) {
                return LocalDate.parse((String) cuota[3], formatter);
            }
        }
        return LocalDate.parse(fechaDesembolso, formatter);
    }

    private double calcularInteresPorDias(double saldoCredito, long dias) {
        return (((tasaInteres / 100)*12)/360) * dias * saldoCredito;
    }

    private double aplicarBonificacion(double interesOrdinario, LocalDate fechaHoy, LocalDate fechaVencimiento) {
        if (fechaHoy.isBefore(fechaVencimiento)) {
            double factorBonificacion = (tasaInteres == 1.1) ? 0.7 : 0.9;
            return interesOrdinario * factorBonificacion;
        } else if (fechaHoy.isEqual(fechaVencimiento)) {
            // Pago puntual
            double factorBonificacion = (tasaInteres == 1.1) ? 0.8 : 0.99;
            return interesOrdinario * factorBonificacion;
        } else {
            // Pago tardío
            return interesOrdinario;
        }
    }

    private double calcularMora(Object[] cuota) {
        try {
            LocalDate fechaHoy = LocalDate.now();
            LocalDate fechaVencimiento = LocalDate.parse((String) cuota[3], formatter);

            if (fechaHoy.isAfter(fechaVencimiento)) {
                // Calcular días de mora
                long diasMora = ChronoUnit.DAYS.between(fechaVencimiento, fechaHoy);

                // Obtener el interés ordinario calculado
                double interesOrdinario = calcularInteresOrdinario(cuota);
                // Mora = (tasa/100 * 12 / 360) * días de mora * interés ordinario
                System.out.println(moraaplicar);
                return (((moraaplicar) * 12) / 360) * diasMora * interesOrdinario;
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
            return interesOrdinario * ivaaplicar;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private double calcularBonificacion(Object[] cuota) {
        try {
            LocalDate fechaHoy = LocalDate.now();
            LocalDate fechaVencimiento = LocalDate.parse((String) cuota[3], formatter);

            double interesOrdinario = calcularInteresOrdinario(cuota);
            double interesConBonificacion = aplicarBonificacion(interesOrdinario, fechaHoy, fechaVencimiento);

            return interesOrdinario - interesConBonificacion;
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
            capitalTresInmediatas += (capital + interesConBonificacion + mora + iva);
            contadorTotales++;
            if (contadorTotales == 3) {
                txtInmediatas.setText(formatoMoneda.format(capitalTresInmediatas));
            }
            return capital + interesConBonificacion + mora + iva;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
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
            String capitalMonto1) {

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

        // Convertir valores a double
        this.tasaInteres = Double.parseDouble(tasa);
        this.ivaaplicar = Double.parseDouble(iva) / 100; // Convertir a decimal
        this.moraaplicar = Double.parseDouble(mora) / 100; // Convertir a decimal

        // Actualizar labels
        lblNombre.setText(nomSocio);
        lblNumSocio.setText(numSocio);
        lblCredito.setText(numCredito);
        lblPlazo.setText("Plazo: " + plazos + " meses");
        lblTasa.setText("Tasa Ordinaria: " + tasa + "%");
        lblMora.setText("Tasa Moratoria: " + mora + "%");
        lblTipo.setText("Tipo: " + tipoCredito);
        lblCodigo.setText("Código Sistema: " + codigoSistema);

        // Cargar cuotas
        cuotas = servicio.traerCuotasxCredito(Integer.parseInt(numCredito), 2);
        ObservableList<Object[]> datosCuotas = FXCollections.observableArrayList();
        for (Object[] resultado : cuotas) {
            datosCuotas.add(resultado);
        }
        tablaCuotas.setItems(datosCuotas);
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