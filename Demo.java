/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package demo;
import java.awt.Color;
import java.awt.Graphics;
import java.sql.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 *
 * @author Tanar
 */
public class Demo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        AdatbazisInicializalas.init();
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/parkolohazszimulacio?useSSL=false&serverTimezone=UTC",
                "root","");
        Session session = new Session(conn);
        String[] rendszamok = {"ABC123","XYZ789","DEF456","GHI321","JKL654",
                               "MNO987","PQR111","STU222","VWX333","YZA444"};
        Random rand = new Random();
        double egysar=50.0;

        ParkingPanel panel = new ParkingPanel();
        JFrame frame = new JFrame("Animált Parkolóház");
        frame.setSize(700,620);
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        new Timer(33,e->panel.animate()).start();

        // GUI frissítés thread
        new Thread(() -> {
            try {
                while(true){
                    List<Parkolas> all = new Query<>(Parkolas.class).getResultList(conn);
                    for(Parkolas p: all)
                        if(p.getAuto()==null) p.setAuto(session.find(Auto.class,p.getRendszam()));
                    panel.updateCars(all);
                    Thread.sleep(1000);
                }
            } catch(Exception e){ e.printStackTrace();}
        }).start();

        // Backend szimuláció thread
        new Thread(() -> {
            try{
                while(true){
                    String rsz = rendszamok[rand.nextInt(rendszamok.length)];
                    int szint = rand.nextInt(9)+1;

                    session.beginTransaction();
                    try{
                        Auto auto = session.find(Auto.class, rsz);
                        if(auto==null){ auto=new Auto(); auto.setRendszam(rsz); auto.setOsszeg(0.0); session.save(auto); }

                        List<Parkolas> aktiv = new Query<>(Parkolas.class)
                            .where(Parkolas_.RENDSZAM.eq(rsz))
                            .where(Parkolas_.VEGE.isNull())
                            .getResultList(conn);

                        if(!aktiv.isEmpty()){
                            Parkolas p = aktiv.get(0);
                            p.setVege(LocalDateTime.now());
                            //long minutes = Duration.between(p.getKezdete(), p.getVege()).toMinutes();
                            long minutes = Duration.between(p.getKezdete(), p.getVege()).toSeconds();
                            double fiz = minutes*(egysar/60.0);
                            auto.setOsszeg(auto.getOsszeg()+fiz);
                            session.markDirty(auto); session.markDirty(p);
                        } else {
                            Parkolas p = new Parkolas();
                            p.setRendszam(rsz); p.setKezdete(LocalDateTime.now());
                            p.setAuto(auto); p.setSzint(szint);
                            session.save(p);
                        }                        
                        session.commit();
                    } catch(Exception e){ session.rollback(); e.printStackTrace(); }

                    Thread.sleep(rand.nextInt(7000)+3000);
                }
            } catch(Exception e){ e.printStackTrace(); }
        }).start();
    }
    
}
class CarVisual {
    String rendszam;
    int x, y;
    int targetX, targetY;
    Color color;

    public CarVisual(String rendszam, int startX, int startY) {
        this.rendszam = rendszam;
        this.x = startX;
        this.y = startY;
        this.targetX = startX;
        this.targetY = startY;
        this.color = Color.GREEN;
    }

    public void moveTowardsTarget() {
        if (x < targetX) x++;
        if (x > targetX) x--;
        if (y < targetY) y++;
        if (y > targetY) y--;
    }
}

class ParkingPanel extends JPanel {
    List<CarVisual> cars = new ArrayList<>();

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 10; i++)
            g.fillRect(50, 50 + i*50, 100, 40);

        for (CarVisual car : cars) {
            g.setColor(car.color);
            g.fillRect(car.x, car.y, 40, 20);
            g.setColor(Color.BLACK);
            g.drawString(car.rendszam, car.x, car.y - 2);
        }
    }

    public void updateCars(List<Parkolas> parkolasok) {
        for (Parkolas p : parkolasok) {
            Optional<CarVisual> opt = cars.stream()
                .filter(c -> c.rendszam.equals(p.getRendszam()))
                .findFirst();
            if (opt.isPresent()) {
                CarVisual c = opt.get();
                if (p.getVege() == null) {
                    c.targetX = 60;
                    //c.targetY = 50 + Arrays.asList(p.getRendszam()).indexOf(p.getRendszam())*50;
                    c.targetY = 50 + (p.getSzint() - 1) * 50;
                    c.color = Color.GREEN;
                } else {
                    c.targetX = -50;
                    c.color = Color.RED;                    
                }
            } else {
                if (p.getVege() == null)
                    cars.add(new CarVisual(p.getRendszam(), -50, 50+(p.getSzint()-1)*50));
            }
        }
    }

    public void animate() {
        for (CarVisual c : cars) c.moveTowardsTarget();
        repaint();
    }
}
