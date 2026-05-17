package com.mutu.modulo_caja.Controllers;

import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Models.*;
import com.mutu.modulo_caja.Models.DTO.PagoCuotaDTO;
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
import org.hibernate.event.spi.SaveOrUpdateEvent;
import org.jfree.data.json.impl.JSONArray;
import org.jfree.data.json.impl.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Component
public class CreditoController implements Initializable {


  @Autowired
  private Servicio servicio;

  // TABLA
  @FXML
  private TableView<PagoCuotaDTO> tablaCuotas;

  @FXML
  private TableColumn<PagoCuotaDTO, String> cuota;

  @FXML
  private TableColumn<PagoCuotaDTO, String> fecha;

  @FXML
  private TableColumn<PagoCuotaDTO, String> colcap;

  @FXML
  private TableColumn<PagoCuotaDTO, String> ord;

  @FXML
  private TableColumn<PagoCuotaDTO, String> colmora;

  @FXML
  private TableColumn<PagoCuotaDTO, String> coliva;

  @FXML
  private TableColumn<PagoCuotaDTO, String> bonif;

  @FXML
  private TableColumn<PagoCuotaDTO, String> tot;

  @FXML
  private TextField txtInmediatas, txtSaldoCredito, txtMonto;

  @FXML
  private Label lblPrimerasN, lblPlazo, lblTasa, lblMora, lblTipo, lblNombre, lblNumSocio, lblCredito;

  public int creditoId = 0;

  public ModelCredito creditoEncontrado = null;

  List<PagoCuotaDTO> copiaCuotas = null;


  private static final DateTimeFormatter FMT_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final NumberFormat FMT_MONEDA =
          NumberFormat.getCurrencyInstance(Locale.US);

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {

    txtMonto.setTextFormatter(new TextFormatter<>(change -> {
      // Solo dígitos y un único punto decimal
      String newText = change.getControlNewText();
      if (!newText.matches("\\d*\\.?\\d*")) {
        return null; // Rechaza el cambio
      }
      return change;
    }));

    configurarTabla();
  }

  private void configurarTabla() {



    cuota.setCellValueFactory(data ->
            new SimpleStringProperty(
                    String.valueOf(data.getValue().getNumCuota())
            ));

    fecha.setCellValueFactory(data -> {
        LocalDate f = data.getValue().getFechaP();
        return new SimpleStringProperty(
                f != null ? f.format(FMT_FECHA) : ""
        );
      });

      colcap.setCellValueFactory(data -> {
        BigDecimal v = data.getValue().getCapital();
        return new SimpleStringProperty(
                v != null ? FMT_MONEDA.format(v) : ""
        );
      });

      ord.setCellValueFactory(data -> {
        BigDecimal v = data.getValue().getIntereses();
        return new SimpleStringProperty(
                v != null ? FMT_MONEDA.format(v) : ""
        );
      });

      colmora.setCellValueFactory(data -> {
        BigDecimal v = data.getValue().getMora();
        return new SimpleStringProperty(
                v != null ? FMT_MONEDA.format(v) : ""
        );
      });

      coliva.setCellValueFactory(data -> {
        BigDecimal v = data.getValue().getIva();
        return new SimpleStringProperty(
                v != null ? FMT_MONEDA.format(v) : ""
        );
      });

      bonif.setCellValueFactory(data -> {
        BigDecimal v = data.getValue().getBonif();
        return new SimpleStringProperty(
                v != null ? FMT_MONEDA.format(v) : ""
        );
      });

      tot.setCellValueFactory(data -> {
        BigDecimal v = data.getValue().getTotal();
        return new SimpleStringProperty(
                v != null ? FMT_MONEDA.format(v) : ""
        );
      });

    tablaCuotas.setRowFactory(tv -> new TableRow<PagoCuotaDTO>() {
      @Override
      protected void updateItem(PagoCuotaDTO item, boolean empty) {
        super.updateItem(item, empty);

        // limpiar estilos anteriores
        setStyle("");

        if (empty || item == null || item.getFechaP() == null) {
          return;
        }

        LocalDate hoy = servicio.traerFechaHoy();
        LocalDate fechaFila = item.getFechaP();

        long dias = ChronoUnit.DAYS.between(fechaFila, hoy);

        // 1 día después -> verde (Atrasada)
        if (dias >= 1 && dias < 29) {
          setStyle("-fx-background-color: #90EE90;");
        }

        // 29 días o más -> amarillo (Vencida)
        if (dias >= 29) {
          setStyle("-fx-background-color: #FFF59D;");
        }
      }
    });
  }

  private void cargarCuotas() {
    List<PagoCuotaDTO> lista =
            servicio.calcularPagoDeCuotas(
                    creditoEncontrado.getId(),
                    creditoEncontrado.getTasa(),
                    creditoEncontrado.getMora(),
                    creditoEncontrado.getIva(),
                    creditoEncontrado.getFd()
            );

    copiaCuotas = lista;

    ModelConfiguracion configuracion = servicio.obtenerConfiguraciones();

    List<PagoCuotaDTO> primeras3 = lista.stream()
            .limit(configuracion.getCuotasMaximasCajero())
            .collect(toList());

    ObservableList<PagoCuotaDTO> datos =
            FXCollections.observableArrayList(primeras3);

    tablaCuotas.setItems(datos);

    double totalCuotasinmediatas = 0;

    for(PagoCuotaDTO c : datos){
      totalCuotasinmediatas += c.getTotal().doubleValue();
    }

    txtInmediatas.setText(FMT_MONEDA.format(totalCuotasinmediatas));

    txtSaldoCredito.setText(FMT_MONEDA.format(creditoEncontrado.getSaldo()));

    lblPrimerasN.setText("Total " + configuracion.getCuotasMaximasCajero() +" Cuotas Inmediatas:");


    ModelSocio socio = servicio.traerSocioXNumero(creditoEncontrado.getSocio());

    lblNombre.setText(socio.getNombres() + " " + socio.getApellidoP() + " " + socio.getApellidoM());
    lblNumSocio.setText(String.valueOf(socio.getNumSocio()));
    lblCredito.setText(String.valueOf(creditoEncontrado.getId()));
    lblPlazo.setText("Plazo: "+ creditoEncontrado.getPlazo() + " meses");
    lblTasa.setText("Tasa Ordinaria: " + creditoEncontrado.getTasa() +"%");
    lblMora.setText("Tasa Moratoria: "+ creditoEncontrado.getMora() +"%");
    ModelTipoCredito tipoCredito = servicio.traerTipoCredito(creditoEncontrado.getTipo_credito());

    lblTipo.setText("Código: " + tipoCredito.getCodigo_sistema());


  }

  public void setDatos(int creditoId) {
    this.creditoEncontrado = servicio.traerDatosCredito(creditoId);
    cargarCuotas();
  }

  private double parseMoneda(String moneda) {
    try {
      Number numero = FMT_MONEDA.parse(moneda);
      return numero.doubleValue();
    } catch (ParseException e) {
      e.printStackTrace();
      return 0;
    }
  }

  @FXML
  public void pagarCredito(KeyEvent event) {

    //Capital que se aplicará
    double capital = 0;
    double montoDadoF2 = 0;
    String jsonEnviar = "";
    LocalDate hoy = servicio.traerFechaHoy();


    switch (event.getCode()) {

      case F1: //Pagar aparte el interés

        if (txtMonto.getText().trim().isEmpty()) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("ERROR");
          alert.setHeaderText("ERROR AL INTENTAR PAGAR EL CRÉDITO");
          alert.setContentText("POR FAVOR, DIGITE UN MONTO Y SELECCIONE UNA FORMA DE PAGAR.");
          alert.showAndWait();
          return;
        }

        capital = Double.parseDouble(txtMonto.getText().trim());
        int indice = 0;

        // 1) Contar cuántas cuotas cubre el capital enviado
        for (PagoCuotaDTO cuotaDTO : copiaCuotas) {
//          if (cuotaDTO.getCapital().doubleValue() > capital) {
//            break;
//          }
          capital -= cuotaDTO.getCapital().doubleValue();
          indice++;
          if (capital <= 0) {
            break;
          }
        }

        // 2) Volver a asignar el monto original
        capital = Double.parseDouble(txtMonto.getText().trim());

        // 3) Aplicar la resta sobre las cuotas correctas (base 0, hasta indice inclusive)
        for (int i = 0; i <= indice && i < copiaCuotas.size(); i++) {
          PagoCuotaDTO pagoCuotaDTO = copiaCuotas.get(i);
          double capCuota = pagoCuotaDTO.getCapital().doubleValue();

          if (capital >= capCuota) {
            pagoCuotaDTO.setCapital(BigDecimal.ZERO);  // cuota cubierta completa

            //Aquí en el total no hace falta hacer nada ya que pues lo envíado superó el capital de la cuota
            //y como se paga aparte el interés el total que dice la tabla se mantiene

            capital -= capCuota;                       // restar ANTES de poner a 0
          } else {
            pagoCuotaDTO.setCapital(
                    BigDecimal.valueOf(capCuota - capital)
                            .setScale(2, RoundingMode.HALF_UP)
            );

            //Sumamos el total si el capital de la cuota fue mayor a lo envíado de capital
            //Se suma intereses + mora + iva + lo envíado o bien el restante de lo que fue capital antes de ponerlo a 0

            pagoCuotaDTO.setTotal(BigDecimal.valueOf(
                    pagoCuotaDTO.getIntereses().doubleValue() +
                            pagoCuotaDTO.getMora().doubleValue() +
                            pagoCuotaDTO.getIva().doubleValue() +
                            capital
            ).setScale(2, RoundingMode.HALF_UP));

            capital = 0;
          }

          // Siempre: si ya tenía fecha, la anterior pasa a ser esa
          if (pagoCuotaDTO.getFechaRealizada() != null) {
            pagoCuotaDTO.setFechaAnterior(pagoCuotaDTO.getFechaRealizada());
          }

          // Siempre: fecha realizada es hoy
          pagoCuotaDTO.setFechaRealizada(hoy);

          // Solo si saldó la cuota completa
          if (pagoCuotaDTO.getCapital().doubleValue() == 0) {
            // Si era el primer pago, fechaAnterior también es hoy
            if (pagoCuotaDTO.getFechaAnterior() == null) {
              pagoCuotaDTO.setFechaAnterior(hoy);
            }
            pagoCuotaDTO.setFechaTerminoPago(hoy);
            pagoCuotaDTO.setStatus(0);
          }

          if (capital == 0) break;
        }

        // Acumular cuotas no tocadas (desde indice+1 en adelante)
        for (int j = indice; j < copiaCuotas.size(); j++) {
          PagoCuotaDTO cuotaPendiente = copiaCuotas.get(j);

          if (cuotaPendiente.getIntereses().doubleValue() > 0) {
            cuotaPendiente.setInteresesAcumulados(
                    BigDecimal.valueOf(cuotaPendiente.getIntereses().doubleValue() + cuotaPendiente.getBonif().doubleValue() +
                            cuotaPendiente.getInteresesAcumulados().doubleValue())
            );
            cuotaPendiente.setIntereses(BigDecimal.ZERO);
          }

          cuotaPendiente.setTotal(BigDecimal.ZERO);

          //Poner la bonificación en 0 si no se procesó ya que pues ya se acumula sin bonificación
          cuotaPendiente.setBonif(BigDecimal.ZERO);

          if (cuotaPendiente.getMora().doubleValue() > 0) {
            cuotaPendiente.setMoraAcumulados(
                    cuotaPendiente.getMora().add(cuotaPendiente.getMoraAcumulados())
            );
            cuotaPendiente.setMora(BigDecimal.ZERO);
          }

          cuotaPendiente.setIva(BigDecimal.ZERO);
        }

        jsonEnviar = buildJsonCuotas();
        System.out.println(jsonEnviar);

        //Mandar CopiaCuotas al método
//        ModelTransaccion transaccion = servicio.pagarCredito(copiaCuotas, creditoEncontrado.getId());
//
//        if (transaccion.getId() != 0) {
//          Alert alert = new Alert(Alert.AlertType.INFORMATION);
//          alert.setTitle("Pago Exitoso");
//          alert.setHeaderText("Crédito #" + transaccion.getCreditoAfectado() + " — Cuota #" + transaccion.getCuotaAfectada());
//          alert.setContentText(
//                  "Total pagado:     Q" + transaccion.getSaldo() + "\n" +
//                          "Capital pagado:   Q" + transaccion.getCapitalCreditoPagado() + "\n" +
//                          "Intereses:        Q" + transaccion.getInteresesCreditoPagado() + "\n" +
//                          "Mora:             Q" + transaccion.getMoraCreditoPagado() + "\n" +
//                          "IVA:              Q" + transaccion.getIvaCreditoPagado() + "\n" +
//                          "Bonificación:     Q" + transaccion.getBonifCreditoPagado() + "\n" +
//                          "Saldo crédito:    Q" + transaccion.getSaldoCredito() + "\n" +
//                          "Fecha:            "  + transaccion.getFechaRegistro() + "\n" +
//                          "Hora:             "  + transaccion.getHora() + "\n" +
//                          "Caja #:           "  + transaccion.getCajaId()
//          );
//          alert.showAndWait();
//        }

        break;

      case F2:

        if (txtMonto.getText().trim().isEmpty()) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("ERROR");
          alert.setHeaderText("ERROR AL INTENTAR PAGAR EL CRÉDITO");
          alert.setContentText("POR FAVOR, DIGITE UN MONTO Y SELECCIONE UNA FORMA DE PAGAR.");
          alert.showAndWait();
          return;
        }

        montoDadoF2 = Double.parseDouble(txtMonto.getText().trim());
        double total = 0;
        int i = 0;
        for (; i < copiaCuotas.size(); i++) {

          System.out.println("When el pepe: " + montoDadoF2);

          PagoCuotaDTO pagoCuotaDTO = copiaCuotas.get(i);

          //Ver si el monto dado cubre la cuota inmediata
          if (montoDadoF2 == pagoCuotaDTO.getTotal().doubleValue()) {
            pagoCuotaDTO.setCapital(BigDecimal.ZERO);

            //Settear fechas
            if (pagoCuotaDTO.getFechaRealizada() != null) {
              pagoCuotaDTO.setFechaAnterior(pagoCuotaDTO.getFechaRealizada());
            }
            pagoCuotaDTO.setFechaRealizada(hoy);
            if (pagoCuotaDTO.getFechaAnterior() == null) {
              pagoCuotaDTO.setFechaAnterior(hoy);
            }
            pagoCuotaDTO.setFechaTerminoPago(hoy);
            pagoCuotaDTO.setStatus(0);


            break;
          } else if(montoDadoF2 > pagoCuotaDTO.getTotal().doubleValue()){
            pagoCuotaDTO.setCapital(BigDecimal.ZERO);
            montoDadoF2 -= pagoCuotaDTO.getTotal().doubleValue();

            System.out.println("1ero: " + montoDadoF2);

            //settear fechas
            if (pagoCuotaDTO.getFechaRealizada() != null) {
              pagoCuotaDTO.setFechaAnterior(pagoCuotaDTO.getFechaRealizada());
            }
            pagoCuotaDTO.setFechaRealizada(hoy);
            if (pagoCuotaDTO.getFechaAnterior() == null) {
              pagoCuotaDTO.setFechaAnterior(hoy);
            }
            pagoCuotaDTO.setFechaTerminoPago(hoy);
            pagoCuotaDTO.setStatus(0);

          } else {

            System.out.println("2do: " + montoDadoF2);


            // Fechas primero, antes de cualquier break
            if (pagoCuotaDTO.getFechaRealizada() != null) {
              pagoCuotaDTO.setFechaAnterior(pagoCuotaDTO.getFechaRealizada());
            }
            pagoCuotaDTO.setFechaRealizada(hoy);

            //El total de la cuota es tal cual monto dado
            pagoCuotaDTO.setTotal(BigDecimal.valueOf(montoDadoF2));
            System.out.println("Total: " + pagoCuotaDTO.getTotal());
            // El monto dado es menor, por lo tanto vamos a ir descontando uno por uno
            //Si aplica IVA

            if (creditoEncontrado.getIva() != 0) {
              //Evaluo las dos condiciones

              //Si hay algo de mora evaluo solo eso sin pasar por intereses
              if (pagoCuotaDTO.getMora().doubleValue() != 0) {
                if (pagoCuotaDTO.getMora().doubleValue() >= montoDadoF2) {
                  //Saco el Iva de lo que me queda
                  double ivaMontoDado = montoDadoF2 * (creditoEncontrado.getIva() / 100);
                  //Se lo resto al monto que me queda asegurando el IVA y ya ir repartiendo en el orden: mora -> interes -> Capital
                  montoDadoF2 -= ivaMontoDado;
                  pagoCuotaDTO.setIva(BigDecimal.valueOf(ivaMontoDado).setScale(2, RoundingMode.HALF_UP));
                } else if (pagoCuotaDTO.getMora().doubleValue() < montoDadoF2) {
                  montoDadoF2 -= pagoCuotaDTO.getIva().doubleValue();
                }
              } else {
                //Si la mora es 0, en lugar de evaluar con mora evalúo con intereses
                if (pagoCuotaDTO.getIntereses().doubleValue() >= montoDadoF2) {
                  //Saco el Iva de lo que me queda
                  double ivaMontoDado = montoDadoF2 * (creditoEncontrado.getIva() / 100);
                  //Se lo resto al monto que me queda asegurando el IVA y ya ir repartiendo en el orden: mora -> interes -> Capital
                  montoDadoF2 -= ivaMontoDado;
                  pagoCuotaDTO.setIva(BigDecimal.valueOf(ivaMontoDado).setScale(2, RoundingMode.HALF_UP));
                } else if (pagoCuotaDTO.getIntereses().doubleValue() < montoDadoF2) {
                  montoDadoF2 -= pagoCuotaDTO.getIva().doubleValue();
                }
              }


            }

            //PAGO DE MORA
              if (montoDadoF2 > pagoCuotaDTO.getMora().doubleValue()) {
                montoDadoF2 -= pagoCuotaDTO.getMora().doubleValue();
                pagoCuotaDTO.setMoraAcumulados(BigDecimal.ZERO);
              } else if (montoDadoF2 < pagoCuotaDTO.getMora().doubleValue()) {
                pagoCuotaDTO.setMoraAcumulados(BigDecimal.valueOf(pagoCuotaDTO.getMora().doubleValue() - montoDadoF2).setScale(2, RoundingMode.HALF_UP));
                pagoCuotaDTO.setMora(  BigDecimal.valueOf( pagoCuotaDTO.getMora().doubleValue() - (pagoCuotaDTO.getMora().doubleValue() - montoDadoF2)).setScale(2, RoundingMode.HALF_UP));
                //Ya que el montoDado fue menor que la mora, acumulo todo el interés ya que no llegué a cubrir absolutamente nada
                pagoCuotaDTO.setInteresesAcumulados(pagoCuotaDTO.getIntereses());
                pagoCuotaDTO.setIntereses(BigDecimal.ZERO);
                //Por parte de la mora, acumulo solo lo que no pude cubrir, es decir, lo que era menos lo que tenia montoDado
                montoDadoF2 = 0;

              } else {
                //Aquí, como montoDado fue igual a la mora pues ponemos monto dado en cero, de mora no acumulamos nada ya que la cubrí toda y de interes
                //Acumulamos todo
                pagoCuotaDTO.setInteresesAcumulados(pagoCuotaDTO.getIntereses());
                pagoCuotaDTO.setIntereses(BigDecimal.ZERO);
                pagoCuotaDTO.setMoraAcumulados(BigDecimal.ZERO);
                montoDadoF2 = 0;
              }

              //PAGO DE INTERESES
                if (montoDadoF2 > 0) {
                //Seguimos restando
                if (montoDadoF2 > pagoCuotaDTO.getIntereses().doubleValue()) {
                  montoDadoF2 -= pagoCuotaDTO.getIntereses().doubleValue();
                  pagoCuotaDTO.setInteresesAcumulados(BigDecimal.ZERO);
                } else if (montoDadoF2 < pagoCuotaDTO.getIntereses().doubleValue()) {
                  pagoCuotaDTO.setInteresesAcumulados(BigDecimal.valueOf((pagoCuotaDTO.getIntereses().doubleValue() - montoDadoF2) + pagoCuotaDTO.getBonif().doubleValue())
                          .setScale(2, RoundingMode.HALF_UP));
                  pagoCuotaDTO.setIntereses(BigDecimal.valueOf(pagoCuotaDTO.getIntereses().doubleValue() - (pagoCuotaDTO.getIntereses().doubleValue() - montoDadoF2)).setScale(2, RoundingMode.HALF_UP));
                  montoDadoF2 = 0;
                } else {
                  pagoCuotaDTO.setInteresesAcumulados(BigDecimal.ZERO);
                  montoDadoF2 = 0;
                }
              } else {
                //Rompemos
                break;
              }

                //PAGO DE CAPITAL
              if (montoDadoF2 > 0) {
                //Seguimos restando
                if (montoDadoF2 > pagoCuotaDTO.getCapital().doubleValue()) {
                  montoDadoF2 -= pagoCuotaDTO.getCapital().doubleValue();
                  pagoCuotaDTO.setCapital(BigDecimal.ZERO);
                } else if (montoDadoF2 < pagoCuotaDTO.getCapital().doubleValue()) {
                  pagoCuotaDTO.setCapital(BigDecimal.valueOf(pagoCuotaDTO.getCapital().doubleValue() - montoDadoF2).setScale(2, RoundingMode.HALF_UP));
                  montoDadoF2 = 0;
                } else {
                  pagoCuotaDTO.setCapital(BigDecimal.ZERO);
                  montoDadoF2 = 0;
                }
              } else {
              // Rompemos
              break;
            }

            // Al final, solo si saldó capital
            if (pagoCuotaDTO.getCapital().doubleValue() == 0) {
              if (pagoCuotaDTO.getFechaAnterior() == null) {
                pagoCuotaDTO.setFechaAnterior(hoy);
              }
              pagoCuotaDTO.setFechaTerminoPago(hoy);
              pagoCuotaDTO.setStatus(0);
            }

          }

          if (montoDadoF2 == 0) {
            break;
          }

        }

        // Después del loop principal, acumular en las cuotas no tocadas
        for (int j = i + 1; j < copiaCuotas.size(); j++) {
          PagoCuotaDTO cuotaPendiente = copiaCuotas.get(j);

          cuotaPendiente.setTotal(BigDecimal.ZERO);

          if (cuotaPendiente.getIntereses().doubleValue() > 0) {
            //Si tiene intereses ya calculados esa cuota los acumulo con todo y bonificación
            cuotaPendiente.setInteresesAcumulados(
                    BigDecimal.valueOf(cuotaPendiente.getIntereses().doubleValue() + cuotaPendiente.getBonif().doubleValue() +
                            cuotaPendiente.getInteresesAcumulados().doubleValue())
            );
            cuotaPendiente.setIntereses(BigDecimal.ZERO);
          }

          if (cuotaPendiente.getMora().doubleValue() > 0) {
            //Lo mismo que con los intereses
            cuotaPendiente.setMoraAcumulados(
                    cuotaPendiente.getMora().add(
                            cuotaPendiente.getMoraAcumulados()
                    )
            );
            cuotaPendiente.setMora(BigDecimal.ZERO);
          }

          cuotaPendiente.setIva(BigDecimal.ZERO);

        }



        jsonEnviar = buildJsonCuotas();
        System.out.println(jsonEnviar);

        //Mandar CopiaCuotas al método
//         transaccion = servicio.pagarCredito(copiaCuotas, creditoEncontrado.getId());
//
//        if (transaccion.getId() != 0) {
//          Alert alert = new Alert(Alert.AlertType.INFORMATION);
//          alert.setTitle("Pago Exitoso");
//          alert.setHeaderText("Crédito #" + transaccion.getCreditoAfectado() + " — Cuota #" + transaccion.getCuotaAfectada());
//          alert.setContentText(
//                  "Total pagado:     Q" + transaccion.getSaldo() + "\n" +
//                          "Capital pagado:   Q" + transaccion.getCapitalCreditoPagado() + "\n" +
//                          "Intereses:        Q" + transaccion.getInteresesCreditoPagado() + "\n" +
//                          "Mora:             Q" + transaccion.getMoraCreditoPagado() + "\n" +
//                          "IVA:              Q" + transaccion.getIvaCreditoPagado() + "\n" +
//                          "Bonificación:     Q" + transaccion.getBonifCreditoPagado() + "\n" +
//                          "Saldo crédito:    Q" + transaccion.getSaldoCredito() + "\n" +
//                          "Fecha:            "  + transaccion.getFechaRegistro() + "\n" +
//                          "Hora:             "  + transaccion.getHora() + "\n" +
//                          "Caja #:           "  + transaccion.getCajaId()
//          );
//          alert.showAndWait();
//        }

        break;

      default:
        break;
    }







  }

  private String buildJsonCuotas() {
    JSONObject payload = new JSONObject();
    payload.put("creditoId", creditoEncontrado.getId());

    JSONArray arr = new JSONArray();
    for (PagoCuotaDTO c : copiaCuotas) {
      JSONObject obj = new JSONObject();
      obj.put("numCuota",  c.getNumCuota());
      obj.put("fechaPago", c.getFechaP() != null
              ? c.getFechaP().format(DateTimeFormatter.ISO_LOCAL_DATE) : null);
      obj.put("capital",   c.getCapital());
      obj.put("intereses", c.getIntereses());
      obj.put("mora",      c.getMora());
      obj.put("iva",       c.getIva());
      obj.put("bonif",     c.getBonif());
      obj.put("total",     c.getTotal());
      obj.put("fechaAnterior",     c.getFechaAnterior());
      obj.put("fechaPagoRealizada",     c.getFechaRealizada());
      obj.put("fechaTerminoPago",     c.getFechaTerminoPago());
      obj.put("interesAcumulado",     c.getInteresesAcumulados());
      obj.put("moraAcumulado",     c.getMoraAcumulados());
      arr.add(obj);
    }
    payload.put("cuotas", arr);

    return payload.toString();
  }






}
