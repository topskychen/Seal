/**
 * 
 */
package utility;

import index.SearchIndex;
import io.IO;
import io.RW;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;

import timer.Timer;

/**
 * @author chenqian
 *
 */
public class VO implements RW{

	private Timer 		timer 			= null;
	private double 		prepareTime 	= -1;
	private double 		verifyTime 	= -1;
	private int 		voSize 			= -1;
	ArrayList<VOCell> 	voCells 		= null;
	Query				query 			= null;
	
	/**
	 * 
	 */
	public VO() {
		// TODO Auto-generated constructor stub
		timer = new Timer();
	}
	
	/**
	 * Prepare VO
	 */
	public void prepare(SearchIndex index, Query query) {
		timer.reset();
		voCells = index.rangeQuery(query);
		timer.stop();
		prepareTime = timer.timeElapseinMs();
	}
	
	public boolean verify(Query query) {
		timer.reset();
		boolean isVerify = true;
		for (VOCell voCell : voCells) {
			if (!voCell.verify(query)) {
				isVerify = false;
				break;
			}
		}
		timer.stop();
		verifyTime = timer.timeElapseinMs();
		voSize = IO.toBytes(this).length;
		return isVerify;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void read(DataInputStream ds) {
		// TODO Auto-generated method stub
		int size = IO.readInt(ds);
		if (size != 0) {
			for (int i = 0; i < size; i ++) {
				VOCell voCell = new VOCell();
				voCell.read(ds);
				voCells.add(voCell);
			}
		}
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		if (voCells == null) {
			IO.writeInt(ds, 0);
		} else {			
			IO.writeInt(ds, voCells.size()); 
			for (VOCell voCell : voCells) {
				voCell.write(ds);
			}
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("");
		sb.append("PrepareTime: " + prepareTime + "ms\n");
		sb.append("VerifyTime: " + verifyTime + "ms\n");
		sb.append("VOSize: " + voSize + "bytes, " + voSize / 1024.0 + " KB\n");
//		if(!precise){
//			for (int i = 0; i < voCells.size(); i ++) {
//				sb.append((i + 1)  + " [ " + voCells.get(i).toString() + " ] : " + Data.TYPE_NAMES[voCells.get(i).voType] + "\n");
//			}
//		}
		return sb.toString();
	}

}