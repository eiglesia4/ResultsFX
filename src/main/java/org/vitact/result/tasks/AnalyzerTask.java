package org.vitact.result.tasks;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import javafx.concurrent.Task;
import org.vitact.result.beans.*;
import org.vitact.result.exceptions.AnalyzerException;
import org.vitact.result.services.FileService;
import org.vitact.result.types.EventTypeEnum;

public class AnalyzerTask extends Task<String> {
	static int TOTAL_STEPS = 4;
	File inputFile, outputFolder;
	int correctMark, reactionMark;
	ArrayList<EventBean> eventBeanArrayList = new ArrayList<>();
	// Results
	int correctAnswers;
	int errorAnswers=0;
	int errorByWrongClick=0;
	int errorByNonAnswer=0;
	ArrayList<Long> correctTimes = new ArrayList<>();
	double meanCorrectTimes=0;
	ArrayList<Long> errorByWrongClickTimes = new ArrayList<>();
	double meanErrorByWrongClickTimes=0;

	@Override
	protected String call() throws Exception {
		// Step 1 => File Upload
		progress(fileLoad(), 1);
		Thread.sleep(2000);
		// Step 2 => Analysis
		progress(analysis(), 2);
		Thread.sleep(2000);
		// Step 3 => Calculate Results
		progress(calculateResults(), 3);
		Thread.sleep(2000);
		// Step 4 => File Generation
		progress(fileSave(), 4);
		Thread.sleep(2000);
		return "OK";
	}

	private void progress(String message, int step) {
		updateMessage(message);
		updateProgress(step, TOTAL_STEPS);
	}

	private String fileLoad() {
		try {
			Scanner scanner = new Scanner(inputFile);
			// Ignore header
			scanner.nextLine();
			while (scanner.hasNextLine()) {
				eventBeanArrayList.add(EventBean.getEvent(scanner.nextLine()));
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			String message = "Input file not found " + inputFile.getAbsolutePath();
			throw  new AnalyzerException(AnalyzerException.DATA_FILE_NOT_FOUND, message, e);
		}
		return "Fichero de eventos cargado con éxito";
	}

	private String analysis() {

		for(int i=0; i<eventBeanArrayList.size(); i++) {
			// Get the line and set the value for correctness
			EventBean eventBean = eventBeanArrayList.get(i);
			eventBean.setType(EventTypeEnum.INCORRECT_STIMULUS);
			if(eventBean.getMarkInt() == correctMark)
				eventBean.setType(EventTypeEnum.CORRECT_STIMULUS);
			if(eventBean.getMarkInt() == reactionMark)
				eventBean.setType(EventTypeEnum.RESPONSE);

			// if incorrect stimulus (IS) => Check if previous line exists and is correct stimulus (CS) => annotate the non answared
			// if response (RS) => Check if previous is CS => annotate correct => otherwise annotate error
			switch (eventBean.getType()) {
				case CORRECT_STIMULUS:{
					eventBean.setCorrect(false); // not answered by default until a response arrives
					break;
				}
				case INCORRECT_STIMULUS:{
					eventBean.setCorrect(true); // not answered by default is correct
					break;
				}
				case RESPONSE:{
					if(i>0 && eventBeanArrayList.get(i-1).getType()==EventTypeEnum.CORRECT_STIMULUS) {
						eventBeanArrayList.get(i - 1).setCorrect(true); // correctly answered
						eventBeanArrayList.get(i - 1).setResponseTime(eventBean.getEpochTime()-eventBeanArrayList.get(i-1).getEpochTime());
					}
					else if(i>0 && eventBeanArrayList.get(i-1).getType()==EventTypeEnum.INCORRECT_STIMULUS) {
						eventBeanArrayList.get(i - 1).setCorrect(false); // incorrectly answered
						eventBeanArrayList.get(i - 1).setResponseTime(
								eventBean.getEpochTime() - eventBeanArrayList.get(i - 1).getEpochTime());
					}
					else if(i>0 && eventBeanArrayList.get(i-1).getType()==EventTypeEnum.RESPONSE)
						break; // ignore double click
					break;
				}
			}
		}
		return "Eventos analizados con éxito";
	}

	private String calculateResults() {
		long totalTimeCorrect = 0;
		long totalTimeError = 0;
		for(int i=0; i<eventBeanArrayList.size(); i++) {
			EventBean eventBean = eventBeanArrayList.get(i);
			switch (eventBean.getType()) {
				case CORRECT_STIMULUS: {
					if(eventBean.isCorrect()) {
						correctAnswers++;
						correctTimes.add(eventBean.getResponseTime());
						totalTimeCorrect+=eventBean.getResponseTime();
					} else {
						errorAnswers++;
						errorByNonAnswer++;
						if(i<eventBeanArrayList.size()-1) {
							EventBean nextEventBean = eventBeanArrayList.get(i+1);
							// Next one is non correct stimulus but I clicked, so this error must be ignored
							if(nextEventBean.getType()== EventTypeEnum.INCORRECT_STIMULUS && !nextEventBean.isCorrect()) {
								errorAnswers--;
								eventBean.setIgnoredFail(true);
							}
						}
					}
					break;
				}
				case INCORRECT_STIMULUS: {
					if(!eventBean.isCorrect()) {
						errorAnswers++;
						errorByWrongClick++;
						errorByWrongClickTimes.add(eventBean.getResponseTime());
						totalTimeError+=eventBean.getResponseTime();
					}
					break;
				}
				case RESPONSE: {
					break;
				}
			}
		}
		meanCorrectTimes = (double) totalTimeCorrect / correctTimes.size();
		meanErrorByWrongClickTimes = (double) totalTimeError / errorByWrongClickTimes.size();
		return "Resultados obtenidos";
	}

	private String fileSave() {
		String baseName = inputFile.getName();
		baseName = baseName.substring(0,baseName.lastIndexOf("."));
		FileService fileService = new FileService(outputFolder);
		// First save log of actions
		EventsFileBean eventsFileBean = new EventsFileBean();
		eventsFileBean.setName(baseName + "_analisis");
		eventsFileBean.setExtension("csv");
		eventsFileBean.setHeader(EventBean.headerOfCSV());
		eventsFileBean.setLines(eventBeanArrayList.stream().filter(eventBean -> eventBean.getType() != EventTypeEnum.RESPONSE).map(eventBean -> {
			return eventBean.toCSV();
		}).collect(Collectors.toList()));
		fileService.saveFile(eventsFileBean);
		// Then save the single file with resumen
		EventsFileBean resumeFileBean = new EventsFileBean();
		resumeFileBean.setName(baseName + "_resumen");
		resumeFileBean.setExtension("txt");
		ArrayList<String> summaryContentList = new ArrayList<>();
		summaryContentList.add("[SUMMARY]");
		summaryContentList.add("Respuestas Correctas="+correctAnswers);
		summaryContentList.add("Respuestas Erróneas="+errorAnswers);
		summaryContentList.add("Respuestas Erróneas por click erróneo="+errorByWrongClick);
		summaryContentList.add("Respuestas Erróneas por click no respuesta="+errorByNonAnswer);
		summaryContentList.add("# Se han ignorado las no respuestas cuando se ha pulsado posteriormente de manera errónea (por ello pueden no coincidir la suma de errores");
		summaryContentList.add("[MEANS]");
		summaryContentList.add("Media de respuesta en aciertos="+meanCorrectTimes);
		summaryContentList.add("Media de respuesta en errores (clicks erróneos)="+meanErrorByWrongClickTimes);
		resumeFileBean.setLines(summaryContentList);
		fileService.saveFile(resumeFileBean);

		return "Ficheros de resultado salvados en la carpeta de salida";
	}



	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public void setOutputFolder(File outputFolder) {
		this.outputFolder = outputFolder;
	}

	public void setCorrectMark(int correctMark) {
		this.correctMark = correctMark;
	}

	public void setReactionMark(int reactionMark) {
		this.reactionMark = reactionMark;
	}
}
