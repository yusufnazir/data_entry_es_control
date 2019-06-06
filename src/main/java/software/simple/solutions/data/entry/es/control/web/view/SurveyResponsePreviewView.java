package software.simple.solutions.data.entry.es.control.web.view;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vaadin.teemu.switchui.Switch;

import com.vaadin.data.HasValue.ValueChangeEvent;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.data.ValueProvider;
import com.vaadin.event.LayoutEvents.LayoutClickEvent;
import com.vaadin.event.LayoutEvents.LayoutClickListener;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

import software.simple.solutions.data.entry.es.control.components.lookup.SurveyLookUpField;
import software.simple.solutions.data.entry.es.control.constants.EsControlStyle;
import software.simple.solutions.data.entry.es.control.entities.Survey;
import software.simple.solutions.data.entry.es.control.entities.SurveyResponse;
import software.simple.solutions.data.entry.es.control.entities.SurveySection;
import software.simple.solutions.data.entry.es.control.properties.SurveyResponseProperty;
import software.simple.solutions.data.entry.es.control.service.ISurveyResponseService;
import software.simple.solutions.data.entry.es.control.service.ISurveySectionService;
import software.simple.solutions.data.entry.es.control.valueobjects.SurveyResponseVO;
import software.simple.solutions.data.entry.es.control.web.view.question.QuestionSectionPreviewLayout;
import software.simple.solutions.framework.core.components.CCheckBox;
import software.simple.solutions.framework.core.components.CGridLayout;
import software.simple.solutions.framework.core.components.CTextField;
import software.simple.solutions.framework.core.components.FilterView;
import software.simple.solutions.framework.core.components.FormView;
import software.simple.solutions.framework.core.components.filter.CStringIntervalLayout;
import software.simple.solutions.framework.core.entities.MappedSuperClass;
import software.simple.solutions.framework.core.exceptions.FrameworkException;
import software.simple.solutions.framework.core.properties.SystemProperty;
import software.simple.solutions.framework.core.util.ComponentUtil;
import software.simple.solutions.framework.core.util.ContextProvider;
import software.simple.solutions.framework.core.web.BasicTemplate;
import software.simple.solutions.framework.core.web.lookup.ApplicationUserLookUpField;

public class SurveyResponsePreviewView extends BasicTemplate<SurveyResponse> {

	private static final Logger logger = LogManager.getLogger(SurveyResponsePreviewView.class);

	private static final long serialVersionUID = 6503015064562511801L;

	public SurveyResponsePreviewView() {
		setEntityClass(SurveyResponse.class);
		setServiceClass(ISurveyResponseService.class);
		setFilterClass(Filter.class);
		setFormClass(Form.class);
	}

	@Override
	public void setUpCustomColumns() {
		addContainerProperty(new ValueProvider<SurveyResponse, String>() {

			@Override
			public String apply(SurveyResponse source) {
				return source.getSurvey().getCaption();
			}
		}, SurveyResponseProperty.SURVEY);
		addContainerProperty(SurveyResponse::getFormName, SurveyResponseProperty.FORM_NAME);
		addContainerProperty(new ValueProvider<SurveyResponse, String>() {

			@Override
			public String apply(SurveyResponse source) {
				if (source.getApplicationUser() != null) {
					return source.getApplicationUser().getUsername();
				}
				return null;
			}
		}, SurveyResponseProperty.APPLICATION_USER);
	}

	/**
	 * Filter implementation
	 * 
	 * @author yusuf
	 *
	 */
	public class Filter extends FilterView {

		private static final long serialVersionUID = 117780868467918033L;

		private CStringIntervalLayout surveyNameFld;
		private CStringIntervalLayout surveyDescriptionFld;
		private CStringIntervalLayout formNameFld;
		private CStringIntervalLayout userNameFld;

		@Override
		public void executeBuild() {

			surveyNameFld = addField(CStringIntervalLayout.class, SurveyResponseProperty.SURVEY, 0, 0);
			surveyDescriptionFld = addField(CStringIntervalLayout.class, SurveyResponseProperty.SURVEY_DESCRIPTION, 0,
					1);
			MappedSuperClass parentEntity = getParentEntity();
			if (parentEntity != null && parentEntity instanceof Survey) {
				Survey survey = (Survey) getParentEntity();
				surveyNameFld.setValue(survey.getName());
				surveyNameFld.setReadOnly(true);

				surveyDescriptionFld.setValue(survey.getDescription());
				surveyDescriptionFld.setReadOnly(true);
			}

			formNameFld = addField(CStringIntervalLayout.class, SurveyResponseProperty.FORM_NAME, 1, 0);
			userNameFld = addField(CStringIntervalLayout.class, SurveyResponseProperty.APPLICATION_USER, 1, 1);
		}

		@Override
		public Object getCriteria() {
			SurveyResponseVO vo = new SurveyResponseVO();
			vo.setSurveyNameInterval(surveyNameFld.getValue());
			vo.setSurveyDescriptionInterval(surveyDescriptionFld.getValue());
			vo.setFormNameInterval(formNameFld.getValue());
			vo.setUserNameInInterval(userNameFld.getValue());
			MappedSuperClass parentEntity = getParentEntity();
			if (parentEntity != null && parentEntity instanceof Survey) {
				Survey survey = (Survey) getParentEntity();
				vo.setSurveyId(survey.getId());
			}
			return vo;
		}
	}

	public class Form extends FormView {

		private HorizontalLayout h1;

		private CGridLayout newFormGrid;
		private CTextField formNameFld;
		private SurveyLookUpField surveyFld;
		private ApplicationUserLookUpField userNameFld;
		private CCheckBox activeFld;
		private Switch editAnswersSwitch;

		private HorizontalLayout sectionMainLayout;
		private VerticalLayout v1;
		private VerticalLayout sectionLayout;
		private Panel sectionsPanel;
		private VerticalLayout sectionPanelLayout;
		private boolean editAnswers = false;
		private CssLayout selectedMenuLayout;

		private SurveyResponse surveyResponse;

		private QuestionSectionPreviewLayout questionSectionPreviewLayout;

		@Override
		public void executeBuild() {
			// setWidth("100%");
			h1 = new HorizontalLayout();
			h1.setMargin(false);
			addComponent(h1);

			newFormGrid = ComponentUtil.createGrid();
			h1.addComponent(newFormGrid);

			formNameFld = newFormGrid.addField(CTextField.class, SurveyResponseProperty.FORM_NAME, 0, 0);
			formNameFld.addStyleName(ValoTheme.LABEL_COLORED);
			formNameFld.addStyleName(ValoTheme.LABEL_H3);

			surveyFld = newFormGrid.addField(SurveyLookUpField.class, SurveyResponseProperty.SURVEY, 0, 1);
			surveyFld.handleForParentEntity(getParentEntity());
			surveyFld.addStyleName(ValoTheme.LABEL_COLORED);

			activeFld = newFormGrid.addField(CCheckBox.class, SystemProperty.SYSTEM_ENTITY_ACTIVE, 1, 0);
			activeFld.setReadOnly(true);

			userNameFld = newFormGrid.addField(ApplicationUserLookUpField.class,
					SurveyResponseProperty.APPLICATION_USER, 1, 1);
			userNameFld.addStyleName(ValoTheme.LABEL_COLORED);
			userNameFld.handleForParentEntity(sessionHolder.getApplicationUser());

			editAnswersSwitch = newFormGrid.addField(Switch.class, SurveyResponseProperty.EDIT_ANSWERS, 2, 0);
			editAnswersSwitch.setVisible(false);
			editAnswersSwitch.setAnimationEnabled(true);
			editAnswersSwitch.addStyleName("compact");

			sectionMainLayout = buildMainLayout();
			addComponent(sectionMainLayout);
		}

		private HorizontalLayout buildMainLayout() {
			sectionMainLayout = new HorizontalLayout();
			sectionMainLayout.setHeight("100%");
			sectionMainLayout.setMargin(false);
			sectionMainLayout.setSizeFull();
			addComponent(sectionMainLayout);
			v1 = new VerticalLayout();
			v1.addStyleName(ValoTheme.LAYOUT_WELL);
			v1.setHeight("100%");
			v1.setWidth("225px");
			sectionMainLayout.addComponentAsFirst(v1);

			sectionsPanel = new Panel();
			sectionsPanel.setHeight("350px");
			v1.addComponent(sectionsPanel);
			v1.setExpandRatio(sectionsPanel, 1);

			sectionPanelLayout = new VerticalLayout();
			sectionPanelLayout.setMargin(false);
			sectionPanelLayout.setSpacing(false);
			sectionsPanel.setContent(sectionPanelLayout);

			sectionLayout = new VerticalLayout();
			sectionLayout.setWidth("800px");
			sectionMainLayout.addComponent(sectionLayout);
			sectionMainLayout.setExpandRatio(sectionLayout, 1);

			return sectionMainLayout;
		}

		private void createMenu() throws FrameworkException {
			sectionPanelLayout.removeAllComponents();
			sectionsPanel.setVisible(false);
			ISurveySectionService surveySectionService = ContextProvider.getBean(ISurveySectionService.class);
			List<SurveySection> surveySections = surveySectionService
					.findAllBySurveyId(surveyResponse.getSurvey().getId());
			if (surveySections != null && !surveySections.isEmpty()) {
				sectionsPanel.setVisible(true);
				surveySections.forEach(p -> {
					createMenuItem(p);
				});
			}
		}

		private CssLayout createMenuItem(SurveySection surveySection) {
			CssLayout menuLayout = new CssLayout();
			menuLayout.setWidth("100%");
			menuLayout.addStyleName(EsControlStyle.QUESTION_MENU);
			menuLayout.setDescription(surveySection.getName());
			menuLayout.setData(surveySection);

			Label label = new Label(surveySection.getName());
			label.addStyleName(ValoTheme.LABEL_LARGE);
			menuLayout.addComponent(label);
			sectionPanelLayout.addComponent(menuLayout);

			menuLayout.addLayoutClickListener(new LayoutClickListener() {

				@Override
				public void layoutClick(LayoutClickEvent event) {
					if (event.getButton().compareTo(MouseButton.LEFT) == 0) {
						selectedMenuLayout = menuLayout;
						createQuestionCard(surveySection);
						sectionPanelLayout.iterator()
								.forEachRemaining(p -> p.removeStyleName(EsControlStyle.QUESTION_MENU_SELECTED));
						menuLayout.addStyleName(EsControlStyle.QUESTION_MENU_SELECTED);
					}
				}
			});
			return menuLayout;
		}

		private void createQuestionCard(SurveySection surveySection) {
			sectionLayout.removeAllComponents();

			sectionPanelLayout.iterator()
					.forEachRemaining(p -> p.removeStyleName(EsControlStyle.QUESTION_MENU_SELECTED));

			questionSectionPreviewLayout = new QuestionSectionPreviewLayout(sessionHolder);
			questionSectionPreviewLayout.setShowInfo(false);
			questionSectionPreviewLayout.setUseGrid(false);
			questionSectionPreviewLayout.setEditable(editAnswers);
			questionSectionPreviewLayout.setSurveySection(surveySection, surveyResponse);
			questionSectionPreviewLayout.setWidth("100%");
			sectionLayout.addComponent(questionSectionPreviewLayout);
		}

		@Override
		public void handleNewForm() throws FrameworkException {
			setWidth("-1px");
			activeFld.setValue(true);
			newFormGrid.setVisible(true);
			sectionMainLayout.setVisible(false);
		}

		@SuppressWarnings("unchecked")
		@Override
		public SurveyResponse setFormValues(Object entity) throws FrameworkException {
			surveyResponse = (SurveyResponse) entity;

			createMenu();

			formNameFld.setValue(surveyResponse.getFormName());
			userNameFld.setValue(surveyResponse.getApplicationUser());
			surveyFld.setValue(surveyResponse.getSurvey());
			activeFld.setValue(surveyResponse.getActive());
			editAnswersSwitch.setVisible(true);
			editAnswersSwitch.setValue(editAnswers);

			editAnswersSwitch.addValueChangeListener(new ValueChangeListener<Boolean>() {

				@Override
				public void valueChange(ValueChangeEvent<Boolean> event) {
					editAnswers = !editAnswers;
					try {
						createMenu();
					} catch (FrameworkException e) {
						e.printStackTrace();
					}
					SurveySection surveySection = (SurveySection) selectedMenuLayout.getData();
					createQuestionCard(surveySection);
					sectionPanelLayout.iterator()
							.forEachRemaining(p -> p.removeStyleName(EsControlStyle.QUESTION_MENU_SELECTED));
					selectedMenuLayout.addStyleName(EsControlStyle.QUESTION_MENU_SELECTED);
				}
			});

			return surveyResponse;
		}

		@Override
		public Object getFormValues() throws FrameworkException {
			SurveyResponseVO vo = new SurveyResponseVO();
			vo.setFormName(formNameFld.getValue());
			vo.setSurveyId(surveyFld.getItemId());
			vo.setApplicationUserId(userNameFld.getItemId());
			vo.setActive(activeFld.getValue());
			return vo;
		}

	}

}
