/**
 * 
 */
package party;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import spatialindex.Region;
import utility.Global;
import utility.Global.MODE;
import utility.StatisticsQuery;
import utility.VO;

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
					Region query = new Region(
							new double[] { Integer.parseInt(tks[0]),
									Integer.parseInt(tks[1]) }, new double[] {
									Integer.parseInt(tks[2]),
									Integer.parseInt(tks[3]) });
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
				} else if (tks.length == 2) {
					Region query = new Region(
							new double[] { Integer.parseInt(tks[0]) },
							new double[] { Integer.parseInt(tks[1]) });
					vo = serviceProvider.rangeQuery(query, runId);
					if (!vo.verify(query)) {
						System.err.println("Fail verify!");
					} else {
						System.out.println("Pass verify!");
					}
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
