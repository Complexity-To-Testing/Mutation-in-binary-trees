package main;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

/**
 * 
 * @author 	Jonathan Carrero
 * @author	Cristhian Rodríguez
 * @author	Yu Liu
 *
 */
public class Main {
	
	/**
	 * Función main que llama a la función principal 'start'
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Engine engine = new Engine();
			engine.start();
		} catch (IOException e) {
			System.out.println("Error - Main: " + e.getMessage());
		}
	}
}
