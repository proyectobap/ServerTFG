import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {
			
	@SuppressWarnings({ "unused", "resource" })
	@Override
	public void run() {
		System.out.println("BACK-END SERVER 0.4.1 (31/05)");
		System.out.println("-------------------");
		
		try {
			
			ServerSocket servidor = new ServerSocket(Principal.getPuerto());
			
			System.out.println("Servidor escuchando peticiones en el puerto: "+servidor.getLocalPort());
			
			while (true) {
				Socket conexionCliente = servidor.accept();
				System.out.print(Consola.date() + " - ");
				System.out.print(Consola.YELLOW+"Cliente "+conexionCliente.getRemoteSocketAddress());
				System.out.print(" (");
				ClientListener listener = new ClientListener(conexionCliente);
			}
			
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
		
	}
}