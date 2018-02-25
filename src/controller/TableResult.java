package controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Producto;

import java.net.URL;
import java.util.ResourceBundle;

public class TableResult implements Initializable {
    @FXML
    public TableView<Producto> tableView;
    @FXML
    private TableColumn<Producto, String> productoColumna, precioCorteIngles, precioFnac, marca;

    public ObservableList<Producto> datosTaula;

    public void initialize(URL location, ResourceBundle resources) {
        productoColumna.setCellValueFactory(cell  -> new SimpleStringProperty(cell.getValue().getNombre()));
        precioCorteIngles.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPrecioCorteIngles()));
        precioFnac.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getPrecioFnac()));
        marca.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMarca()));
    }
}
