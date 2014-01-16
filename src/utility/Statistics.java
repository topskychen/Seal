/**
 * 
 */
package utility;

/**
 * @author chenqian
 *
 */
public class Statistics {

	double prepareTime = 0;
	double verifyTime = 0;
	double voSize = 0;
	int num = 0;
	
	public void append(double prepareTime, double verifyTime, long voSize) {
		this.prepareTime += prepareTime;
		this.verifyTime += verifyTime;
		this.voSize += voSize;
		num ++;
	}
	
	public double getAvePrepareTime() {
		return prepareTime / num;
	}
	
	public double getAveVerifyTime() {
		return verifyTime / num;
	}
	
	public double getAveVOSize() {
		return voSize / num;
	}
	
	public void reSet() {
		prepareTime = 0;
		verifyTime = 0;
		voSize = 0;
		num = 0;
	}
	
	/**
	 * 
	 */
	public Statistics() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("prepareTime: " + getAvePrepareTime() + " ms\n");
		sb.append("verifyTime: " + getAveVerifyTime() + " ms\n");
		sb.append("VO Size: " + getAveVOSize() / 1024 + " KB\n");
		return sb.toString();
	}
	

}
