package main;

import java.awt.Label;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import java.util.zip.ZipEntry;

import javax.management.openmbean.OpenDataException;

import org.omg.PortableServer.THREAD_POLICY_ID;
import org.omg.PortableServer.ThreadPolicyOperations;

import data.CustomFileRead;
import data.CustomFileWrite;

public class Engine {
	private String fileName;
    private CustomFileWrite fileWrite;
    private CustomFileRead fileRead;
	private double weighMutant;
	private double weighTest;
	private int currentIndexFile;
	private int nodes;
	private int levels;
	Scanner sc;
	Scanner in;

	/**
	 * Constructora sin argumentos
	 * @throws IOException 
	 */
	public Engine() throws IOException {
		this.sc = new Scanner(System.in);
		this.weighMutant = 0;
		this.weighTest = 0;
		this.currentIndexFile = 0;
		this.nodes = 0;
		this.levels = 0;
	}
	
	/**
	 * Función principal de 'Engine' que controla la ejecución y cuya complejidad es O(n),
	 * donde n es el número de nodos que tiene el árbol
	 * @throws IOException 
	 */
	public void start() throws IOException{
		System.out.println("Elija una opción: \n" + 
				" (1) generar mutante \n" + 
				" (2) aplicar test \n" + 
				" (3) aplicar test suite");
		int op = this.sc.nextInt();
		if(op == 1)
			generateMutant();
		else if(op == 2)
			applyOptimiseTest();
		else
			applyTest();
	}
	
	/**
	 * Lleva a cabo la generación de un mutante (o muchos) y lo almacena
	 * @throws IOException 
	 */
	public void generateMutant() throws IOException{
		int totalArrays, op;
		System.out.println("Elija otra opción: \n" + 
				" (1) uno, mucha profundidad \n" + 
				" (2) algunos, podrían repetirse \n" + 
				" (3) todos, poca profundidad \n" +
				" (4) algunos, con un solo error aleatorio");
		op = this.sc.nextInt();
		System.out.println("Introduzca el número de niveles: ");
		this.levels = this.sc.nextInt();
		this.nodes = (int) (Math.pow(2, this.levels));
		ArrayList<Integer> origen = initializeOrigen();
		
		if(op == 4){
			System.out.println("Introduzca el número de árboles: ");
			totalArrays = this.sc.nextInt();
			
			for(int i = 0; i < totalArrays; i++){
				this.fileName = "prueba" + i + ".txt";
				this.fileWrite = new CustomFileWrite(this.fileName);
				ArrayList<Integer> mutant = initializeOrigen();
				int randomPosition = randInt(2, mutant.size() - 1);
				mutant.set(randomPosition, 1); // Metemos un error entre la raíz y el tamaño del array
				mutant.set(0, -1);
				calculateOnlyError(origen, mutant);
				saveMutant(mutant);
				this.weighMutant = 0;
			}
		}else if(op == 3){
			int size = this.nodes-1;// Hay que restar 1 porque el número real de nodos es 2^n - 1
			generateTotalMutants(0, size, new int[size], origen);
		}else{
			if(op == 2){
				System.out.println("Introduzca el número de árboles: ");
				totalArrays = this.sc.nextInt();	
			}else
				totalArrays = 1;
			
			for(int i = 0; i < totalArrays; i++){ // Generamos tantos mutantes como nos haya dicho el usuario
				this.fileName = "prueba" + i + ".txt";
				this.fileWrite = new CustomFileWrite(this.fileName);
				ArrayList<Integer> mutant = initializeMutant();
				calculateFirtsWeigh(origen, mutant);
				saveMutant(mutant);
				this.weighMutant = 0; // En cada mutante reiniciamos el weigh
			}
		}
		System.out.println("He terminado.");
	}
	
	/**
	 * Función recursiva que genera todos los mutantes para una cierta profundidad
	 * @param index
	 * @param size
	 * @param current
	 * @param origen
	 * @throws IOException
	 */
	private  void generateTotalMutants(int index, int size, int[] current, ArrayList<Integer> origen) throws IOException {
	    if(index == size){
	    	ArrayList<Integer> mutant = initializeMutant();
	    	for(int i = 1; i < mutant.size(); i++) // Empieza desde i = 1 porque en la primera posición hay un -1
	        	mutant.set(i, current[i-1]); // 'current[i-1]' porque el array current tiene una posición menos que el 'mutant'
	        this.fileName = "prueba" + currentIndexFile + ".txt";
			this.fileWrite = new CustomFileWrite(this.fileName);
			calculateWeigh(origen, mutant);
			saveMutant(mutant);
			this.weighMutant = 0; // En cada mutante reiniciamos el weigh
			currentIndexFile++;
	    }else{
	        for(int i = 0; i < 2; i++){
	            current[index] = i;
	            generateTotalMutants(index+1, size, current, origen);
	        }
	    }
	}
	
	/**
	 * Aplica un test suite elegido por el usuario
	 * @throws IOException 
	 */
	public void applyOptimiseTest() throws IOException{
		System.out.println("Introduzca los niveles del árbol a testear (la raíz cuenta como nivel): ");
		this.levels = this.sc.nextInt();
		this.nodes = (int) (Math.pow(2, this.levels));
		ArrayList<Integer> origen = initializeOrigen(); // Inicializamos el array origen
		ArrayList<Integer> positions = getTest(); // Inicializamos el array que contiene el test suite
		
		// Leer mutantes desde ficheros
		System.out.println("Introduzca el número de ficheros (mutantes) que desea testear: ");
		int numMutants = this.sc.nextInt(), j = 1; // 'j' es el índice del array 'positions'
		double wDeadsTest = 0.0, wTotalTest = 0.0; // Almacena el weigh del mutante i-ésimo y 'wDeadsTest' el peso de los mutantes muertos
		boolean keep = true; // Si el mutante se mata, se pone a false
		
		for(int i = 0; i < numMutants; i++){ // Recorremos todos los mutantes desde 0 hasta 'numMutants'
			this.fileRead = new CustomFileRead("prueba" + i + ".txt");
			this.in = new Scanner(this.fileRead);
			this.weighMutant = this.fileRead.readDouble(this.in);
			ArrayList<Integer> mutant = this.fileRead.readArray(this.in); // Preparamos el mutante i-ésimo
			while(keep && j < positions.size()){ // Mientras no le hayamos matado o queden posiciones por comprobar
				if(origen.get(positions.get(j)) != mutant.get(positions.get(j))){
					wDeadsTest += this.weighMutant;
					keep = false;
				}
				j++;
			}
			wTotalTest += this.weighMutant;
			j = 1; // Reiniciamos el índice que recorre el array 'positions'
			keep = true; // Y 'keep' lo ponemos a true
		}
		System.out.println("Peso mutantes muertos: " + wDeadsTest); // Peso de los mutantes muertos
		System.out.println("Peso mutantes totales: " + wTotalTest); // Peso de los mutantes totales
		System.out.println("Distinguishing Rate: " + calculateDR(wDeadsTest, wTotalTest));
	}
	
	/**
	 * Aplica tests por niveles
	 * @throws IOException 
	 */
	public void applyTest() throws IOException{
		System.out.println("Introduzca hasta qué nivel llegará el test (incluida la raíz): ");
		this.levels = this.sc.nextInt();
		this.nodes = (int) (Math.pow(2, this.levels));
		ArrayList<Integer> origen = initializeOrigen(); // Inicializamos el array origen
		
		// Leer mutantes desde ficheros
		System.out.println("Introduzca el número de ficheros (mutantes) que desea testear: ");
		int numMutants = this.sc.nextInt(), j = 2; // Se pone 'j = 2' porque la primera posición no cuenta y la segunda es la raíz (tampoco cuenta)
		double wDeadsTest = 0.0, wTotalTest = 0.0; // Almacena el weigh del mutante i-ésimo y 'wDeadsTest' el peso de los mutantes muertos
		boolean keep = true; // Si el mutante se mata, se pone a false
		
		for(int i = 0; i < numMutants; i++){ // Recorremos todos los mutantes desde 0 hasta 'numMutants'
			this.fileRead = new CustomFileRead("prueba" + i + ".txt");
			this.in = new Scanner(this.fileRead);
			this.weighMutant = this.fileRead.readDouble(this.in);
			ArrayList<Integer> mutant = this.fileRead.readArray(this.in); // Preparamos el mutante i-ésimo
			while(keep && j < origen.size()){ // Mientras no le hayamos matado o queden posiciones por comprobar
				if(mutant.get(1) == 1 || (origen.get(j) != mutant.get(j))){
					wDeadsTest += this.weighMutant;
					keep = false;
				}
				j++;
			}
			wTotalTest += this.weighMutant;
			j = 2; // Reiniciamos el índice que recorre el array 'origen'
			keep = true; // Y 'keep' lo ponemos a true
		}
		System.out.println("Peso mutantes muertos: " + wDeadsTest); // Peso de los mutantes muertos
		System.out.println("Peso mutantes totales: " + (wTotalTest)); // Peso de los mutantes totales
		System.out.println("Distinguishing Rate: " + calculateDR(wDeadsTest, wTotalTest));
	}

	/**
	 * 	Devuelve el Distinguishing Rate
	 * @param total
	 * @param deads
	 * @return
	 */
	public double calculateDR(double wDeadsTest, double wTotalTest){
		return wDeadsTest / wTotalTest;
	}
	
	/**
	 * Devuelve el test suite introducido por el usuario
	 * @return
	 */
	public ArrayList<Integer> getTest(){
		System.out.println("Introduzca la longitud del test (incluida la raíz): ");
		int longTest = this.sc.nextInt();
		ArrayList<Integer> positions = new ArrayList<Integer>(Collections.nCopies(longTest, 0)); // Creamos un array para almacenar las posiciones a evaluar
		positions.set(0, 1); // Obviamente, en la primera posición siempre irá la raíz del árbol
		if(longTest > 1){
			System.out.println("A continuación, deberá escoger el camino que desea testear. Para ello, pulse (a) para ir \n"
					+ "a la izquierda o (b) para ir a la derecha en el árbol. Haga esto n-1 veces, donde n es la longitud de su test.");
			char op;
			for(int i = 1; i < longTest; i++){
				op = this.sc.next().charAt(0);
				if(op == 'a') // Si elige ir hacia la izquierda
					positions.set(i, positions.get(i-1)*2); // Metemos el índice del hijo izquierdo
				else // Si elige ir hacia la derecha
					positions.set(i, (positions.get(i-1)*2) + 1); // Metemos el índice del hijo derecho	
			}
		}
		return positions;
	}
	
	/**
	 * Devuelve un test aleatorio de una longitud dada
	 * @param size
	 * @return
	 */
	public ArrayList<Integer> getRandomTest(int size){
		return null;
	}
	
	/**
	 * Almacena el array 'mutant' en el fichero de escritura
	 * @param file
	 * @param fileName
	 * @param mutant
	 * @param weigh
	 */
	public void saveMutant(ArrayList<Integer> mutant){
		String text = "";
		text += Double.toString(this.weighMutant) + " 0 ";
		for(int i = 1; i < mutant.size(); i++) // Almacenamos el array
			text += mutant.get(i) + " "; 
		text += mutant.get(0); // Metemos el -1 al final del texto
	
		this.fileWrite.writeFile(this.fileWrite, text);
		this.fileWrite.closeFileWrite(this.fileWrite);
	}
	
	/**
	 * Calcula el peso de los mutantes del primer ejemplo
	 * @param origen
	 * @param mutant
	 * @return
	 */
	public void calculateWeigh(ArrayList<Integer> origen, ArrayList<Integer> mutant){
		int interval = 1, base = 4, exponent = 0;
		double penalty = 4;
		for(int i = 2; i < this.nodes; i++){ // 'i = 2', la raíz no tiene peso (no tiene sentido que lo tenga)
			if(i == interval * 2){ // Comprobamos si es necesario actualizar la penalización (porque hemos "descendido" en el árbol)
				exponent++; // Incrementamos el exponente
				penalty = Math.pow(base, exponent);
				interval = interval * 2;
			}
			if(origen.get(i) != mutant.get(i)) // Si los elementos i-ésimos son distintos
				this.weighMutant = this.weighMutant + (1/penalty); // Actualizamos el peso del muntante; new weigh = weigh + 1/penalty
		}
		this.weighMutant = 1 - this.weighMutant;
	}
	
	/**
	 * Calcula el peso de los mutantes del tercer ejemplo
	 * @param origen
	 * @param mutant
	 * @return
	 */
	public void calculateOnlyError(ArrayList<Integer> origen, ArrayList<Integer> mutant){
		int interval = 1, base = 4, exponent = 0;
		double penalty = 4;
		for(int i = 2; i < this.nodes; i++){ // 'i = 2', la raíz no tiene peso (no tiene sentido que lo tenga)
			if(i == interval * 2){ // Comprobamos si es necesario actualizar la penalización (porque hemos "descendido" en el árbol)
				exponent++; // Incrementamos el exponente
				penalty = Math.pow(base, exponent);
				interval = interval * 2;
			}
			if(origen.get(i) != mutant.get(i)) // Si los elementos i-ésimos son distintos
				this.weighMutant = this.weighMutant + (1/penalty); // Actualizamos el peso del muntante; new weigh = weigh + 1/penalty
		}
	}
	
	/**
	 * Calcula el peso de los mutantes del segundo ejemplo
	 * @param origen
	 * @param mutant
	 */
	public void calculateFirtsWeigh(ArrayList<Integer> origen, ArrayList<Integer> mutant){
		int interval = 1, base = 4, exponent = 0, i = 2;
		double penalty = 4;
		boolean keep = true;
		while(i < this.nodes && keep){
			if(i == interval * 2){ // Comprobamos si es necesario actualizar la penalización (porque hemos "descendido" en el árbol)
				exponent++; // Incrementamos el exponente
				penalty = Math.pow(base, exponent);
				interval = interval * 2;
			}
			if(origen.get(i) != mutant.get(i)){ // Si los elementos i-ésimos son distintos
				this.weighMutant = this.weighMutant + (1/penalty); // Actualizamos el peso del muntante; new weigh = weigh + 1/penalty
				keep = false;
			}
			i++;
		}
		this.weighMutant = 1 - this.weighMutant;
	}
	
	/**
	 * Inicializa el array original
	 * @param nodes
	 * @return
	 */
	public ArrayList<Integer> initializeOrigen(){
		ArrayList<Integer> origen = new ArrayList<Integer>(Collections.nCopies(this.nodes, 0));
		return origen;
	}
	
	/**
	 * Inicializa un mutante
	 * @param nodes
	 * @return
	 */
	public ArrayList<Integer> initializeMutant(){
		ArrayList<Integer> mutant = new ArrayList<Integer>(Collections.nCopies(this.nodes, 0));
		for(int i = 1; i < this.nodes; i++) // Inicializamos de forma aleatoria
			mutant.set(i, randInt(0, 1));
		mutant.set(0, -1); // En la primera posición metemos -1 porque será útil al escribir en fichero
		return mutant;
	}
	
	/**
	 * Generador de números aleatorios entre el intervalo [min, max]
	 * @param min
	 * @param max
	 * @return
	 */
	public static int randInt(int min, int max){
		Random rand = new Random();
		return rand.nextInt((max - min) + 1) + min;
	}
}
