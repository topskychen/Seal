package generator;

import spatialindex.Point;

class Data {
	Point 		point;
	double		tiStp;
	
	Data(Point point, double tiStp) {
		this.point = point;
		this.tiStp = tiStp;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(point.getCoord(0) + " ");
		sb.append(point.getCoord(1) + " ");
		sb.append(tiStp);
		return sb.toString();
	}

	public String toStringInt() {
		StringBuffer sb = new StringBuffer();
		sb.append((int) point.getCoord(0) + " ");
		sb.append((int) point.getCoord(1) + " ");
        sb.append((int) tiStp);
		return sb.toString();
	}

	public static Data parseData(String inStr) {
		String[] tks = inStr.split(" ");
//		System.out.println("line:" + inStr + ".") ;
		Point point = new Point(new double[] {Double.parseDouble(tks[0]), 
				Double.parseDouble(tks[1])});
		Data data = new Data(point, Double.parseDouble(tks[2]));
		return data;
	}

    public static Data parseDataInt(String inStr) {
        String[] tks = inStr.split(" ");
//		System.out.println("line:" + inStr + ".") ;
        Point point = new Point(new double[] {Double.parseDouble(tks[0]),
                Double.parseDouble(tks[1])});
        Data data = new Data(point, Double.parseDouble(tks[2]));
        return data;
    }
}