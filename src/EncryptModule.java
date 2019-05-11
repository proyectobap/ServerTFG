import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.RSAPublicKeySpec;

public class EncryptModule {
	
	private static PublicKey publicKey;
	private static PrivateKey privateKey;
	private static RSAPublicKeySpec publicKeySpec;
	private static SecureRandom secureRandom;
	
	public static void initializeKey() throws Exception {
		
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(1024);
		System.out.println("Generador de claves inicializado...");
		
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		System.out.println("Claves generadas");
		
		publicKey = keyPair.getPublic();
		privateKey = keyPair.getPrivate();
		
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		publicKeySpec = keyFactory.getKeySpec(publicKey, RSAPublicKeySpec.class);
		System.out.println("Clave pública exportable generada");
		System.out.println();
		System.out.println();
		
		 secureRandom = new SecureRandom();
		
	}
	
	public static PublicKey getPublicKey() {
		return publicKey;
	}
	
	public static PrivateKey getPrivateKey() {
		return privateKey;
	}
	
	public static BigInteger getExponent() {
		return publicKeySpec.getPublicExponent();
	}
	
	public static BigInteger getModulus() {
		return publicKeySpec.getModulus();
	}

	public static SecureRandom getSecureRandom() {
		return secureRandom;
	}
	
	
	
}
