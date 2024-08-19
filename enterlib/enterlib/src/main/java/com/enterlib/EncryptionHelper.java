package com.enterlib;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

public class EncryptionHelper {
	private static final String ALGORITHM = "AES";

	private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
	
	private static final String TAG = EncryptionHelper.class.getSimpleName();
	
	public static byte[] generateKey(byte[] randomNumberSeed) {
		SecretKey sKey = null;
		try{
			KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			random.setSeed(randomNumberSeed);
			keyGen.init(256,random);
			sKey = keyGen.generateKey();
		} catch(NoSuchAlgorithmException e) {
			Log.e(TAG,"No such algorithm exception");
		}
		return sKey.getEncoded();
	}
	
	public static byte[] generateKey(String stringKey) {
		SecretKeySpec sks;
		sks = new  SecretKeySpec(stringKey.getBytes(),ALGORITHM);
		return sks.getEncoded();
	}
	
	public static byte[] encrypt(byte[]key, byte[]data){
		Cipher cipher;
		SecretKeySpec sKeySpec = new SecretKeySpec(key,ALGORITHM);
		byte[]encrypted = null;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, sKeySpec);
			encrypted = cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "No such algorithm exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (NoSuchPaddingException e) {
			Log.e(TAG, "No such padding exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			Log.e(TAG, "Invalid key exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (IllegalBlockSizeException e) {
			Log.e(TAG, "Illegal block size exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (BadPaddingException e) {
			Log.e(TAG, "Bad padding exception");
			throw new RuntimeException(e.getMessage(), e);
		}
		return encrypted;
	}
	
	public static byte[] encrypt(byte[]key, byte[]iv, byte[]data){
		Cipher cipher;
		SecretKeySpec sKeySpec = new SecretKeySpec(key,ALGORITHM);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		
		byte[]encrypted = null;
		try {
			cipher = Cipher.getInstance(TRANSFORMATION);			
			cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, ivSpec);
			encrypted = cipher.doFinal(data);
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "No such algorithm exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (NoSuchPaddingException e) {
			Log.e(TAG, "No such padding exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			Log.e(TAG, "Invalid key exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (IllegalBlockSizeException e) {
			Log.e(TAG, "Illegal block size exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (BadPaddingException e) {
			Log.e(TAG, "Bad padding exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			Log.e(TAG, "Invalid algoritm parameter exception");
			throw new RuntimeException(e.getMessage(), e);
		}
		return encrypted;
	}
	
	public static byte[] decrypt(byte[]key, byte[]iv, byte[]encrypted){
		Cipher cipher;
		SecretKeySpec sKeySpec = new SecretKeySpec(key,ALGORITHM);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		byte[]data = null;
		try {
			cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, sKeySpec, ivSpec);
			encrypted = cipher.doFinal(data);
			
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "No such algorithm exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (NoSuchPaddingException e) {
			Log.e(TAG, "No such padding exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			Log.e(TAG, "Invalid key exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (IllegalBlockSizeException e) {
			Log.e(TAG, "Illegal block size exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (BadPaddingException e) {
			Log.e(TAG, "Bad padding exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			Log.e(TAG, "Invalid algoritm parameter exception");
			throw new RuntimeException(e.getMessage(), e);
		}
		return data;
	}
	
	public static byte[] decrypt(byte[]key, byte[]encrypted){
		Cipher cipher;
		SecretKeySpec sKeySpec = new SecretKeySpec(key,ALGORITHM);
		byte[]data = null;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, sKeySpec);
			encrypted = cipher.doFinal(data);
			
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG, "No such algorithm exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (NoSuchPaddingException e) {
			Log.e(TAG, "No such padding exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			Log.e(TAG, "Invalid key exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (IllegalBlockSizeException e) {
			Log.e(TAG, "Illegal block size exception");
			throw new RuntimeException(e.getMessage(), e);
		} catch (BadPaddingException e) {
			Log.e(TAG, "Bad padding exception");
			throw new RuntimeException(e.getMessage(), e);
		}
		return data;
	}
	
	public static String armorEncrypt(byte[]key, byte[]data) {
		return Base64.encodeToString(encrypt(key, data), Base64.DEFAULT);
	}
	
	public static String armorDecrypt(byte[]key, String data) {
		return new String(decrypt(key, Base64.decode(data, Base64.DEFAULT)));
	}
	
	public static String armorEncrypt(byte[]key, byte[]iv, byte[]data) {
		return Base64.encodeToString(encrypt(key, iv, data), Base64.DEFAULT);
	}
	
	public static String armorDecrypt(byte[]key, byte[]iv, String data) {
		return new String(decrypt(key, iv, Base64.decode(data, Base64.DEFAULT)));
	}
}
