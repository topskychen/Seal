/**
 * 
 */
package party;

import index.VO;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;

import spatialindex.IShape;
import spatialindex.Point;
import spatialindex.Region;
import utility.Global;
import utility.Global.MODE;
import utility.StatisticsQuery;

/**
 * @author chenqian
 * 
 */
public class Client {

	StatisticsQuery	statQ	= null;

	/**
	 * 
	 */
	public Client(StatisticsQuery statQ) {
		this.statQ = statQ;
	}

	public void rangeQuery(ServiceProvider serviceProvider, String fileName,
			int runId) {
		Scanner in;
		try {
			String[] tks = fileName.split("_");
			fileName = tks[0] + "_" + tks[tks.length - 1];
			in = new Scanner(new File(fileName + ".qr"));
			int lineNo = 0;
			while (in.hasNext()) {
				tks = in.nextLine().split(" ");
				lineNo++;
				if (lineNo > Global.QUERY_LIM)
					break;
				VO vo = null;
				double[] lb = new double[tks.length / 2];
				for (int i = 0; i < lb.length; ++i) {
					lb[i] = Integer.parseInt(tks[i]);
				}
				double[] ub = new double[tks.length / 2];
				for (int i = 0; i < ub.length; ++i) {
					ub[i] = Integer.parseInt(tks[lb.length + i]);
				}
				Region query = new Region(lb, ub);
				vo = serviceProvider.rangeQuery(query, runId);
				if (!vo.verify(query)) {
					if (!Global.BATCH_QUERY) {
						System.err.print("Fail verify!");
					} else {
						System.err.println("x");
					}
				} else {
					if (!Global.BATCH_QUERY) {
						System.out.println("Pass verify!");
					} else {
					}
				}
				if (!Global.BATCH_QUERY)
					System.out.println(vo.toString());
//				if (tks.length == 4) {
//				}
				if (vo != null) {
					if (Global.G_MODE != MODE.LAZY || lineNo != 1) {
						statQ.append(vo.getPrepareTime(), vo.getVerifyTime(),
								vo.getVOSize());
					}
				}
				if (lineNo % Global.PRINT_LIM == 0) {
					System.out.print(".");
				}
			}
			in.close();
			System.out.println();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private Region prepareQueryFromKNN(ArrayList<IShape> points) {
		Region query = new Region(points.get(0).getMBR());
		for (IShape p : points) {
			query = query.combinedRegion(p.getMBR());
		}
		return query;
	} 
	
	public void knn(ServiceProvider sp, String fileName, int runId, int k) {
		Scanner in;
		try {
			String[] tks = fileName.split("_");
			fileName = tks[0] + "_" + tks[tks.length - 1];
			in = new Scanner(new File(fileName + ".qr"));
			int lineNo = 0;
			while (in.hasNext()) {
				tks = in.nextLine().split(" ");
				lineNo++;
				if (lineNo > Global.QUERY_LIM)
					break;
				VO vo = null;
				double[] coords = new double[tks.length / 2];
				for (int i = 0; i < coords.length; ++i) {
					coords[i] = Integer.parseInt(tks[i]);
				}
				Point point = new Point(coords);
				Region query = prepareQueryFromKNN(sp.kNN(point, k));
//				System.out.println(query);
				vo = sp.rangeQuery(query, runId);
				if (!vo.verify(query)) {
					if (!Global.BATCH_QUERY) {
						System.err.print("Fail verify!");
					} else {
						System.err.println("x");
					}
				} else {
					if (!Global.BATCH_QUERY) {
						System.out.println("Pass verify!");
					} else {
					}
				}
				if (!Global.BATCH_QUERY)
					System.out.println(vo.toString());
				if (vo != null) {
					if (Global.G_MODE != MODE.LAZY || lineNo != 1) {
						statQ.append(vo.getPrepareTime(), vo.getVerifyTime(),
								vo.getVOSize());
					}
				}
				if (lineNo % Global.PRINT_LIM == 0) {
					System.out.print(".");
				}
			}
			in.close();
			System.out.println();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
//			System.out.println(points.get(i));
			Region region = new Region(points.get(i).getMBR().combinedRegion(points.get(i+1).getMBR()));
			region.m_pLow[0] = 0;
//			System.out.println(region);
			res.add(region);
		}
		return res;
	}
	
	public void skyline(ServiceProvider sp, int runId) {
//		TimerAD timer = new TimerAD();
		ArrayList<IShape> points = sp.skyline();
//		System.out.println(points.size());
//		System.out.println(timer.timeElapseinS());
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

	public static ArrayList<Region> initQuery(String fileName) {
		ArrayList<Region> res = new ArrayList<Region>();
		Scanner in;
		try {
			in = new Scanner(new File(fileName + ".qr"));
			while (in.hasNext()) {
				String[] tks = in.nextLine().split(" ");
				Region query = new Region(new double[] {
						Integer.parseInt(tks[0]), Integer.parseInt(tks[1]) },
						new double[] { Integer.parseInt(tks[2]),
								Integer.parseInt(tks[3]) });
				res.add(query);
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return res;
	}

}
