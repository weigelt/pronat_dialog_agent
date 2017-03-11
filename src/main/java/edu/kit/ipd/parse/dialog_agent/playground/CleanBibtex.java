package edu.kit.ipd.parse.dialog_agent.playground;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
//import java.io.PrintWriter;
import java.util.Scanner;

public class CleanBibtex {

	public static void main(String[] args) {
		BufferedReader br = null;
		PrintWriter out = null;
		try {
			br = new BufferedReader(new FileReader("/Users/Mario/Desktop/schlereth_ma/Ausarbeitung/Abschlussarbeit/My Collection.bib"));	
			out = new PrintWriter("/Users/Mario/Desktop/schlereth_ma/Ausarbeitung/Abschlussarbeit/thesis.bib");
		    String line = br.readLine();
		    out.println(line);

		    while (line != null) {
		        line = br.readLine();
		        if (line != null) {
		        	String lineIdentifier = line.split("=")[0].replaceAll("\\s+","");
		        	if (lineIdentifier.equals("url")) {
		        		System.out.println(line);
				        String[] urlLineArray = line.split("(?!^)");
				        List<String> urlList = new ArrayList();
				        for (int i = 0; i < urlLineArray.length; i++) {
				        	urlList.add(urlLineArray[i]);
				        }
				        
				        for (int i = 0; i < urlList.size(); i++) {
				        	if (urlList.get(i).equals("_")) {
				        		urlList.remove(i + 1); // hier ist die Reihenfolge entscheidend!
				        		urlList.remove(i - 2);
				        	}
				        	if (urlList.get(i).equals("&")) {
				        		urlList.remove(i + 1); // hier ist die Reihenfolge entscheidend!
				        		urlList.remove(i - 2);
				        	}
				        	if (urlList.get(i).equals("~")) {
				        		urlList.remove(i + 1); // hier ist die Reihenfolge entscheidend!
				        		urlList.remove(i - 1);
				        	}
				        }
	
				        String cleanedUrl = "";
				        for (int i = 0; i < urlList.size(); i++) {
				        	cleanedUrl = cleanedUrl + urlList.get(i).toString();
				        }
				        System.out.println(cleanedUrl);
				        System.out.println();		
				        out.println(cleanedUrl);
		        	} else {
		        		out.println(line);
		        	}
		        }
		    }
		    br.close();
		    out.close();
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} 
	}

}
