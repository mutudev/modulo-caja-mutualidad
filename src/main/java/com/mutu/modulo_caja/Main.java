package com.mutu.modulo_caja;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;

@SpringBootApplication

public class Main extends Application {

	public static ConfigurableApplicationContext context;

	public static void main(String[] args) {
		System.setProperty("java.awt.headless","false");
		SwingUtilities.invokeLater(()->{
			java.awt.Toolkit.getDefaultToolkit();
		});
		launch(); // esto inicia la app JavaFX
	}

	@Override
	public void start(Stage stage) throws Exception {
		// Inicia contexto de Spring
		context = SpringApplication.run(Main.class);

		// Carga FXML con controlador Spring
		FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/java/fx/login.fxml"));
		fxml.setControllerFactory(context::getBean);

		// Crea la escena
		Scene scene = new Scene(fxml.load());

		// Aplica CSS personalizado
		scene.getStylesheets().add(getClass().getResource("/assets/css/estilos.css").toExternalForm());

		// Aplica tema JMetro
		JMetro jMetro = new JMetro(Style.LIGHT);
		jMetro.setScene(scene);

		// Configura la ventana
		stage.setTitle("AUTENTICACIÓN DE USUARIO");

		Image icon = new Image(getClass().getResourceAsStream("/assets/images/logo.png"));
		stage.getIcons().add(icon);
		stage.setAlwaysOnTop(false);
		stage.setScene(scene);
		stage.setResizable(true);
		stage.centerOnScreen();
		stage.show();
	}
}
