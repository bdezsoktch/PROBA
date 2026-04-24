/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package demo;

import java.util.List;

/**
 *
 * @author Tanar
 */
// Auto.java
@Entity(table="autok")
public class Auto {
    @Id
    @Column(name="rendszam")
    private String rendszam;

    @Column(name="osszeg")
    private double osszeg;

    @OneToMany(mappedBy="auto")
    private List<Parkolas> parkolasok;

    public Auto() {}

    public double getOsszeg(){
        return osszeg;
    }
    public String getRendszam(){
        return rendszam;
    }
    public void setOsszeg(double osszeg){
        this.osszeg=osszeg;
    }
    public void setRendszam(String rendszam){
        this.rendszam=rendszam;
    }
}
