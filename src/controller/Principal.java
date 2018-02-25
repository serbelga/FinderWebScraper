package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 * FXML Controller class
 *
 * @author serbelga
 */
public class Principal implements Initializable {
    @FXML
    private ComboBox<String> comboBoxArticulo;
    @FXML
    private MenuButton menuButton;
    @FXML
    private CheckBox fnacCheckBox;
    @FXML
    private ArrayList<RadioMenuItem> radioMenuItemList;
    @FXML
    private CheckBox corteinglesCheckBox;
    @FXML
    private Button buscarButton;

    ChromeOptions options;
    boolean fnac, corteIngles;
    String tipo, marca, exePath;
    WebDriver driver;
    ArrayList<Producto> productos;
    ArrayList<String> marcasSelected;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        String[] tipos = {"Cafeteras de cápsulas", "Cafeteras de goteo", "Cafeteras espresso manuales", "Cafeteras automáticas"};
        String[] marcas = {"Bosch", "Bialetti", "Krups", "Ufesa", "Saeco", "Taurus", "AEG", "Fagor", "Electrolux", "Digrato", "Orbegozo", "Philips", "Aigostar", "Jura", "Jata", "Russell Hobbs", "Saivod", "Severin", "Haier", "Miele"};
        comboBoxArticulo.setItems(FXCollections.observableArrayList(tipos));
        for (int i = 0; i < marcas.length; i++){
            radioMenuItemList.get(i).setText(marcas[i]);
        }
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac")) exePath = "chromedriver";
        else exePath = "chromedriver.exe";
        System.setProperty("webdriver.chrome.driver", exePath);
        options = new ChromeOptions();
        options.addArguments("--start-maximized");
        productos = new ArrayList<>();
    }

    public void buscar(ActionEvent actionEvent) throws IOException {
        //Tipos de artículo y marcas seleccionadas
        buscarButton.setDisable(true);
        tipo = comboBoxArticulo.getValue();
        marcasSelected = new ArrayList<>();
        for (RadioMenuItem r : radioMenuItemList){
            if(r.isSelected()) marcasSelected.add(r.getText());
        }
        productos.clear();
        fnac = fnacCheckBox.isSelected();
        corteIngles = corteinglesCheckBox.isSelected();
        //Comparar productos
        if(corteIngles && fnac){
            //Búsqueda de productos en Corte Inglés según los filtros
            busquedaCorteIngles();
            //Búsqueda de cada producto de la lista de productos devueltos por el corte ingles en fnac para conocer su precio
            if(productos.size() > 0) {
                buscarProductosEnFnac(productos);
            }
            //Búsqueda de productos en Fnac según los filtros
            busquedaFnac();
            //En caso de que se encuentre un producto de el corte inglés en Fnac, se asigna el precio de fnac y se le cambia el nombre al que tiene en Fnac
            //Como se realiza una búsqueda también en fnac, pueden haber productos con el mismo nombre
            //El for se encarga de que en productos no haya repetidos y en caso de que los haya, se quede con el que tiene los dos precios
            for(int i = 0; i < productos.size()-1; i++){
                for(int j = i + 1; j < productos.size(); j++){
                    if (productos.get(i).getNombre().equals(productos.get(j).getNombre())) {
                        if(productos.get(i).getPrecioCorteIngles().equals("X")) productos.get(i).setPrecioCorteIngles(productos.get(j).getPrecioCorteIngles());
                        productos.remove(j);
                    }
                }
            }
            setResultado(productos);
        }
        else if (fnac) {
            busquedaFnac();
            if(productos.size() > 0) setResultado(productos);
        }
        else if (corteIngles) {
            busquedaCorteIngles();
            if(productos.size() > 0) setResultado(productos);
        }
        if (!corteIngles && !fnac) {
            getAlertDialog("Error", "Error de búsqueda", "Al menos una tienda debe estar seleccionada para realizar la búsqueda.");
        }
        buscarButton.setDisable(false);
    }

    private void busquedaAmazon() throws IOException {
        driver = new ChromeDriver(options);
        AmazonBuscador amazonBuscador = new AmazonBuscador(driver);
        realizarBusqueda(amazonBuscador);
    }

    private void busquedaFnac() throws IOException {
        driver = new ChromeDriver(options);
        FnacBuscador fnacBuscador = new FnacBuscador(driver);
        realizarBusqueda(fnacBuscador);
    }

    private void busquedaCorteIngles() throws IOException {
        driver = new ChromeDriver(options);
        CorteInglesBuscador corteInglesBuscador = new CorteInglesBuscador(driver);
        realizarBusqueda(corteInglesBuscador);
    }

    private void buscarProductosEnFnac(ArrayList<Producto> productos){
        driver = new ChromeDriver(options);
        ArrayList<String> productosEnFnacYCorteIngles = new ArrayList<>();
        FnacBuscador fnacBuscador = new FnacBuscador(driver);
        for(Producto p : productos){
            fnacBuscador.buscarProducto(p);
        }
    }

    public void setResultado(ArrayList<Producto> productos) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../view/tableResult.fxml"));
            Parent root = loader.load();
            TableResult tablaController = loader.getController();
            tablaController.datosTaula = FXCollections.observableArrayList(productos);
            tablaController.tableView.setItems(tablaController.datosTaula);
            Scene scene = new Scene(root);
            Stage tabla = new Stage();
            tabla.setMinHeight(350);
            tabla.setMinWidth(850);
            tabla.setTitle("Tabla");
            tabla.initModality(Modality.APPLICATION_MODAL);
            tabla.setScene(scene);
            tabla.showAndWait();
        } catch(IllegalArgumentException e){
            getAlertDialog("Error", "Error de creación de la tabla", "Error al crear la tabla que contiene los productos.");
        }
    }

    public void realizarBusqueda(Buscador buscador) throws IOException {
        //No se ha seleccionado tipo de artículo ni tipo de marca
        if (tipo == null && marcasSelected.size() == 0) {
            productos.addAll(buscador.getListaCafeteras());
            System.out.println(productos.size());
        }
        //Se ha seleccionado un tipo de artículo
        else if (tipo != null) {
            switch (tipo) {
                case "Cafeteras automáticas":
                    buscador.selectCafeterasAutomaticas();
                    break;
                case "Cafeteras de goteo":
                    buscador.selectCafeterasGoteo();
                    break;
                case "Cafeteras espresso manuales":
                    buscador.selectCafeterasEspresso();
                    break;
                case "Cafeteras de cápsulas":
                    buscador.selectCafeterasCapsulas();
                    break;
                default:
                    break;
            }
            //Se ha seleccionado una o más marcas
            if(marcasSelected.size() != 0)
                try {
                    productos.addAll(buscador.getProductosMarca(marcasSelected));
                } catch (ListaVaciaException e) {
                    getAlertDialog("Error", "Lista vacía", e.getMessage());
                }
                //No se ha seleccionado ninguna marca
            else productos.addAll(buscador.getListaCafeteras());
            System.out.println(productos.size());
        }
        //No se ha seleccionado tipo de artículo pero sí una o más marcas
        else {
            try {
                productos.addAll(buscador.getProductosMarca(marcasSelected));
            } catch (ListaVaciaException e) {
                getAlertDialog("Error", "Lista vacía", e.getMessage());
            }
            System.out.println(productos.size());
        }
    }

    private void getAlertDialog(String title, String headerText, String contentText){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
    }
}
