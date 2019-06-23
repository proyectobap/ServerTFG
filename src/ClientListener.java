import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientListener implements Runnable {
	
	private final Thread hilo;
	private final AccesoSQL acceso;
    private final Socket clientSocket;
    private final String clientAddress;
    
    private boolean running;
    private String user;
    private String preguntaEnc;
    private String e = null;
    private int loginId;
    
    private JSONObject pregunta;
    
    private ObjectInputStream entrada = null;
    private ObjectOutputStream salida = null;
    
    private PublicKey clientPublicKey;
    private SecretKey claveSimetricaSecreta;
    private byte[] claveSimetrica;
    private Cipher cifradorAsimetrico;
    private Cipher cifradorSimetrico;
    
    private GCMParameterSpec parameterSpec;
    
    public void setLoginId(int id) {
    	this.loginId = id;
    }
    
    public ClientListener(Socket cliente) {
        
    	acceso = new AccesoSQL(this);
    	
    	this.clientSocket = cliente;
    	hilo = new Thread(this, "Cliente "+clientSocket.getRemoteSocketAddress());
    	
    	String clientFullAddress = clientSocket.getRemoteSocketAddress().toString();
		clientAddress = clientFullAddress.substring(0, clientFullAddress.indexOf(":"));
    	
    	System.out.print(" - [SQL:");
    	
        try {
			if (acceso.connect()) {
				System.out.print(Consola.GREEN+"OK"+Consola.RESET+"]");
				
				running = true;
		        hilo.start();
				
			} else {
				System.out.println(Consola.RED+"FAIL"+Consola.RESET+"]");
				killThread();
			}
		} catch (SQLException e) {
			System.out.println(Consola.RED+"FAIL"+Consola.RESET+"] -> "+e.getMessage());
			killThread();
		}
        
    }

	@Override
	public void run() {
		
		try {
			
			System.out.print(" - [SOCK:");
			
			entrada = new ObjectInputStream(clientSocket.getInputStream());
			salida = new ObjectOutputStream(clientSocket.getOutputStream());
			
			System.out.print(Consola.GREEN+"OK"+Consola.RESET+"]");

		} catch (Exception e) {
			
			System.out.println(Consola.RED+"FAIL"+Consola.RESET+"] -> "+e.getMessage());
			killThread();
			return;
			
		}
		
		try {
			
			System.out.print(" - [ICA:");
			intercambioClaves();
			
			cifradorAsimetrico = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cifradorSimetrico = Cipher.getInstance("AES/GCM/NoPadding");
			
			byte[] testString = new byte[7];
            new Random().nextBytes(testString);
            String test = new String(Base64.getEncoder().encode(testString));
            String encTest = asymetricEncrypt(test);

            enviar(encTest);
            
            String check = asymetricDecript((String) entrada.readObject());
            
            if (test.equals(check)) {
            	System.out.print(Consola.GREEN+"OK"+Consola.RESET+"]");
                enviarResponse(206);
            } else {
            	System.out.println(Consola.RED+"FAIL"+Consola.RESET+"]");
                enviarResponse(400);
            }
			
		} catch (Exception e) {
			
			System.out.println(Consola.RED+"FAIL"+Consola.RESET+"] -> "+e.getMessage());
			killThread();
			return;
			
		}
		
		try {
			
			System.out.print(" - [ICS:");
			inicializacionClaveSimetrica();
			enviar(new String(Base64.getEncoder().encode(claveSimetrica)));
			System.out.print(Consola.GREEN+"OK"+Consola.RESET+"]");
			
		} catch (Exception e) {
		
			System.out.println(Consola.RED+"FAIL"+Consola.RESET+"] -> "+e.getMessage());
			killThread();
			return;
			
		}
			
		
		try {
			
			System.out.print(" - [LOG:");
			
			String[] credentials = (symetricDecript((String) entrada.readObject())).trim().split(",");
			
			JSONObject resp = acceso.login(credentials);
			
			enviar(symetricEncrypt(resp));
			
			if (resp.getInt("response") == 400) {
				running = false;
				System.out.println(Consola.RED+"FAIL"+Consola.RESET+"]");
			} else {
				System.out.println(Consola.GREEN+"OK"+Consola.RESET+"]");
				user = credentials[0];
				Principal.addUser(user, this);
				hilo.setName(user+clientSocket.getRemoteSocketAddress());
				Consola.message(hilo.getName() + " conectado");
			}
			
		} catch (Exception e) {
			
			System.out.println(Consola.RED+"FAIL"+Consola.RESET+"] -> "+e.getMessage());
			killThread();
			return;
			
		}

		try {
	
			while (running) {
				
				preguntaEnc = (String) entrada.readObject();
				pregunta = new JSONObject(symetricDecript(preguntaEnc));
				
				switch (pregunta.getString("peticion").toLowerCase()) {
				
				// Tablas auxiliares
				
				case "listticketstatus":
					enviar(symetricEncrypt(acceso.list(2)));
					Consola.info(hilo.getName() + " -> Listado Estado de Tickets");
					break;
					
				case "listusertype":
					enviar(symetricEncrypt(acceso.list(3)));
					Consola.info(hilo.getName() + " -> Listado Tipos de Usuario");
					break;
					
				case "listeventtype":
					enviar(symetricEncrypt(acceso.list(1)));
					Consola.info(hilo.getName() + " -> Listado Tipos de Eventos");
					break;
					
				case "listelementtype":
					enviar(symetricEncrypt(acceso.list(0)));
					Consola.info(hilo.getName() + " -> Listado Tipos de Elementos");
					break;
					
				// Inserci�n
					
				case "newticket":
					enviar(symetricEncrypt(acceso.newTicket(pregunta)));
					Consola.info(hilo.getName() + " -> Crear Ticket");
					break;
					
				case "newuser":
					enviar(symetricEncrypt(acceso.newUser(pregunta)));
					Consola.info(hilo.getName() + " -> Crear Usuario");
					break;
					
				case "newlogin":
					enviar(symetricEncrypt(acceso.newUser(pregunta)));
					Consola.info(hilo.getName() + " -> Asociar Login");
					break;
					
				case "newevent":
					enviar(symetricEncrypt(acceso.newEvent(pregunta)));
					Consola.info(hilo.getName() + " -> Crear Evento");
					break;
					
				case "newtask":
					enviar(symetricEncrypt(acceso.newTask(pregunta)));
					Consola.info(hilo.getName() + " -> Crear Tarea");
					break;
					
				case "newhardware":
					enviar(symetricEncrypt(acceso.newHardware(pregunta)));
					Consola.info(hilo.getName() + " -> Crear Hardware");
					break;
					
				case "newsoftware":
					enviar(symetricEncrypt(acceso.newSoftware(pregunta)));
					Consola.info(hilo.getName() + " -> Crear Software");
					break;
					
				// Consulta Tablas
				
				case "listticket":
					enviar(symetricEncrypt(acceso.listarTickets()));
					Consola.info(hilo.getName() + " -> Listado Tickets");
					break;
					
				case "listticketfilter":
					enviar(symetricEncrypt(acceso.listarTicketsFiltro(pregunta)));
					Consola.info(hilo.getName() + " -> Listado Tickets con filtro");
					break;
					
				case "ticketview":
					enviar(symetricEncrypt(acceso.cogerTicket(pregunta)));
					Consola.info(hilo.getName() + " -> Petici�n ticket");
					break;
					
				case "listusers":
					enviar(symetricEncrypt(acceso.userList()));
					Consola.info(hilo.getName() + " -> Listado usuarios");
					break;
					
				case "listevents":
					enviar(symetricEncrypt(acceso.eventList(pregunta)));
					Consola.info(hilo.getName() + " -> Listado eventos");
					break;
					
				case "listelements":
					enviar(symetricEncrypt(acceso.elementList()));
					Consola.info(hilo.getName() + " -> Listado elementos");
					break;
					
				// Modificar registros					
					
				case "modifypassword":
					enviar(symetricEncrypt(acceso.modifyUserPassword(pregunta, loginId)));
					Consola.info(hilo.getName() + " -> Modificaci�n Contrase�a");
					break;
					
				case "modifyownuser":
					enviar(symetricEncrypt(acceso.modifyOwnUser(pregunta, loginId)));
					Consola.info(hilo.getName() + " -> Modificaci�n Usuario");
					break;
					
				case "modifyuser":
					enviar(symetricEncrypt(acceso.modifyUser(pregunta)));
					Consola.info(hilo.getName() + " -> Modificaci�n Usuario por admin");
					break;
					
				case "modifyevent":
					enviar(symetricEncrypt(acceso.modifyEvent(pregunta)));
					Consola.info(hilo.getName() + " -> Modificaci�n Evento");
					break;
					
				case "modifytask":
					enviar(symetricEncrypt(acceso.modifyTask(pregunta)));
					Consola.info(hilo.getName() + " -> Modificaci�n Tarea");
					break;
					
				case "modifyticket":
					enviar(symetricEncrypt(acceso.modifyTicket(pregunta)));
					Consola.info(hilo.getName() + " -> Modificaci�n Ticket");
					break;
					
				case "solveticket":
					enviar(symetricEncrypt(acceso.solveTicket(pregunta)));
					Consola.info(hilo.getName() + " -> Soluci�n de Ticket");
					break;
					
				case "modifyelement":
					enviar(symetricEncrypt(acceso.modifyElement(pregunta)));
					Consola.info(hilo.getName() + " -> Modificaci�n Elemento");
					break;
					
				case "modifyhardware":
					enviar(symetricEncrypt(acceso.modifyHardware(pregunta)));
					Consola.info(hilo.getName() + " -> Modificaci�n Hardware");
					break;
					
				case "modifysoftware":
					enviar(symetricEncrypt(acceso.modifySoftware(pregunta)));
					Consola.info(hilo.getName() + " -> Modificaci�n Software");
					break;
					
				// Runtime Options
					
				case "exit":
					running = false;
					break;
					
				case "poweroff":
					running = false;
					Consola.error(hilo.getName() + " ha iniciado el apagado del Servidor!".toUpperCase());
					Principal.powerOff();
					break;
					
				default:
					enviar(symetricEncrypt(acceso.help()));
					Consola.info(hilo.getName() + " -> Comando ayuda");
					break;
				}
				
			}
			
			running = true;
			entrada.close();
    		salida.close();
    		clientSocket.close();
    		acceso.closeConnection();
    		Principal.getHilos().remove(clientAddress);
			
		} catch (JSONException e) {
			this.e = e.getMessage();
		} catch (IOException e) {
			this.e = e.getMessage();
		} catch (SQLException e) {
			this.e = e.getMessage();
		} catch (InvalidKeyException e) {
			this.e = e.getMessage();
		} catch (InvalidAlgorithmParameterException e) {
			this.e = e.getMessage();
		} catch (IllegalBlockSizeException e) {
			this.e = e.getMessage();
		} catch (BadPaddingException e) {
			this.e = e.getMessage();
		} catch (ClassNotFoundException e) {
			this.e = e.getMessage();
		} catch (Exception e) {
			this.e = e.getMessage();
		} finally {
			if (e == null) {
				Consola.event(hilo.getName() + " desconectado.");
			} else {
				if (running) {
					Consola.error(hilo.getName() + " se desconect� con el error \"" + e + "\"");
				} else {
					Consola.event(hilo.getName() + " ha sido desconectado.");
				}
			}
			killThread();
		}
	}
	
	/******************************************************************************/
	
	private void inicializacionClaveSimetrica() {
		claveSimetrica = new byte[16];
		EncryptModule.getSecureRandom().nextBytes(claveSimetrica);
		claveSimetricaSecreta = new SecretKeySpec(claveSimetrica, "AES");
	}
	
	/******************************************************************************/
	
	private void intercambioClaves() throws 
    			InvalidKeySpecException, 
    			NoSuchAlgorithmException, 
    			IOException, 
    			ClassNotFoundException {
		
		BigInteger moduloPublico = (BigInteger) entrada.readObject();
		BigInteger exponentePublico = (BigInteger) entrada.readObject();
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		clientPublicKey = keyFactory.generatePublic(new RSAPublicKeySpec(moduloPublico,exponentePublico));
		
		salida.writeObject(EncryptModule.getModulus());
        salida.flush();
        salida.writeObject(EncryptModule.getExponent());
        salida.flush();
	}
	
	/******************************************************************************/
	
    private String asymetricEncrypt(String mensaje) throws 
            	IllegalBlockSizeException, 
            	BadPaddingException, 
            	InvalidKeyException, UnsupportedEncodingException {
        
        cifradorAsimetrico.init(Cipher.ENCRYPT_MODE, clientPublicKey);
        byte[] mensajeCifrado = cifradorAsimetrico.doFinal(mensaje.getBytes());
        return new String(Base64.getEncoder().encode(mensajeCifrado));
    }
    
    /******************************************************************************/
    
    private String asymetricDecript(String mensajeCifrado64) throws 
	            IllegalBlockSizeException, 
	            BadPaddingException, 
	            InvalidKeyException {
        
        byte[] mensajeCifrado = Base64.getDecoder().decode(mensajeCifrado64);
        cifradorAsimetrico.init(Cipher.DECRYPT_MODE, EncryptModule.getPrivateKey());
        return new String(cifradorAsimetrico.doFinal(mensajeCifrado));
    }
    
/**********************************************************************************/
	
    private String symetricEncrypt(JSONObject json) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException { 
    	byte[] iv = new byte[12];
    	EncryptModule.getSecureRandom().nextBytes(iv);
    	parameterSpec = new GCMParameterSpec(128, iv);
    	cifradorSimetrico.init(Cipher.ENCRYPT_MODE, claveSimetricaSecreta, parameterSpec);
    	
    	byte[] cipherText = cifradorSimetrico.doFinal(json.toString().getBytes());
    	ByteBuffer bf = ByteBuffer.allocate(4+iv.length+cipherText.length); 
		bf.putInt(iv.length);
		bf.put(iv);
		bf.put(cipherText);
		
		byte[] cipherMessage = bf.array();
		return new String(Base64.getEncoder().encode(cipherMessage));
    }
    
    /******************************************************************************/
    
    private String symetricDecript(String mensajeCifrado64) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException { 
    	byte[] cifMen = Base64.getDecoder().decode(mensajeCifrado64);
        ByteBuffer bf = ByteBuffer.wrap(cifMen);
        int ivLength = bf.getInt();
        if (ivLength < 12 || ivLength >=16) {
        	throw new IllegalArgumentException("invalid iv length");
        }
        byte[] iv = new byte[ivLength];
        bf.get(iv);
        byte[] cipherText = new byte[bf.remaining()];
        bf.get(cipherText);
        
        parameterSpec = new GCMParameterSpec(128, iv);
        cifradorSimetrico.init(Cipher.DECRYPT_MODE, claveSimetricaSecreta, parameterSpec);
        return new String(cifradorSimetrico.doFinal(cipherText));
    }
    
    /******************************************************************************/
    
    private void enviar(Object mensaje) throws IOException {
        salida.writeObject(mensaje);
        salida.flush();
    }
    
    /******************************************************************************/
    
    private void enviarResponse(int response) throws IOException {
        salida.writeInt(response);
        salida.flush();
    }
    
    /******************************************************************************/
    
    public void killThread() {
    	
    	running = false;
    	
    	try {
    		salida.close();
    	} catch (Exception e) {}
    	
    	try {
    		entrada.close();
    	} catch (Exception e) {}
		
		try {
			clientSocket.close();
		} catch (Exception e) {}
		
		Principal.getHilos().remove(user);
    }
    
    /******************************************************************************/
    
    public Thread getHilo() {
    	return hilo;
    }
	
}
