package org.droidmate.analyzer;

import org.droidmate.analyzer.api.IApi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by nataniel on 19.05.17.
 */
public class ReportFormatter {
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    static String formatApiList(List<IApi> apis){
        StringBuilder b = new StringBuilder();
        apis.forEach(p -> b.append(String.format("\t%s\n", p.toString())));

        return b.toString();
    }

    static String formatDate(Date date){
        return dateFormat.format(date);
    }
}
