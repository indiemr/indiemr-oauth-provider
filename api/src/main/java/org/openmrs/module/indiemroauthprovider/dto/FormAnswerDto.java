package org.openmrs.module.indiemroauthprovider.dto;

import java.util.List;

/**
 * One raw question/answer pair. The question TEXT is snapshotted at fetch time so a later edit to
 * the clinic's form cannot rewrite what the doctor sees under an old submission.
 */
public class FormAnswerDto {
	
	private String questionId;
	
	private String question;
	
	private List<String> values;
	
	public FormAnswerDto() {
	}
	
	public FormAnswerDto(String questionId, String question, List<String> values) {
		this.questionId = questionId;
		this.question = question;
		this.values = values;
	}
	
	public String getQuestionId() {
		return questionId;
	}
	
	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}
	
	public String getQuestion() {
		return question;
	}
	
	public void setQuestion(String question) {
		this.question = question;
	}
	
	public List<String> getValues() {
		return values;
	}
	
	public void setValues(List<String> values) {
		this.values = values;
	}
}
