import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server extends Thread {
		
	public Server() {
		Principal.setHilos(new HashMap<String, ClientListener>());
	}
	
	@SuppressWarnings({ "unused", "resource" })
	@Override
	public void run() {
		System.out.println("BACK-END SERVER 0.5.2 (07/06)");
		System.out.println("-------------------");
		
		try {
			
			ServerSocket servidor = new ServerSocket(Principal.getPuerto());
			
			System.out.println("Servidor escuchando peticiones en el puerto: "+servidor.getLocalPort());
			
			while (true) {
				Socket conexionCliente = servidor.accept();
				
				String clientFullAddress = conexionCliente.getRemoteSocketAddress().toString();
				String clientAddress = clientFullAddress.substring(0, clientFullAddress.indexOf(":"));
				
				if (Principal.getHilos().containsKey(clientAddress)) {
					ClientListener subs = Principal.getHilos().get(clientAddress);
					Consola.event("Server -> Kick client " + clientAddress + ". Reason: Duplicated IP");
					subs.killThread();
					Principal.getHilos().remove(clientAddress);
					Thread.sleep(500);
				}
	
				System.out.print(Consola.date() + " - ");
				System.out.print(Consola.YELLOW+"Cliente "+conexionCliente.getRemoteSocketAddress()+Consola.RESET);
				
				Principal.getHilos().put(clientAddress, new ClientListener(conexionCliente));
			
			}
			
		} catch (IOException | InterruptedException e) {
			System.out.println(e.getMessage());
		}
		
	}
	
}