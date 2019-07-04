import java.util.Map;

public class Principal {
	
	/*
	 * Clase principal del programa.
	 * 
	 * En esta clase fijamos la dirección del servidor SQL, que como estará en la misma máquina que
	 * esta aplicación, se pone la dirección de loopback
	 * 
	 * También se encarga de iniciar el hilo del servidor
	 */

	private final static String DIRECCIONIP = "127.0.0.1";
	private final static int PUERTO = 35698;
	// Hilo principal del servidor
	private static Thread hiloServidor;
	// Mapa que contendrá todas las conexiones
	private static Map<String, ClientListener> hilos;
	
	// Dirección IP servidor SQL
	public static String getDireccionip() {
		return DIRECCIONIP;
	}
	// Puerto servidor SQL 
	public static int getPuerto() {
		return PUERTO;
	}
		
	public static void main(String[] args) {
		try {
			// Inicializa el módulo de encriptación
			EncryptModule.initializeKey();
			// Inicia el hilo del servidor
			hiloServidor = new Server();
			hiloServidor.start();
			
		} catch (Exception e) {
			// El único fallo que puede dar es un error a la hora de inicializar los certificados
			// del móulo de encriptación
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
		
		// Introduce una conexión en el mapa de conexiones.
		// Si la conexión ya existiese, cerraría la más antigua
		if (getHilos().containsKey(user)) {
			ClientListener subs = getHilos().get(user);
			// Método que cierra una conexión
			subs.killThread();
			getHilos().remove(user);
			Consola.event("Server -> Kick client " + user + ". Reason: New login");
		}
		
		getHilos().put(user, cli);
		
	}
	
	// Método para apagar el servidor.
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
