import java.util.Map;

public class Principal {

	private final static String DIRECCIONIP = "127.0.0.1";
	private final static int PUERTO = 35698;
	private static Thread hiloServidor;
	private static Map<String, ClientListener> hilos;
	
	public static String getDireccionip() {
		return DIRECCIONIP;
	}
	public static int getPuerto() {
		return PUERTO;
	}
		
	public static void main(String[] args) {
		try {
			EncryptModule.initializeKey();
			hiloServidor = new Server();
			hiloServidor.start();
		} catch (Exception e) {
			System.err.println("Fallo al inicializar el certificado");
			System.out.println(e.getMessage());
		}
		
	}
	
	public static Map<String, ClientListener> getHilos() {
		return hilos;
	}
	
	public static void setHilos(Map<String, ClientListener> hilos) {
		Principal.hilos = hilos;
	}
}
