/* Joshua Lemmon
*  100555320
*  CSCI 4020u - Compilers
*  Assignment 1
*/

import java.io.*;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class MyLexer
{
	//declaring containers
	String fileContents = new String();
	List<String> output = new ArrayList<String>();
	Map<String,String> tokens = new HashMap<String, String>();

	//reads in tokens and puts them in the tokens hashmap
	public void readTokens(String fname)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(fname));
			String line;
			int i = 0;
			while((line = br.readLine()) != null)
			{
				String[] l = line.split("\\s+");
				if(l.length != 0)
				{
					if(l[1].equals("Id"))
						tokens.put("id",l[1]);
					else if (l[1].equals("Number"))
						tokens.put("num", l[1]);
					else if (l[1].equals("T_ref"))
						tokens.put("ref",l[1]);
					else if (l[1].equals("T_call"))
						tokens.put("call",l[1]);
					else
						tokens.put(l[0],l[1]);
				}
			}

			br.close();
		}catch(IOException ioe)
		{
			System.out.println(ioe.getMessage());
		}
	}
	//reads in inputted java file into a string
	public void readJavaFile(String fname)
	{
		try
		{
			Scanner sc = new Scanner(new File(fname));
			fileContents = sc.useDelimiter("\\Z").next();
			sc.close();
		}catch(IOException ioe)
		{
			System.out.println(ioe.getMessage());
		}
	}



	public static void main(String argc[])
	{
		MyLexer ml = new MyLexer();
		if(argc.length < 2)
			System.out.println("Error missing input file.");
		else
		{
			ml.readTokens(argc[0]);
			ml.readJavaFile(argc[1]);
			System.out.println("Printing input file contents:\n" + ml.fileContents);
			System.out.println("Printing token list");
			Iterator it = ml.tokens.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry pair = (Map.Entry)it.next();
				System.out.println(pair.getKey() + " " + pair.getValue());
				it.remove();
			}
		}
	}
}