import java.util.*;
import IA.Red.*;

public class RedSensorsState {
    private final ArrayList<IA.Red.Centro> datacenters;
    private final ArrayList<IA.Red.Sensor> sensors;
    private double [][] adjacencyMatrix;
    private final double [][] dist;
    private final int ncent, nElements;

    public RedSensorsState(int ncent, int nsens, int seedc, int seeds, int option){
        datacenters = new CentrosDatos(ncent, seedc);
        sensors = new Sensores(nsens, seeds);
        this.ncent = ncent;
        nElements = ncent+nsens;
        adjacencyMatrix = new double[nElements][nElements];
        dist = new double[nElements][nElements];
        switch (option) {
            case 2 -> initialSolution2();
            case 3 -> initialSolutionTestLoop();
            default -> initialSolution1();
        }
    }

    public RedSensorsState(int ncent, int nsens, double [][] dist, double [][] adjacencyMatrix, ArrayList<IA.Red.Centro> datacenters, ArrayList<IA.Red.Sensor> sensors){
        this.ncent = ncent;
        nElements = ncent+nsens;
        this.dist = dist;
        this.adjacencyMatrix = adjacencyMatrix;
        this.datacenters = datacenters;
        this.sensors = sensors;
    }

    public void initialSolution1(){
        int src_X, src_Y, dst_X,  dst_Y;
        for(int i = 0; i < nElements; ++i){
            if (i < ncent) {
							src_X = datacenters.get(i).getCoordX();
							src_Y = datacenters.get(i).getCoordY();
						}
            else { 
							src_X = sensors.get(i- ncent).getCoordX();
							src_Y = sensors.get(i- ncent).getCoordY();
						}
            for(int j = 0; j < nElements; ++j){
                dist[i][j] = 0; adjacencyMatrix[i][j]=0;
                if (i != j && (i >= ncent || j >= ncent)){
                    if (i >= ncent){
                        if (j < ncent) {
                            dst_X = datacenters.get(j).getCoordX();
                            dst_Y = datacenters.get(j).getCoordY();
                            if ((i < (25+ ncent)+j*25) && (i >= (25+ ncent)+(j-1)*25)) {
                                double capture = sensors.get(i- ncent).getCapacidad();
                                adjacencyMatrix[i][j] = capture;
                                adjacencyMatrix[i][i] =  j;
                                break;
                            }
                        }
                        else {
                            dst_X = sensors.get(j - ncent).getCoordX();
                            dst_Y = sensors.get(j - ncent).getCoordY();
                            if (i >= ((25* ncent)+ ncent)
																&& ((i-(25* ncent)) < (3+ ncent)+(j- ncent)*3)
																&& ((i-(25* ncent)) >= (3+ ncent)+(j- ncent -1)*3))
														{
                                double capture = sensors.get(i- ncent).getCapacidad();
                                double c = adjacencyMatrix[j][j];
                                int cd = (int) c;
                                adjacencyMatrix[i][j] = capture;
                                propagateThroughput(adjacencyMatrix[j][cd], sensors.get(j- ncent).getCapacidad()*3, sensors.get(i- ncent).getCapacidad(), cd, j );
                                adjacencyMatrix[i][i] = j;
                                break;
                            }
                        }
                        dist[i][j] = distance(src_X, src_Y, dst_X, dst_Y);
                    }
                }
            }
        }
       print_map();

    }


    public void initialSolution2(){
        int src_X, src_Y, dst_X,  dst_Y;
        for(int i = 0; i < nElements; ++i) {
            if (i < ncent) {
                src_X = datacenters.get(i).getCoordX();
                src_Y = datacenters.get(i).getCoordY();
            } else {
                src_X = sensors.get(i - ncent).getCoordX();
                src_Y = sensors.get(i - ncent).getCoordY();
            }
            for (int j = 0; j < nElements; ++j) {
                dist[i][j] = 0;
                adjacencyMatrix[i][j] = 0;
                if (i != j && (i >= ncent || j >= ncent)) {
                    if (i >= ncent) {
                        if (j < ncent) {
                            dst_X = datacenters.get(j).getCoordX();
                            dst_Y = datacenters.get(j).getCoordY();
                            double capture = sensors.get(i - ncent).getCapacidad();
                            if (canConnect(j, 25) && ((adjacencyMatrix[j][j] + capture) <= (150))) {
                                adjacencyMatrix[i][j] = capture;
                                adjacencyMatrix[i][i] =  j;
                                break;
                            }
                        } else {
                            dst_X = sensors.get(j - ncent).getCoordX();
                            dst_Y = sensors.get(j - ncent).getCoordY();
                            double capture = sensors.get(i - ncent).getCapacidad();
                            double c = adjacencyMatrix[j][j];
                            int cd = (int) c;
                            if (canConnect(j, 3) 
																&& ((adjacencyMatrix[j][cd] + capture) <= (3 * sensors.get(j - ncent).getCapacidad()))
																&& ((adjacencyMatrix[cd][cd] + capture) <= (150)) )
														{
                                adjacencyMatrix[i][j] = capture;
                                propagateThroughput(adjacencyMatrix[j][cd], sensors.get(j- ncent).getCapacidad()*3, sensors.get(i- ncent).getCapacidad(), cd, j );
                                adjacencyMatrix[i][i] = j;
                                break;
                            }
                        }
                        dist[i][j] = distance(src_X, src_Y, dst_X, dst_Y);
                    }
                }
            }
        }
         print_map();
    }
		public void initialSolutionTestLoop(){
        int src_X, src_Y, dst_X,  dst_Y;
        for(int i = ncent; i < nElements -1; ++i) {
						src_X = sensors.get(i- ncent).getCoordX();
						src_Y = sensors.get(i- ncent).getCoordY();
						dst_X = sensors.get(i+1- ncent).getCoordX();
						dst_Y = sensors.get(i+1- ncent).getCoordY();
						dist[i][i+1] = distance(src_X, src_Y, dst_X, dst_Y);
						adjacencyMatrix[i][i+1] = sensors.get(i- ncent).getCapacidad();
						adjacencyMatrix[i][i] = i+1;
				}
				src_X = sensors.get(nElements -1- ncent).getCoordX();
				src_Y = sensors.get(nElements -1- ncent).getCoordY();
				dst_X = sensors.get(0).getCoordX();
				dst_Y = sensors.get(0).getCoordY();
				dist[nElements -1][ncent] = distance(src_X, src_Y, dst_X, dst_Y);
				adjacencyMatrix[nElements -1][ncent] = sensors.get(nElements -1- ncent).getCapacidad();
				adjacencyMatrix[nElements -1][nElements -1] = ncent;
				print_map();
		}

		public double getCost(int nodo){
			// pre:nodo es un nodo
			// post:devuelve el coste de todos los hijos de nodo.
			ArrayList connected = getConnectedThroughput(nodo);
			if(connected.size() == 0 && nodo < ncent) return 0.0;
			if(connected.size() == 0){
				double dist = getDistance(nodo,(int)adjacencyMatrix[nodo][nodo]);
				return  dist * dist * sensors.get(nodo-ncent).getCapacidad();
			}
			double childrenThroughput = 0.0;
			for(int i = 0; i <  connected.size(); ++i){
				childrenThroughput+= getCost((int)connected.get(i));
			}
			double dist = getDistance(nodo,(int)adjacencyMatrix[nodo][nodo]);
			double throughput = childrenThroughput > 3*sensors.get(nodo-ncent).getCapacidad()?
				sensors.get(nodo-ncent).getCapacidad():childrenThroughput;
			return dist * dist * throughput;
		}

    public void propagateThroughput(double throughput, double capacity,double capture, int cd, int j){
        if (capture + throughput <= capacity) adjacencyMatrix[j][cd] += capture;
        else adjacencyMatrix[j][cd] = capacity;
    }

    public boolean canConnect(int j, int limit){
        int numConnexions = 0;
				ArrayList connections = getConnected(j);
				if(connections.size() >= limit) return false;
				ArrayList visited=new ArrayList();
				visited.add(j);
        return findLoop(j,visited);
    }
		private boolean findLoop(int j, ArrayList visited){
			int next = (int) adjacencyMatrix[j][j];
			if(next < ncent) return true;
            for (Object o : visited) {
                if ((int) o == next) return false;
            }
			visited.add(next);
			return findLoop(next,visited);
		}

    public double distance(int X1, int Y1, int X2, int Y2){
        return ((double) X1 - (double) X2)*((double) X1 - (double) X2)+((double) Y1 - (double) Y2)*((double) Y1 - (double) Y2);
    }

    public double getDistance(int i, int j){
        if (i < 0 || i >= nElements || j < 0 || j >= nElements) return -1;
        return dist[i][j];
    }

    public double getCost(int i, int j){
        if (i < 0 || i >= nElements || j < 0 || j >= nElements) return -1;
        return dist[i][j] * adjacencyMatrix[i][j];
    }

    public void print_map(){
        for (int i = 0; i < nElements; ++i) {
            System.out.print("fila: " + i + " ");
            for (int j = 0; j < nElements; ++j)
                System.out.print(adjacencyMatrix[i][j] + " ");
            System.out.println();
        }
    }

    public ArrayList getConnectedThroughput(int node){
	    // returns an ArrayList with thorugput the nodes connected to "node"
			ArrayList children = new ArrayList();
			for(int i = 0; i < nElements; ++i){
				if((i != node) && (adjacencyMatrix[i][node] != 0)){
					children.add((int)adjacencyMatrix[i][node]);
				}
			}
			return children;
    }
		 
    public ArrayList getConnected(int node){
	    // returns an ArrayList with thorugput the nodes connected to "node"
			ArrayList children = new ArrayList();
			for(int i = 0; i < nElements; ++i){
				if((i != node) && (adjacencyMatrix[i][node] != 0)){
					children.add(i);
				}
			}
			return children;
    }

    public void newConnection(int i, int j, int newConnection, double capture){
       disconnect(i,j,capture);
       connect(i,newConnection,capture);
    }

    private void disconnect(int i, int j, double capture){
        double c;
        adjacencyMatrix[i][j] = 0;
        adjacencyMatrix[i][i] = 0;
        if(j < ncent) adjacencyMatrix[j][j] -= capture;
        else{
            c = adjacencyMatrix[j][j];
            int cd = (int) c;
            adjacencyMatrix[j][cd] = dataVolume(j);
        }
    }

    public double dataVolume(int j){
        double sum = 0;
        int num_connexions = 0;
        for(int i = ncent; i < nElements; ++i){
            if (adjacencyMatrix[i][j] > 0){
                sum += adjacencyMatrix[i][j];
                ++num_connexions;
            }
            if(num_connexions >=25) return sum;
        }
        return 0;
    }

    private void connect(int i, int newConnection, double capture){
        double c;
        adjacencyMatrix[i][newConnection] = capture;
        adjacencyMatrix[i][i] = newConnection;
        if(newConnection >= ncent){
            c = adjacencyMatrix[newConnection][newConnection];
            int cd = (int) c;
            propagateThroughput(adjacencyMatrix[newConnection][cd], sensors.get(newConnection- ncent).getCapacidad()*3, sensors.get(i- ncent).getCapacidad(), cd, newConnection );
        }
    }

    public double heuristic(){return 0; }

		public int getNcent(){
					return ncent;
		}
		public int getnElements(){
			return nElements;
		}

		public double[][] getDist() {
				return dist;
		}

		public double[][] getAdjacencyMatrix() {
				return adjacencyMatrix.clone();
		}

		public ArrayList<Centro> getDatacenters() {
				return datacenters;
		}

		public ArrayList<Sensor> getSensors() {
				return sensors;
		}
}
