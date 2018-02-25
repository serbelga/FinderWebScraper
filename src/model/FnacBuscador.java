package model;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;

public class FnacBuscador implements Buscador {
    private WebDriver driver;
    private WebDriverWait waiting;

    public FnacBuscador(WebDriver driver) {
        this.driver = driver;
        driver.get("https://www.fnac.es/SearchResult/ResultList.aspx?SCat=23!1%2c23001!2&Search=cafetera&sft=1&sl=0.84762764");
    }

    @Override
    public void addToListaCafeteras(ArrayList<Producto> listaCafeteras) {
        waitPageLoad();
        WebElement productList = driver.findElement(By.id("dontTouchThisDiv"));
        ArrayList<WebElement> resultados = (ArrayList<WebElement>) productList.findElements(By.xpath("//li[contains(@class, 'Article-item')]"));
        System.out.println(resultados.size());
        //Recorre la lista de productos y obtiene nombre, marca y precio de cada producto
        for (int i = 0; i < resultados.size(); i++) {
            WebElement actual_Elemento = resultados.get(i);
            Producto p;
            String nombre, marca, precio;
            try {
                nombre = actual_Elemento.findElement(By.className("Article-desc")).findElement(By.xpath("./descendant::a")).getText();
            } catch (WebDriverException e){
                nombre = "";
            }
            try {
                marca = actual_Elemento.findElement(By.className("Article-descSub")).findElement(By.xpath("./descendant::span")).findElement(By.xpath("./descendant::a")).getText();
            } catch (WebDriverException e){
                try{
                    marca = actual_Elemento.findElement(By.className("moreInfos")).findElement(By.className("data")).getText();
                } catch (WebDriverException e1){
                    marca = "";
                }
            }
            try {
                precio = actual_Elemento.findElement(By.className("userPrice")).getText();
            } catch (WebDriverException e){
                precio = "";
            }
            p = new Producto(nombre,"X", precio, marca);
            listaCafeteras.add(p);
        }
    }

    @Override
    public ArrayList<Producto> getListaCafeteras() {
        ArrayList<Producto> listaCafeteras = new ArrayList<>();
        boolean nextPag = true;
        int i = 0;
        //Pasar páginas, hasta la 5, y añadir a la lista de productos, los productos de cada página
        while (nextPag && i < 4) {
            try {
                addToListaCafeteras(listaCafeteras);
                WebElement next = driver.findElement(By.className("top-toolbar-right")).findElement(By.xpath("//a[contains(@title, 'Página siguiente')]"));
                next.click();
                i++;
            } catch (WebDriverException e){
                nextPag = false;
            }
        }
        return listaCafeteras;
    }

    @Override
    public void selectCafeterasCapsulas() {
        waitFilters();
        WebElement tipoLeftNav = driver.findElement(By.xpath("//*[@id=\"col_gauche\"]/div/div[1]/div[3]/ul/li/ul/li[2]/a"));
        tipoLeftNav.click();
    }

    @Override
    public void selectCafeterasGoteo() {
        waitFilters();
        WebElement tipoLeftNav = driver.findElement(By.xpath("//*[@id=\"col_gauche\"]/div/div[1]/div[3]/ul/li/ul/li[3]/a"));
        tipoLeftNav.click();
    }

    @Override
    public void selectCafeterasEspresso() {
        waitFilters();
        WebElement tipoLeftNav = driver.findElement(By.xpath("//*[@id=\"col_gauche\"]/div/div[1]/div[3]/ul/li/ul/li[1]/a"));
        tipoLeftNav.click();
    }

    @Override
    public void selectCafeterasAutomaticas() {
        waitFilters();
        WebElement tipoLeftNav = driver.findElement(By.xpath("//*[@id=\"col_gauche\"]/div/div[1]/div[3]/ul/li/ul/li[1]/a"));
        tipoLeftNav.click();
    }

    @Override
    public ArrayList<Producto> getProductosMarca(ArrayList<String> marca) throws ListaVaciaException {
        //Cerrar ventana de cookies
        WebElement cerrar = driver.findElement(By.xpath("//*[@id=\"htmlPopinCookies\"]/p/i"));
        cerrar.click();
        ArrayList<Producto> productos = new ArrayList<>();
        for(String m : marca){
            doWait();
            WebElement filter = driver.findElement(By.xpath("//*[@id=\"col_gauche\"]/div/div[2]/div[1]"));
            filter.click();
            //Si ya se había seleccionado una marca, se deselecciona (En FNAC solo puede seleccionarse una marca a la vez), en caso contrario selecciona la marca[i]
            try {
                WebElement cerrarMarcaBuscada = driver.findElement(By.xpath("//*[@id=\"col_gauche\"]/div/div[2]/div[2]/div[2]/span/ul"));
                cerrarMarcaBuscada.click();
            } catch (WebDriverException e) {

            }
            try {
                //Seleccionar una marca, y añadir a la actual lista de productos
                String res = m.toLowerCase();
                res = m.substring(0, 1) + res.substring(1, res.length());
                doWait();
                WebElement listaMarcasYBoton = driver.findElement(By.className("js-FiltersContainer"));
                WebElement selectMarca = listaMarcasYBoton.findElement(By.xpath("//a[@title ='" + res + "']")).findElement(By.xpath("./descendant::label"));
                selectMarca.click();
                productos.addAll(getListaCafeteras());
            } catch (WebDriverException e1) {
                //No encuentra la marca en la lista, hace click en Ver más
                try {
                    WebElement selectVerMas = driver.findElement(By.xpath("//*[@id=\"col_gauche\"]/div/div[2]/div[3]/div[2]/button"));
                    selectVerMas.click();
                    String res = m.toLowerCase();
                    res = m.substring(0, 1) + res.substring(1, res.length());
                    WebElement listaMarcasYBoton = driver.findElement(By.className("js-FiltersContainer"));
                    WebElement selectMarca = listaMarcasYBoton.findElement(By.xpath("//a[@title ='" + res + "']")).findElement(By.xpath("./descendant::label"));
                    selectMarca.click();
                    productos.addAll(getListaCafeteras());
                } catch (NoSuchElementException e2) {
                    //No se ha encontrado la marca
                    System.out.println("Marca no encontrada: " + m);
                }
            }
        }
        if(productos.size() == 0) throw new ListaVaciaException("No se ha encontrado ningún producto con las características especificadas");
        return productos;
    }


    private void waitFilters(){
        waiting = new WebDriverWait(driver, 10);
        waiting.until(ExpectedConditions.presenceOfElementLocated(By.xpath("/html/body/div[2]/div/div[1]/div[2]")));
    }

    private void waitProductList() {
        waiting = new WebDriverWait(driver, 10);
        waiting.until(ExpectedConditions.presenceOfElementLocated(By.id("dontTouchThisDiv")));
    }
    
    private void doWait(){
        try{
            Thread.sleep(2000);
        } catch (InterruptedException e){

        }
    }

    public void buscarProducto(Producto p) {
        //Buscar un producto en Fnac
        //Escribir nombre del producto en el diálogo de búsqueda
        String searchText = p.getEan() + '\n';
        String nombre;
        String precio;
        try {
            driver.get("http://www.fnac.es/");
            waiting = new WebDriverWait(driver, 5);
            waiting.until(ExpectedConditions.presenceOfElementLocated(By.name("Search")));
            WebElement searchInputBox = driver.findElement(By.name("Search"));
            searchInputBox.sendKeys(searchText);
            waiting = new WebDriverWait(driver, 5);
            waiting.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"dontTouchThisDiv\"]/ul/li[1]")));
            nombre = driver.findElement(By.id("dontTouchThisDiv")).findElement(By.className("Article-desc")).findElement(By.xpath("./descendant::a")).getText();
            precio = driver.findElement(By.xpath("//*[@id=\"dontTouchThisDiv\"]/ul/li[1]")).findElement(By.className("userPrice")).getText();
            p.setNombre(nombre);
            p.setPrecioFnac(precio);
        } catch(WebDriverException e) {
            precio = "X";
            p.setPrecioFnac(precio);
        }
    }

    private void waitPageLoad(){
        try{
            while(true){
                Thread.sleep(1000);
                if((Boolean) ((JavascriptExecutor) driver).executeScript("return jQuery.active == 0")) break;
            }
        } catch(InterruptedException e){

        }
    }
}
