package ch.cern.mig.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * Filename filter which can be used to filter by regular expressions.
 * <p/>
 * It can work in two modes:
 * <ul>
 * <li>match mode (default): filename must match the regular expression
 * <li>find mode: regular expression occurs in the filename
 * </ul>
 *
 * @author Massimo Paladin - massimo.paladin@gmail.com <br />
 *         Copyright (C) CERN 2012-2013
 */
public class RegExpFilenameFilter implements FilenameFilter {
    private Pattern regexp = null;
    private boolean matches = true;

    /**
     * Create a FilenameFilter given a Pattern in match mode.
     *
     * @param pattern the pattern to be used during filtering
     */
    public RegExpFilenameFilter(Pattern pattern) {
        this(pattern, true);
    }

    /**
     * Create a FilenameFilter given a Pattern.
     *
     * @param pattern the pattern to be used during filtering
     * @param matches true if match mode should be applied, false if find mode
     *                should be used
     */
    public RegExpFilenameFilter(Pattern pattern, boolean matches) {
        this.regexp = pattern;
        this.matches = matches;
    }

    public boolean accept(File arg0, String arg1) {
        if (matches) {
            return regexp.matcher(arg1).matches();
        } else {
            return regexp.matcher(arg1).find();
        }
    }
}
