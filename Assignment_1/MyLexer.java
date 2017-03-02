/* Joshua Lemmon
*  100555320
*  CSCI 4020u - Compilers
*  Assignment 1
*/

import java.io.*;
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
	List<List<String>> symbolTable = new ArrayList<List<String>>();
	Map<String,String> tokens = new HashMap<String, String>();

	public void parse()
	{
		String t = new String();
		//System.out.println(fileContents.length());
		for(int i = 0; i < fileContents.length(); i++)
		{
			//get current character
			char currChar = fileContents.charAt(i);
			//set peek character to null char
			char peek = '\0';
			//check if at end of file
			if (i != fileContents.length()-1)
				peek = fileContents.charAt(i+1);
			//
			t += currChar;

			if(t.matches("[A-Za-z]+[\\w]*"))
			{
				System.out.println("id " + t);
				//output.add(t);
				t = "";
			}
			else if (t.matches("-?[0-9]+(.[0-9]+)?"))
			{
				System.out.println("num " + t);
				//output.add(t);
				t = "";
			}
			else if(t.matches("\""))
			{
				while(peek != '\"	')
				{
					t+=peek;
					peek = fileContents.charAt(i++);
				}
				output.add("<" +tokens.get("..")+">");
				t ="";
			}
			else if(t.matches("[//][\\w]*"))
			{
				while(peek != '\n')
				{
					t+=peek;
					peek = fileContents.charAt(i++);
				}
				t = "";
			}
			//else if(t.matches("[/*][[\\w]*[\*/]"))
			//{
			//	t = "";
			//}
			else if(tokens.get(t+peek) != null)
			{
				output.add("<" + tokens.get(t+peek) + ">");
				i++;
				t = "";
			}
			else if(tokens.get(t) != null)
			{
				output.add("<" + tokens.get(t) + ">");
				t = "";
			}
			else
			{
				System.out.println("Error "+t);
				output.add(t);
				t = "";
			}
		}

	}

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
			System.out.println(fileContents);
			sc.close();
		}catch(IOException ioe)
		{
			System.out.println(ioe.getMessage());
		}
	}

	public static void main(String argc[])
	{
		System.out.println(argc[1]);
		MyLexer ml = new MyLexer();
		if(argc.length < 2)
			System.out.println("Error missing input file.");
		else
		{
			ml.readTokens(argc[0]);
			ml.readJavaFile(argc[1]);
			ml.parse();
			/*System.out.println("Printing input file contents:\n" + ml.fileContents);
			System.out.println("Printing token list:");
			Iterator it = ml.tokens.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry pair = (Map.Entry)it.next();
				System.out.println(pair.getKey() + " " + pair.get());
				it.remove();
			}*/
			for(int i = 0; i < ml.output.size(); i++)
			{
				System.out.print(ml.output.get(i) + " ");
				if(i%5 == 0 && i != 0)
					System.out.println();
			}
		}
	}
}