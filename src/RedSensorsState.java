import java.util.*;
import IA.Red.*;

public class RedSensorsState {
    private final ArrayList<IA.Red.Centro> datacenters;
    private final ArrayList<IA.Red.Sensor> sensors;
    private double [][] map;
    private final double [][] dist;
    private final int n, m;

    public RedSensorsState(int ncent, int nsens, int seedc, int seeds, boolean option){
        datacenters = new CentrosDatos(ncent, seedc);
        sensors = new Sensores(nsens, seeds);
        n = ncent;
        m = ncent+nsens;
        map = new double[m][m];
        dist = new double[m][m];
        if (option) initialSolution1();
        else initialSolution2();
    }

    public void initialSolution1(){
        int X1, Y1, X2,  Y2;
        for(int i = 0; i < m; ++i){
            if (i < n) {X1 = datacenters.get(i).getCoordX(); Y1 = datacenters.get(i).getCoordY(); }
            else { X1 = sensors.get(i-n).getCoordX(); Y1 = sensors.get(i-n).getCoordY(); }
            for(int j = 0; j < m; ++j){
                dist[i][j] = 0; map[i][j]=0;
                if (i != j && (i >= n || j >= n)){
                    if (i >= n){
                        if (j < n) {
                            X2 = datacenters.get(j).getCoordX();
                            Y2 = datacenters.get(j).getCoordY();
                            if ((i < (25+n)+j*25) && (i >= (25+n)+(j-1)*25)) map[i][j] = sensors.get(i-n).getCapacidad();
                        }
                        else {
                            X2 = sensors.get(j - n).getCoordX();
                            Y2 = sensors.get(j - n).getCoordY();
                            if (i >= ((25*n)+n) && ((i-100) < (3+n)+(j-n)*3) && ((i-100) >= (3+n)+(j-n-1)*3)) {
                                double data = sensors.get(i-n).getCapacidad();
                                map[i][j] = data;
                                update_volume(j, data);
                            }
                        }
                        dist[i][j] = distance(X1, Y1, X2, Y2);
                    }
                }
            }
        }
        for (int i = 0; i < m; ++i) {
            System.out.print("fila: " + i + " ");
            for (int j = 0; j < m; ++j)
                System.out.print(map[i][j] + " ");
            System.out.println("");
        }

    }

    public void initialSolution2(){ // falta mirar el caso en el que no se pueda evitar perdidas de datos
        int X1, Y1, X2,  Y2;
        boolean success = false;
        boolean lost = false;
        int sensor_not_connected = -1;
        for(int i = 0; i < m; ++i) {
            success = false;
            if (i < n) {
                X1 = datacenters.get(i).getCoordX();
                Y1 = datacenters.get(i).getCoordY();
            } else {
                X1 = sensors.get(i - n).getCoordX();
                Y1 = sensors.get(i - n).getCoordY();
            }
            for (int j = 0; j < m; ++j) {
                dist[i][j] = 0;
                map[i][j] = 0;
                if (i != j && (i >= n || j >= n)) {
                    if (i >= n) {
                        if (j < n) {
                            X2 = datacenters.get(j).getCoordX();
                            Y2 = datacenters.get(j).getCoordY();
                            double capture = sensors.get(i - n).getCapacidad();
                            if (canConnect(j, 25) && ((dataVolume2(j) + capture) <= (150))) {
                                map[i][j] = capture;
                                success = true;
                                break;
                            }
                        } else {
                            X2 = sensors.get(j - n).getCoordX();
                            Y2 = sensors.get(j - n).getCoordY();
                            double capture = sensors.get(i - n).getCapacidad();
                            if (canConnect(j, 3) && ((dataVolume(j) + capture) <= (3 * sensors.get(j - n).getCapacidad()))) {
                                map[i][j] = capture;
                                update_volume(j, capture);
                                success = true;
                                break;
                            }
                        }
                        dist[i][j] = distance(X1, Y1, X2, Y2);
                    }
                }
            }
            if (i >= n && !lost && !success) {
                lost = !success;
                sensor_not_connected = i;
            }
        }
            System.out.println("lost: "+lost+" sensor: "+sensor_not_connected);
            for (int i = 0; i < m; ++i) {
                System.out.print("fila: " + i + " ");
                for (int j = 0; j < m; ++j)
                    System.out.print(map[i][j] + " ");
                System.out.println("");
            }

    }

    public boolean canConnect(int j, int limit){ //optimizar, hacer que calcule la suma a la vez
        int numConnexions = 0;
        for(int i = n; i < m; ++i){
            if (map[i][j] > 0) ++numConnexions;
            if (numConnexions >= limit) {
                System.out.println("no se puede conectar");
                return false;
            }
        }
        return true;
    }

    public double distance(int X1, int Y1, int X2, int Y2){
        return ((double) X1 - (double) X2)*((double) X1 - (double) X2)+((double) Y1 - (double) Y2)*((double) Y1 - (double) Y2);
    }

    public double getDistance(int i, int j){
        if (i < 0 || i >= m || j < 0 || j >= m) return -1;
        return dist[i][j];
    }

    public double getCost(int i, int j){
        if (i < 0 || i >= m || j < 0 || j >= m) return -1;
        return dist[i][j] * dataVolume(i);
    }

    public double dataVolume(int i){
        for(int j = 0; j < 4; ++j){
            double data = map[i][j];
            if (data > 0) return data;
        }
        return -1;
    }

    public double dataVolume2(int j){ //Cambiar el nombre de la función
        // --> se podria reutilizar la matriz para guardar la suma!!
        int sum = 0;
        for(int i = n; i < m; ++i){
            double data = map[i][j];
            sum += map[i][j];
        }
        return -1;
    }
    
    public void update_volume(int i, double data){
        for(int j = 0; j < 4; ++j){
            if (map[i][j] > 0)
            {
                map[i][j] += data;
                break;
            }
        }
    }


}