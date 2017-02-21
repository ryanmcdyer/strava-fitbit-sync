import java.io.Console;
import java.util.Arrays;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


public class Combiner {
	private static String fileHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
	private static String fileXlmns = "";
	private static String fileTime = "";
	private static String fileName = "";
	private static String fileFooter = "  </trkseg>\n </trk>\n</gpx>";

	public static String getFileHeader() {
		return fileHeader;
	}

	public static void setFileXlmns(String s) {
		fileXlmns = s;
	}
	public static String getFileXlmns() {
		return fileXlmns;
	}

	public static void setFileTime(String s) {
		fileTime = s;
	}
	public static String getFileTime() {
		return fileTime;
	}

	public static void setFileName(String s) {
		fileName = s;
	}
	public static String getFileName() {
		return fileName;
	}

	public static String getFileFooter() {
		return fileFooter;
	}

	public static void main(String [] args) throws IOException {

		ArrayList<TrkSeg> data = new ArrayList<>();
		ArrayList<TrkSeg> stravaData = new ArrayList<>();
		ArrayList<TrkSeg> fitbitData = new ArrayList<>();

		FileReader stravafr = null;
		FileReader fitbitfr = null;

		String stravaFileName = "";
		String fitbitFileName = "";

		Console c = System.console();
		if (c == null) {
      System.err.println("No console, exiting.");
      System.exit(1);
    }

		while(stravaData.size() == 0) {
			try {
				stravaFileName = c.readLine("Enter Strava file name: ");
			} catch(Exception e) {
				;
			}
			stravaData = GPXReader.readStravaFile(stravaFileName);
		}

		while(fitbitData.size() == 0) {
			try {
				fitbitFileName = c.readLine("Enter Fitbit file name: ");
			} catch(Exception e) {
				;
			}
			fitbitData = GPXReader.readFitbitFile(fitbitFileName);
		}

		data = GPXJoiner.join(stravaData, fitbitData);

		Collections.sort(data);
		GPXWriter.writeFile(data);

	}
}

class GPXJoiner {
	static ArrayList<TrkSeg> join(ArrayList<TrkSeg> stravaData, ArrayList<TrkSeg> fitbitData) {
		ArrayList<TrkSeg> allData = new ArrayList<TrkSeg>();
		Instant stravaTime;
		TrkSeg t = new TrkSeg();

		for (int i = 0; i < stravaData.size(); i++) {
			stravaTime = stravaData.get(i).getTime();

			for(int j = 0; j < fitbitData.size(); j++) {
				if(stravaTime.compareTo(fitbitData.get(j).getTime()) == 0) {
					t = stravaData.get(i);
					t.setHeartRate(fitbitData.get(j).getHeartRate());
					stravaData.set(i, t);
				}
			}
			allData.add(stravaData.get(i));
		}
		return allData;
	}
}

class GPXReader {
	static ArrayList<TrkSeg> readFitbitFile(String fileName) {
		return readFile(fileName, false);
	}
	static ArrayList<TrkSeg> readStravaFile(String fileName) {
		return readFile(fileName, true);
	}
	static ArrayList<TrkSeg> readFile(String fileName, boolean isStravaFile) {
		ArrayList<TrkSeg> list = new ArrayList<>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			StringBuilder sb = new StringBuilder();
			String line;
			/*****
			Read the headers etc
			*****/
			if(isStravaFile) {
				line = br.readLine(); //Line 1
				line = br.readLine(); //Line 2
				line = br.readLine(); //<metadata>
				line = br.readLine(); //<time> $time </time>
				sb.append(line);
				sb.append(System.lineSeparator());
				Combiner.setFileTime(sb.toString());
				sb = new StringBuilder();
				line = br.readLine(); //</metadata>
				line = br.readLine(); //<trk>
				line = br.readLine(); //<name> $name </name>
				sb.append(line);
				sb.append(System.lineSeparator());
				Combiner.setFileName(sb.toString());
				sb = new StringBuilder();
				line = br.readLine(); //<trkseg>
			} else {
				line = br.readLine(); //Line 1
				line = br.readLine(); //2nd line contains useful metadata
				sb.append(line);
				sb.append(System.lineSeparator());
				Combiner.setFileXlmns(sb.toString());
				sb = new StringBuilder();
				line = br.readLine(); //<metadata>
				line = br.readLine(); //<time> $time </time>
				line = br.readLine(); //</metadata>
				line = br.readLine(); //<trk>
				line = br.readLine(); //<name> $name </name>
				line = br.readLine(); //<trkseg>
			}

			sb = new StringBuilder();

			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append(System.lineSeparator());

				if(line.contains("</trkpt>")) {
					sb.append(line);
					sb.append(System.lineSeparator());
					list.add(new TrkSeg(sb.toString()));
					sb = new StringBuilder();
				} else if(line.contains("</gpx>")) {
					System.out.println("File processed.");
				}
			}

			br.close();

		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("File not found, exiting.");
			System.exit(0);
		}
		return list;
	}
}

class GPXWriter {
	static void writeFile(ArrayList<TrkSeg> list) {
		try {
			PrintWriter writer = new PrintWriter("out.gpx", "UTF-8");
			//Write out file header
			writer.print(Combiner.getFileHeader());
			writer.print(Combiner.getFileXlmns());
			writer.print("<metadata>\n");
			writer.print(Combiner.getFileTime());
			writer.print("</metadata>\n");
			writer.print("<trk>\n");
			writer.print(Combiner.getFileName());
			writer.print("<trkseg>\n");
			writer.flush();

			//Write out sorted list
			for(TrkSeg t : list) {
				writer.println(t);
			}
			writer.flush();

			//Write out footer
			writer.print(Combiner.getFileFooter());
			writer.flush();

			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

class TrkSeg implements Comparable<TrkSeg> {
	private double lat;
	private double lon;
	private double ele;
	private Instant time;
	private int hr;

	public Instant getTime() {
		return time;
	}

	public void setHeartRate(int hrParam) {
		hr = hrParam;
	}

	public int getHeartRate() {
		return hr;
	}

	TrkSeg() {
	}

	TrkSeg(String s) {
		s = s.substring(s.indexOf("lat=\""));
		s = s.substring(s.indexOf("\"") + 1);
		lat = Double.parseDouble(s.substring(0, s.indexOf("\"")));

		s = s.substring(s.indexOf("lon=\""));
		s = s.substring(s.indexOf("\"") + 1);
		lon = Double.parseDouble(s.substring(0, s.indexOf("\"")));

		s = s.substring(s.indexOf("<ele>"));
		s = s.substring(s.indexOf(">") + 1);
		ele = Double.parseDouble(s.substring(0, s.indexOf("<")));

		s = s.substring(s.indexOf("<time>"));
		s = s.substring(s.indexOf(">") + 1);
		time = Instant.parse(s.substring(0, s.indexOf("<")));
		if(s.indexOf("gpxtpx:hr") >= 0) {
			s = s.substring(s.indexOf("gpxtpx:hr"));
			s = s.substring(s.indexOf(">") + 1);
			hr = Integer.parseInt(s.substring(0, s.indexOf("<")));
		} else {
			hr = 0;
		}
	}

	public int compareTo(TrkSeg other) {
		// compareTo should return < 0 if this is supposed to be
		// less than other, > 0 if this is supposed to be greater than
		// other and 0 if they are supposed to be equal
		return this.time.compareTo(other.time);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("  <trkpt lat=\"" + lat + "\" lon=\"" + lon + "\">\n");
		sb.append("    <ele>" + ele + "</ele>\n");
		sb.append("    <time>" + time + "</time>\n");
		if(hr > 0) {
			sb.append("    <extensions>\n");
			sb.append("      <gpxtpx:TrackPointExtension>\n");
			sb.append("      <gpxtpx:hr>" + hr + "</gpxtpx:hr>\n");
			sb.append("      </gpxtpx:TrackPointExtension>\n");
			sb.append("    </extensions>\n");
		}
		sb.append("  </trkpt>");
		return sb.toString();
	}

}
