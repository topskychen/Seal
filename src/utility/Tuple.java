/**
 * 
 */
package utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import IO.DataIO;
import IO.RW;

/**
 * @author chenqian
 *
 */
public class Tuple implements RW{

	private int[] point = null;
	private int t;
	
	/**
	 * 
	 */
	public Tuple() {
		// TODO Auto-generated constructor stub
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
		point = DataIO.readIntArrays(ds);
		t = DataIO.readInt(ds);
	}

	@Override
	public void write(DataOutputStream ds) {
		// TODO Auto-generated method stub
		DataIO.writeIntArrays(ds, point);
		DataIO.writeInt(ds, t);
	}

	@Override
	public void loadBytes(byte[] data) {
		// TODO Auto-generated method stub
		DataInputStream ds = new DataInputStream(new ByteArrayInputStream(data));
		read(ds);
	}

	@Override
	public byte[] toBytes() {
		// TODO Auto-generated method stub
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		DataOutputStream ds = new DataOutputStream(bs);
		write(ds);
		return bs.toByteArray();
	}

}
