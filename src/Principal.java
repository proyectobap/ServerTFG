
public class Principal {

	private final static String DIRECCIONIP = "127.0.0.1";
	private final static int PUERTO = 35698;
	private static Thread hiloServidor;
	
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
	
	public static void shutdown() {
		System.exit(0);
	}
	
}
