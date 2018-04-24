package data;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Scanner;

public class CustomFileRead extends FileReader implements ICustomFileRead{
	private int endLine;
	
	/**
	 * Constructora que recibe nombre de fichero
	 * @param name
	 * @throws IOException 
	 */
	public CustomFileRead(String name) throws IOException {
		super(new java.io.File( "." ).getCanonicalPath() + "/src/data/files/" + name);
		this.endLine = -1;
	}
	
	@Override
	public Double readDouble(Scanner in) {
		return in.nextDouble();
	}

	@Override
	public ArrayList<Integer> readArray(Scanner in) {
		ArrayList<Integer> mutant = new ArrayList<Integer>();
		int element = in.nextInt();
		while(element != endLine) {
			if (element != endLine)
				mutant.add(element);
			element = in.nextInt();
		}
		return mutant;
	}

	@Override
	public void closeFileRead(CustomFileRead file) {
		try {
			file.close();
		} catch (IOException e) {
			System.out.println("Error - closeFileRead: " + e.getMessage());
		}
	}
}