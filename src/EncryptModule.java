import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.RSAPublicKeySpec;

public class EncryptModule {
	
	/*
	 * Esta clase se encarga de generar las claves de cifrado asim�trico y sim�trico.
	 */
	
	// Referencia de las claves generadas
	private static PublicKey publicKey;
	private static PrivateKey privateKey;
	private static RSAPublicKeySpec publicKeySpec;
	private static SecureRandom secureRandom;
	
	// Inicializa las claves asim�tricas
	public static void initializeKey() throws Exception {
		
		// Claves de tipo RSA
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		// 1024 bits
		keyPairGenerator.initialize(1024);
		System.out.println("Generador de claves inicializado...");
		
		// Guarda las claves privada y p�blica
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		System.out.println("Claves generadas");
		
		// Separa ambas claves en objetos diferentes
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
		
		// Genera la clave p�blica exportable
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		publicKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
		System.out.println("Clave p�blica exportable generada");
		System.out.println();
		System.out.println();
		
		// Genera un objeto capaz de generar n�meros aleatorios
		secureRandom = new SecureRandom();
		
	}
	
	// Devuelve la clave p�blica
	public static PublicKey getPublicKey() {
		return publicKey;
	}
	
	// Devuelve la clave privada
	public static PrivateKey getPrivateKey() {
		return privateKey;
	}
	
	// La clave p�blica se divide en dos objetos BigInteger, el exponente y el m�dulo
	
	// Devuelve el exponente de la clave p�blica
	public static BigInteger getExponent() {
		return publicKeySpec.getPublicExponent();
	}
	
	// Devuelve el m�dulo de la clave p�blica
	public static BigInteger getModulus() {
		return publicKeySpec.getModulus();
	}

	public static SecureRandom getSecureRandom() {
		return secureRandom;
	}
	
	
	
}
