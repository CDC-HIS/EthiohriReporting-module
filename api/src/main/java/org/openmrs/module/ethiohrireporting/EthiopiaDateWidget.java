package org.openmrs.module.htmlwidgets.web.html;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import org.openmrs.api.context.Context;
import org.openmrs.module.htmlwidgets.web.WidgetConfig;

public class EthiopiaDateWidget implements Widget {

	/** 
	 * @see Widget#render(WidgetConfig)
	 */
	public void render(WidgetConfig config, Writer w) throws IOException {
		
		//HtmlUtil.renderResource(w, config.getRequest(), "/css/jquery.calendars.picker.css");
		HtmlUtil.renderResource(w, config.getRequest(), "/scripts/calendar/ethiopianCalendar.js");
		// HtmlUtil.renderResource(w, config.getRequest(), "/scripts/jquery/jquery.calendars.js");
		// HtmlUtil.renderResource(w, config.getRequest(), "/scripts/jquery/jquery.calendars.plus.js");
		// HtmlUtil.renderResource(w, config.getRequest(), "/scripts/jquery/jquery.plugin.js");
		// HtmlUtil.renderResource(w, config.getRequest(), "/scripts/jquery/jquery.calendars.picker.js");
		// HtmlUtil.renderResource(w, config.getRequest(), "/scripts/jquery/jquery.calendars.ethiopian.js");
		
		config.setFixedAttribute("size", "20");
		config.setFixedAttribute("onClick", "showCalendar(this);");
		
		Object v = config.getDefaultValue();
		if (v != null && v instanceof Date) {
			config.setDefaultValue(Context.getDateFormat().format((Date) v));
		}
		TextWidget widget = WidgetFactory.getInstance(TextWidget.class, config);
		widget.render(config, w);
	}
}
