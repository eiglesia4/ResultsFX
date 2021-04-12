package org.vitact.result.types;

public enum EventTypeEnum {
	CORRECT_STIMULUS("CORRECT_STIMULUS"),
	INCORRECT_STIMULUS("INCORRECT_STIMULUS"),
	RESPONSE("RESPONSE"),
	UNKNOWN_TYPE("UNKNOWN_TYPE");

	String description;

	EventTypeEnum(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public static EventTypeEnum getEventTypeFromDescription(String description) {
		for (EventTypeEnum myEvent: EventTypeEnum.values()) {
			if(myEvent.equals(description))
				return myEvent;
		}
		return null;
	}
}
