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

	public String stripToken(String s)
	{
		return s.substring(1,s.length()-1);
	}

	public String getSymbolNum(String t)
	{
		for(int i=0; i< symbolTable.length;i++)
			if(symbolTable[i][0].equals(t))
				return Integer.toString(i+1);
		return "";
	}

	public void addToTable(String t)
	{
		//increase symbol table size if idCount exceeds the table size
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
			//check if t matches an id or keyword
			if(t.matches("[A-Za-z]"))
			{
				i++;
				//loop until an invalid id character is met
				while((Character.isLetter(peek) || Character.isDigit(peek) || peek == '_'))
				{
						i++;
						t+= peek;
						peek = fileContents.charAt(i);
				}
				//check if t is a keyword
				if(tokens.get(t) != null)
				{
					if(tokens.get(t).equals("Prim_type"))
						output.add("<"+tokens.get(t)+", "+ t+">");
					else
						output.add("<"+tokens.get(t)+">");
				}
				//check if t is a ref with a dot operator
				else if(t.equals("System") || t.equals("out"))
				{
					if(peek == '.')
					{
						output.add("<"+tokens.get("ref")+", "+t+">");
						peek = fileContents.charAt(i+1);
					}
				}
				//check if t is a ref
				else if(t.equals("String"))
					output.add("<"+tokens.get("ref")+", "+t+">");
				//check if t is a call
				else if(t.equals("println") || t.equals("print"))
					output.add("<"+tokens.get("call")+", "+t+">");
				//check if t is an id
				else
				{
					//if t is a new id
					if(isNewSymbol(t))
					{
						idCount++;
						addToTable(t);
						symbolTable[idCount-1][1] = "no type";
						symbolTable[idCount-1][2] = "no value";
						int j = output.size()-1;
						String prevToken = stripToken(output.get(j));
						String type = new String();
						if(prevToken.equals("T_cls_brkt")|| prevToken.substring(0,3).equals("Pri")|| prevToken.substring(0,5).equals("T_ref")|| prevToken.equals("T_class"))
						{
							if(prevToken.equals("T_cls_brkt"))
							{
								type = type + "]";
								j--;
								if(stripToken(output.get(j)).substring(0,1).equals("Nu"))
									type = stripToken(output.get(j--)) + type;
								type = "[" + type;
								j--;
								prevToken = stripToken(output.get(j));
							}
							if(prevToken.equals("T_class"))
							{
								type = "no type";
							}
							else
							{
								String temp[] = prevToken.split(", ");
								type = temp[1] + type;
							}
							symbolTable[idCount-1][1] = type;
						}
						else if(prevToken.equals("T_Comma"))
						{
							j--;
							prevToken = stripToken(output.get(j));
							System.out.println("token before comma is: " + prevToken);
							while(prevToken.substring(0,2).equals("Id") == false)
							{
								j--;
								prevToken = stripToken(output.get(j));
								System.out.println("token: " + prevToken);
							}
							String[] temp = prevToken.split(", ");
							System.out.println("found token: " + prevToken);
							System.out.println("found id: " + symbolTable[Integer.parseInt(temp[1])-1][0]);
							System.out.println("found type: " + symbolTable[Integer.parseInt(temp[1])-1][1]);
							symbolTable[idCount-1][1] = symbolTable[Integer.parseInt(temp[1])-1][1];
						}
						output.add("<"+tokens.get("id")+", "+idCount+">");
					}
					//if t is already defined
					else
						output.add("<"+tokens.get("id")+", " + getSymbolNum(t) + ">");
				}
				//check if peek is a terminal and not whitespace
				if(tokens.get(Character.toString(peek)) != null)
				{
					//if peek is +,-,*,<,>,=
					if(tokens.get(Character.toString(peek)).equals("T_binary_op") || tokens.get(Character.toString(peek)).equals("Comp_op")|| peek == '=' || peek == '!')
					{
						//start parsing the operators/comparators
						t = Character.toString(peek);
						peek = fileContents.charAt(i+1);
						//check if the operator is going to be ++, --, *=, -=, +=, ==, !=
						if(peek == '-' || peek == '+' || peek == '=' || peek == '!')
						{
							t+=peek;
							i++;
							output.add("<" + tokens.get(t) +", '"+t+ "' >");
						}
						//don't want to include '=' or '!' in token
						else if(t.equals("=") || t.equals("!"))
							output.add("<"+tokens.get("=")+">");
						//add t to output
						else
							output.add("<" + tokens.get(t)+", '" + t+"' >");
					}
					//add peek to output if its not an operator, comparator or assignment
					else
						output.add("<" + tokens.get(Character.toString(peek)) +">");
				}
				t = "";
			}
			//check if t matches a number
			else if (t.matches("[0-9]"))
			{
				i++;
				//loop while peek is a valid character for a number
				while(Character.isDigit(peek) || peek == '.')
				{
					t+=peek;
					i++;
					if(peek == '.')
						i++;
					peek = fileContents.charAt(i);
				}
				//check if a character appears after number, if true output error
				if (Character.isLetter(peek))
				{
					errorFound = true;
					System.out.println("Error on line " + lineCount + ": No such lexeme can be matched");
				}
				//check if t matches valid number pattern
				else if(t.matches("[0-9]+(?:\\.[0-9]+)?"))
					output.add("<"+tokens.get("num")+", " + t+">");
				else
					System.out.println("Error in num: "+t);
				//check if peek is a terminal and not whitespace
				if(tokens.get(Character.toString(peek)) != null)
				{
					//if peek is +,-,*,<,>,=
					if(tokens.get(Character.toString(peek)).equals("T_binary_op") || tokens.get(Character.toString(peek)).equals("Comp_op")|| peek == '=' || peek == '!')
					{
						//start parsing the operators/comparators
						t = Character.toString(peek);
						peek = fileContents.charAt(i+1);
						//check if the operator is going to be ++, --, *=, -=, +=, ==, !=
						if(peek == '-' || peek == '+' || peek == '=' || peek == '!')
						{
							t+=peek;
							i++;
							output.add("<" + tokens.get(t) + ", '" + t + "' >");
						}
						//don't want to include '=' and '!' in token
						else if(t.equals("=") || t.equals("!"))
							output.add("<" + tokens.get("=") + ">");
						//add t to output
						else
							output.add("<" + tokens.get(t)+", '" + t + "' >");
					}
					//add peek to output if its not an operator, comparator or assignment
					else
						output.add("<" + tokens.get(Character.toString(peek)) +">");
				}
				t = "";
			}
			//check if t is the beginning of a string literal
			else if(t.matches("\""))
			{
				//loop until peek is a " character
				while(peek != '\"')
				{
					t+=peek;
					i++;
					peek = fileContents.charAt(i);
				}
				//add " to t
				t+=peek;
				i++;
				//add the literal to output
				output.add("<" +tokens.get("\"..\"")+">");
				peek = fileContents.charAt(i);
				//check if terminal comes right after the literal and add it to output if true
				if(tokens.get(Character.toString(peek)) != null)
					output.add("<"+tokens.get(Character.toString(peek))+">");
				t ="";
			}
			//check if t is a single line comment
			else if(t.matches("/") && peek == '/')
			{
				i+=2;
				//loop until end of line
				while(peek != '\n')
				{
					t+=peek;
					i++;
					peek = fileContents.charAt(i);
				}
				lineCount++;
				t = "";
			}
			//check if t is a multiline comment
			else if(t.matches("/") && peek == '*')
			{
				t+=peek;
				i+=2;
				peek = fileContents.charAt(i);
				//loop until the next */ gets reached
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
			//check if t is a terminal
			else if(tokens.get(t) != null)
			{
				//if t is an operator, comparator or assignment
				if(tokens.get(t).equals("T_binary_op") || tokens.get(t).equals("Comp_op") || t.equals("=") || t.equals("!"))
				{
					//check if next character makes a 2-character terminal(++,--,>=,<=,==,!=)
					if(peek == '-' || peek == '+' || peek == '=' || peek == '!')
					{
						t+=peek;
						output.add("<" + tokens.get(t) + ", '" + t + "' >");
					}
					else
					{ 
						if(t.equals("=") || t.equals("!"))
							output.add("<" + tokens.get(t) + ">");
						else
							output.add("<" + tokens.get(t) + ", '" + t + "' >");
					}
				}
				else
					output.add("<" + tokens.get(t) +">");
				t = "";
			}
			//if t isn't a valid(e.g. is whitespace) token set it to empty for next iteration
			else
				t = "";
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
			//read in the token file
			ml.readTokens(argc[0]);
			//read in the file to parse
			ml.readJavaFile(argc[1]);
			//begin parsing
			ml.parse();
			//begin setting symbol table values
			//ml.setSymbolTable();
			if(!ml.errorFound)
			{
				for(int i = 0; i < ml.output.size(); i++)
				{
					System.out.print(ml.output.get(i) + " ");
					if ((i+1)%8 == 0)
						System.out.println("");
				}
				System.out.println("\n\n\tSymbol table");
				for(int i = 0; i < ml.symbolTable.length;i++)
					System.out.println(String.format("%-10s %-10s %s",ml.symbolTable[i][0], ml.symbolTable[i][1], ml.symbolTable[i][2]));
			}
		}
		System.out.println("");
	}
}