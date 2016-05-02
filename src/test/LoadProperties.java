package test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.h2.tools.DeleteDbFiles;

public class LoadProperties {
	
	public Properties load() throws URISyntaxException, IOException {
		return load("config.properties");
	}
	
	public Properties load(String file) throws URISyntaxException, IOException {
		String path = this.getClass().getClassLoader().getResource(file).toString();
		
		URI uri = new URI(path).resolve(".");
		String dir = uri.toString();
		dir = dir.substring(5, dir.length() - 1);
		
		DeleteDbFiles.execute(dir, "proxyDatabase1", true);
		DeleteDbFiles.execute(dir, "proxyDatabase2", true);
		DeleteDbFiles.execute(dir, "proxyDatabase3", true);
		
		byte [] propBytes = Files.readAllBytes(Paths.get(new URI(path)));
		
		String info = new String(propBytes).replaceAll("~", dir);
		
		InputStream is = new ByteArrayInputStream(info.getBytes());
		Properties p = new Properties();
		p.load(is);
		
		return p;
	}
}
