/**
 * 
 */
package party;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import utility.Query;
import utility.VO;

/**
 * @author chenqian
 *
 */
public class Client {

	
	/**
	 * 
	 */
	public Client() {
		// TODO Auto-generated constructor stub
	}
	
	public void rangeQuery(ServiceProvider serviceProvider, String fileName) {
		Scanner in;
		try {
			in = new Scanner(new File(fileName));
			while(in.hasNext()) {
				String[] tks = in.nextLine().split(" ");
				if (tks.length != 2) {
					//TODO
				} else {
					Query query = new Query(Integer.parseInt(tks[0]), Integer.parseInt(tks[1]));
					VO vo = serviceProvider.rangeQuery(query);
					
				}
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
