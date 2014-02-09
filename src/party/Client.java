/**
 * 
 */
package party;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import utility.Query;
import utility.StatisticsQuery;
import utility.StatisticsUpdate;
import utility.VO;

/**
 * @author chenqian
 *
 */
public class Client {

	StatisticsUpdate 	statU = null;
	StatisticsQuery		statQ = null;
	
	/**
	 * 
	 */
	public Client(StatisticsUpdate statU, StatisticsQuery statQ) {
		this.statU	= statU;
		this.statQ	= statQ;
	}
	
	public void rangeQuery(ServiceProvider serviceProvider, String fileName, int runId) {
		Scanner in;
		try {
			in = new Scanner(new File(fileName + ".qr"));
			while(in.hasNext()) {
				String[] 	tks = in.nextLine().split(" ");
				VO 			vo 	= null;
				if (tks.length == 4) {
					Query query = new Query(Integer.parseInt(tks[0]), Integer.parseInt(tks[1]), Integer.parseInt(tks[2]), Integer.parseInt(tks[3]));
					vo = serviceProvider.rangeQuery(query, runId);
					if (!vo.verify(query)) {
						System.out.println("Fail verify!");
					} else {
						System.out.println("Pass verify!");
					}
					System.out.println(vo.toString());
				} else if (tks.length == 2){
					Query query = new Query(Integer.parseInt(tks[0]), Integer.parseInt(tks[1]));
					vo = serviceProvider.rangeQuery(query, runId);
					if (!vo.verify(query)) {
						System.out.println("Fail verify!");
					} else {
						System.out.println("Pass verify!");
					}
					System.out.println(vo.toString());
				}
				statQ.append(vo.getPrepareTime(), vo.getVerifyTime(), vo.getVOSize());
			}
			in.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
