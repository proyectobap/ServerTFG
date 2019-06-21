import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server extends Thread {
		
	public Server() {
		Principal.setHilos(new HashMap<String, ClientListener>());
	}
	
	@SuppressWarnings({ "resource" })
	@Override
	public void run() {
		System.out.println("BACK-END SERVER 0.7 (21/06)");
		System.out.println("-------------------");
		
		try {
			
			ServerSocket servidor = new ServerSocket(Principal.getPuerto());
			
			System.out.println("Servidor escuchando peticiones en el puerto: "+servidor.getLocalPort());
			
			while (true) {
				
				Socket conexionCliente = servidor.accept();
				System.out.print(Consola.date() + " - ");
				System.out.print(Consola.YELLOW+"Cliente "+conexionCliente.getRemoteSocketAddress()+Consola.RESET);
				new ClientListener(conexionCliente);
			
			}
			
		} catch (IOException e) {
			System.out.println(Consola.CYAN+"Servidor desconectado"+Consola.RESET);
		}
		
	}
	
}