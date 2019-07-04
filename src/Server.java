import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server extends Thread {
	
	/*
	 * Clase/Hilo del que consta el servicio. Encargado de aceptar peticiones y derivar su manejo al hilo
	 * propio de cada cliente.
	 */
		
	public Server() {
		// Al instanciar el servidor, se inicializa el mapa de conexiones 
		Principal.setHilos(new HashMap<String, ClientListener>());
	}
	
	@Override
	public void run() {
		System.out.println("BACK-END SERVER 1.0 (04/07)");
		System.out.println("---------------------------");
		
		try {
			
			// Crea el socket de escucha. Al no indicarle IP, solo puerto, lo hace en la máquina local
			// equivalente a 127.0.0.1
			ServerSocket servidor = new ServerSocket(Principal.getPuerto());
			
			System.out.println("Servidor escuchando peticiones en el puerto: "+servidor.getLocalPort());
			
			while (true) {
				// Siempre está a la escucha de peticiones
				Socket conexionCliente = servidor.accept();
				System.out.print(Consola.date() + " - ");
				System.out.print(Consola.YELLOW+"Cliente "+conexionCliente.getRemoteSocketAddress()+Consola.RESET);
				// Inicia el hilo que gestiona la conexión con cada cliente individual
				new ClientListener(conexionCliente);
			
			}
			
		} catch (IOException e) {
			System.out.println(Consola.CYAN+"Servidor desconectado"+Consola.RESET);
		}
		
	}
	
}