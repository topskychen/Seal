/**
 * 
 */
package party;

import index.Query.QueryType;
import index.VO;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import spatialindex.IShape;
import spatialindex.Point;
import spatialindex.Region;
import utility.Simulator;
import utility.StatisticsQuery;

/**
 * @author chenqian
 * 
 */
public class Client {

	List<IShape>	queries	= new LinkedList<IShape>();
	StatisticsQuery	statQ	= null;
	Simulator		sim		= null;
	int 			queryLimit = 100;
	/**
	 * 
	 */
	public Client(StatisticsQuery statQ, Simulator sim) {
		this.statQ = statQ;
		this.sim = sim;
	}
	
	public void loadFile(String fileName) {
		queries.clear();
		Scanner in = null;
		try {
			if (sim.getQueryType() == QueryType.range_query) {
				in = new Scanner(new File(sim.getFileName() + "_" + sim.getDim() + "_" + sim.getQuerySize() + ".rq"));
				String[] tks = null;
				while (in.hasNext()) {
					tks = in.nextLine().split(" ");
					double[] lb = new double[tks.length / 2];
					for (int i = 0; i < lb.length; ++i) {
						lb[i] = Integer.parseInt(tks[i]);
					}
					double[] ub = new double[tks.length / 2];
					for (int i = 0; i < ub.length; ++i) {
						ub[i] = Integer.parseInt(tks[lb.length + i]);
					}
					Region query = new Region(lb, ub);
					queries.add(query);
				}
				in.close();
			} else if (sim.getQueryType() == QueryType.knn) {
				in = new Scanner(new File(sim.getFileName() + "_" + sim.getDim() + "_" + sim.getQuerySize() + ".rq"));
				String[] tks = null;
				while (in.hasNext()) {
					tks = in.nextLine().split(" ");
					double[] lb = new double[tks.length / 2];
					for (int i = 0; i < lb.length; ++i) {
						lb[i] = Integer.parseInt(tks[i]);
					}
					Point query = new Point(lb);
					queries.add(query);
				}
				in.close();
			} else if (sim.getQueryType() == QueryType.skyline) {
				//TODO
			} else {
				System.out.println("No such query type!");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void rangeQuery(ServiceProvider sp, int runId) {
		int queryCnt = 0;
		for (IShape query : queries) {
			if (queryCnt++ > queryLimit) break;
			VO vo = sp.rangeQuery(query, runId);
			if (!vo.verify(query)) {
				System.err.print("x");
			} else {
				System.out.print(".");
			}
			if (vo != null) {
				statQ.append(vo.getPrepareTime(), vo.getVerifyTime(),
						vo.getVOSize());
			}
		}
		System.out.println();
	}
	
	private Region prepareQueryFromKNN(ArrayList<IShape> points) {
		Region query = new Region(points.get(0).getMBR());
		for (IShape p : points) {
			query = query.combinedRegion(p.getMBR());
		}
		return query;
	} 
	
	public void knn(ServiceProvider sp, int runId, int k) {
		for (IShape query : queries) {
			Region rq = prepareQueryFromKNN(sp.kNN(query, k));
			VO vo = sp.rangeQuery(rq, runId);
			if (!vo.verify(query)) {
				System.err.println("x");
			} else {
			}
			if (vo != null) {
				statQ.append(vo.getPrepareTime(), vo.getVerifyTime(),
						vo.getVOSize());
			}
		}
	}
	
	ArrayList<Region> prepareQueriesFromSkyline(ArrayList<IShape> points) {
		ArrayList<Region> res = new ArrayList<Region>();
		Collections.sort(points, new Comparator<IShape>() {

			@Override
			public int compare(IShape o1, IShape o2) {
				double[] low1 = o1.getMBR().m_pLow;
				double[] low2 = o2.getMBR().m_pLow;
				for (int i = 0; i < low1.length; ++i) {
					if (low1[i] < low2[i]) {
						return -1;
					} else if (low1[i] > low2[i]) {
						return 1;
					}
				}
				return 0;
			}});
		for (int i = 0; i < points.size()-1; ++i) {
			Region region = new Region(points.get(i).getMBR().combinedRegion(points.get(i+1).getMBR()));
			region.m_pLow[0] = 0;
			res.add(region);
		}
		return res;
	}
	
	public void skyline(ServiceProvider sp, int runId) {
		ArrayList<IShape> points = sp.skyline();
		ArrayList<Region> queries = prepareQueriesFromSkyline(points);
		double prepareTime = 0, verifyTime = 0;
		long voSize = 0;
		for (Region query : queries) {
			VO vo = sp.rangeQuery(query, runId);
			if (!vo.verify(query)) {
				System.err.println("x");
			} else {
				
			}
			prepareTime += vo.getPrepareTime();
			verifyTime += vo.getVerifyTime();
			voSize += vo.getVOSize();
		}
		statQ.append(prepareTime, verifyTime, voSize);
	}

	/**
	 * @param args
	 *            sth
	 */
	public static void main(String[] args) {
	}
}
