/**
 * 
 */
package party;

import index.VO;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
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
				if (tks.length == 4) {
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
				}
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
