import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Consola {
	
	/*
	 * Esta clase est� pensada para facilitar el retorno de informaci�n a la consola del sistema de una 
	 * forma visualmente agradable y �til. Da formato a la salida de informaci�n.
	 */

	// Constantes de colores
	public static final String RED="\033[31m"; 
	public static final String GREEN="\033[32m"; 
	public static final String YELLOW="\033[33m"; 
	public static final String BLUE="\033[34m"; 
	public static final String PURPLE="\033[35m"; 
	public static final String CYAN="\033[36m"; 
	public static final String WHITE="\033[37m";
	public static final String RESET="\u001B[0m";
	
	// Patr�n de fecha
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); 
	
	// M�todos para mostrar la informaci�n dependiendo del tipo que sea.
	// Para usar estos m�todos solo habr�a que pasarle la cadena de texto a mostrar
	// y se asociar�a autom�ticamente el color y la fecha del evento
	
	public static void info(String texto) {
		System.out.println(date() + " - " + texto);
	}
	
	public static void error(String texto) {
		System.out.println(date() + " - " + RED + texto + RESET);
	}
	
	public static void message(String texto) {
		System.out.println(date() + " - " + GREEN + texto + RESET);
	}
	
	public static void event(String texto) {
		System.out.println(date() + " - " + PURPLE + texto + RESET);
	}
	
	// Devuelve la fecha y hora actual en base al formato indicado anteriormente
	public static String date() {
		return RESET + LocalDateTime.now().format(formatter);
	}
	
}
