package data;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Scanner;

public class CustomFileWrite extends FileWriter implements ICustomFileWrite{
	
	/**
	 * Constructora que recibe nombre de fichero
	 * @param name
	 * @throws IOException 
	 */
	public CustomFileWrite(String name) throws IOException {
		super(new java.io.File( "." ).getCanonicalPath() + "/src/data/files/" + name);
	}
	
	@Override
	public void writeFile(CustomFileWrite file, String text) {
		try {
			file.write(text + " \n");
		} catch (IOException e) {
			System.out.println("Error - writeFile: " + e.getMessage());
		}
	}
	
	@Override
	public void closeFileWrite(CustomFileWrite file) {
		try {
			file.close();
		} catch (IOException e) {
			System.out.println("Error - closeFileWrite: " + e.getMessage());
		}
	}
}