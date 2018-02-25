package model;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.util.ArrayList;

public class AmazonBuscador implements Buscador {
    private WebDriver driver;
    private WebDriverWait waiting;

    public AmazonBuscador(WebDriver driver) {
        this.driver = driver;
        driver.get("http://www.amazon.es/");
    }

    @Override
    public void addToListaCafeteras(ArrayList<Producto> listaCafeteras) {
        doWait();
        ArrayList<WebElement> resultados = (ArrayList<WebElement>) driver.findElements(By.xpath("//*[contains(@class, 's-item-container')]"));
        System.out.println(resultados.size());
        for (int i = 0; i < resultados.size(); i++) {
            WebElement actual_Elemento = resultados.get(i);
            Producto p;
            String nombre = "";
            String precio = "";
            try { //Error al obtener nombre del producto
                nombre = actual_Elemento.findElement(By.xpath("./descendant::h2")).getText();
                try { //Error al obtener el precio del producto pero el nombre se ha obtenido correctamente
                    precio += actual_Elemento.findElement(By.className("a-price-whole")).getText() + ",";
                    precio += actual_Elemento.findElement(By.className("a-price-fraction")).getText() + "€";
                    p = new Producto(nombre, "X", precio, "");
                } catch (WebDriverException e1){
                    try {
                        precio = driver.findElement(By.id("result_" + i)).findElement(By.xpath("//span[contains(@class, 'a-size-base a-color-price s-price a-text-bold')]")).getText();
                        p = new Producto(nombre, "X", precio, "");
                    } catch (WebDriverException e2) {
                        p = new Producto(nombre, "X", "?", "");
                    }
                }
            } catch (WebDriverException e) {
                p = new Producto("", "X", "", "");
            }
            listaCafeteras.add(p);
        }
    }

    @Override
    public ArrayList<Producto> getListaCafeteras() {
        ArrayList<Producto> listaCafeteras = new ArrayList<>();
        boolean nextPag = true;
        int i = 0;
        while (nextPag && i < 2) {
            try {
                doWait();
                addToListaCafeteras(listaCafeteras);
                WebElement next = driver.findElement(By.id("pagnNextString"));
                Actions actions = new Actions(driver);
                actions.moveToElement(next);
                actions.perform();
                JavascriptExecutor jse = (JavascriptExecutor) driver;
                jse.executeScript("window.scrollBy(0,100)", "");
                next.click();
                i++;
            } catch (NoSuchElementException|ElementNotVisibleException e){
                System.out.println(e.getMessage());
                nextPag = false;
            }
        }
        return listaCafeteras;
    }

    public void initBusqueda(){
        String searchText = "Cafetera" + '\n';
        WebElement searchInputBox = driver.findElement(By.name("field-keywords"));
        searchInputBox.sendKeys(searchText);
    }

    @Override
    public void selectCafeterasCapsulas() {
        initBusqueda();
        waitFilters();
        WebElement leftNav = driver.findElement(By.id("leftNavContainer"));
        WebElement tipoLeftNav = leftNav.findElement(By.xpath("//ul[1]/ul/div/li[3]/span/a/span"));
        tipoLeftNav.click();
    }

    @Override
    public void selectCafeterasGoteo() {
        initBusqueda();
        waitFilters();
        WebElement leftNav = driver.findElement(By.id("leftNavContainer"));
        WebElement tipoLeftNav = leftNav.findElement(By.xpath("//ul[1]/ul/div/li[1]/span/a/span"));
        tipoLeftNav.click();
    }

    @Override
    public void selectCafeterasEspresso() {
        initBusqueda();
        waitFilters();
        WebElement leftNav = driver.findElement(By.id("leftNavContainer"));
        WebElement tipoLeftNav = leftNav.findElement(By.xpath("//ul[1]/ul/div/li[5]/span/a/span"));
        tipoLeftNav.click();
    }

    @Override
    public void selectCafeterasAutomaticas() {
        initBusqueda();
        waitFilters();
        WebElement leftNav = driver.findElement(By.id("leftNavContainer"));
        WebElement tipoLeftNav = leftNav.findElement(By.xpath("//ul[1]/ul/div/li[4]/span/a/span"));
        tipoLeftNav.click();
    }

    public void selectMarca(ArrayList<String> marca) {
        doWait();
        for(String m : marca){
            doWait();
            WebElement leftNavMarca = driver.findElement(By.id("leftNavContainer"));
            WebElement scrollMarca = leftNavMarca.findElement(By.xpath("//h4[contains(text(), 'Marca')]"));
            Actions actions = new Actions(driver);
            actions.moveToElement(scrollMarca);
            actions.perform();
            JavascriptExecutor jse = (JavascriptExecutor) driver;
            jse.executeScript("window.scrollBy(0,100)", "");
            try {
                doWait();
                WebElement listaMarcas = driver.findElement(By.xpath("//*[@id=\"leftNavContainer\"]/ul[5]/div"));
                WebElement selectMarca = listaMarcas.findElement(By.xpath("//*[contains(@class, 'a-checkbox-label')]/span[contains(text(), '"+m+"')]"));
                selectMarca.click();
            } catch (WebDriverException e1){
                try {
                    WebElement masMarca = driver.findElement(By.xpath("//*[@id=\"leftNavContainer\"]/ul[5]"));
                    WebElement selectVerMas = masMarca.findElement(By.xpath("//*[contains(text(), 'Ver más')]"));
                    selectVerMas.click();
                    WebElement listaMarcasVerMas = driver.findElement(By.id("refinementList"));
                    WebElement marcaElegida = listaMarcasVerMas.findElement(By.xpath("//*[contains(text(), '"+m+"')]"));
                    marcaElegida.click();
                } catch (NoSuchElementException er){
                    System.out.println("Marca no encontrada: "+m);
                    driver.navigate().back();
                }
            }
        }
    }

    @Override
    public ArrayList<Producto> getProductosMarca(ArrayList<String> marca) {
        return null;
    }

    private void waitFilters(){
        waiting = new WebDriverWait(driver, 10);
        waiting.until(ExpectedConditions.presenceOfElementLocated(By.id("leftNavContainer")));
    }

    private void waitProductList(){
        //waiting = new WebDriverWait(driver, 10).until(driver -> ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete"));
    }

    public void waitForLoad(WebDriver driver) {
        ExpectedCondition<Boolean> pageLoadCondition = new ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                    }
                };
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }

    public void buscarProducto(Producto p) {
        String searchText = p.getNombre() + '\n';
        String precio = "";
        try {
            driver.get("http://www.amazon.es/");
            WebElement searchInputBox = driver.findElement(By.name("field-keywords"));
            searchInputBox.sendKeys(searchText);
            waiting = new WebDriverWait(driver, 10);
            waiting.until(ExpectedConditions.presenceOfElementLocated(By.id("result_0")));
            WebElement actual_Elemento = driver.findElement(By.id("result_0"));
            precio += actual_Elemento.findElement(By.xpath("//*[@id=\"result_0\"]/div/div[4]/div[1]/a/span/span[2]/span[1]")).getText() + ",";
            precio += actual_Elemento.findElement(By.className("a-price-fraction")).getText() + "€";
            p.setPrecioFnac(precio);
        } catch(WebDriverException e) {
            try { //el precio tiene un formato diferente
                precio = driver.findElement(By.xpath("//*[@id=\"result_0\"]/div/div[4]/div[1]/a/span[2]")).getText();
                p.setPrecioFnac(precio);
            } catch (WebDriverException e1) {
                p.setPrecioFnac(precio);
            }
        }
    }

    public void waitForPageLoaded(WebDriver driver)
    {
        ExpectedCondition<Boolean> expectation = driver1 -> ((JavascriptExecutor) driver1).executeScript("return document.readyState").equals("complete");
        Wait<WebDriver> wait = new WebDriverWait(driver,30);
        try
        {
            wait.until(expectation);
        }
        catch(Throwable error)
        {
            System.err.println("Timeout waiting for Page Load Request to complete.");
        }
    }

    private void doWait(){
        try{
            Thread.sleep(2000);
        } catch (InterruptedException e){

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
