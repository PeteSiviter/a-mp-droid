package com.mediaportal.ampdroid.api.soap;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import org.kobjects.isodate.IsoDate;
import org.ksoap2.serialization.Marshal;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

/**
 * 
 * @author Vladimir Used to marshal Dates - crucial to serialization for SOAP
 */
public class MarshalDate implements Marshal {

   public Object readInstance(XmlPullParser parser, String namespace, String name,
         PropertyInfo expected) throws IOException, XmlPullParserException {

      return IsoDate.stringToDate(parser.nextText(), IsoDate.DATE_TIME);
   }

   public void register(SoapSerializationEnvelope cm) {
      cm.addMapping(cm.xsd, "DateTime", Date.class, this);
   }

   public void writeInstance(XmlSerializer writer, Object obj) throws IOException {
      Date date = (Date) obj;
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      int offset = (int)((cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / 60000 );

      cal.add(Calendar.MINUTE, offset); 
      writer.text(IsoDate.dateToString(cal.getTime(), IsoDate.DATE_TIME));
   }

}