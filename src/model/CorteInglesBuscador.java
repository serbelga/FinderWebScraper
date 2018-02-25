package model;

import org.json.simple.parser.JSONParser;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.ArrayList;
import org.json.simple.*;

import javax.swing.*;

public class CorteInglesBuscador implements Buscador {
    private WebDriver driver;
    private WebDriverWait waiting;
    private WebElement leftNav;

    public CorteInglesBuscador(WebDriver driver) {
        this.driver = driver;
        driver.get("https://www.elcorteingles.es/electrodomesticos/cafeteras/?level=6");
    }

    @Override
    public void addToListaCafeteras(ArrayList<Producto> listaCafeteras){
        waitProductList();
        ArrayList<WebElement> resultados = (ArrayList<WebElement>) driver.findElements(By.xpath("//li[contains(@class, 'product ')]"));
        for (int i = 0; i < resultados.size(); i++) {
            WebElement actual_Elemento = resultados.get(i);
            Producto p;
            String nombre, precio, marca, ean;
            try {
                nombre = actual_Elemento.findElement(By.className("product-name")).findElement(By.xpath("./descendant::a")).getAttribute("title");
            } catch (WebDriverException e) {
                nombre = "";
            }
            try {
                marca = actual_Elemento.findElement(By.className("product-name")).findElement(By.xpath("./descendant::h4")).getText();
                if(marca.length() > 2) marca = marca.substring(0,1) + marca.substring(1).toLowerCase(); //Solo primera letra en mayúsculas
            } catch (WebDriverException e) {
                marca = "";
            }
            try {
                precio = actual_Elemento.findElement(By.className("product-price")).findElement(By.xpath("./span")).getText();
            } catch (WebDriverException e) {
                precio = "";
            }
            //Extraer EAN del producto para una posible búsqueda posterior, sino, le asigna el nombre para poder realizar la búsqueda
            try{
                WebElement a = actual_Elemento.findElement(By.xpath("./descendant::span"));
                String b = a.getAttribute("data-json");
                //System.out.println(b);
                JSONParser jsonParser = new JSONParser();
                JSONObject jsonObject = (JSONObject) jsonParser.parse(b);
                ean = (String) jsonObject.get("gtin");
                //System.out.println(ean);
            } catch (Exception e){
                ean = nombre;
            }
            p = new Producto(nombre, precio, "X", marca);
            p.setEan(ean);
            listaCafeteras.add(p);
        }
    }

    @Override
    public ArrayList<Producto> getListaCafeteras() {
        //Ampliar número de productos por página
        numElemPerPag();
        ArrayList<Producto> listaCafeteras = new ArrayList<>();
        boolean nextPag = true;
        //Pasar páginas
        while (nextPag) {
            try {
                //Añadir productos a la lista de cafeteras y pasa la siguiente página si existe
                addToListaCafeteras(listaCafeteras);
                try{
                    Thread.sleep(2000);
                } catch (InterruptedException e){

                }
                WebElement next = driver.findElement(By.id("product-list")).findElement(By.xpath("//a[contains(text(), 'Siguiente')]"));
                next.click();
            } catch (WebDriverException e){
                nextPag = false;
            }
        }
        return listaCafeteras;
    }

    @Override
    public void selectCafeterasCapsulas(){
        waitFilters();
        WebElement tipoLeftNav = leftNav.findElement(By.xpath("//li[1]/ul/li[2]/a"));
        tipoLeftNav.click();
    }

    @Override
    public void selectCafeterasGoteo(){
        waitFilters();
        WebElement tipoLeftNav = leftNav.findElement(By.xpath("//li[1]/ul/li[3]/a"));
        tipoLeftNav.click();
    }

    @Override
    public void selectCafeterasEspresso(){
        waitFilters();
        WebElement tipoLeftNav = leftNav.findElement(By.xpath("//li[1]/ul/li[5]/a"));
        tipoLeftNav.click();
    }

    @Override
    public void selectCafeterasAutomaticas(){
        waitFilters();
        WebElement tipoLeftNav = leftNav.findElement(By.xpath("//li[1]/ul/li[4]/a"));
        tipoLeftNav.click();
    }

    @Override
    public ArrayList<Producto> getProductosMarca(ArrayList<String> marca) throws ListaVaciaException {
        int marcasDisponibles = 0;
        //Seleccionar todas las marcas seleccionadas por el usuario
        for(String m : marca){
            WebElement leftNavMarca = driver.findElement(By.id("filters"));
            try {
                doWait();
                WebElement selectMarca = leftNavMarca.findElement(By.xpath("//a[contains(@title, '" + m + "')]"));
                selectMarca.click();
                marcasDisponibles++; //Ha encontrado la marca
            } catch (WebDriverException e1){
                try {
                    WebElement masMarca = driver.findElement(By.xpath("//*[contains(text(), 'Mostrar más')]"));
                    masMarca.click();
                    waitModalWindow();
                    try {
                        WebElement selectMarca = driver.findElement(By.id("modal")).findElement(By.id(m));
                        selectMarca.click();
                        driver.findElement(By.id("modal")).findElement(By.id("mdl-url-filter")).click();
                        marcasDisponibles++;
                    } catch (WebDriverException e){
                        driver.findElement(By.id("modal-close")).click();
                        WebElement aux = driver.findElement(By.xpath("//*[@id=\"price-slider-container\"]/p"));
                        Actions moveMouse = new Actions(driver);
                        moveMouse.moveToElement(aux).build().perform();
                        doWait();
                    }
                } catch (NoSuchElementException er){
                    System.out.println("Marca no encontrada.");
                }
            }
        }
        if(marcasDisponibles == 0) throw new ListaVaciaException("No se ha encontrado ningún producto con las características especificadas");
        //Devolver la lista de cafeteras
        return getListaCafeteras();
    }

    private void waitFilters(){
        waiting = new WebDriverWait(driver, 10);
        waiting.until(ExpectedConditions.presenceOfElementLocated(By.id("filters")));
        leftNav = driver.findElement(By.id("filters"));
    }

    private void waitModalWindow(){
        waiting = new WebDriverWait(driver, 10);
        waiting.until(ExpectedConditions.presenceOfElementLocated(By.id("modal")));
    }

    private void waitProductList(){
        waiting = new WebDriverWait(driver, 10);
        waiting.until(ExpectedConditions.presenceOfElementLocated(By.id("product-list")));
    }

    private void numElemPerPag(){
        WebElement productosPorPag = driver.findElement(By.xpath("//*[@id=\"elements-per-page\"]/li[2]"));
        productosPorPag.click();
    }

    private void doWait(){
        try{
            Thread.sleep(1000);
        } catch (InterruptedException e){

        }
    }
}
