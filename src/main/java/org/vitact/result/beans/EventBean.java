package org.vitact.result.beans;

import org.apache.logging.log4j.*;
import org.vitact.result.exceptions.AnalyzerException;
import org.vitact.result.types.EventTypeEnum;

public class EventBean {
	public static Logger logger = LogManager.getRootLogger();

	// Intrinsic
	int id;
	int markInt;
	String markStr;
	long epochTime;

	// From analysis
	EventTypeEnum type;
	boolean correct;
	boolean ignoredFail;
	long responseTime; // only for correct stimuli answered correctly

	public EventBean() {
	}

	public EventBean(int id, int markInt, String markStr, long epochTime) {
		this.id = id;
		this.markInt = markInt;
		this.markStr = markStr;
		this.epochTime = epochTime;
	}

	public static EventBean getEvent(String composedLine) throws AnalyzerException {
		String data[] = composedLine.split(";");
		try {
			Integer.parseInt(data[0]);
			Integer.parseInt(data[1]);
			Long.parseLong(data[3]);
		} catch (NumberFormatException e) {
			String message = "Error parsing line: " + composedLine;
			logger.error(message);
			throw new AnalyzerException(AnalyzerException.FILE_LINE_BAD_FORMAT, message, e);
		}
		return new EventBean(Integer.parseInt(data[0]), Integer.parseInt(data[1]), data[2], Long.parseLong(data[3]));
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getMarkInt() {
		return markInt;
	}

	public void setMarkInt(int markInt) {
		this.markInt = markInt;
	}

	public String getMarkStr() {
		return markStr;
	}

	public void setMarkStr(String markStr) {
		this.markStr = markStr;
	}

	public long getEpochTime() {
		return epochTime;
	}

	public void setEpochTime(long epochTime) {
		this.epochTime = epochTime;
	}

	public EventTypeEnum getType() {
		return type;
	}

	public void setType(EventTypeEnum type) {
		this.type = type;
	}

	public boolean isCorrect() {
		return correct;
	}

	public void setCorrect(boolean correct) {
		this.correct = correct;
	}

	public boolean isIgnoredFail() {
		return ignoredFail;
	}

	public void setIgnoredFail(boolean ignoredFail) {
		this.ignoredFail = ignoredFail;
	}

	public long getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(long responseTime) {
		this.responseTime = responseTime;
	}

	@Override
	public String toString() {
		return "EventBean{" + "id=" + id + ", markInt=" + markInt + ", markStr='" + markStr + '\''
				+ ", epochTime=" + epochTime + ", type=" + type + ", correct=" + correct
				+ ", ignoredFail=" + ignoredFail + ", responseTime=" + responseTime + '}';
	}

	public String toCSV() {
		StringBuffer sb = new StringBuffer();
		sb.append(id); sb.append(";");
		sb.append(markInt); sb.append(";");
		sb.append(markStr); sb.append(";");
		sb.append(epochTime); sb.append(";");
		sb.append(type); sb.append(";");
		sb.append(correct); sb.append(";");
		sb.append(ignoredFail); sb.append(";");
		sb.append(responseTime);
		return sb.toString();
	}

	public static String headerOfCSV() {
		StringBuffer sb = new StringBuffer();
		sb.append("id"); sb.append(";");
		sb.append("markInt"); sb.append(";");
		sb.append("markStr"); sb.append(";");
		sb.append("epochTime"); sb.append(";");
		sb.append("type"); sb.append(";");
		sb.append("correct"); sb.append(";");
		sb.append("ignoredFail"); sb.append(";");
		sb.append("responseTime");
		return sb.toString();
	}
}
