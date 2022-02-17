package ca.bc.gov.nrs.wfdm.wfdm_clamav_scan_handler;

import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.adobe.xmp.impl.Utils;

public class PropertyLoader {

	static final Logger logger = LogManager.getLogger(PropertyLoader.class);

	public static String getProperty(String key) {
		ClassLoader classLoader = Utils.class.getClassLoader();

		try {
			InputStream is = classLoader.getResourceAsStream("wfdm-file-index-config.properties");
			Properties prop = new Properties();
			prop.load(is);
			return prop.getProperty(key);
		} catch (Exception e) {
			return null;
		}
	}
}