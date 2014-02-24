package generator;

import java.util.ArrayList;

class Trajectory {
	ArrayList<Data> 	tra		= new ArrayList<Data>();
	
	public void add(Data data) {
		tra.add(data);
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < tra.size(); i ++) {
			if (i != 0) sb.append("\t");
			sb.append(tra.get(i));
		}
		return sb.toString();
	}
	
	public String toStringInt() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < tra.size(); i ++) {
			if (i != 0) sb.append("\t");
			sb.append(tra.get(i).toStringInt());
		}
		return sb.toString();
	}
	
	public static Trajectory parseTra(String inStr) {
		String[] tks = inStr.split("\t");
		Trajectory tra = new Trajectory();
		for (String tk : tks) {
			tra.add(Data.parseData(tk));
		}
		return tra;
	}
}