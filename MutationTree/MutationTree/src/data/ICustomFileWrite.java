package data;

public interface ICustomFileWrite {
	/** Cierra el fichero de escritura */
	public abstract void closeFileWrite(CustomFileWrite file);
	/** Escribe en el fichero de escritura */
	public abstract void writeFile(CustomFileWrite file, String text);
}
