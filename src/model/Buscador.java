package model;

import java.util.ArrayList;

public interface Buscador {
    //Métodos para seleccionar el tipo de cafetera del filtro lateral
    void selectCafeterasCapsulas();
    void selectCafeterasGoteo();
    void selectCafeterasEspresso();
    void selectCafeterasAutomaticas();
    //Devuelve la lista de productos dada la lista de marcas seleccionadas en el desplegable
    ArrayList<Producto> getProductosMarca(ArrayList<String> marca) throws ListaVaciaException;
    void addToListaCafeteras(ArrayList<Producto> listaCafeteras);
    //Devuelve los productos que hay actualmente en la lista de productos
    ArrayList<Producto> getListaCafeteras();
}
