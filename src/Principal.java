import java.util.Map;

public class Principal {
	
	/*
	 * Clase principal del programa.
	 * 
	 * En esta clase fijamos la direcci�n del servidor SQL, que como estar� en la misma m�quina que
	 * esta aplicaci�n, se pone la direcci�n de loopback
	 * 
	 * Tambi�n se encarga de iniciar el hilo del servidor
	 */

	private final static String DIRECCIONIP = "127.0.0.1";
	private final static int PUERTO = 35698;
	// Hilo principal del servidor
	private static Thread hiloServidor;
	// Mapa que contendr� todas las conexiones
	private static Map<String, ClientListener> hilos;
	
	// Direcci�n IP servidor SQL
	public static String getDireccionip() {
		return DIRECCIONIP;
	}
	// Puerto servidor SQL 
	public static int getPuerto() {
		return PUERTO;
	}
		
	public static void main(String[] args) {
		try {
			// Inicializa el m�dulo de encriptaci�n
			EncryptModule.initializeKey();
			// Inicia el hilo del servidor
			hiloServidor = new Server();
			hiloServidor.start();
			
		} catch (Exception e) {
			// El �nico fallo que puede dar es un error a la hora de inicializar los certificados
			// del m�ulo de encriptaci�n
			System.out.println("Fallo al inicializar el certificado");
			System.out.println(e.getMessage());
		}
		
	}
	
	public static Map<String, ClientListener> getHilos() {
		return hilos;
	}
	
	public static void setHilos(Map<String, ClientListener> hilos) {
		Principal.hilos = hilos;
	}
	
	public static void addUser(String user, ClientListener cli) {
		
		// Introduce una conexi�n en el mapa de conexiones.
		// Si la conexi�n ya existiese, cerrar�a la m�s antigua
		if (getHilos().containsKey(user)) {
			ClientListener subs = getHilos().get(user);
			// M�todo que cierra una conexi�n
			subs.killThread();
			getHilos().remove(user);
			Consola.event("Server -> Kick client " + user + ". Reason: New login");
		}
		
		getHilos().put(user, cli);
		
	}
	
	// M�todo para apagar el servidor.
	public static void powerOff() {
		// Cierra todas las conexiones previamente
		for (ClientListener c : hilos.values()) {
			Consola.event("Expulsando a "+c.getHilo().getName());
			c.killThread();
		}
		Consola.message("Servidor desconectado");
		System.exit(0);
	}
	
}
