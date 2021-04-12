package org.vitact.result.services;

import java.io.*;
import java.util.List;
import org.apache.logging.log4j.*;
import org.vitact.result.beans.EventsFileBean;
import org.vitact.result.exceptions.AnalyzerException;

public class FileService {
	public Logger logger = LogManager.getRootLogger();
	File outputFolder;

	public FileService(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	public boolean saveFile(EventsFileBean fileBean) throws AnalyzerException {
		String fileName = fileBean.getName() + "." + fileBean.getExtension();
		File outputFile = new File(outputFolder, fileName);
		;
		try(FileWriter fileWriter = new FileWriter(outputFile);
			PrintWriter printWriter = new PrintWriter(fileWriter)) {
			if (fileBean.getHeader() != null)
				printWriter.println(fileBean.getHeader());
			for (String line : fileBean.getLines()) {
				printWriter.println(line);
			}
			return true;
		}
		catch (IOException e) {
			String message = "Cannot write output file " + outputFile.getAbsolutePath();
			throw new AnalyzerException(AnalyzerException.OUTPUT_FILE_WRITE_ERROR, message, e);
		}
	}
}
