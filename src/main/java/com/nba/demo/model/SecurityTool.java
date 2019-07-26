package com.nba.demo.model;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class SecurityTool {
	public static Decoder decoder=null;
	public static Encoder encoder=null;
	public static Cipher rsaDecodeCipher=null;
	public static String publicKey=null;

	static{
		try {
			KeyPairGenerator generator=KeyPairGenerator.getInstance("RSA");
			generator.initialize(1024);
			encoder=Base64.getEncoder();
			decoder=Base64.getDecoder();
			KeyPair keyPair = generator.generateKeyPair();
			RSAPublicKey publicKey = (RSAPublicKey)keyPair.getPublic();
			RSAPrivateKey privateKey= (RSAPrivateKey)keyPair.getPrivate();
			SecurityTool.publicKey=encoder.encodeToString(publicKey.getEncoded());
			rsaDecodeCipher=Cipher.getInstance("RSA");
			rsaDecodeCipher.init(Cipher.DECRYPT_MODE, privateKey);
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
	}

	/*
	 * RSA私钥解码
	 * */
	public static String rsaDecode(String src) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException{
		return new String(rsaDecodeCipher.doFinal(decoder.decode(src.getBytes("utf-8"))));
	}
	public static String getPublicKey(){
		return publicKey;
	}
	public static String aesDecode(String src,String key) throws IllegalBlockSizeException, BadPaddingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, UnsupportedEncodingException{
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"));
		return new String(cipher.doFinal(decoder.decode(src.getBytes("utf-8"))));
	}
	public static String aesEncode(String src,String key) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException{
		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"));
		return new String(encoder.encode(cipher.doFinal(src.getBytes("utf-8"))));
	}

}
