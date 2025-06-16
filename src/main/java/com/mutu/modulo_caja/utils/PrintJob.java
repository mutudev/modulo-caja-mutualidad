package com.mutu.modulo_caja.utils;

import br.com.adilson.util.Extenso;
import br.com.adilson.util.PrinterMatrix;
import com.mutu.modulo_caja.Controllers.LoginController;

public class PrintJob {

  public PrintJob() {}

  public PrinterMatrix imprimirCambio(
      String nombre,
      String totalop,
      String cambio,
      String recibido,
      String fechaTicket,
      String horaFormateada) {

    String Cajero = "CAJERO: " + nombre;
    String descripcion1 = "LA NO OBJECION A ESTE COMPROBANTE";
    String descripcion2 = "IMPLICA SU ACEPTACION";
    PrinterMatrix printer = new PrinterMatrix();
    Extenso e = new Extenso();
    e.setNumber(21.59);
    printer.setOutSize(30, 60); // Columna 1
    printer.printTextWrap(1, 2, 1, 60, "FECHA DE OPERACIONES: " + fechaTicket); // Columna 2
    printer.printTextWrap(2, 3, 1, 60, "HORA DE OPERACIONES: " + horaFormateada); // Columna 2
    printer.printTextWrap(3, 4, 1, 60, Cajero);
    printer.printTextWrap(5, 6, 1, 60, "--LISTADO DE OPERACIONES--");
    printer.printTextWrap(6, 7, 1, 60, "TOTAL OPERACIONES: " + totalop);
    printer.printTextWrap(7, 8, 1, 60, "TOTAL RECIBIDO: " + recibido);
    printer.printTextWrap(8, 9, 1, 60, "CAMBIO: " + cambio);
    printer.printTextWrap(10, 11, 1, 60, descripcion1);
    printer.printTextWrap(11, 12, 1, 60, descripcion2);

    return printer;
  }

  public PrinterMatrix imprimirAhorro(
      String empresa,
      String rfc,
      String direc,
      String numsocio,
      String folio,
      String nomsocio,
      String numcuenta,
      String abono,
      String abonoletras,
      String fecha,
      String hora,
      String ahorro) {
    PrinterMatrix printer = new PrinterMatrix();
    String numSocio = "SOCIO: " + numsocio;
    String nombre = "NOMBRE: " + nomsocio;
    String cuenta = "No. CUENTA: " + numcuenta;
    String tipoCuenta = "TIPO DE CUENTA: AHORRO   -   ABONO AHORRO";
    String efectivo = "ABONO: " + abono;
    String descripcion1 = "LA NO OBJECION A ESTE COMPROBANTE";
    String descripcion2 = "IMPLICA SU ACEPTACION";
    String cajero = "USUARIO: " + LoginController.usuarioLoggeado;
    String totalAhorro = "AHORRO: " + ahorro;
    Extenso e = new Extenso();
    e.setNumber(21.59);
    printer.setOutSize(30, 60);
    printer.printTextWrap(1, 2, 1, 60, empresa); // Columna 1
    printer.printTextWrap(2, 3, 1, 60, "RFC: " + rfc); // Columna 2
    printer.printTextWrap(3, 4, 1, 60, direc); // Columna 3
    printer.printTextWrap(4, 5, 1, 60, "FECHA: " + fecha); // Columna 4
    printer.printTextWrap(5, 6, 1, 60, numSocio);
    printer.printTextWrap(5, 6, 30, 60, "FOLIO: " + folio); // Columna 5// Columna 6
    printer.printTextWrap(6, 7, 1, 60, nombre); // Columna 7
    printer.printTextWrap(7, 8, 1, 60, cuenta); // Columna 8
    printer.printTextWrap(8, 9, 1, 60, tipoCuenta);
    printer.printTextWrap(10, 11, 1, 60, efectivo); // Columna 10

    int numchar = abonoletras.length();
    if (numchar <= 43) {
      printer.printTextWrap(10, 11, 1, 60, efectivo);
      printer.printTextWrap(11, 12, 1, 60, abonoletras);
      printer.printTextWrap(13, 14, 1, 60, "___________________________________");
      printer.printTextWrap(14, 15, 1, 60, nombre);
      printer.printTextWrap(15, 16, 1, 60, descripcion1);
      printer.printTextWrap(16, 17, 1, 60, descripcion2);
      printer.printTextWrap(17, 18, 1, 60, "HORA: " + hora);
      printer.printTextWrap(17, 18, 16, 60, cajero); // Columna 16
      printer.printTextWrap(18, 19, 1, 60, totalAhorro); // Columna 17// Columna 18


    } else {
      String primeralinea = "";
      String segundalinea = "";
      String[] partes = abonoletras.split("(?i)\\s*CON\\s*");
      if (partes.length >= 2) {
        primeralinea = partes[0];
        segundalinea = "CON " + partes[1];
      }
      printer.printTextWrap(9, 10, 1, 60, efectivo);
      printer.printTextWrap(10, 11, 1, 60, primeralinea);
      printer.printTextWrap(11, 12, 1, 60, segundalinea);
      printer.printTextWrap(13, 14, 1, 60, "___________________________________");
      printer.printTextWrap(14, 15, 1, 60, nombre);
      printer.printTextWrap(15, 16, 1, 60, descripcion1);
      printer.printTextWrap(16, 17, 1, 60, descripcion2);
      printer.printTextWrap(17, 18, 1, 60, "HORA: " + hora);
      printer.printTextWrap(17, 18, 16, 60, cajero); // Columna 16
      printer.printTextWrap(18, 19, 1, 60, totalAhorro); // Columna 17// Columna 18
    }

    return printer;
  }

  public PrinterMatrix imprimirPrevision(
      String empresa,
      String rfc,
      String direc,
      String numsocio,
      String folio,
      String nomsocio,
      String abono,
      String abonoletras,
      String fecha,
      String hora,
      String montoAsignado,
      String montoCubierto) {

    PrinterMatrix printer = new PrinterMatrix();
    String numSocio = "SOCIO: " + numsocio;
    String nombre = "NOMBRE: " + nomsocio;
    String tipoCuenta = "TIPO DE CUENTA: PREVISION SOCIAL - ABONO A PS";
    String efectivo = "ABONO: " + abono;
    String descripcion1 = "LA NO OBJECION A ESTE COMPROBANTE";
    String descripcion2 = "IMPLICA SU ACEPTACION";
    String cajero = "USUARIO: " + LoginController.usuarioLoggeado;
    Extenso e = new Extenso();
    e.setNumber(21.59);
    printer.setOutSize(30, 60);
    printer.printTextWrap(1, 2, 1, 60, empresa); // Columna 1
    printer.printTextWrap(2, 3, 1, 60, "RFC: " + rfc); // Columna 2
    printer.printTextWrap(3, 4, 1, 60, direc); // Columna 3
    printer.printTextWrap(4, 5, 1, 60, "FECHA: " + fecha); // Columna 4
    printer.printTextWrap(5, 6, 1, 60, numSocio);
    printer.printTextWrap(5, 6, 30, 60, "FOLIO: " + folio); // Columna 5// Columna 6
    printer.printTextWrap(6, 7, 1, 60, nombre); // Columna 7
    printer.printTextWrap(7, 8, 1, 60, tipoCuenta);
    printer.printTextWrap(9, 10, 1, 60, efectivo); // Columna 10
    printer.printTextWrap(10, 11, 1, 60, abonoletras);
    printer.printTextWrap(12, 13, 1, 60, "___________________________________");
    printer.printTextWrap(13, 14, 1, 60, nombre);
    printer.printTextWrap(14, 15, 1, 60, descripcion1);
    printer.printTextWrap(15, 16, 1, 60, descripcion2);
    printer.printTextWrap(16, 17, 1, 60, "HORA: " + hora);
    printer.printTextWrap(16, 17, 16, 60, cajero);
    printer.printTextWrap(17, 18, 1, 60, "MONTO CUBIERTO: " + montoCubierto);
    printer.printTextWrap(18, 19, 1, 60, "MONTO ASIGNADO: " + montoAsignado); // Columna 16

    return printer;
  }

  public PrinterMatrix imprimirCapSocial(
      String empresa,
      String rfc,
      String direc,
      String numsocio,
      String folio,
      String nomsocio,
      String abono,
      String abonoletras,
      String fecha,
      String hora,
      String parteMUT,
      String parteNGU) {
    PrinterMatrix printer = new PrinterMatrix();
    String numSocio = "SOCIO: " + numsocio;
    String nombre = "NOMBRE: " + nomsocio;
    String tipoCuenta = "TIPO DE CUENTA: CAPITAL SOCIAL - ABONO A CS";
    String efectivo = "ABONO: " + abono;
    String descripcion1 = "LA NO OBJECION A ESTE COMPROBANTE";
    String descripcion2 = "IMPLICA SU ACEPTACION";
    String cajero = "USUARIO: " + LoginController.usuarioLoggeado;
    String psmut = "PS MUT: " + parteMUT;
    String psngu = "PS NGU: " + parteNGU;
    //    String totalAhorro = "AHORRO: " + ahorro;
    Extenso e = new Extenso();
    e.setNumber(21.59);
    printer.setOutSize(30, 60);
    printer.printTextWrap(1, 2, 1, 60, empresa); // Columna 1
    printer.printTextWrap(2, 3, 1, 60, "RFC: " + rfc); // Columna 2
    printer.printTextWrap(3, 4, 1, 60, direc); // Columna 3
    printer.printTextWrap(4, 5, 1, 60, "FECHA: " + fecha); // Columna 4
    printer.printTextWrap(5, 6, 1, 60, numSocio);
    printer.printTextWrap(5, 6, 30, 60, "FOLIO: " + folio); // Columna 5// Columna 6
    printer.printTextWrap(6, 7, 1, 60, nombre); // Columna 7
    printer.printTextWrap(7, 8, 1, 60, tipoCuenta);
    printer.printTextWrap(9, 10, 1, 60, efectivo);
    printer.printTextWrap(10, 11, 1, 60, abonoletras);
    printer.printTextWrap(12, 13, 1, 60, "___________________________________");
    printer.printTextWrap(13, 14, 1, 60, nombre);
    printer.printTextWrap(14, 15, 1, 60, descripcion1);
    printer.printTextWrap(15, 16, 1, 60, descripcion2);
    printer.printTextWrap(16, 17, 1, 60, "HORA: " + hora);
    printer.printTextWrap(16, 17, 16, 60, cajero);
    if (Integer.parseInt(numsocio) <= 8543) {
      printer.printTextWrap(17, 18, 1, 60, psmut);
      printer.printTextWrap(17, 18, 25, 60, psngu);
    } else {
      printer.printTextWrap(17, 18, 1, 60, psngu);
    }

    return printer;
  }

  public PrinterMatrix imprimirCancelarAhorro(
      String empresa,
      String rfc,
      String direc,
      String numsocio,
      String folio,
      String nomsocio,
      String numcuenta,
      String montoextraido,
      String montoextraidoletras,
      String ahorrooriginal,
      String ahorroactualizado,
      String fecha,
      String hora) {
    PrinterMatrix printer = new PrinterMatrix();
    String numSocio = "SOCIO: " + numsocio;
    String nombre = "NOMBRE: " + nomsocio;
    String cuenta = "No. CUENTA: " + numcuenta;
    String tipoCuenta = "COMPROBANTE DE CANCELACION DE ABONO A AHORRO";
    String efectivo = "MONTO CANCELADO: " + montoextraido;
    String descripcion1 = "LA NO OBJECION A ESTE COMPROBANTE";
    String descripcion2 = "IMPLICA SU ACEPTACION";
    String antiguo = "AHORRO ANTES DE CANCELACION: " + ahorrooriginal;
    String nuevo = "AHORRO ACTUAL: "+ ahorroactualizado;
    String cajero = "USUARIO: " + LoginController.usuarioLoggeado;
    Extenso e = new Extenso();
    e.setNumber(21.59);
    printer.setOutSize(30, 60);
    printer.printTextWrap(1, 2, 1, 60, empresa); // Columna 1
    printer.printTextWrap(2, 3, 1, 60, "RFC: " + rfc); // Columna 2
    printer.printTextWrap(3, 4, 1, 60, direc); // Columna 3
    printer.printTextWrap(4, 5, 1, 60, "FECHA: " + fecha); // Columna 4
    printer.printTextWrap(5, 6, 1, 60, numSocio);
    printer.printTextWrap(5, 6, 30, 60, "FOLIO: " + folio); // Columna 5// Columna 6
    printer.printTextWrap(6, 7, 1, 60, nombre); // Columna 7
    printer.printTextWrap(7, 8, 1, 60, cuenta); // Columna 8
    printer.printTextWrap(8, 9, 1, 60, tipoCuenta);

    int numchar = montoextraidoletras.length();
    if (numchar <= 43) {
      printer.printTextWrap(10, 11, 1, 60, efectivo);
      printer.printTextWrap(11, 12, 1, 60, montoextraidoletras);
      printer.printTextWrap(13, 14, 1, 60, "___________________________________");
      printer.printTextWrap(14, 15, 1, 60, nombre);
      printer.printTextWrap(15, 16, 1, 60, descripcion1);
      printer.printTextWrap(16, 17, 1, 60, descripcion2);
      printer.printTextWrap(17, 18, 1, 60, "HORA: " + hora);
      printer.printTextWrap(17, 18, 16, 60, cajero); // Columna 16
      printer.printTextWrap(18, 19, 1, 60, antiguo); // Columna 17// Columna 18
      printer.printTextWrap(19, 20, 1, 60, nuevo);

    } else {
      String primeralinea = "";
      String segundalinea = "";
      String[] partes = montoextraidoletras.split("(?i)\\s*CON\\s*");
      if (partes.length >= 2) {
        primeralinea = partes[0];
        segundalinea = "CON " + partes[1];
      }
      printer.printTextWrap(9, 10, 1, 60, efectivo);
      printer.printTextWrap(10, 11, 1, 60, primeralinea);
      printer.printTextWrap(11, 12, 1, 60, segundalinea);
      printer.printTextWrap(13, 14, 1, 60, "___________________________________");
      printer.printTextWrap(14, 15, 1, 60, nombre);
      printer.printTextWrap(15, 16, 1, 60, descripcion1);
      printer.printTextWrap(16, 17, 1, 60, descripcion2);
      printer.printTextWrap(17, 18, 1, 60, "HORA: " + hora);
      printer.printTextWrap(17, 18, 16, 60, cajero); // Columna 16
      printer.printTextWrap(18, 19, 1, 60, antiguo);
      printer.printTextWrap(19, 20, 1, 60, nuevo);// Columna 17// Columna 18
    }

    return printer;
  }

  public PrinterMatrix imprimirCancelacionPrevision(
          String empresa,
          String rfc,
          String direc,
          String numsocio,
          String folio,
          String nomsocio,
          String abonocancelado,
          String abonoletras,
          String montopagadoviejo,
          String montoasignadoviejo,
          String montopagadonuevo,
          String montoasignadonuevo,
          String fecha,
          String hora
          ) {

    PrinterMatrix printer = new PrinterMatrix();
    String numSocio = "SOCIO: " + numsocio;
    String nombre = "NOMBRE: " + nomsocio;
    String tipoCuenta = "COMPROBANTE DE CANCELACION ABONO A PREVISION SOCIAL";
    String efectivo = "MONTO CANCELADO: " ;
    String descripcion1 = "LA NO OBJECION A ESTE COMPROBANTE";
    String descripcion2 = "IMPLICA SU ACEPTACION";
    String cajero = "USUARIO: " + LoginController.usuarioLoggeado;
    Extenso e = new Extenso();
    e.setNumber(21.59);
    printer.setOutSize(30, 60);
    printer.printTextWrap(1, 2, 1, 60, empresa); // Columna 1
    printer.printTextWrap(2, 3, 1, 60, "RFC: " + rfc); // Columna 2
    printer.printTextWrap(3, 4, 1, 60, direc); // Columna 3
    printer.printTextWrap(4, 5, 1, 60, "FECHA: " + fecha); // Columna 4
    printer.printTextWrap(5, 6, 1, 60, numSocio);
    printer.printTextWrap(5, 6, 30, 60, "FOLIO: " + folio); // Columna 5// Columna 6
    printer.printTextWrap(6, 7, 1, 60, nombre); // Columna 7
    printer.printTextWrap(7, 8, 1, 60, tipoCuenta);
    printer.printTextWrap(9, 10, 1, 60, efectivo); // Columna 10
    printer.printTextWrap(10, 11, 1, 60, abonoletras);
    printer.printTextWrap(12, 13, 1, 60, "___________________________________");
    printer.printTextWrap(13, 14, 1, 60, nombre);
    printer.printTextWrap(14, 15, 1, 60, descripcion1);
    printer.printTextWrap(15, 16, 1, 60, descripcion2);
    printer.printTextWrap(16, 17, 1, 60, "HORA: " + hora);
    printer.printTextWrap(16, 17, 16, 60, cajero);
    printer.printTextWrap(17, 18, 1, 60, "MC ANT: " + montopagadoviejo);
    printer.printTextWrap(18, 19, 1, 60, "MA ANT: " + montoasignadoviejo);
    printer.printTextWrap(17, 18, 20, 60, "MC NUV: " + montopagadonuevo);
    printer.printTextWrap(18, 19, 20, 60, "MA NUV: " + montoasignadonuevo);// Columna 16

    return printer;
  }
  public PrinterMatrix imprimirCancelacionCapSocial(
          String empresa,
          String rfc,
          String direc,
          String numsocio,
          String folio,
          String nomsocio,
          String abonocancelado,
          String abonoletras,
          String fecha,
          String hora,
          String parteMUTAntigua,
          String parteNGUAntigua,
          String parteMUTNueva,
          String parteNGUNueva) {
    PrinterMatrix printer = new PrinterMatrix();
    String numSocio = "SOCIO: " + numsocio;
    String nombre = "NOMBRE: " + nomsocio;
    String tipoCuenta = "TIPO DE CUENTA: CAPITAL SOCIAL - ABONO A CS";
    String efectivo = "ABONO CANCELADO: " + abonocancelado;
    String descripcion1 = "LA NO OBJECION A ESTE COMPROBANTE";
    String descripcion2 = "IMPLICA SU ACEPTACION";
    String cajero = "USUARIO: " + LoginController.usuarioLoggeado;
    String psmutantigua = "PS MUT ANT: " + parteMUTAntigua;
    String psnguantigua = "PS NGU ANT: " + parteNGUAntigua;

    String psmutnuev = "PS MUT NUV: " + parteMUTNueva;
    String psngunuev = "PS NGU NUV: " + parteNGUNueva;
    //    String totalAhorro = "AHORRO: " + ahorro;
    Extenso e = new Extenso();
    e.setNumber(21.59);
    printer.setOutSize(30, 60);
    printer.printTextWrap(1, 2, 1, 60, empresa); // Columna 1
    printer.printTextWrap(2, 3, 1, 60, "RFC: " + rfc); // Columna 2
    printer.printTextWrap(3, 4, 1, 60, direc); // Columna 3
    printer.printTextWrap(4, 5, 1, 60, "FECHA: " + fecha); // Columna 4
    printer.printTextWrap(5, 6, 1, 60, numSocio);
    printer.printTextWrap(5, 6, 30, 60, "FOLIO: " + folio); // Columna 5// Columna 6
    printer.printTextWrap(6, 7, 1, 60, nombre); // Columna 7
    printer.printTextWrap(7, 8, 1, 60, tipoCuenta);
    printer.printTextWrap(9, 10, 1, 60, efectivo);
    printer.printTextWrap(10, 11, 1, 60, abonoletras);
    printer.printTextWrap(12, 13, 1, 60, "___________________________________");
    printer.printTextWrap(13, 14, 1, 60, nombre);
    printer.printTextWrap(14, 15, 1, 60, descripcion1);
    printer.printTextWrap(15, 16, 1, 60, descripcion2);
    printer.printTextWrap(16, 17, 1, 60, "HORA: " + hora);
    printer.printTextWrap(16, 17, 16, 60, cajero);
    if (Integer.parseInt(numsocio) <= 8543) {
      printer.printTextWrap(17, 18, 1, 60, psmutantigua);
      printer.printTextWrap(17, 18, 25, 60, psmutnuev);
      printer.printTextWrap(18, 19, 1, 60, psnguantigua);
      printer.printTextWrap(18, 19, 25, 60, psngunuev);
    } else {
      printer.printTextWrap(17, 18, 1, 60, psnguantigua);
      printer.printTextWrap(18, 19, 25, 60, psngunuev);
    }

    return printer;
  }
}
