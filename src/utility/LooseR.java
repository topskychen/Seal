/**
 * 
 */
package utility;

import java.util.List;

import party.DataOwner;
import spatialindex.IShape;
import spatialindex.Point;
import spatialindex.Region;

/**
 * @author chenqian
 * 
 */
public class LooseR {

	int						start		= 0;
	int						len			= 250;
	int 					qlen 		= 50;
	List<DataOwner>	dataOwners;
	List<IShape>		queries;

	/**
	 * 
	 */
	public LooseR() {
		Sim sim = new Sim();
		sim.init();
		dataOwners = sim.dataOwners;
		sim.client.loadFile("./data/GO");
		queries = sim.client.getQueries();
	}

	public void run(double L) {
		double outOfBounds = 0;
		double interQueries = 0;
		for (DataOwner owner : dataOwners) {
			Point o = owner.getPoint(0);
			Region region = new Region(new double[] { o.m_pCoords[0]-L/2, o.m_pCoords[1]-L/2, o.m_pCoords[2]-L/2}, new double[] { o.m_pCoords[0] + L/2,
					o.m_pCoords[1] + L/2 , o.m_pCoords[2] + L/2});
			double a = 0;
			for (int i = 1; i < 20; i++) {
				Point point = (Point) owner.getPoint(i);
				if (point != null) {
					if (!region.contains(point)) {
						a++;
						o = owner.getPoint(i);
						region = new Region(new double[] { o.m_pCoords[0]-L/2, o.m_pCoords[1]-L/2, o.m_pCoords[2]-L/2}, new double[] { o.m_pCoords[0] + L/2,
								o.m_pCoords[1] + L/2 , o.m_pCoords[2] + L/2});
					}
				}
			}
			outOfBounds += a/19;
			a = 0;
			for (int q = 0; q < 50; q++) {
				Region query = (Region) queries.get(q);
				if (query.intersects(region)) {
					interQueries++;
				}
			}
		}
//		System.out.println("------------" + L + "--------------");
//		System.out.println(1.0 * outOfBounds/dataOwners.size());
//		System.out.println(1.0 * interQueries/dataOwners.size());
		System.out.println(1.0 * outOfBounds/dataOwners.size() + 0.2 * interQueries/dataOwners.size());
//		System.out.println("----------------------------");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		double L = 5000, tmp = L;
		LooseR looseR = new LooseR();		
		for (int i = 4; i >= -4; i--) {
			tmp = L/Math.pow(Math.sqrt(2), i);
			looseR.run(tmp);
		}
	}
}
