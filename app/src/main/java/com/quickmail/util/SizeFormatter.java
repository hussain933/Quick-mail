package com.quickmail.util;

import java.text.DecimalFormat;

public class SizeFormatter {
    public static String formatMB(long bytes) {
        double mb = bytes / (1024.0 * 1024.0);
        return new DecimalFormat("0.0").format(mb);
    }
}
