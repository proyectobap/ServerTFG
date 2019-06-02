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
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

public class ClientListener implements Runnable {
	
	private final Thread hilo;
    private final Socket clientSocket;
    private boolean running;
    
    private AccesoSQL acceso;
    
    private String preguntaEnc;
    private JSONObject pregunta;
    
    private ObjectInputStream entrada = null;
    private ObjectOutputStream salida = null;
    
    private PublicKey clientPublicKey;
    private SecretKey claveSimetricaSecreta;
    private byte[] claveSimetrica;
    private Cipher cifradorAsimetrico;
    private Cipher cifradorSimetrico;
    
    private GCMParameterSpec parameterSpec;
    
    public ClientListener(Socket cliente) {
        
    	acceso = new AccesoSQL();
    	
    	this.clientSocket = cliente;
    	hilo = new Thread(this, "Cliente "+clientSocket.getRemoteSocketAddress());
    	
    	System.out.print(" - [SQL:");
    	
        try {
			if (acceso.connect()) {
				System.out.print(Consola.GREEN+"OK"+Consola.RESET+"]");
				
				running = true;
		        hilo.start();
				
			} else {
				System.out.println(Consola.RED+"FAIL"+Consola.RESET+"]");
				running = true;
		        hilo.start();
				//clientSocket.close();
			}
		} catch (SQLException e) {
			System.out.println(Consola.RED+"FAIL"+Consola.RESET+"] -> "+e.getMessage());
			running = true;
	        hilo.start();
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
			
		}
		
		try {
			
			System.out.print(" - [ICS:");
			inicializacionClaveSimetrica();
			enviar(new String(Base64.getEncoder().encode(claveSimetrica)));
			System.out.print(Consola.GREEN+"OK"+Consola.RESET+"]");
			
		} catch (Exception e) {
		
			System.out.println(Consola.RED+"FAIL"+Consola.RESET+"] -> "+e.getMessage());
			
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
				Consola.message(hilo.getName() + " conectado");
			}

	
			while (running) {
				
				preguntaEnc = (String) entrada.readObject();
				pregunta = new JSONObject(symetricDecript(preguntaEnc));
				
				switch (pregunta.getString("peticion").toLowerCase()) {
				
				case "testlogin":
					enviar(symetricEncrypt(acceso.loginList()));
					Consola.info(hilo.getName() + " -> TestLogin");
					break;
				case "newticket":
					enviar(symetricEncrypt(acceso.newTicket(pregunta)));
					Consola.info(hilo.getName() + " -> Crear Ticket");
					break;
				case "newuser":
					enviar(symetricEncrypt(acceso.newUser(pregunta)));
					Consola.info(hilo.getName() + " -> Crear Usuario");
					break;
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
				case "listticket":
					enviar(symetricEncrypt(acceso.listarTickets()));
					Consola.info(hilo.getName() + " -> Listado Tickets");
					break;
				case "exit":
					running = false;
					continue;
				case "help":
					enviar(symetricEncrypt(acceso.help()));
					Consola.info(hilo.getName() + " -> Comando ayuda");
					break;
				default:
					enviar(symetricEncrypt(JsonTreatment.nullResponse()));
					break;
				}
				
				
			}
			
			entrada.close();
    		salida.close();
    		clientSocket.close();
    		acceso.closeConnection();
    		Consola.event(hilo.getName() + " desconectado.");
			
		} catch (IOException 
				| ClassNotFoundException 
				| InvalidKeyException 
				| IllegalBlockSizeException 
				| BadPaddingException 
				| SQLException 
				| JSONException 
				| InvalidAlgorithmParameterException e) {
			
			Consola.error(hilo.getName() + " se desconectó con el error \""+e.getMessage()+"\"");
			
			try {
				entrada.close();
	    		salida.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			try {
				clientSocket.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
    		
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
    	try {
    		salida.close();
    	} catch (Exception e) {}
    	
    	try {
    		entrada.close();
    	} catch (Exception e) {}
		
		try {
			clientSocket.close();
		} catch (Exception e) {}
    }
	
}
