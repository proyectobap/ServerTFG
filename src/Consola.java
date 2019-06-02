import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Consola {

	public static final String RED="\033[31m"; 
	public static final String GREEN="\033[32m"; 
	public static final String YELLOW="\033[33m"; 
	public static final String BLUE="\033[34m"; 
	public static final String PURPLE="\033[35m"; 
	public static final String CYAN="\033[36m"; 
	public static final String WHITE="\033[37m";
	public static final String RESET="\u001B[0m";
	
	private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"); 
	
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
	
	public static String date() {
		return RESET + LocalDateTime.now().format(formatter);
	}
	
}
