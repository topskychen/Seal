/**
 * 
 */
package utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;

import index.Entry;
import io.RW;

/**
 * @author chenqian
 *
 */
public class VOCell implements RW{

	ArrayList<Integer> 	ids 		= null;
	Entry 				entry		= null;
	
	public boolean verify(Query query) {
		//TODO
		
		return false;
	}
	/**
	 * 
	 */
	public VOCell(ArrayList<Integer> ids, Entry entry) {
		// TODO Auto-generated constructor stub
//		for ()
	}
	
	public VOCell() {}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public void read(DataInputStream ds) {
		// TODO Auto-generated method stub
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
	}

}
