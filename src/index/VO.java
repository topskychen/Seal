/**
 * 
 */
package index;

import io.IO;
import io.RW;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.TreeSet;

import party.TrustedRegister;
import spatialindex.IShape;
import timer.Timer;
import utility.Global;

/**
 * @author chenqian
 *
 */
public class VO implements RW{

	boolean 			verbose			= false;
	private Timer 		timer 			= null;
	private double 		prepareTime 	= -1;
	private double 		verifyTime 		= -1;
	private int 		voSize 			= -1;
	private int			ansNo			= -1;
	ArrayList<VOCell> 	voCells 		= null;
//	Query				query 			= null;
	private int			runId 			= -1;
	TreeSet<Integer>	ansIds			= null;
	
	
	/**
	 * 
	 */
	public VO(int runId) {
		// TODO Auto-generated constructor stub
		this.runId 	= runId;
		timer 		= new Timer();
		ansIds 		= new TreeSet<Integer>();
	}
	
	/**
	 * Prepare VO
	 */
	public void prepare(SearchIndex index, IShape query) {
		timer.reset();
		voCells = index.rangeQuery(query);
		timer.stop();
		prepareTime = timer.timeElapseinMs();
	}
	
	public boolean verify(IShape query) {
		timer.reset();
		boolean isVerify = true;
		ansNo = 0;
		for (VOCell voCell : voCells) {
			if (!voCell.verify(query, ansIds)) {
				isVerify = false;
				System.out.print("x\n");
				System.out.println(voCell);
				break;
			} else {
                if (!Global.BATCH_QUERY) {
				    System.out.print(".");
			    }
            }
			ansNo += voCell.getAnsNo();
		}
		if (!verifyComplete()) {
			isVerify = false;
		}
		timer.stop();
		verifyTime = timer.timeElapseinMs();
		voSize = IO.toBytes(this).length;
		return isVerify;
	}
	
	public boolean verifyComplete() {
		BigInteger ss = BigInteger.ZERO;
		for (VOCell voCell : voCells) {
			ss = ss.add(voCell.getPartialSS());
		}
		return ss.equals(TrustedRegister.totalSS.get(runId));
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
		sb.append("VOCells : " + voCells.size() + "\n");
		sb.append("AnsNo : " + ansNo + " [");
//		Integer[] ids = ansIds.toArray(new Integer[0]);
//		for (int i = 0; i < ids.length && i < Constants.PRINT_LIM; i ++) {
//			if (i != 0) sb.append(" ");
//			sb.append(ids[i]);
//		}
		sb.append("]\n");
		sb.append("PrepareTime: " + prepareTime + " ms\n");
		sb.append("VerifyTime: " + verifyTime + " ms\n");
		sb.append("VOSize: " + voSize + " bytes, " + voSize / 1024.0 + " KB\n");
		if(verbose){
			for (int i = 0; i < voCells.size(); i ++) {
				sb.append(voCells.get(i));
			}
		}
		return sb.toString();
	}

	public double getPrepareTime() {
		return prepareTime;
	}
	
	public double getVerifyTime() {
		return verifyTime;
	}
	
	public int getVOSize() {
		return voSize;
	}
}
