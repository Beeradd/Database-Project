//Simple alertbox prompt that interrupts current scene, transitions back to login scene
import java.util.*;
import javafx.application.Application;
import javafx.event.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.*;


public class AlertBox 
{
	static boolean answer;
	
	public static boolean display(String title)
	{
		Stage window = new Stage();
		
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle(title);
		window.setMinWidth(250);
		window.setResizable(false);
		
		Label lbl = new Label();
		lbl.setText("Are you sure you want to logout?");
		
		Button yes_btn = new Button("Yes");
		yes_btn.setOnAction(e -> 
		{
			answer = true;
			window.close();
		});
		
		Button no_btn = new Button("No");
		no_btn.setOnAction(e ->
		{
			answer = false;
			window.close();
		});
		
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(10));
		hbox.setSpacing(25);
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().addAll(yes_btn, no_btn);
		
		VBox vbox = new VBox();
		vbox.setPadding(new Insets(10));
		vbox.setSpacing(10);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll(lbl, hbox);
		
		Scene scene = new Scene(vbox);
		window.setScene(scene);
		window.showAndWait();
		
		return answer;
	}
	
	public static void printError(String title, String error)
	{
		Stage window = new Stage();
		
		window.initModality(Modality.APPLICATION_MODAL);
		window.setTitle(title);
		window.setMinWidth(250);
		window.setResizable(false);
		
		Label lbl = new Label();
		lbl.setText(error);
		lbl.setTextFill(Color.RED); 
		
		Button ok_btn = new Button("Ok");
		ok_btn.setOnAction(e-> window.close());
		
		HBox hbox = new HBox();
		hbox.setPadding(new Insets(10));
		hbox.setSpacing(25);
		hbox.setAlignment(Pos.CENTER);
		hbox.getChildren().add(ok_btn);
		
		VBox vbox = new VBox();
		vbox.setPadding(new Insets(10));
		vbox.setSpacing(10);
		vbox.setAlignment(Pos.CENTER);
		vbox.getChildren().addAll(lbl, hbox);
		
		Scene scene = new Scene(vbox);
		window.setScene(scene);
		window.showAndWait();
		
	}
}
