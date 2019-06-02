import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class Server extends Thread {
		
	private Map<String, ClientListener> hilos;
	
	public Server() {
		hilos = new HashMap<String, ClientListener>();
	}
	
	@SuppressWarnings({ "unused", "resource" })
	@Override
	public void run() {
		System.out.println("BACK-END SERVER 0.5 (02/06)");
		System.out.println("-------------------");
		
		try {
			
			ServerSocket servidor = new ServerSocket(Principal.getPuerto());
			
			System.out.println("Servidor escuchando peticiones en el puerto: "+servidor.getLocalPort());
			
			while (true) {
				Socket conexionCliente = servidor.accept();
				
				String clientFullAddress = conexionCliente.getRemoteSocketAddress().toString();
				String clientAddress = clientFullAddress.substring(0, clientFullAddress.indexOf(":"));
				
				if (hilos.containsKey(clientAddress)) {
					ClientListener subs = hilos.get(clientAddress);
					Consola.event("Server -> Kick client " + clientAddress + ". Reason: Duplicated IP");
					subs.killThread();
					hilos.remove(clientAddress);
					Thread.sleep(500);
				}
	
				System.out.print(Consola.date() + " - ");
				System.out.print(Consola.YELLOW+"Cliente "+conexionCliente.getRemoteSocketAddress()+Consola.RESET);
				
				hilos.put(clientAddress, new ClientListener(conexionCliente));
			
			}
			
		} catch (IOException | InterruptedException e) {
			System.out.println(e.getMessage());
		}
		
	}
}