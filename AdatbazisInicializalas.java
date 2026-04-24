/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demo;

/**
 *
 * @author Tanar
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement; //ennél jellemzően egy String-ként adjuk meg az SQL utasítást
import java.sql.PreparedStatement; //ennél pedig a String-ben megjelenhetnek ?, amikbe konkrétumokat később lesz lehetőségünk beépíteni

public class AdatbazisInicializalas {
    private static final String DB_URL="jdbc:mysql://localhost:3306";
    private static final String DB_NAME="parkolohazszimulacio";
    private static final String USER="root";
    private static final String PASSWORD="";
    public static void init(){
        try(Connection kapcsolat=DriverManager.getConnection(DB_URL,USER,PASSWORD);
                Statement utasitas=kapcsolat.createStatement()){
            utasitas.execute("CREATE DATABASE IF NOT EXISTS "+DB_NAME);
        }catch(Exception ex){
            System.out.println(ex.getMessage().startsWith("Communications link failure")?"Start the MySQL server and try again...":ex.getMessage());
            System.exit(1);
        }
        try(Connection kapcsolat=DriverManager.getConnection(DB_URL+"/"+DB_NAME,USER,PASSWORD);
                Statement utasitas=kapcsolat.createStatement()){
            utasitas.execute("""
                CREATE TABLE IF NOT EXISTS autok(
                    rendszam VARCHAR(7) PRIMARY KEY,
                    osszeg DOUBLE DEFAULT 0);""");
        }catch(Exception ex){}
        try(Connection kapcsolat=DriverManager.getConnection(DB_URL+"/"+DB_NAME,USER,PASSWORD);
                Statement utasitas=kapcsolat.createStatement()){
            utasitas.execute("""
                CREATE TABLE IF NOT EXISTS parkolasok(
                    sorszam INT AUTO_INCREMENT PRIMARY KEY,
                    rendszam VARCHAR(7),
                    kezdete DATETIME,
                    vege DATETIME,
                    szint INT,
                    FOREIGN KEY (rendszam) REFERENCES autok(rendszam));""");
        }catch(Exception ex){}        
    }
}
