package com.dadabeatnik.properties.editor;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


/**
 * Main Composite
 * 
 * @author Phillip Beauvoir
 */
public class MainComposite extends Composite {
    
    private PropertiesTableViewer fPropertiesTableViewer;

    public MainComposite(Composite parent, int style) {
        super(parent, style);
        
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        setLayout(layout);
        
        Composite tableComposite = new Composite(this, SWT.NULL);
        tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        fPropertiesTableViewer = new PropertiesTableViewer(tableComposite, SWT.NONE);
    }

    public PropertiesTableViewer getTableViewer() {
        return fPropertiesTableViewer;
    }
    
    public void setProperties(List<PropertyEntry> fProperties) {
        fPropertiesTableViewer.setInput(fProperties);
    }

}
