package cz.teaculture.util;

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
}
