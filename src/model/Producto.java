package model;

public class Producto {
    private String nombre, precioCorteIngles, precioFnac, marca, ean;

    public Producto(String nombre, String precioCorteIngles, String precioFnac, String marca) {
        this.nombre = nombre;
        this.precioCorteIngles = precioCorteIngles;
        this.precioFnac = precioFnac;
        this.marca = marca;
        this.ean = "";
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPrecioCorteIngles() {
        return precioCorteIngles;
    }

    public void setPrecioCorteIngles(String precioCorteIngles) {
        this.precioCorteIngles = precioCorteIngles;
    }

    public String getPrecioFnac() {
        return precioFnac;
    }

    public void setPrecioFnac(String precioFnac) {
        this.precioFnac = precioFnac;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getEan() {
        return ean;
    }

    public void setEan(String ean) {
        this.ean = ean;
    }
}
