package com.dadabeatnik.properties.editor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PropertyEntry {
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^\\s*#(.*)$"); //$NON-NLS-1$
    private static final Pattern MAIN_PATTERN = Pattern.compile("^\\s*(!|;)?\\s*([^:=]+)\\s*(=|:)?\\s*(.*)\\s*$"); //$NON-NLS-1$
    
    private String fLine;
    private boolean fIsComment, fIsProperty;
    
    private String fKey, fValue;

    public PropertyEntry(String line) {
        fLine = line;
        
        Matcher commentMatcher = COMMENT_PATTERN.matcher(fLine);
        if(commentMatcher.find()) {
            fIsComment = true;
        }
        else {
            Matcher mainMatcher = MAIN_PATTERN.matcher(fLine);
            if(mainMatcher.find()) {
                fIsProperty = true;
                readProperty(mainMatcher);
            }
        }
    }
    
    public String getOriginalLine() {
        return fLine;
    }
    
    public String getEditedLine() {
        if(isProperty()) {
            return fKey + "=" + escapeUnicodeString(fValue, false); //$NON-NLS-1$
        }
        return fLine;
    }
    
    public boolean isComment() {
        return fIsComment;
    }
    
    public boolean isProperty() {
        return fIsProperty;
    }
    
    public String getKey() {
        return fKey;
    }
    
    public String getValue() {
        return fValue;
    }
    
    public void setValue(String value) {
        if(!isProperty()) {
            throw new IllegalArgumentException("PropertyEntry is not a Property type"); //$NON-NLS-1$
        }
        fValue = value;
    }
    
    private void readProperty(Matcher mainMatcher) {
        for(int i = 0; i <= mainMatcher.groupCount(); i++) {
            int start = mainMatcher.start(i);
            int end = mainMatcher.end(i);
            if((start > -1) && (end > -1)) {
                String str = fLine.substring(mainMatcher.start(i), mainMatcher.end(i));
                switch(i) {
                    case 1:
                        //commented = true;
                        break;
                    case 2:
                        fKey = str.trim();
                        break;
                    case 4:
                        fValue = escapeUnicodeString(str, true);
                        break;
                }
            }
        }
    }

    private String escapeUnicodeString(String inputString, boolean reverse) {
        StringBuffer buffer = new StringBuffer();
        
        for(int i = 0; i < inputString.length(); i++) {
            if(reverse) {
                if(i < inputString.length() - 1) {
                    if(inputString.substring(i, i + 2).equals("\\u")) { //$NON-NLS-1$
                        buffer.append((char)Integer.parseInt(inputString.substring(i + 2, i + 6), 0x10));
                        i += 5;
                    }
                    else {
                        buffer.append(inputString.substring(i, i + 1));
                    }
                }
                else {
                    buffer.append(inputString.substring(i, i + 1));
                }
            }
            else {
                char ch = inputString.charAt(i);
                if(ch >= 0x0020 && ch <= 0x007e) {
                    buffer.append(ch);
                }
                else {
                    buffer.append("\\u"); //$NON-NLS-1$
                    String hex = Integer.toHexString(inputString.charAt(i) & 0xFFFF);
                    hex = hex.toLowerCase();
                    for(int j = hex.length(); j < 4; j++) {
                        buffer.append('0');
                    }
                    buffer.append(hex.toUpperCase());
                }
            }
        }
        
        return buffer.toString();
    }

}
