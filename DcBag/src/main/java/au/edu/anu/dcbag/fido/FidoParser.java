package au.edu.anu.dcbag.fido;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FidoParser
{
	private static final Logger LOGGER = LoggerFactory.getLogger(Thread.currentThread().getClass());
	private static PythonExecutor pyExec;
	
	private final String output;
	private final PronomFormat fileFormat;
	
	public FidoParser(File fileToId) throws IOException, URISyntaxException
	{
		pyExec = new PythonExecutor(new File(FidoParser.class.getResource("fido.py").toURI()));
		pyExec.execute("\"" + fileToId.getCanonicalPath() + "\"");
		output = pyExec.getOutputAsString();
		fileFormat = new PronomFormat(this.output);
		LOGGER.debug("Fido returned {}", output);
	}
	
	public String getOutput()
	{
		return this.output;
	}
	
	public PronomFormat getFileFormat()
	{
		return fileFormat;
	}
}
