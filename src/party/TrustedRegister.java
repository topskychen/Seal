/**
 * 
 */
package party;

import IO.DataIO;
import IO.RW;
import crypto.AES;
import utility.EncKey;

/**
 * @author chenqian
 *
 */
public class TrustedRegister {

	private byte[] sk = null;
	private EncKey encKey = null;
	
	public byte[] getSecretShare(int id, RW value) {
		byte[] content = DataIO.concat(new Integer(id).toString().getBytes(), value.toBytes());
		return AES.encrypt(sk, content);
	}
	
	public EncKey getEncKey () {
		return encKey;
	}
	
	/**
	 * 
	 */
	public TrustedRegister() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
