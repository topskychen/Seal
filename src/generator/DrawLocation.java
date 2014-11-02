package generator;

import graphics.Data.DrawType;
import graphics.DrawCollection;
import graphics.ShowData;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import spatialindex.IShape;


/**
 * See the location distribution.
 * @author chenqian
 *
 */
public class DrawLocation {

	public DrawLocation(String fileName) {
		List<IShape> points = LocationParser.parseLocation(fileName);
		ArrayList<graphics.Data> datas = new ArrayList<graphics.Data>();
		int i = 0;
		for (IShape point : points) {
			if (i++ > 5000) break;
			datas.add(new graphics.Data(point, Color.BLACK, DrawType.Point));
		}
		ShowData showData = new ShowData(new DrawCollection(datas));
		ShowData.draw(showData);
	}

	public static void main(String[] args) {
		new DrawLocation("./data/Location.txt");
	}

}
