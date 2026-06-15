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

import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
  DateTimeFormatter formato =
          DateTimeFormatter.ofPattern("HH:mm:ss");

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

        LocalDate hoy = servicio.traerFechaHoy();
        LocalDate fechaFila = data.getValue().getFechaP();

        if (hoy.isAfter(fechaFila.plusDays(29))) {
          return new SimpleStringProperty(
                  v != null ? FMT_MONEDA.format(v) : ""
          );
        } else {
          return new SimpleStringProperty(
                  FMT_MONEDA.format(0)
          );
        }

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
        if (dias >= 1 && dias < 90) {
          setStyle("-fx-background-color: #90EE90;");
        }

        // 29 días o más -> amarillo (Vencida)
        if (dias >= 90) {
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
            //.limit(configuracion.getCuotasMaximasCajero())
            .limit(copiaCuotas.size())
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
    BigDecimal capital = BigDecimal.ZERO;
    BigDecimal montoDadoF2 = BigDecimal.ZERO;
    LocalDate hoy = servicio.traerFechaHoy();
    List<ModelHistorialAcumulados> historialAcumulados = new ArrayList<>();
    List<PagoCuotaDTO> copiaDTO = new ArrayList<>();



    switch (event.getCode()) {

      case F1: //Pagar aparte el interés

        //Para settear las fechas anteriores creamos una copia de como estaba cada una de las afectas
        //Sin guardar todas para evitar tener cuotas que no nos sirvan de nada


        if (txtMonto.getText().trim().isEmpty()) {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("ERROR");
          alert.setHeaderText("ERROR AL INTENTAR PAGAR EL CRÉDITO");
          alert.setContentText("POR FAVOR, DIGITE UN MONTO Y SELECCIONE UNA FORMA DE PAGAR.");
          alert.showAndWait();
          return;
        }

        capital = new BigDecimal(txtMonto.getText().trim()).setScale(2, RoundingMode.HALF_UP);
        int indice = 0;

        BigDecimal saldo = BigDecimal.valueOf(parseMoneda(txtSaldoCredito.getText()))
                .setScale(2, RoundingMode.HALF_UP);

        if(capital.compareTo(saldo) > 0){
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("ERROR");
          alert.setHeaderText("ERROR AL INTENTAR PAGAR EL CRÉDITO");
          alert.setContentText("EL MONTO QUE TRATA DE PAGAR ES MAYOR AL SALDO DEL CRÉDITO.");
          alert.showAndWait();
          return;
        }



        // 1) Contar cuántas cuotas cubre el capital enviado
        for (PagoCuotaDTO cuotaDTO : copiaCuotas) {
          capital = capital.subtract(cuotaDTO.getCapital()).setScale(2, RoundingMode.HALF_UP);
          indice++;
          if (capital.compareTo(BigDecimal.ZERO) <= 0) {
            break;
          }
        }

        // 2) Volver a asignar el monto original
        capital = new BigDecimal(txtMonto.getText().trim()).setScale(2, RoundingMode.HALF_UP);

        // 3) Aplicar la resta sobre las cuotas correctas (base 0, hasta indice inclusive)
        for (int i = 0; i <= indice && i < copiaCuotas.size(); i++) {
          PagoCuotaDTO pagoCuotaDTO = copiaCuotas.get(i);

          BigDecimal interesesAnteriores = pagoCuotaDTO.getIntereses();
          BigDecimal moraAnterior = pagoCuotaDTO.getMora();
          BigDecimal ivaAnterior = pagoCuotaDTO.getIva();


          copiaDTO.add(new PagoCuotaDTO(pagoCuotaDTO.getNumCuota(), pagoCuotaDTO.getFechaAnterior(), pagoCuotaDTO.getFechaTerminoPago()));

          BigDecimal capCuota = pagoCuotaDTO.getCapital().setScale(2, RoundingMode.HALF_UP);

          //Crear acumulado
          if (pagoCuotaDTO.getIntereses().compareTo(BigDecimal.ZERO) != 0
                  || pagoCuotaDTO.getMora().compareTo(BigDecimal.ZERO) != 0) {
            ModelHistorialAcumulados historialAcumulado = new ModelHistorialAcumulados();
            historialAcumulado.setCreditoId(Long.valueOf(pagoCuotaDTO.getCreditoId()));
            historialAcumulado.setNumCuota(pagoCuotaDTO.getNumCuota());
            historialAcumulado.setInteresAcumulado(pagoCuotaDTO.getInteresesAcumulados());
            historialAcumulado.setMoraAcumulado(pagoCuotaDTO.getMoraAcumulados());
            historialAcumulado.setEstado(true);
            historialAcumulado.setFr(hoy);

            //Agregamos al arreglo de acumulados
            historialAcumulados.add(historialAcumulado);
          }

          pagoCuotaDTO.setInteresesAcumulados(BigDecimal.ZERO);
          pagoCuotaDTO.setMoraAcumulados(BigDecimal.ZERO);


          if (capital.compareTo(capCuota) >= 0) {
            pagoCuotaDTO.setCapital(BigDecimal.ZERO);  // cuota cubierta completa

            //Aquí en el total no hace falta hacer nada ya que pues lo envíado superó el capital de la cuota
            //y como se paga aparte el interés el total que dice la tabla se mantiene
            if (hoy.isBefore(pagoCuotaDTO.getFechaP().plusDays(29))) {
              pagoCuotaDTO.setMora(BigDecimal.ZERO);
            }

            capital = capital.subtract(capCuota).setScale(2, RoundingMode.HALF_UP);
          } else {
            pagoCuotaDTO.setCapital(
                    capCuota.subtract(capital).setScale(2, RoundingMode.HALF_UP)
            );

            //Sumamos el total si el capital de la cuota fue mayor a lo envíado de capital
            //Se suma intereses + mora + iva + lo envíado o bien el restante de lo que fue capital antes de ponerlo a 0

            if (hoy.isAfter(pagoCuotaDTO.getFechaP().plusDays(29))) {
              pagoCuotaDTO.setTotal(
                      pagoCuotaDTO.getIntereses()
                              .add(pagoCuotaDTO.getMora())
                              .add(pagoCuotaDTO.getIva())
                              .add(capital)
                              .setScale(2, RoundingMode.HALF_UP)
              );
            } else {
              pagoCuotaDTO.setTotal(
                      pagoCuotaDTO.getIntereses()
                              .add(pagoCuotaDTO.getIva())
                              .add(capital)
                              .setScale(2, RoundingMode.HALF_UP)
              );
              pagoCuotaDTO.setMora(BigDecimal.ZERO);
            }

            capital = BigDecimal.ZERO;
          }

//          pagoCuotaDTO.setFechaAnteriorAntes(pagoCuotaDTO.getFechaAnterior());
//          pagoCuotaDTO.setFechaTerminoPagoAntes(pagoCuotaDTO.getFechaTerminoPago());
          pagoCuotaDTO.setFechaAnterior(pagoCuotaDTO.getFechaRealizada());
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


          if (pagoCuotaDTO.getIsCondonado()) {
            pagoCuotaDTO.setIntereses(interesesAnteriores);
            pagoCuotaDTO.setMora(moraAnterior);
            pagoCuotaDTO.setIva(ivaAnterior);

          }

          if (capital.compareTo(BigDecimal.ZERO) == 0) break;
        }

        // Acumular cuotas no tocadas (desde indice+1 en adelante)
        for (int j = indice; j < copiaCuotas.size(); j++) {
          PagoCuotaDTO cuotaPendiente = copiaCuotas.get(j);

          if (cuotaPendiente.getIntereses().doubleValue() != 0 || cuotaPendiente.getMora().doubleValue() != 0) {
            ModelHistorialAcumulados historialAcumulado = new ModelHistorialAcumulados();
            historialAcumulado.setCreditoId(Long.valueOf(cuotaPendiente.getCreditoId()));
            historialAcumulado.setNumCuota(cuotaPendiente.getNumCuota());
            historialAcumulado.setInteresAcumulado(cuotaPendiente.getInteresesAcumulados());
            historialAcumulado.setMoraAcumulado(cuotaPendiente.getMoraAcumulados());
            historialAcumulado.setEstado(true);
            historialAcumulado.setFr(hoy);

            //Agregamos al arreglo de acumulados
            historialAcumulados.add(historialAcumulado);
          }

           if (cuotaPendiente.getIntereses().doubleValue() > 0) {

            cuotaPendiente.setInteresesAcumulados(
                    BigDecimal.valueOf(cuotaPendiente.getIntereses().doubleValue() + cuotaPendiente.getBonif().doubleValue())
            );
            cuotaPendiente.setIntereses(BigDecimal.ZERO);
          }

          cuotaPendiente.setTotal(BigDecimal.ZERO);

          //Poner la bonificación en 0 si no se procesó ya que pues ya se acumula sin bonificación
          cuotaPendiente.setBonif(BigDecimal.ZERO);

          if (cuotaPendiente.getMora().doubleValue() > 0) {
            cuotaPendiente.setMoraAcumulados(
                    cuotaPendiente.getMora()
            );
            cuotaPendiente.setMora(BigDecimal.ZERO);
          }

          cuotaPendiente.setIva(BigDecimal.ZERO);
        }

        //Mandar CopiaCuotas al método
        ModelTransaccion transaccion = servicio.pagarCredito(copiaCuotas, creditoEncontrado.getId(), historialAcumulados, copiaDTO);

        if (transaccion.getId() != 0) {
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("ABONO EXITOSO");
          alert.setHeaderText("ABONO EXITOSO");
          alert.setContentText(
                  "ABONO A CRÉDITO "  + transaccion.getCreditoAfectado() + " REALIZADO CON ÉXITO");
          alert.showAndWait();



          ModelEmpresa empresa = servicio.traerEmpresa(creditoEncontrado.getEmpresa());
          String empresaDirec = empresa.getCalle() + " " + empresa.getCruzamiento() + " COL. CENTRO";
          MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
          String moneyAsWords = converter.asWords(transaccion.getSaldo().setScale(2, RoundingMode.HALF_UP)).toUpperCase() + " MXN";

          ModelTipoCredito tipoCredito = servicio.traerTipoCredito(creditoEncontrado.getTipo_credito());
          double interesesSinBonificar = transaccion.getInteresesCreditoPagado().doubleValue() + transaccion.getBonifCreditoPagado().doubleValue();

          ModelSocio socio = servicio.traerSocioXNumero(creditoEncontrado.getSocio());
          PrintJob impresion = new PrintJob();
          PrinterMatrix printer = impresion.imprimirAbonoACredito(
                  empresa.getRazonSocial(),
                  empresa.getRfc(),
                  empresaDirec,
                  String.valueOf(creditoEncontrado.getSocio()),
                  String.valueOf(transaccion.getId()),
                  socio.getNombres() + " " + socio.getApellidoP() + " " + socio.getApellidoM(),
                  FMT_FECHA.format(transaccion.getFechaRegistro()),
                  formato.format(transaccion.getHora()),
                  LoginController.usuarioLoggeado,
                  FMT_MONEDA.format(transaccion.getCapitalCreditoPagado()),
                  moneyAsWords,
                  FMT_MONEDA.format(transaccion.getSaldo()),
                  tipoCredito.getCodigo_sistema(),
                  FMT_MONEDA.format(transaccion.getMoraCreditoPagado()),
                  FMT_MONEDA.format(transaccion.getInteresesCreditoPagado()),
                  FMT_MONEDA.format(transaccion.getBonifCreditoPagado()),
                  FMT_MONEDA.format(transaccion.getIvaCreditoPagado()),
                  FMT_MONEDA.format(transaccion.getSaldoCredito()),
                  FMT_MONEDA.format(interesesSinBonificar)
          );
          printer.toFile("impresion_Abono.txt");



          new Thread(() -> {
            try {
              enviarImpresora("impresion_Abono.txt");
            } catch (Exception e) {
              Alert alert2 = new Alert(Alert.AlertType.ERROR);
              alert2.setTitle("ERROR");
              alert2.setHeaderText("ERROR AL IMPRIMIR");
              alert2.setContentText(
                      e.getMessage().toUpperCase());
              alert2.showAndWait();
            }
          }).start();

          //Aumentamos las transacciones del cajero
          CajeroController.bufferOperaciones += transaccion.getSaldo().doubleValue();

          //Cerramos la ventana
          cerrarVentana();

        }

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

        montoDadoF2 = new BigDecimal(txtMonto.getText().trim()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = BigDecimal.ZERO;
        int i = 0;

        for (; i < copiaCuotas.size(); i++){
          PagoCuotaDTO pagoCuotaDTO = copiaCuotas.get(i);
          total = total.add(pagoCuotaDTO.getTotal()).setScale(2, RoundingMode.HALF_UP);
        }


        if(total.compareTo(montoDadoF2) < 0){
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("ERROR");
          alert.setHeaderText("ERROR AL INTENTAR PAGAR EL CRÉDITO");
          alert.setContentText("EL MONTO QUE TRATA DE PAGAR ES MAYOR AL SALDO DEL CRÉDITO.");
          alert.showAndWait();
          return;
        }

        i = 0;

        for (; i < copiaCuotas.size(); i++) {

          PagoCuotaDTO pagoCuotaDTO = copiaCuotas.get(i);


          BigDecimal interesesAnteriores = pagoCuotaDTO.getIntereses();
          BigDecimal moraAnterior = pagoCuotaDTO.getMora();
          BigDecimal ivaAnterior = pagoCuotaDTO.getIva();
          copiaDTO.add(new PagoCuotaDTO(pagoCuotaDTO.getNumCuota(), pagoCuotaDTO.getFechaAnterior(), pagoCuotaDTO.getFechaTerminoPago()));

          //Ver si el monto dado cubre la cuota inmediata
          if (montoDadoF2.compareTo(pagoCuotaDTO.getTotal().setScale(2, RoundingMode.HALF_UP)) == 0) {
            pagoCuotaDTO.setCapital(BigDecimal.ZERO);

            //Crear acumulado
            if (pagoCuotaDTO.getIntereses().compareTo(BigDecimal.ZERO) != 0
                    || pagoCuotaDTO.getMora().compareTo(BigDecimal.ZERO) != 0) {
              ModelHistorialAcumulados historialAcumulado = new ModelHistorialAcumulados();
              historialAcumulado.setCreditoId(Long.valueOf(pagoCuotaDTO.getCreditoId()));
              historialAcumulado.setNumCuota(pagoCuotaDTO.getNumCuota());
              historialAcumulado.setInteresAcumulado(pagoCuotaDTO.getInteresesAcumulados());
              historialAcumulado.setMoraAcumulado(pagoCuotaDTO.getMoraAcumulados());
              historialAcumulado.setEstado(true);
              historialAcumulado.setFr(hoy);

              //Agregamos al arreglo de acumulados
              historialAcumulados.add(historialAcumulado);
            }

            pagoCuotaDTO.setInteresesAcumulados(BigDecimal.ZERO);
            pagoCuotaDTO.setMoraAcumulados(BigDecimal.ZERO);

            //Settear fechas

            if (pagoCuotaDTO.getFechaRealizada() != null) {
              pagoCuotaDTO.setFechaAnterior(pagoCuotaDTO.getFechaRealizada());
            } else {
              pagoCuotaDTO.setFechaAnterior(hoy);
            }

            pagoCuotaDTO.setFechaRealizada(hoy);
            pagoCuotaDTO.setFechaTerminoPago(hoy);

            pagoCuotaDTO.setStatus(0);

            if (hoy.isBefore(pagoCuotaDTO.getFechaP().plusDays(29))) {
              pagoCuotaDTO.setMora(BigDecimal.ZERO);
            }

            if (pagoCuotaDTO.getIsCondonado()) {
              pagoCuotaDTO.setIntereses(interesesAnteriores);
              pagoCuotaDTO.setMora(moraAnterior);
              pagoCuotaDTO.setIva(ivaAnterior);

            }

            break;
          } else if(montoDadoF2.compareTo(pagoCuotaDTO.getTotal().setScale(2, RoundingMode.HALF_UP)) > 0) {
            pagoCuotaDTO.setCapital(BigDecimal.ZERO);
            montoDadoF2 = montoDadoF2.subtract(
                    pagoCuotaDTO.getTotal().setScale(2, RoundingMode.HALF_UP)
            ).setScale(2, RoundingMode.HALF_UP);

            //Crear acumulado
            if (pagoCuotaDTO.getIntereses().compareTo(BigDecimal.ZERO) != 0
                    || pagoCuotaDTO.getMora().compareTo(BigDecimal.ZERO) != 0) {
              ModelHistorialAcumulados historialAcumulado = new ModelHistorialAcumulados();
              historialAcumulado.setCreditoId(Long.valueOf(pagoCuotaDTO.getCreditoId()));
              historialAcumulado.setNumCuota(pagoCuotaDTO.getNumCuota());
              historialAcumulado.setInteresAcumulado(pagoCuotaDTO.getInteresesAcumulados());
              historialAcumulado.setMoraAcumulado(pagoCuotaDTO.getMoraAcumulados());
              historialAcumulado.setEstado(true);
              historialAcumulado.setFr(hoy);

              //Agregamos al arreglo de acumulados
              historialAcumulados.add(historialAcumulado);
            }


            if(pagoCuotaDTO.getFechaRealizada() != null) {
              pagoCuotaDTO.setFechaAnterior(pagoCuotaDTO.getFechaRealizada());
            } else {
              pagoCuotaDTO.setFechaAnterior(hoy);

            }

            pagoCuotaDTO.setInteresesAcumulados(BigDecimal.ZERO);
            pagoCuotaDTO.setMoraAcumulados(BigDecimal.ZERO);
            pagoCuotaDTO.setFechaRealizada(hoy);
            pagoCuotaDTO.setFechaTerminoPago(hoy);
            pagoCuotaDTO.setStatus(0);

            if (hoy.isBefore(pagoCuotaDTO.getFechaP().plusDays(29))) {
              pagoCuotaDTO.setMora(BigDecimal.ZERO);
            }

            if (pagoCuotaDTO.getIsCondonado()) {
              pagoCuotaDTO.setIntereses(interesesAnteriores);
              pagoCuotaDTO.setMora(moraAnterior);
              pagoCuotaDTO.setIva(ivaAnterior);

            }

          } else {



            // Fechas primero, antes de cualquier break

            pagoCuotaDTO.setFechaAnterior(pagoCuotaDTO.getFechaRealizada());
            pagoCuotaDTO.setFechaRealizada(hoy);  
            //El total de la cuota es tal cual monto dado
            pagoCuotaDTO.setTotal(montoDadoF2.setScale(2, RoundingMode.HALF_UP));
            // El monto dado es menor, por lo tanto vamos a ir descontando uno por uno


            //Crear acumulado
            if (pagoCuotaDTO.getIntereses().compareTo(BigDecimal.ZERO) != 0
                    || pagoCuotaDTO.getMora().compareTo(BigDecimal.ZERO) != 0) {
              ModelHistorialAcumulados historialAcumulado = new ModelHistorialAcumulados();
              historialAcumulado.setCreditoId(Long.valueOf(pagoCuotaDTO.getCreditoId()));
              historialAcumulado.setNumCuota(pagoCuotaDTO.getNumCuota());
              historialAcumulado.setInteresAcumulado(pagoCuotaDTO.getInteresesAcumulados());
              historialAcumulado.setMoraAcumulado(pagoCuotaDTO.getMoraAcumulados());
              historialAcumulado.setEstado(true);
              historialAcumulado.setFr(hoy);

              //Agregamos al arreglo de acumulados
              historialAcumulados.add(historialAcumulado);
            }

            //Si aplica IVA

            if (creditoEncontrado.getIva() != 0) {
              //Evaluo las dos condiciones

              BigDecimal pagoEvaluar;
              if (hoy.isAfter(pagoCuotaDTO.getFechaP().plusDays(29))) {
                pagoEvaluar = pagoCuotaDTO.getIva()
                        .add(pagoCuotaDTO.getIntereses())
                        .add(pagoCuotaDTO.getMora())
                        .setScale(2, RoundingMode.HALF_UP);
              } else {
                pagoEvaluar = pagoCuotaDTO.getIva()
                        .add(pagoCuotaDTO.getIntereses())
                        .setScale(2, RoundingMode.HALF_UP);
              }


              //Si hay algo de mora evaluo solo eso sin pasar por intereses
              if (pagoEvaluar.compareTo(montoDadoF2) > 0) {
                BigDecimal ivaMontoDado = montoDadoF2
                        .multiply(BigDecimal.valueOf(creditoEncontrado.getIva()).divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP))
                        .setScale(2, RoundingMode.HALF_UP);
                montoDadoF2 = montoDadoF2.subtract(ivaMontoDado).setScale(2, RoundingMode.HALF_UP);
                pagoCuotaDTO.setIva(ivaMontoDado);
              } else {
                montoDadoF2 = montoDadoF2.subtract(
                        pagoCuotaDTO.getIva().setScale(2, RoundingMode.HALF_UP)
                ).setScale(2, RoundingMode.HALF_UP);
              }

            }

            //PAGO DE MORA

            if (hoy.isAfter(pagoCuotaDTO.getFechaP().plusDays(29))) {
              BigDecimal mora = pagoCuotaDTO.getMora().setScale(2, RoundingMode.HALF_UP);
              if (montoDadoF2.compareTo(mora) > 0) {
                montoDadoF2 = montoDadoF2.subtract(mora).setScale(2, RoundingMode.HALF_UP);
                pagoCuotaDTO.setMoraAcumulados(BigDecimal.ZERO);
              } else if (montoDadoF2.compareTo(mora) < 0) {


                pagoCuotaDTO.setMoraAcumulados(
                        mora.subtract(montoDadoF2).setScale(2, RoundingMode.HALF_UP)
                );
                pagoCuotaDTO.setMora(
                        mora.subtract(mora.subtract(montoDadoF2)).setScale(2, RoundingMode.HALF_UP)
                );
                pagoCuotaDTO.setInteresesAcumulados(pagoCuotaDTO.getIntereses());
                pagoCuotaDTO.setIntereses(BigDecimal.ZERO);
                montoDadoF2 = BigDecimal.ZERO;

              } else {
                //Aquí, como montoDado fue igual a la mora pues ponemos monto dado en cero, de mora no acumulamos nada ya que la cubrí toda y de interes
                //Acumulamos todo
                pagoCuotaDTO.setInteresesAcumulados(pagoCuotaDTO.getIntereses());
                pagoCuotaDTO.setIntereses(BigDecimal.ZERO);
                pagoCuotaDTO.setMoraAcumulados(BigDecimal.ZERO);
                montoDadoF2 = BigDecimal.ZERO;
              }
            } else {
              pagoCuotaDTO.setMoraAcumulados(pagoCuotaDTO.getMora());
              pagoCuotaDTO.setMora(BigDecimal.ZERO);
            }


              //PAGO DE INTERESES
                if (montoDadoF2.compareTo(BigDecimal.ZERO) > 0) {
                //Seguimos restando
                  BigDecimal intereses = pagoCuotaDTO.getIntereses().setScale(2, RoundingMode.HALF_UP);
                  if (montoDadoF2.compareTo(intereses) > 0) {
                    montoDadoF2 = montoDadoF2.subtract(intereses).setScale(2, RoundingMode.HALF_UP);
                    pagoCuotaDTO.setInteresesAcumulados(BigDecimal.ZERO);
                } else if (montoDadoF2.compareTo(intereses) < 0) {


                    pagoCuotaDTO.setInteresesAcumulados(
                            intereses.subtract(montoDadoF2)
                                    .add(pagoCuotaDTO.getBonif())
                                    .setScale(2, RoundingMode.HALF_UP)
                    );
                    pagoCuotaDTO.setIntereses(
                            intereses.subtract(intereses.subtract(montoDadoF2)).setScale(2, RoundingMode.HALF_UP)
                    );
                    montoDadoF2 = BigDecimal.ZERO;
                } else {
                  pagoCuotaDTO.setInteresesAcumulados(BigDecimal.ZERO);
                  montoDadoF2 = BigDecimal.ZERO;
                }
              } else {
                //Rompemos
                break;
              }

                //PAGO DE CAPITAL
              if (montoDadoF2.compareTo(BigDecimal.ZERO) > 0) {
                //Seguimos restando
                BigDecimal cap = pagoCuotaDTO.getCapital().setScale(2, RoundingMode.HALF_UP);
                if (montoDadoF2.compareTo(cap) > 0) {
                  montoDadoF2 = montoDadoF2.subtract(cap).setScale(2, RoundingMode.HALF_UP);
                  pagoCuotaDTO.setCapital(BigDecimal.ZERO);
                } else if (montoDadoF2.compareTo(cap) < 0) {
                  pagoCuotaDTO.setCapital(cap.subtract(montoDadoF2).setScale(2, RoundingMode.HALF_UP));
                  montoDadoF2 = BigDecimal.ZERO;
                } else {
                  pagoCuotaDTO.setCapital(BigDecimal.ZERO);
                  montoDadoF2 = BigDecimal.ZERO;
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

            if (pagoCuotaDTO.getIsCondonado()) {
              pagoCuotaDTO.setIntereses(interesesAnteriores);
              pagoCuotaDTO.setMora(moraAnterior);
              pagoCuotaDTO.setIva(ivaAnterior);

            }


          }

          if (montoDadoF2.compareTo(BigDecimal.ZERO) == 0) {
            break;
          }

        }

        // Después del loop principal, acumular en las cuotas no tocadas
        for (int j = i + 1; j < copiaCuotas.size(); j++) {
          PagoCuotaDTO cuotaPendiente = copiaCuotas.get(j);

          if (cuotaPendiente.getIntereses().doubleValue() != 0 || cuotaPendiente.getMora().doubleValue() != 0) {
            ModelHistorialAcumulados historialAcumulado = new ModelHistorialAcumulados();
            historialAcumulado.setCreditoId(Long.valueOf(cuotaPendiente.getCreditoId()));
            historialAcumulado.setNumCuota(cuotaPendiente.getNumCuota());
            historialAcumulado.setInteresAcumulado(cuotaPendiente.getInteresesAcumulados());
            historialAcumulado.setMoraAcumulado(cuotaPendiente.getMoraAcumulados());
            historialAcumulado.setEstado(true);
            historialAcumulado.setFr(hoy);

            //Agregamos al arreglo de acumulados
            historialAcumulados.add(historialAcumulado);
          }

          cuotaPendiente.setTotal(BigDecimal.ZERO);

          if (cuotaPendiente.getIntereses().doubleValue() > 0) {
            //Si tiene intereses ya calculados esa cuota los acumulo con todo y bonificación
            cuotaPendiente.setInteresesAcumulados(
                    BigDecimal.valueOf(cuotaPendiente.getIntereses().doubleValue() + cuotaPendiente.getBonif().doubleValue())
            );
            cuotaPendiente.setIntereses(BigDecimal.ZERO);
          }



          if (cuotaPendiente.getMora().doubleValue() > 0) {
            //Lo mismo que con los intereses
            cuotaPendiente.setMoraAcumulados(
                    cuotaPendiente.getMora()
            );
            cuotaPendiente.setMora(BigDecimal.ZERO);
          }

          cuotaPendiente.setIva(BigDecimal.ZERO);
          cuotaPendiente.setBonif(BigDecimal.ZERO);

        }




        //Mandar CopiaCuotas al método
        transaccion = servicio.pagarCredito(copiaCuotas, creditoEncontrado.getId(), historialAcumulados, copiaDTO);

        if (transaccion.getId() != 0) {
          Alert alert = new Alert(Alert.AlertType.INFORMATION);
          alert.setTitle("ABONO EXITOSO");
          alert.setHeaderText("ABONO EXITOSO");
          alert.setContentText(
          "ABONO A CRÉDITO "  + transaccion.getCreditoAfectado() + " REALIZADO CON ÉXITO");
          alert.showAndWait();
          ModelEmpresa empresa = servicio.traerEmpresa(creditoEncontrado.getEmpresa());
          String empresaDirec = empresa.getCalle() + " " + empresa.getCruzamiento() + " COL. CENTRO";
          MoneyConverters converter = MoneyConverters.SPANISH_BANKING_MONEY_VALUE;
          String moneyAsWords = converter.asWords(transaccion.getSaldo().setScale(2, RoundingMode.HALF_UP)).toUpperCase() + " MXN";

          ModelTipoCredito tipoCredito = servicio.traerTipoCredito(creditoEncontrado.getTipo_credito());
          double interesesSinBonificar = transaccion.getInteresesCreditoPagado().doubleValue() + transaccion.getBonifCreditoPagado().doubleValue();

          ModelSocio socio = servicio.traerSocioXNumero(creditoEncontrado.getSocio());
          PrintJob impresion = new PrintJob();
          PrinterMatrix printer = impresion.imprimirAbonoACredito(
                  empresa.getRazonSocial(),
                  empresa.getRfc(),
                  empresaDirec,
                  String.valueOf(creditoEncontrado.getSocio()),
                  String.valueOf(transaccion.getId()),
                  socio.getNombres() + " " + socio.getApellidoP() + " " + socio.getApellidoM(),
                  FMT_FECHA.format(transaccion.getFechaRegistro()),
                  formato.format(transaccion.getHora()),
                  LoginController.usuarioLoggeado,
                  FMT_MONEDA.format(transaccion.getCapitalCreditoPagado()),
                  moneyAsWords,
                  FMT_MONEDA.format(transaccion.getSaldo()),
                  tipoCredito.getCodigo_sistema(),
                  FMT_MONEDA.format(transaccion.getMoraCreditoPagado()),
                  FMT_MONEDA.format(transaccion.getInteresesCreditoPagado()),
                  FMT_MONEDA.format(transaccion.getBonifCreditoPagado()),
                  FMT_MONEDA.format(transaccion.getIvaCreditoPagado()),
                  FMT_MONEDA.format(transaccion.getSaldoCredito()),
                  FMT_MONEDA.format(interesesSinBonificar)
          );
          printer.toFile("impresion_Abono.txt");


          new Thread(() -> {
            try {
              enviarImpresora("impresion_Abono.txt");
            } catch (Exception e) {
              Alert alert2 = new Alert(Alert.AlertType.ERROR);
              alert2.setTitle("ERROR");
              alert2.setHeaderText("ERROR AL IMPRIMIR");
              alert2.setContentText(
                      e.getMessage().toUpperCase());
              alert2.showAndWait();
            }
          }).start();

          //Aumentamos las transacciones del cajero
          CajeroController.bufferOperaciones += transaccion.getSaldo().doubleValue();

          //Cerramos la ventana
          cerrarVentana();

        }

        break;

      default:
        break;
    }







  }

  private void enviarImpresora(String rutaArchivo) {
    PrintService defaultPrintService = PrintServiceLookup.lookupDefaultPrintService();
    if (defaultPrintService == null) {
      mostrarError("Error de impresión".toUpperCase(), "No se encontró una impresora predeterminada.".toUpperCase());
      return;
    }

    try (InputStream inputStream = new FileInputStream(rutaArchivo)) {
      DocFlavor docFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
      Doc document        = new SimpleDoc(inputStream, docFormat, null);
      PrintRequestAttributeSet attributeSet = new HashPrintRequestAttributeSet();

      defaultPrintService.createPrintJob().print(document, attributeSet);

    } catch (IOException e) {
      mostrarError("Error de impresión".toUpperCase(), "No se pudo leer el archivo: ".toUpperCase() + e.getMessage());
    } catch (PrintException e) {
      mostrarError("Error de impresión ".toUpperCase(), e.getMessage());
    }
  }

  private void mostrarError(String titulo, String mensaje) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(titulo);
    alert.setHeaderText(titulo);
    alert.setContentText(mensaje);
    alert.showAndWait();
  }

  /** Cierra la ventana actual de forma limpia. */
  private void cerrarVentana() {
    Stage ventana = (Stage) txtMonto.getScene().getWindow();
    ventana.close();
  }







}
