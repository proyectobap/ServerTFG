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
	
	// Referencia al propio hilo de la clase
	private final Thread hilo;
	// Referencia a la clase SQL
	private final AccesoSQL acceso;
	// Referencia al Socket
    private final Socket clientSocket;
    // Referencia a la direcci�n
    private final String clientAddress;
    
    // par�metro para saber si el hilo debe seguir ejecut�ndose
    private boolean running;
    
    // Par�metros varios
    private String user;
    private String preguntaEnc;
    private String e = null;
    private JSONObject pregunta;
    
    // Informaci�n sobre el usuario almacenada en el proceso de login
    private int loginId;
    private int userId;
    
    // Streams de entrada y salida
    private ObjectInputStream entrada = null;
    private ObjectOutputStream salida = null;
    
    // Referencias a claves
    private PublicKey clientPublicKey;
    private SecretKey claveSimetricaSecreta;
    private byte[] claveSimetrica;
    private Cipher cifradorAsimetrico;
    private Cipher cifradorSimetrico;
    private GCMParameterSpec parameterSpec;
    
    public void setLoginId(int id) {
    	this.loginId = id;
    }
    
    public void setUserId(int id) {
    	this.userId = id;
    }
    
    // Constructor
    public ClientListener(Socket cliente) {
        
    	// Inicializa la conexi�n SQL
    	acceso = new AccesoSQL(this);
    	
    	this.clientSocket = cliente;
    	// Pone al hilo el nombre de "Cliente + la IP y puerto del cliente
    	hilo = new Thread(this, "Cliente "+clientSocket.getRemoteSocketAddress());
    	
    	String clientFullAddress = clientSocket.getRemoteSocketAddress().toString();
    	// Captura en una cadena de texto la direcci�n IP, sin puerto, para detectar
    	// conexiones duplicadas desde la misma IP
		clientAddress = clientFullAddress.substring(0, clientFullAddress.indexOf(":"));
    	
		// Comprobaci�n SQL
    	System.out.print(" - [SQL:");
    	
        try {
        	// Si se ha conectado a la base de datos...
			if (acceso.connect()) {
				System.out.print(Consola.GREEN+"OK"+Consola.RESET+"]");
				// ... inicia la ejecuci�n del hilo
				running = true;
		        hilo.start();
				
			} else {
				// Si no, cierra el proceso
				System.out.println(Consola.RED+"FAIL"+Consola.RESET+"]");
				killThread();
			}
		} catch (SQLException e) {
			// Si hubiese habido alg�n fallo, lo muestra por consola
			System.out.println(Consola.RED+"FAIL"+Consola.RESET+"] -> "+e.getMessage());
			killThread();
		}
        
    }

    // M�todo ejecutado al iniciarse el hilo
	@Override
	public void run() {
		
		try {
			
			// Comprobaci�n Socket
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
			
			// Comprobaci�n del Intemcambio de Clave Asim�trica
			System.out.print(" - [ICA:");
			// Inicia el proceso para intercambiar la clave p�blica con el cliente
			intercambioClaves();
			
			// Inicializa el cifrador para clave asim�trica RSA
			cifradorAsimetrico = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			// Inicializa el cifrador para clave sim�trica AES
			cifradorSimetrico = Cipher.getInstance("AES/GCM/NoPadding");
			
			// Para comprobar que la recepci�n y env�o de las claves es correcta
			// se genera una cadena aleatoria
			byte[] testString = new byte[7];
            new Random().nextBytes(testString);
            String test = new String(Base64.getEncoder().encode(testString));
            // Y la env�a al cliente codificada con su clave p�blica
            String encTest = asymetricEncrypt(test);
            enviar(encTest);
            
            // El cliente descifra la cadena, la cifra con la su clave privada y la env�a
            // de vuelta al servidor, encriptado con la clave p�blica de este.
            // El servidor la desencripta y comprueba que el resultado enviado es el mismo
            // que el recibido
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
			
			// Comprobaci�n del Intemcambio de Clave Sim�trica
			System.out.print(" - [ICS:");
			
			// Una vez comprobado que el sistema de clave asim�trica funciona, el servidor
			// env�a una clave sim�trica generada para el intercambio de mensajes
			inicializacionClaveSimetrica();
			enviar(new String(Base64.getEncoder().encode(claveSimetrica)));
			System.out.print(Consola.GREEN+"OK"+Consola.RESET+"]");
			
		} catch (Exception e) {
		
			System.out.println(Consola.RED+"FAIL"+Consola.RESET+"] -> "+e.getMessage());
			killThread();
			return;
			
		}

		try {
			
			// Comprobaci�n del login
			System.out.print(" - [LOG:");
			
			// El servidor recibe las credenciales del cliente y las compara con la base de datos
			String[] credentials = (symetricDecript((String) entrada.readObject())).trim().split(",");
			JSONObject resp = acceso.login(credentials);
			// env�a la respuesta generada por la clase SQL
			enviar(symetricEncrypt(resp));
			
			// Si el login es incorrecto detiene la ejecuci�n
			if (resp.getInt("response") == 400) {
				running = false;
				System.out.println(Consola.RED+"FAIL"+Consola.RESET+"]");
			} else {
				// Si el login es correcto, continua la ejecuci�n
				System.out.println(Consola.GREEN+"OK"+Consola.RESET+"]");
				user = credentials[0];
				// A�ade la conexi�n al mapa de conexiones
				Principal.addUser(user, this);
				hilo.setName(user+clientSocket.getRemoteSocketAddress());
				Consola.message(hilo.getName() + " conectado");
			}
	
			while (running) {
				
				preguntaEnc = (String) entrada.readObject();
				pregunta = new JSONObject(symetricDecript(preguntaEnc));
				
				// Por defecto activa siempre el autocommit
				acceso.setAutoCommit(true);
				
				// Las peticiones est�n explicadas en el documento pdf y la clase SQL
				// Se convierten en min�sculas para evitar errores con el env�o de los comandos
				switch (pregunta.getString("peticion").toLowerCase()) {
				
				// Tablas auxiliares
				
				case "listticketstatus":
					try {
						enviar(symetricEncrypt(acceso.list(2)));
						Consola.info(hilo.getName() + " -> Listado Estado de Tickets");
					} catch (SQLException e) {
						// Todas las peticiones controlan los posibles errores y se los ofrece al cliente
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Listado Estado de Tickets -> "+ e.getMessage());
					}
					break;
					
				case "listusertype":
					try {
						enviar(symetricEncrypt(acceso.list(3)));
						Consola.info(hilo.getName() + " -> Listado Tipos de Usuario");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Listado Tipos de Usuario -> "+ e.getMessage());
					}
					break;
					
				case "listeventtype":
					try {
						enviar(symetricEncrypt(acceso.list(1)));
						Consola.info(hilo.getName() + " -> Listado Tipos de Eventos");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Listado Tipos de Eventos -> "+ e.getMessage());
					}
					break;
					
				case "listelementtype":
					try {
						enviar(symetricEncrypt(acceso.list(0)));
						Consola.info(hilo.getName() + " -> Listado Tipos de Elementos");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Listado Tipos de Elementos -> "+ e.getMessage());
					}
					
					break;
					
				// Inserci�n
					
				case "newticket":
					try {
						enviar(symetricEncrypt(acceso.newTicket(pregunta)));
						Consola.info(hilo.getName() + " -> Crear Ticket");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Crear Ticket -> "+ e.getMessage());
					}
					
					break;
					
				case "newuser":
					try {
						enviar(symetricEncrypt(acceso.newUser(pregunta)));
						Consola.info(hilo.getName() + " -> Crear Usuario");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Crear Usuario -> "+ e.getMessage());
					}
					
					break;
					
				case "newlogin":
					try {
						enviar(symetricEncrypt(acceso.newLogin(pregunta)));
						Consola.info(hilo.getName() + " -> Asociar Login");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Asociar Login -> "+ e.getMessage());
					}
					
					break;
					
				case "newevent":
					try {
						enviar(symetricEncrypt(acceso.newEvent(pregunta)));
						Consola.info(hilo.getName() + " -> Crear Evento");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Crear Evento -> "+ e.getMessage());
					}
					
					break;
					
				case "newtask":
					try {
						enviar(symetricEncrypt(acceso.newTask(pregunta)));
						Consola.info(hilo.getName() + " -> Crear Tarea");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Crear Tarea -> "+ e.getMessage());
						acceso.rollback();
					}
					
					break;
					
				case "newhardware":
					try {
						enviar(symetricEncrypt(acceso.newHardware(pregunta)));
						Consola.info(hilo.getName() + " -> Crear Hardware");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Crear Hardware -> "+ e.getMessage());
						acceso.rollback();
					}
					break;
					
				case "newsoftware":
					try {
						enviar(symetricEncrypt(acceso.newSoftware(pregunta)));
						Consola.info(hilo.getName() + " -> Crear Software");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Crear Software -> "+ e.getMessage());
						acceso.rollback();
					}
					break;
					
				case "assignelement":
					try {
						enviar(symetricEncrypt(acceso.assignElement(pregunta)));
						Consola.info(hilo.getName() + " -> Asociar Elemento");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Asociar Elemento -> "+ e.getMessage());
					}
					break;
					
				case "assigntech":
					try {
						enviar(symetricEncrypt(acceso.assignTech(pregunta)));
						Consola.info(hilo.getName() + " -> Asociar T�cnico");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Asociar T�cnico -> "+ e.getMessage());
					}
					break;
					
				// Consulta Tablas
				
				case "listticket":
					try {
						enviar(symetricEncrypt(acceso.listarTickets()));
						Consola.info(hilo.getName() + " -> Listado Tickets");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Listado Tickets -> "+ e.getMessage());
					}
					break;
					
				case "listticketfilter":
					try {
						enviar(symetricEncrypt(acceso.listarTicketsFiltro(pregunta)));
						Consola.info(hilo.getName() + " -> Listado Tickets con filtro");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Listado Tickets con filtro -> "+ e.getMessage());
					}
					break;
					
				case "ticketview":
					try {
						enviar(symetricEncrypt(acceso.cogerTicket(pregunta)));
						Consola.info(hilo.getName() + " -> Petici�n ticket");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Petici�n ticket -> "+ e.getMessage());
					}
					break;
					
				case "listusers":
					try {
						enviar(symetricEncrypt(acceso.userList()));
						Consola.info(hilo.getName() + " -> Listado usuarios");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Listado usuarios -> "+ e.getMessage());
					}
					break;
					
				case "listevents":
					try {
						enviar(symetricEncrypt(acceso.eventList(pregunta)));
						Consola.info(hilo.getName() + " -> Listado eventos");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Listado eventos -> "+ e.getMessage());
					}
					break;
					
				case "listelements":
					try {
						enviar(symetricEncrypt(acceso.elementList()));
						Consola.info(hilo.getName() + " -> Listado elementos");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Listado elementos -> "+ e.getMessage());
					}
					break;
					
				case "elementdetails":
					try {
						enviar(symetricEncrypt(acceso.elementDetail(pregunta)));
						Consola.info(hilo.getName() + " -> Informaci�n elemento");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Informaci�n elemento -> "+ e.getMessage());
					}
					break;
					
				case "techrelation":
					try {
						enviar(symetricEncrypt(acceso.techRelation(pregunta)));
						Consola.info(hilo.getName() + " -> Informaci�n t�cnicos asociados a ticket");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Informaci�n t�cnicos asociados a ticket -> "+ e.getMessage());
					}
					break;
					
				case "elementrelation":
					try {
						enviar(symetricEncrypt(acceso.elementRelation(pregunta)));
						Consola.info(hilo.getName() + " -> Informaci�n elementos asociados a ticket");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Informaci�n elementos asociados a ticket -> "+ e.getMessage());
					}
					break;
					
				// Modificar registros					
					
				case "modifyownpassword":
					try {
						enviar(symetricEncrypt(acceso.modifyOwnUserPassword(pregunta, loginId)));
						Consola.info(hilo.getName() + " -> Modificaci�n Contrase�a propia");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Modificaci�n Contrase�a propia -> "+ e.getMessage());
					}
					break;
					
				case "modifypassword":
					try {
						enviar(symetricEncrypt(acceso.modifyUserPassword(pregunta)));
						Consola.info(hilo.getName() + " -> Modificaci�n Contrase�a");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Modificaci�n Contrase�a -> "+ e.getMessage());
					}
					break;
					
				case "modifyownuser":
					try {
						enviar(symetricEncrypt(acceso.modifyOwnUser(pregunta, userId)));
						Consola.info(hilo.getName() + " -> Modificaci�n Usuario");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Modificaci�n Usuario -> "+ e.getMessage());
					}
					break;
					
				case "modifyuser":
					try {
						enviar(symetricEncrypt(acceso.modifyUser(pregunta)));
						Consola.info(hilo.getName() + " -> Modificaci�n Usuario por admin");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Modificaci�n Usuario por admin -> "+ e.getMessage());
					}
					break;
					
				case "modifyevent":
					try {
						enviar(symetricEncrypt(acceso.modifyEvent(pregunta)));
						Consola.info(hilo.getName() + " -> Modificaci�n Evento");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Modificaci�n Evento -> "+ e.getMessage());
					}
					break;
					
				case "modifytask":
					try {
						enviar(symetricEncrypt(acceso.modifyTask(pregunta)));
						Consola.info(hilo.getName() + " -> Modificaci�n Tarea");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Modificaci�n Tarea -> "+ e.getMessage());
					}
					break;
					
				case "modifyticket":
					try {
						enviar(symetricEncrypt(acceso.modifyTicket(pregunta)));
						Consola.info(hilo.getName() + " -> Modificaci�n Ticket");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Modificaci�n Ticket -> "+ e.getMessage());
					}
					break;
					
				case "solveticket":
					try {
						enviar(symetricEncrypt(acceso.solveTicket(pregunta)));
						Consola.info(hilo.getName() + " -> Soluci�n de Ticket");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Soluci�n de Ticket -> "+ e.getMessage());
					}
					break;
					
				case "modifyelement":
					try {
						enviar(symetricEncrypt(acceso.modifyElement(pregunta)));
						Consola.info(hilo.getName() + " -> Modificaci�n Elemento");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Modificaci�n Elemento -> "+ e.getMessage());
					}
					break;
					
				case "modifyhardware":
					try {
						enviar(symetricEncrypt(acceso.modifyHardware(pregunta)));
						Consola.info(hilo.getName() + " -> Modificaci�n Hardware");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Modificaci�n Hardware -> "+ e.getMessage());
					}
					break;
					
				case "modifysoftware":
					try {
						enviar(symetricEncrypt(acceso.modifySoftware(pregunta)));
						Consola.info(hilo.getName() + " -> Modificaci�n Software");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Modificaci�n Software -> "+ e.getMessage());
					}
					break;
					
				// Borrado
					
				case "deleteelementassigned":
					try {
						enviar(symetricEncrypt(acceso.deleteAssignedElement(pregunta)));
						Consola.info(hilo.getName() + " -> Eliminaci�n asignaci�n Elemento");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Eliminaci�n asignaci�n Elemento -> "+ e.getMessage());
					}
					break;
					
				case "deletetechassigned":
					try {
						enviar(symetricEncrypt(acceso.deleteAssignedTech(pregunta)));
						Consola.info(hilo.getName() + " -> Eliminaci�n asignaci�n T�cnico");
					} catch (SQLException e) {
						enviar(symetricEncrypt(JsonTreatment.sendSQLErrorCode(e.getMessage())));
						Consola.error(hilo.getName() + " -> Eliminaci�n asignaci�n T�cnico -> "+ e.getMessage());
					}
					break;
					
				// Runtime Options
					
					// Desconexi�n del cliente
				case "exit":
					running = false;
					break;
					
					// Apaga el servidor
				case "poweroff":
					running = false;
					Consola.error(hilo.getName() + " ha iniciado el apagado del Servidor!".toUpperCase());
					Principal.powerOff();
					break;
					
					// Si el comando no existe
				default:
					enviar(symetricEncrypt(acceso.help()));
					Consola.info(hilo.getName() + " -> Comando ayuda");
					break;
				}
				
			}
			
			// Proceso de cierre normal
			entrada.close();
    		salida.close();
    		clientSocket.close();
    		acceso.closeConnection();
    		Principal.getHilos().remove(clientAddress);
			
		} catch (JSONException 
				| IOException 
				| InvalidKeyException 
				| InvalidAlgorithmParameterException 
				| IllegalBlockSizeException 
				| BadPaddingException 
				| ClassNotFoundException 
				| SQLException e) {
			
			this.e = e.getMessage();
			
		} finally {
			
			if (e == null) {
				// Si no ha habido fallos, la "e" apuntar� a null
				Consola.event(hilo.getName() + " desconectado.");
			} else {
				// Cuando el cliente tiene un error queda con running a verdadero
				if (running) {
					Consola.error(hilo.getName() + " se desconect� con el error \"" + e + "\"");
					// Intenta cerrar procesos abiertos despu�s del fallo
					killThread();
				} else {
					// Si el cliente es desconectado por el servidor, marca el running a falso
					// y mostrar�a este mensaje
					Consola.event(hilo.getName() + " ha sido desconectado.");
				}
			}
		}
	}
	
	/******************************************************************************/
	
	// Inicializa la clave sim�trica
	private void inicializacionClaveSimetrica() {
		claveSimetrica = new byte[16];
		EncryptModule.getSecureRandom().nextBytes(claveSimetrica);
		claveSimetricaSecreta = new SecretKeySpec(claveSimetrica, "AES");
	}
	
	/******************************************************************************/
	
	// Intercambia la clave p�blica con el cliente y obtiene la del mismo
	private void intercambioClaves() throws 
    			InvalidKeySpecException, 
    			NoSuchAlgorithmException, 
    			IOException, 
    			ClassNotFoundException {
		
		// Lee el m�dulo de la clave p�blica enviado por el cliente
		BigInteger moduloPublico = (BigInteger) entrada.readObject();
		// Lee el exponente de la clave p�blica enviado por el cliente
		BigInteger exponentePublico = (BigInteger) entrada.readObject();
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		// Genera la clave p�blica del cliente
		clientPublicKey = keyFactory.generatePublic(new RSAPublicKeySpec(moduloPublico,exponentePublico));
		
		// Env�a la clave p�blica del servidor al cliente
		salida.writeObject(EncryptModule.getModulus());
        salida.flush();
        salida.writeObject(EncryptModule.getExponent());
        salida.flush();
	}
	
	/******************************************************************************/
	
	// M�todo para cifrar un texto con la clave p�blica del cliente
    private String asymetricEncrypt(String mensaje) throws 
            	IllegalBlockSizeException, 
            	BadPaddingException, 
            	InvalidKeyException, UnsupportedEncodingException {
        
        cifradorAsimetrico.init(Cipher.ENCRYPT_MODE, clientPublicKey);
        byte[] mensajeCifrado = cifradorAsimetrico.doFinal(mensaje.getBytes());
        return new String(Base64.getEncoder().encode(mensajeCifrado));
    }
    
    /******************************************************************************/
    
    // M�todo para descifrar mensajes provenientes del cliente con la clave privada del servidor
    private String asymetricDecript(String mensajeCifrado64) throws 
	            IllegalBlockSizeException, 
	            BadPaddingException, 
	            InvalidKeyException {
        
        byte[] mensajeCifrado = Base64.getDecoder().decode(mensajeCifrado64);
        cifradorAsimetrico.init(Cipher.DECRYPT_MODE, EncryptModule.getPrivateKey());
        return new String(cifradorAsimetrico.doFinal(mensajeCifrado));
    }
    
    /******************************************************************************/
	
    // M�todo encargado de encriptar un mensaje con la clave sim�trica
    private String symetricEncrypt(JSONObject json) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException { 
    	// Vector de inicializaci�n para encriptaci�n AES
    	byte[] iv = new byte[12];
    	// Es importante por motivos de seguridad nunca reutilizar el mismo vector
    	// en m�s de una petici�n con la misma clave, por eso usamos un randomSecure
    	// en lugar de un random
    	EncryptModule.getSecureRandom().nextBytes(iv);
    	parameterSpec = new GCMParameterSpec(128, iv);
    	cifradorSimetrico.init(Cipher.ENCRYPT_MODE, claveSimetricaSecreta, parameterSpec);
    	
    	// Cifra el mensaje introduciendo tambi�n el vector de inicializaci�n para que el
    	// cliente pueda descifrar el mensaje
    	byte[] cipherText = cifradorSimetrico.doFinal(json.toString().getBytes());
    	ByteBuffer bf = ByteBuffer.allocate(4+iv.length+cipherText.length); 
		// A�ade la longitud del vector
    	bf.putInt(iv.length);
    	// A�ade el vector
		bf.put(iv);
		// A�ade el texto cifrado
		bf.put(cipherText);
		
		byte[] cipherMessage = bf.array();
		return new String(Base64.getEncoder().encode(cipherMessage));
    }
    
    /******************************************************************************/
    
 // M�todo encargado de desencriptar un mensaje con la clave sim�trica
    private String symetricDecript(String mensajeCifrado64) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException { 
    	byte[] cifMen = Base64.getDecoder().decode(mensajeCifrado64);
        ByteBuffer bf = ByteBuffer.wrap(cifMen);
        // Consigue el vector de inicializaci�n del cliente
        int ivLength = bf.getInt();
        // Comprueba la longitud del vector, no deber�a ser nunca superior a 16
        // ni inferior a 12
        if (ivLength < 12 || ivLength >=16) {
        	throw new IllegalArgumentException("invalid iv length");
        }
        byte[] iv = new byte[ivLength];
        bf.get(iv);
        byte[] cipherText = new byte[bf.remaining()];
        bf.get(cipherText);
        
        // Descifra el mensaje con ayuda del vector de inicializaci�n
        parameterSpec = new GCMParameterSpec(128, iv);
        cifradorSimetrico.init(Cipher.DECRYPT_MODE, claveSimetricaSecreta, parameterSpec);
        return new String(cifradorSimetrico.doFinal(cipherText));
    }
    
    /******************************************************************************/
    
    // M�todo para enviar objetos al cliente
    private void enviar(Object mensaje) throws IOException {
        salida.writeObject(mensaje);
        salida.flush();
    }
    
    /******************************************************************************/
    
    // M�todo para enviar respuesta al cliente en el intercambio de clave asim�trica
    private void enviarResponse(int response) throws IOException {
        salida.writeInt(response);
        salida.flush();
    }
    
    /******************************************************************************/
    
    // Detiene el hilo marcando la variable running a falsa
    public void killThread() {
    	
    	running = false;
    	
    	// Intenta cerrar cualquier objeto que haya podido quedar abierto
    	
    	try {
    		salida.close();
    	} catch (Exception e) {}
    	
    	try {
    		entrada.close();
    	} catch (Exception e) {}
		
		try {
			clientSocket.close();
		} catch (Exception e) {}
		
		// Quita la conexi�n del mapa de conexiones
		Principal.getHilos().remove(user);
    }
    
    /******************************************************************************/
    
    public Thread getHilo() {
    	return hilo;
    }
	
}
