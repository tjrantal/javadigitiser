module timo.home{
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.swing;
	requires java.desktop;
	requires jcodec;
	exports timo.home;
	opens timo.home.fxml to javafx.fxml;
}