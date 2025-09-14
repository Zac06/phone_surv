package it.zac06;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class MD5Computer {
	private MessageDigest md;
	private HexFormat hf;
	
	public MD5Computer() {
		try {
			this.md=MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.out.println("dafaq??");
		}
		
		this.hf=HexFormat.of().withLowerCase();
	}
	
	public String compute(String str) {
		md.reset();
		
		md.update(str.getBytes());
		byte[] digest=md.digest();
		
		return hf.formatHex(digest);
	}
	
	public String compute(byte[] content) {
		md.reset();
		
		md.update(content);
		byte[] digest=md.digest();
		
		return hf.formatHex(digest);
	}
}
