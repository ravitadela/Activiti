package org.activiti.engine.impl.el;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import de.odysseus.el.misc.TypeConverterImpl;

public class ActivitiTypeConverter extends TypeConverterImpl {

    public ActivitiTypeConverter() {}

    private static final long serialVersionUID = 1L;

    protected String coerceToString(Object value) {
        String coercedVal = null;
        if (value instanceof Date) {
            Date date = (Date)value;
            DateTimeFormatter format = ISODateTimeFormat.dateTime();
            coercedVal =  format.print(new DateTime(date, DateTimeZone.UTC));
        } else {
            coercedVal = super.coerceToString(value);
        }
        return coercedVal;
    }
}
