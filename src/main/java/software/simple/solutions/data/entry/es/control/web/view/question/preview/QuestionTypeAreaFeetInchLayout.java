package software.simple.solutions.data.entry.es.control.web.view.question.preview;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import software.simple.solutions.data.entry.es.control.entities.SurveyQuestion;
import software.simple.solutions.data.entry.es.control.entities.SurveyResponse;
import software.simple.solutions.data.entry.es.control.entities.SurveyResponseAnswer;
import software.simple.solutions.data.entry.es.control.service.ISurveyResponseAnswerService;
import software.simple.solutions.data.entry.es.control.valueobjects.SurveyResponseAnswerVO;
import software.simple.solutions.framework.core.components.CDecimalField;
import software.simple.solutions.framework.core.components.SessionHolder;
import software.simple.solutions.framework.core.exceptions.FrameworkException;
import software.simple.solutions.framework.core.util.ContextProvider;
import software.simple.solutions.framework.core.util.NumberUtil;

public class QuestionTypeAreaFeetInchLayout extends VerticalLayout {

	private CDecimalField lenghtFeetFld;
	private CDecimalField lenghtInchFld;
	private CDecimalField widthFeetFld;
	private CDecimalField widthInchFld;
	private HorizontalLayout horizontalLayout;

	private SurveyQuestion surveyQuestion;
	private SurveyResponse surveyResponse;
	private SessionHolder sessionHolder;
	private boolean editable = false;

	public QuestionTypeAreaFeetInchLayout(SessionHolder sessionHolder, SurveyQuestion surveyQuestion,
			SurveyResponse surveyResponse) {
		this.sessionHolder = sessionHolder;
		this.surveyQuestion = surveyQuestion;
		this.surveyResponse = surveyResponse;
		buildMainLayout();

		updateFields();
	}

	private void buildMainLayout() {
		horizontalLayout = new HorizontalLayout();
		horizontalLayout.setMargin(false);
		horizontalLayout.setSpacing(true);
		addComponent(horizontalLayout);

		lenghtFeetFld = new CDecimalField(sessionHolder);
		lenghtFeetFld.setValueChangeMode(ValueChangeMode.BLUR);
		lenghtFeetFld.setWidth("100px");
		horizontalLayout.addComponent(lenghtFeetFld);

		horizontalLayout.addComponent(new Label("feet"));

		lenghtInchFld = new CDecimalField(sessionHolder);
		lenghtInchFld.setValueChangeMode(ValueChangeMode.BLUR);
		lenghtInchFld.setWidth("100px");
		horizontalLayout.addComponent(lenghtInchFld);

		horizontalLayout.addComponent(new Label("inch by"));

		widthFeetFld = new CDecimalField(sessionHolder);
		widthFeetFld.setValueChangeMode(ValueChangeMode.BLUR);
		widthFeetFld.setWidth("100px");
		horizontalLayout.addComponent(widthFeetFld);

		horizontalLayout.addComponent(new Label("feet"));

		widthInchFld = new CDecimalField(sessionHolder);
		widthInchFld.setValueChangeMode(ValueChangeMode.BLUR);
		widthInchFld.setWidth("100px");
		horizontalLayout.addComponent(widthInchFld);

		horizontalLayout.addComponent(new Label("inch"));

		if (surveyResponse != null) {
			ISurveyResponseAnswerService surveyResponseAnswerService = ContextProvider
					.getBean(ISurveyResponseAnswerService.class);
			try {
				SurveyResponseAnswer surveyResponseAnswer = surveyResponseAnswerService
						.getSurveyResponseAnswer(surveyResponse.getId(), surveyQuestion.getId());
				if (surveyResponseAnswer != null && StringUtils.isNotBlank(surveyResponseAnswer.getResponseText())) {
					String responseText = surveyResponseAnswer.getResponseText();
					String[] split = responseText.split(":");
					List<String> list = Arrays.asList(split);
					for (int i = 0; i < list.size(); i++) {
						switch (i) {
						case 0:
							lenghtFeetFld.setBigDecimalValue(NumberUtil.getBigDecimal(list.get(i)));
							break;
						case 1:
							lenghtInchFld.setBigDecimalValue(NumberUtil.getBigDecimal(list.get(i)));
							break;
						case 2:
							widthFeetFld.setBigDecimalValue(NumberUtil.getBigDecimal(list.get(i)));
							break;
						case 3:
							widthInchFld.setBigDecimalValue(NumberUtil.getBigDecimal(list.get(i)));
							break;
						default:
							break;
						}
					}
				}

				lenghtFeetFld.addValueChangeListener(new FieldValueChangeListener(surveyResponseAnswer));
				lenghtInchFld.addValueChangeListener(new FieldValueChangeListener(surveyResponseAnswer));
				widthFeetFld.addValueChangeListener(new FieldValueChangeListener(surveyResponseAnswer));
				widthInchFld.addValueChangeListener(new FieldValueChangeListener(surveyResponseAnswer));
			} catch (FrameworkException e) {
				e.printStackTrace();
			}
		}
	}

	private class FieldValueChangeListener implements ValueChangeListener<String> {

		private SurveyResponseAnswer surveyResponseAnswer;

		public FieldValueChangeListener(SurveyResponseAnswer surveyResponseAnswer) {
			this.surveyResponseAnswer = surveyResponseAnswer;
		}

		@Override
		public void valueChange(ValueChangeEvent<String> event) {
			String value = lenghtFeetFld.getValue() + ":" + lenghtInchFld.getValue() + ":" + widthFeetFld.getValue()
					+ ":" + widthInchFld.getValue();

			ISurveyResponseAnswerService surveyResponseAnswerService = ContextProvider
					.getBean(ISurveyResponseAnswerService.class);
			SurveyResponseAnswerVO surveyResponseAnswerVO = new SurveyResponseAnswerVO();
			surveyResponseAnswerVO.setId(surveyResponseAnswer == null ? null : surveyResponseAnswer.getId());
			surveyResponseAnswerVO.setActive(true);
			surveyResponseAnswerVO
					.setUniqueId(surveyResponseAnswer == null ? null : surveyResponseAnswer.getUniqueId());
			surveyResponseAnswerVO.setSurveyResponseId(surveyResponse.getId());
			surveyResponseAnswerVO.setSurveyQuestionId(surveyQuestion.getId());
			surveyResponseAnswerVO.setCurrentRoleId(sessionHolder.getSelectedRole().getId());
			surveyResponseAnswerVO.setCurrentUserId(sessionHolder.getApplicationUser().getId());
			surveyResponseAnswerVO.setResponseText(value);
			try {
				surveyResponseAnswerService.updateAnswerForSingle(surveyResponseAnswerVO);
			} catch (FrameworkException e) {
				e.printStackTrace();
			}
		}

	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
		updateFields();
	}

	private void updateFields() {
		if (horizontalLayout != null) {
			horizontalLayout.setEnabled(editable);
		}
	}

}
