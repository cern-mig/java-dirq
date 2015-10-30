package ch.cern.mig.utils;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * Filename filter which can be used to filter by regular expressions.
 * <p>
 * It can work in two modes:
 * <ul>
 * <li>match mode (default): filename must match the regular expression
 * <li>find mode: regular expression occurs in the filename
 * </ul>
 *
 * @author Lionel Cons &lt;lionel.cons@cern.ch&gt;
 * @author Massimo Paladin &lt;massimo.paladin@gmail.com&gt;
 * Copyright (C) CERN 2012-2015
 */
public class RegExpFilenameFilter implements FilenameFilter {

    private Pattern regexp;
    private boolean matches;

    /**
     * Create a FilenameFilter given a Pattern in match mode.
     *
     * @param pattern the pattern to be used during filtering
     */
    public RegExpFilenameFilter(final Pattern regexp) {
        this(regexp, true);
    }

    /**
     * Create a FilenameFilter given a Pattern.
     *
     * @param pattern the pattern to be used during filtering
     * @param matches true if match mode should be applied, false if find mode
     *                should be used
     */
    public RegExpFilenameFilter(final Pattern regexp, final boolean matches) {
        this.regexp = regexp;
        this.matches = matches;
    }

    public boolean accept(final File dir, final String name) {
        if (matches) {
            return regexp.matcher(name).matches();
        } else {
            return regexp.matcher(name).find();
        }
    }

}
