package software.simple.solutions.data.entry.es.control.components;

import java.util.ArrayList;
import java.util.List;

import software.simple.solutions.data.entry.es.control.constants.QuestionType;
import software.simple.solutions.data.entry.es.control.properties.QuestionTypeProperty;
import software.simple.solutions.framework.core.components.CComboBox;
import software.simple.solutions.framework.core.pojo.ComboItem;
import software.simple.solutions.framework.core.util.PropertyResolver;

public class QuestionTypeSelect extends CComboBox {

	private static final long serialVersionUID = 8665850289294199446L;

	public QuestionTypeSelect() {
		List<ComboItem> comboItems = new ArrayList<ComboItem>();
		comboItems.add(new ComboItem(QuestionType.SINGLE,
				PropertyResolver.getPropertyValueByLocale(QuestionTypeProperty.SINGLE)));
		comboItems.add(new ComboItem(QuestionType.DATE,
				PropertyResolver.getPropertyValueByLocale(QuestionTypeProperty.DATE)));
		comboItems.add(new ComboItem(QuestionType.LENGTH_FT_INCH,
				PropertyResolver.getPropertyValueByLocale(QuestionTypeProperty.LENGTH_FT_INCH)));
		comboItems.add(new ComboItem(QuestionType.AREA_FT_INCH,
				PropertyResolver.getPropertyValueByLocale(QuestionTypeProperty.AREA_FT_INCH)));
		comboItems.add(new ComboItem(QuestionType.CHOICES,
				PropertyResolver.getPropertyValueByLocale(QuestionTypeProperty.CHOICES)));
		comboItems.add(new ComboItem(QuestionType.MATRIX,
				PropertyResolver.getPropertyValueByLocale(QuestionTypeProperty.MATRIX)));
		setItems(comboItems);
	}

}
