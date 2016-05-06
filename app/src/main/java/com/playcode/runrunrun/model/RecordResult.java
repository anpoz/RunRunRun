package com.playcode.runrunrun.model;

import java.util.List;


public class RecordResult {
	private String resultCode;
	private String message;
	private List<RecordsEntity> records;
	
	public RecordResult() {
		super();
	}

	public RecordResult(String resultCode, String message, List<RecordsEntity> records) {
		super();
		this.resultCode = resultCode;
		this.message = message;
		this.records = records;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<RecordsEntity> getRecords() {
		return records;
	}

	public void setRecords(List<RecordsEntity> records) {
		this.records = records;
	}
	
	
}
