package Bonus;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class CSVReader {

    public static Collection<String> read(String id) {

        String csvFile = "C:\\Users\\Ivo\\Desktop\\test\\groundTruth.txt";
        String line = "";
        String cvsSplitBy = " ";
        Collection<String> result = new ArrayList<String>();

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] queries = line.split(cvsSplitBy);

                if(queries[0].matches(id + ".") && !(queries[2].equals("L0"))){
//	                System.out.println("Query " + country[0] + " , id " + country[1] );
	                result.add(queries[1]);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
		return result;

    }

}
