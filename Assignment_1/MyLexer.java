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
	String[][] symbolTable = new String[1][3];
	Map<String,String> tokens = new HashMap<String, String>();
	int idCount = 0;
	int lineCount = 1;
	boolean errorFound = false;

	public String getSymbolNum(String t)
	{
		for(int i=0; i< symbolTable.length;i++)
			if(symbolTable[i][0].equals(t))
				return Integer.toString(i+1);
		return "";
	}

	public void addToTable(String t)
	{
		if(idCount > symbolTable.length)
		{
			String[][] temp = new String[idCount][3];
			for(int i = 0; i < symbolTable.length;i++)
			{
				temp[i] = symbolTable[i];
			}
			temp[idCount-1][0] = t;
			symbolTable = new String[idCount][3];
			symbolTable = temp;
		}
		else
		{
			symbolTable[idCount-1][0] = t;
		}
	}

	public boolean isNewSymbol(String t)
	{
		for(int i = 0; i < symbolTable.length;i++)
		{
			if(t.equals(symbolTable[i][0]))
				return false;
		}
		return true;
	}

	public void parse()
	{
		String t = new String();
		for(int i = 0; i < fileContents.length(); i++)
		{
			//get current character
			char currChar = fileContents.charAt(i);
			//set peek character to null char
			char peek = '\0';
			//check if at end of file
			if (i != fileContents.length()-1)
				peek = fileContents.charAt(i+1);
			//add current char to current token string t
			t += currChar;

			//check if t matches a variable name
			if(peek == '\n')
			{
				lineCount++;
				i++;
				peek = fileContents.charAt(i);
			}
			if(peek == '\t')
			{
				i++;
				peek = fileContents.charAt(i);
			}
			if(t.matches("[A-Za-z]"))
			{
				i++;
				while((Character.isLetter(peek) || Character.isDigit(peek) || peek == '_')/* && tokens.get(peek) == null && peek != ' ' && peek != '\t' && peek != '\n'*/)
				{
					i++;
					t+= peek;
					peek = fileContents.charAt(i);
				}
				if(tokens.get(t) != null)
					output.add("<"+tokens.get(t)+">");
				else if(t.equals("System") || t.equals("out"))
				{
					if(peek == '.')
						output.add("<"+tokens.get("ref")+", "+t+">");
				}
				else if(t.equals("String"))
					output.add("<"+tokens.get("ref")+", "+t+">");
				else if(t.equals("println") || t.equals("print"))
					output.add("<"+tokens.get("call")+", "+t+">");
				else
				{
					if(isNewSymbol(t))
					{	
						
						idCount++;
						addToTable(t);
						symbolTable[idCount-1][1] = "no value";
						symbolTable[idCount-1][2] = "no value";
						output.add("<"+tokens.get("id")+", "+idCount+">");
					}
					else
					{
						output.add("<"+tokens.get("id")+", " + getSymbolNum(t) + ">");
					}
				}
				if(tokens.get(Character.toString(peek)) != null)
					output.add("<"+tokens.get(Character.toString(peek))+">");
				System.out.println("t: " + t + " peek: " + peek);
				t = "";
			}
			//check if t matches a number
			else if (t.matches("[0-9]"))
			{
				i++;
				while(Character.isDigit(peek) || peek == '.')
				{
					t+=peek;
					i++;
					if(peek == '.') {
						i++;
					}
					peek = fileContents.charAt(i);
				}
				if (Character.isLetter(peek))
				{
					errorFound = true;
					System.out.println("Error on line " + lineCount + ": No such lexeme can be matched");
				}
				else if(t.matches("[0-9]+(?:\\.[0-9]+)?"))
				{
					output.add("<"+tokens.get("num")+", " + t+">");
				}
				else
					System.out.println("Error in num: "+t);
				t = "";
			}
			//check if t is the beginning of a string literal
			else if(t.matches("\""))
			{
				while(peek != '\"')
				{
					t+=peek;
					i++;
					peek = fileContents.charAt(i);
				}
				t+=peek;
				i++;
				output.add("<" +tokens.get("\"..\"")+">");
				t ="";
			}
			//check if t is a comment
			else if(t.matches("/") && peek == '/')
			{
				i+=2;
				while(peek != '\n')
				{
					t+=peek;
					i++;
					peek = fileContents.charAt(i);
				}
				lineCount++;
				t = "";
			}
			else if(t.matches("/") && peek == '*')
			{
				t+=peek;
				i+=2;
				peek = fileContents.charAt(i);
				while(peek != '*' && fileContents.charAt(i+1) != '/')
				{
					if(peek == '\n')
						lineCount++;
					t+=peek;
					peek = fileContents.charAt(i++);
				}
				t+=peek;
				t+=fileContents.charAt(i++);
				t+=fileContents.charAt(i++);
				t = "";
			}
			//check if t is part of a 2-character operator
			else if(tokens.get((t+peek)) != null)
			{
				System.out.println("compare: " + t+peek);
				output.add("<" + tokens.get(t+peek) + ">");
				i++;
				t = "";
			}
			//check if t is just a single character terminal
			else if(tokens.get(t) != null)
			{
				output.add("<" + tokens.get(t) + ">");
				t = "";
			}
			//output error if t doesnt match a valid token type
			else
			{
				//System.out.println("Error " + t);
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
			sc.close();
		}catch(IOException ioe)
		{
			System.out.println(ioe.getMessage());
		}
	}

	public static void main(String argc[])
	{
		System.out.println("");
		MyLexer ml = new MyLexer();
		if(argc.length < 2)
			System.out.println("Error missing input file.");
		else
		{
			ml.readTokens(argc[0]);
			ml.readJavaFile(argc[1]);
			ml.parse();
			/*System.out.println("Printing input file contents:\n" + ml.fileContents);
			System.out.println("Printing token list:");*/
			/*Iterator it = ml.tokens.entrySet().iterator();
			while(it.hasNext())
			{
				Map.Entry pair = (Map.Entry)it.next();
				System.out.println(pair.getKey() + " " + pair.getValue());
				it.remove();
			}*/
			if(!ml.errorFound)
			{
				for(int i = 0; i < ml.output.size(); i++)
				{
					System.out.print(ml.output.get(i) + " ");
					if ((i+1)%8 == 0)
						System.out.println("");
				}
				System.out.println("\n\tSymbol table");
				for(int i = 0; i < ml.symbolTable.length;i++)
					System.out.println(ml.symbolTable[i][0] + "\t" + ml.symbolTable[i][1] + "\t" + ml.symbolTable[i][2]);
			}
		}
		System.out.println("");
	}
}