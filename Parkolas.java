/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demo;

import java.time.LocalDateTime;

/**
 *
 * @author Tanar
 */
// Parkolas.java
@Entity(table="parkolasok")
public class Parkolas {
    @Id
    @Column(name="sorszam")
    private int sorszam;

    @Column(name="rendszam")
    private String rendszam;

    @ManyToOne(joinColumn="rendszam")
    private Auto auto;

    @Column(name="kezdete")
    private LocalDateTime kezdete;

    @Column(name="vege")
    private LocalDateTime vege;

    @Column(name="szint")
    private int szint; 

    public Parkolas() {}
    
    public LocalDateTime getKezdete(){
        return kezdete;
    }
    public LocalDateTime getVege(){
        return vege;
    }
    public Auto getAuto(){
        return auto;
    }
    public String getRendszam(){
        return rendszam;
    }
    public int getSzint(){
        return szint;
    }
    public void setKezdete(LocalDateTime kezdete){
        this.kezdete=kezdete;
    }
    public void setVege(LocalDateTime vege){
        this.vege=vege;
    }
    public void setRendszam(String rendszam){
        this.rendszam=rendszam;
    }
    public void setAuto(Auto auto){
        this.auto=auto;
    }
    public void setSzint(int szint){
        this.szint=szint;
    }
}
