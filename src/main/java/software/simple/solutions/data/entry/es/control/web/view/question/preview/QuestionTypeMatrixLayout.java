package software.simple.solutions.data.entry.es.control.web.view.question.preview;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.data.ValueProvider;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.shared.ui.grid.HeightMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.Grid.SelectionMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import software.simple.solutions.data.entry.es.control.constants.Axis;
import software.simple.solutions.data.entry.es.control.constants.MatrixColumnType;
import software.simple.solutions.data.entry.es.control.entities.SurveyQuestion;
import software.simple.solutions.data.entry.es.control.entities.SurveyQuestionAnswerChoice;
import software.simple.solutions.data.entry.es.control.entities.SurveyQuestionAnswerChoiceSelection;
import software.simple.solutions.data.entry.es.control.entities.SurveyResponse;
import software.simple.solutions.data.entry.es.control.entities.SurveyResponseAnswer;
import software.simple.solutions.data.entry.es.control.service.ISurveyQuestionAnswerChoiceSelectionService;
import software.simple.solutions.data.entry.es.control.service.ISurveyQuestionAnswerChoiceService;
import software.simple.solutions.data.entry.es.control.service.ISurveyResponseAnswerService;
import software.simple.solutions.data.entry.es.control.valueobjects.SurveyResponseAnswerVO;
import software.simple.solutions.framework.core.components.CCheckBox;
import software.simple.solutions.framework.core.components.CDecimalField;
import software.simple.solutions.framework.core.components.CDiscreetNumberField;
import software.simple.solutions.framework.core.components.CPopupDateField;
import software.simple.solutions.framework.core.components.MessageWindowHandler;
import software.simple.solutions.framework.core.components.SessionHolder;
import software.simple.solutions.framework.core.constants.Constants;
import software.simple.solutions.framework.core.exceptions.FrameworkException;
import software.simple.solutions.framework.core.util.ContextProvider;

public class QuestionTypeMatrixLayout extends VerticalLayout {

	private static final Logger logger = LogManager.getLogger(QuestionTypeMatrixLayout.class);

	private VerticalLayout matrixContainerLayout;

	private SurveyQuestion surveyQuestion;
	private SurveyResponse surveyResponse;
	private SessionHolder sessionHolder;
	private Grid<MatrixRow> grid;

	private ISurveyQuestionAnswerChoiceService surveyQuestionAnswerChoiceService;
	private ISurveyQuestionAnswerChoiceSelectionService surveyQuestionAnswerChoiceSelectionService;

	private boolean editable = false;

	public QuestionTypeMatrixLayout(SessionHolder sessionHolder, SurveyQuestion surveyQuestion,
			SurveyResponse surveyResponse) {
		this.sessionHolder = sessionHolder;
		this.surveyQuestion = surveyQuestion;
		this.surveyResponse = surveyResponse;
		surveyQuestionAnswerChoiceService = ContextProvider.getBean(ISurveyQuestionAnswerChoiceService.class);
		surveyQuestionAnswerChoiceSelectionService = ContextProvider
				.getBean(ISurveyQuestionAnswerChoiceSelectionService.class);
	}

	public void build() {
		buildMainLayout();
		setUpFields();
	}

	private void buildMainLayout() {
		matrixContainerLayout = new VerticalLayout();
		matrixContainerLayout.setMargin(false);
		matrixContainerLayout.setWidth("100%");
		addComponent(matrixContainerLayout);
	}

	public void setUpFields() {
		if (surveyQuestion != null) {
			try {
				createMatrixFromSurveyQuestion();
			} catch (FrameworkException e) {
				logger.error(e.getMessage(), e);
				new MessageWindowHandler(e);
			}
		}
	}

	public void createMatrixFromSurveyQuestion() throws FrameworkException {
		matrixContainerLayout.removeAllComponents();

		List<SurveyResponseAnswer> surveyResponseAnswers = null;
		if (surveyResponse != null) {
			ISurveyResponseAnswerService surveyResponseAnswerService = ContextProvider
					.getBean(ISurveyResponseAnswerService.class);
			surveyResponseAnswers = surveyResponseAnswerService.getSurveyResponseAnswers(surveyResponse.getId(),
					surveyQuestion.getId());
		}

		List<SurveyQuestionAnswerChoice> surveyQuestionAnswerChoices = surveyQuestionAnswerChoiceService
				.findBySurveyQuestion(surveyQuestion.getId());
		if (surveyQuestionAnswerChoices != null && !surveyQuestionAnswerChoices.isEmpty()) {
			grid = new Grid<MatrixRow>();
			grid.setHeightMode(HeightMode.UNDEFINED);
			grid.setSelectionMode(SelectionMode.NONE);
			grid.setWidth("100%");
			matrixContainerLayout.addComponent(grid);

			List<SurveyQuestionAnswerChoice> columns = surveyQuestionAnswerChoices.stream()
					.filter(p -> p.getAxis().equalsIgnoreCase(Axis.COLUMN)).collect(Collectors.toList());
			if (columns != null && !columns.isEmpty()) {
				boolean usedRowHeader = false;
				for (SurveyQuestionAnswerChoice surveyQuestionAnswerChoice : columns) {
					if (StringUtils.isNotBlank(surveyQuestionAnswerChoice.getLabel())) {
						if (!usedRowHeader) {
							Column<MatrixRow, String> column = grid.addColumn(new ValueProvider<MatrixRow, String>() {

								@Override
								public String apply(MatrixRow source) {
									return source.surveyQuestionAnswerChoice.getLabel();
								}
							});
							column.setSortable(false);
							column.setCaption(surveyQuestionAnswerChoice.getLabel())
									.setId(surveyQuestionAnswerChoice.getLabel());
							usedRowHeader = true;
						} else {
							Column<MatrixRow, Component> column = grid
									.addComponentColumn(new ComponentColumn(surveyQuestionAnswerChoice))
									.setId(surveyQuestionAnswerChoice.getId().toString())
									.setCaption(surveyQuestionAnswerChoice.getLabel());
							column.setSortable(false);
						}
					}
				}

				if (surveyResponseAnswers == null) {
					surveyResponseAnswers = new ArrayList<SurveyResponseAnswer>();
				}
				List<SurveyQuestionAnswerChoice> rows = surveyQuestionAnswerChoices.stream()
						.filter(p -> p.getAxis().equalsIgnoreCase(Axis.ROW)).collect(Collectors.toList());
				List<MatrixRow> matrixRows = new ArrayList<QuestionTypeMatrixLayout.MatrixRow>();
				for (SurveyQuestionAnswerChoice surveyQuestionAnswerChoice : rows) {

					Map<Long, List<SurveyResponseAnswer>> map = surveyResponseAnswers.stream()
							.filter(p -> (p.getSurveyQuestionAnswerChoiceRow().getId()
									.compareTo(surveyQuestionAnswerChoice.getId()) == 0))
							.collect(Collectors.groupingBy(p -> p.getSurveyQuestionAnswerChoiceColumn().getId()));
					MatrixRow matrixRow = new MatrixRow();
					matrixRow.surveyQuestionAnswerChoice = surveyQuestionAnswerChoice;
					matrixRow.responseMap = map;
					matrixRows.add(matrixRow);
				}
				grid.setItems(matrixRows);
			}
		}
	}

	private final class ComponentColumn implements ValueProvider<MatrixRow, Component> {

		private SurveyQuestionAnswerChoice column;
		private SurveyResponseAnswer surveyResponseAnswer;

		public ComponentColumn(SurveyQuestionAnswerChoice column) {
			this.column = column;
		}

		@Override
		public Component apply(MatrixRow source) {
			List<SurveyResponseAnswer> answers = null;
			if (source.responseMap != null) {
				answers = source.responseMap.get(column.getId());
			}
			SurveyQuestionAnswerChoice row = source.surveyQuestionAnswerChoice;
			String matrixColumnType = column.getMatrixColumnType();
			switch (matrixColumnType) {
			case MatrixColumnType.TEXT:
				TextField textField = new TextField();
				textField.setValueChangeMode(ValueChangeMode.BLUR);
				textField.setWidth("100%");
				textField.setEnabled(editable);
				textField.addStyleName(ValoTheme.TEXTFIELD_SMALL);

				if (answers != null && answers.size() == 1) {
					surveyResponseAnswer = answers.get(0);
					textField.setValue(surveyResponseAnswer.getResponseText());
				}

				textField.addValueChangeListener(new ValueChangeListener<String>() {

					@Override
					public void valueChange(ValueChangeEvent<String> event) {
						String value = event.getValue();
						ISurveyResponseAnswerService surveyResponseAnswerService = ContextProvider
								.getBean(ISurveyResponseAnswerService.class);
						SurveyResponseAnswerVO surveyResponseAnswerVO = new SurveyResponseAnswerVO();
						surveyResponseAnswerVO
								.setId(surveyResponseAnswer == null ? null : surveyResponseAnswer.getId());
						surveyResponseAnswerVO.setActive(true);
						surveyResponseAnswerVO
								.setUniqueId(surveyResponseAnswer == null ? null : surveyResponseAnswer.getUniqueId());
						surveyResponseAnswerVO.setSurveyResponseId(surveyResponse.getId());
						surveyResponseAnswerVO.setSurveyQuestionId(surveyQuestion.getId());
						surveyResponseAnswerVO.setCurrentRoleId(sessionHolder.getSelectedRole().getId());
						surveyResponseAnswerVO.setCurrentUserId(sessionHolder.getApplicationUser().getId());
						surveyResponseAnswerVO.setQuestionAnswerChoiceRowId(row.getId());
						surveyResponseAnswerVO.setQuestionAnswerChoiceColumnId(column.getId());
						surveyResponseAnswerVO.setResponseText(value);
						try {
							surveyResponseAnswer = surveyResponseAnswerService
									.updateAnswerMatrixCellForText(surveyResponseAnswerVO);
						} catch (FrameworkException e) {
							e.printStackTrace();
						}

					}
				});

				return textField;
			case MatrixColumnType.DATE:
				CPopupDateField dateField = new CPopupDateField();
				dateField.setEnabled(editable);
				dateField.addStyleName(ValoTheme.DATEFIELD_SMALL);
				dateField.setDateFormat(Constants.SIMPLE_DATE_FORMAT.toPattern());

				if (answers != null && answers.size() == 1) {
					surveyResponseAnswer = answers.get(0);
					if (StringUtils.isNotBlank(surveyResponseAnswer.getResponseText())) {
						LocalDate localDate = LocalDate.parse(surveyResponseAnswer.getResponseText(),
								DateTimeFormatter.ofPattern(Constants.SIMPLE_DATE_FORMAT.toPattern()));
						dateField.setValue(localDate);
					}
				}
				dateField.addValueChangeListener(new ValueChangeListener<LocalDate>() {

					@Override
					public void valueChange(ValueChangeEvent<LocalDate> event) {
						LocalDate localDate = event.getValue();

						ISurveyResponseAnswerService surveyResponseAnswerService = ContextProvider
								.getBean(ISurveyResponseAnswerService.class);
						SurveyResponseAnswerVO surveyResponseAnswerVO = new SurveyResponseAnswerVO();
						surveyResponseAnswerVO
								.setId(surveyResponseAnswer == null ? null : surveyResponseAnswer.getId());
						surveyResponseAnswerVO.setActive(true);
						surveyResponseAnswerVO
								.setUniqueId(surveyResponseAnswer == null ? null : surveyResponseAnswer.getUniqueId());
						surveyResponseAnswerVO.setSurveyResponseId(surveyResponse.getId());
						surveyResponseAnswerVO.setSurveyQuestionId(surveyQuestion.getId());
						surveyResponseAnswerVO.setCurrentRoleId(sessionHolder.getSelectedRole().getId());
						surveyResponseAnswerVO.setCurrentUserId(sessionHolder.getApplicationUser().getId());
						surveyResponseAnswerVO.setQuestionAnswerChoiceRowId(row.getId());
						surveyResponseAnswerVO.setQuestionAnswerChoiceColumnId(column.getId());
						surveyResponseAnswerVO.setResponseText(localDate == null ? null
								: localDate
										.format(DateTimeFormatter.ofPattern(Constants.SIMPLE_DATE_FORMAT.toPattern())));
						try {
							surveyResponseAnswerService.updateAnswerMatrixCellForText(surveyResponseAnswerVO);
						} catch (FrameworkException e) {
							e.printStackTrace();
						}
					}
				});

				return dateField;
			case MatrixColumnType.DECIMAL_NUMBER:
				CDecimalField decimalField = new CDecimalField(sessionHolder);
				decimalField.setEnabled(editable);
				decimalField.setValueChangeMode(ValueChangeMode.BLUR);
				decimalField.setWidth("100%");
				decimalField.addStyleName(ValoTheme.TEXTFIELD_SMALL);

				if (answers != null && answers.size() == 1) {
					surveyResponseAnswer = answers.get(0);
					decimalField.setValue(surveyResponseAnswer.getResponseText());
				}

				decimalField.addValueChangeListener(new ValueChangeListener<String>() {

					@Override
					public void valueChange(ValueChangeEvent<String> event) {
						String value = event.getValue();
						ISurveyResponseAnswerService surveyResponseAnswerService = ContextProvider
								.getBean(ISurveyResponseAnswerService.class);
						SurveyResponseAnswerVO surveyResponseAnswerVO = new SurveyResponseAnswerVO();
						surveyResponseAnswerVO
								.setId(surveyResponseAnswer == null ? null : surveyResponseAnswer.getId());
						surveyResponseAnswerVO.setActive(true);
						surveyResponseAnswerVO
								.setUniqueId(surveyResponseAnswer == null ? null : surveyResponseAnswer.getUniqueId());
						surveyResponseAnswerVO.setSurveyResponseId(surveyResponse.getId());
						surveyResponseAnswerVO.setSurveyQuestionId(surveyQuestion.getId());
						surveyResponseAnswerVO.setCurrentRoleId(sessionHolder.getSelectedRole().getId());
						surveyResponseAnswerVO.setCurrentUserId(sessionHolder.getApplicationUser().getId());
						surveyResponseAnswerVO.setQuestionAnswerChoiceRowId(row.getId());
						surveyResponseAnswerVO.setQuestionAnswerChoiceColumnId(column.getId());
						surveyResponseAnswerVO.setResponseText(value);
						try {
							surveyResponseAnswer = surveyResponseAnswerService
									.updateAnswerMatrixCellForText(surveyResponseAnswerVO);
						} catch (FrameworkException e) {
							e.printStackTrace();
						}
					}
				});

				return decimalField;
			case MatrixColumnType.WHOLE_NUMBER:
				CDiscreetNumberField discreetNumberField = new CDiscreetNumberField();
				discreetNumberField.setValueChangeMode(ValueChangeMode.BLUR);
				discreetNumberField.setEnabled(editable);
				discreetNumberField.setWidth("100%");
				discreetNumberField.addStyleName(ValoTheme.TEXTFIELD_SMALL);

				if (answers != null && answers.size() == 1) {
					surveyResponseAnswer = answers.get(0);
					discreetNumberField.setValue(surveyResponseAnswer.getResponseText());
				}

				discreetNumberField.addValueChangeListener(new ValueChangeListener<String>() {

					@Override
					public void valueChange(ValueChangeEvent<String> event) {
						String value = event.getValue();
						ISurveyResponseAnswerService surveyResponseAnswerService = ContextProvider
								.getBean(ISurveyResponseAnswerService.class);
						SurveyResponseAnswerVO surveyResponseAnswerVO = new SurveyResponseAnswerVO();
						surveyResponseAnswerVO
								.setId(surveyResponseAnswer == null ? null : surveyResponseAnswer.getId());
						surveyResponseAnswerVO.setActive(true);
						surveyResponseAnswerVO
								.setUniqueId(surveyResponseAnswer == null ? null : surveyResponseAnswer.getUniqueId());
						surveyResponseAnswerVO.setSurveyResponseId(surveyResponse.getId());
						surveyResponseAnswerVO.setSurveyQuestionId(surveyQuestion.getId());
						surveyResponseAnswerVO.setCurrentRoleId(sessionHolder.getSelectedRole().getId());
						surveyResponseAnswerVO.setCurrentUserId(sessionHolder.getApplicationUser().getId());
						surveyResponseAnswerVO.setQuestionAnswerChoiceRowId(row.getId());
						surveyResponseAnswerVO.setQuestionAnswerChoiceColumnId(column.getId());
						surveyResponseAnswerVO.setResponseText(value);
						try {
							surveyResponseAnswerService.updateAnswerMatrixCellForText(surveyResponseAnswerVO);
						} catch (FrameworkException e) {
							e.printStackTrace();
						}

					}
				});
				return discreetNumberField;
			case MatrixColumnType.SINGLE_SELECTION:
			case MatrixColumnType.MULTIPLE_SELECTION:
//				CCheckBox singleSelectionFld = new CCheckBox();
//
//				if (answers != null && answers.size() == 1) {
//					surveyResponseAnswer = answers.get(0);
//					singleSelectionFld.setValue(surveyResponseAnswer.getSelected());
//				}
//
//				return singleSelectionFld;
			case MatrixColumnType.SINGLE_COMPOSITE_SELECTION:
			case MatrixColumnType.MULTIPLE_COMPOSITE_SELECTION:
				HorizontalLayout horizontalLayout = new HorizontalLayout();
				horizontalLayout.setMargin(false);
				horizontalLayout.setWidth("-1px");
				try {

					List<SurveyQuestionAnswerChoiceSelection> selections = surveyQuestionAnswerChoiceSelectionService
							.getBySurveyQuestionAnswerChoice(column.getId());
					if (selections != null) {

						Map<Long, List<SurveyResponseAnswer>> selectionMap = null;
						if (answers != null) {
							selectionMap = answers.stream().collect(
									Collectors.groupingBy(p -> p.getSurveyQuestionAnswerChoiceSelection().getId()));
						}
						for (SurveyQuestionAnswerChoiceSelection selection : selections) {
							CCheckBox checkBox = new CCheckBox();
							checkBox.setEnabled(editable);
							checkBox.setCaption(selection.getLabel());
							horizontalLayout.addComponent(checkBox);

							if (selectionMap != null) {
								List<SurveyResponseAnswer> responses = selectionMap.get(selection.getId());
								if (responses != null && responses.size() == 1) {
									surveyResponseAnswer = responses.get(0);
									checkBox.setValue(surveyResponseAnswer.getSelected());
								}
							}

							checkBox.addValueChangeListener(new ValueChangeListener<Boolean>() {

								@Override
								public void valueChange(ValueChangeEvent<Boolean> event) {
									Boolean selected = event.getValue();

									ISurveyResponseAnswerService surveyResponseAnswerService = ContextProvider
											.getBean(ISurveyResponseAnswerService.class);
									SurveyResponseAnswerVO surveyResponseAnswerVO = new SurveyResponseAnswerVO();
									surveyResponseAnswerVO
											.setId(surveyResponseAnswer == null ? null : surveyResponseAnswer.getId());
									surveyResponseAnswerVO.setActive(true);
									surveyResponseAnswerVO.setUniqueId(
											surveyResponseAnswer == null ? null : surveyResponseAnswer.getUniqueId());
									surveyResponseAnswerVO.setSurveyResponseId(surveyResponse.getId());
									surveyResponseAnswerVO.setSurveyQuestionId(surveyQuestion.getId());
									surveyResponseAnswerVO.setCurrentRoleId(sessionHolder.getSelectedRole().getId());
									surveyResponseAnswerVO.setCurrentUserId(sessionHolder.getApplicationUser().getId());
									surveyResponseAnswerVO.setQuestionAnswerChoiceRowId(row.getId());
									surveyResponseAnswerVO.setQuestionAnswerChoiceColumnId(column.getId());
									surveyResponseAnswerVO.setQuestionAnswerChoiceSelectionId(selection.getId());
									surveyResponseAnswerVO.setSelected(selected);
									try {
										surveyResponseAnswer = surveyResponseAnswerService
												.updateAnswerMatrixCellForSelection(surveyResponseAnswerVO);
									} catch (FrameworkException e) {
										e.printStackTrace();
									}
								}
							});
						}
					}
				} catch (FrameworkException e) {
					logger.error(e.getMessage(), e);
					new MessageWindowHandler(e);
				}
				return horizontalLayout;
			default:
				break;
			}
			return null;
		}
	}

	private class MatrixRow {
		private String responseText;
		private SurveyQuestionAnswerChoice surveyQuestionAnswerChoice;
		private Map<Long, List<SurveyResponseAnswer>> responseMap;
	}

	public void setPreviewMode() {
		setUpFields();
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
		updateFields();
	}

	private void updateFields() {
		if (grid != null) {
			grid.setEnabled(editable);
		}
	}

}
