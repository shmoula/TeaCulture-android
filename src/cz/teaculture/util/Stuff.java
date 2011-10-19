package cz.teaculture.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.util.Log;

/**
 * Trida s pomocnymi rutinami
 * @author vbalak
 *
 */
public class Stuff {
	public static void logException(Exception e, String component) {
		Log.e(component, e.getMessage(), e);
		Writer result = new StringWriter();
		e.printStackTrace(new PrintWriter(result));
	}
	
	/**
	 * Vytvori z objektu byteArray
	 * (pro prasacke ukladani do databaze)
	 * @param o
	 * @return
	 * @throws IOException
	 */
	public static byte[] getObjectAsByteArray(Object o) throws IOException{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
    	ObjectOutput out = new ObjectOutputStream(bos);   
    	out.writeObject(o);
    	
    	byte[] bArray = bos.toByteArray();
    	
    	out.close();
    	bos.close();
    	
    	return bArray;
	}
	
	/**
	 * Pokusi se zrekonstruovat objekt z byteArray
	 * @param <T>
	 * @param bArray
	 * @param t
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public static <T>T setObjectFromByteArray(byte[] bArray, Class <T> t) throws IOException {
		T result = null;
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bArray);
		ObjectInput in = new ObjectInputStream(bis);
		Object o = null;
		
		try {
			o = in.readObject();
		} catch (ClassNotFoundException e) {
			// nic, proste to vrati null B-)
		}
		
		//if(t.isInstance(o)){
			result = (T) o;

		bis.close();
		in.close();
		
		return result;
	}
	
}
