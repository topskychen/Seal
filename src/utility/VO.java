/**
 * 
 */
package utility;

import index.Entry;
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
	private double 		verifiyTime 	= -1;
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
		ArrayList<Entry> entries = index.rangeQuery(query);
		for (Entry entry: entries) {
			//TODO
		}
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
		verifiyTime = timer.timeElapseinMs();
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
	
	

}
