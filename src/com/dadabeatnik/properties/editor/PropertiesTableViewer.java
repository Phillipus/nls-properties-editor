/*******************************************************************************
 * Copyright (c) 2010, 2012 Phillip Beauvoir
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Phillip Beauvoir
 *******************************************************************************/
package com.dadabeatnik.properties.editor;

import java.util.List;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TableColumn;



/**
 * Properties Table Viewer
 * 
 * @author Phillip Beauvoir
 */
public class PropertiesTableViewer extends TableViewer {

    /**
     * The Column Names
     */
    private static String[] columnNames = {
            "Key", //$NON-NLS-1$
            "Value" //$NON-NLS-1$
    };
    
    
    public PropertiesTableViewer(Composite parent, int style) {
        super(parent, style | SWT.FULL_SELECTION);
        
        // So that the custom cell label works
        TableColumnLayout tableColumnLayout = new TableColumnLayout();
        parent.setLayout(tableColumnLayout);

        TableColumn[] columns = new TableColumn[columnNames.length];
        
        for(int i = 0; i < columnNames.length; i++) {
            columns[i] = new TableColumn(getTable(), SWT.NONE);
            columns[i].setText(columnNames[i]);
        }
        
        tableColumnLayout.setColumnData(columns[0], new ColumnWeightData(20, true));
        tableColumnLayout.setColumnData(columns[1], new ColumnWeightData(40, true));
        
        // Column names are properties
        setColumnProperties(columnNames);

        getTable().setHeaderVisible(true);
        
        CellEditor[] editors = new CellEditor[2];  // One for each column
        editors[0] = null;
        editors[1] = new TextCellEditor(getTable());
        setCellEditors(editors);
        setCellModifier(new PropertiesTableCellModifier());
        
        setContentProvider(new PropertiesTableContentProvider());
        setLabelProvider(new PropertiesTableLabelProvider());
    }

    private class PropertiesTableContentProvider implements IStructuredContentProvider {

        public Object[] getElements(Object inputElement) {
            if(inputElement instanceof List<?>) {
                return ((List<?>)inputElement).toArray();
            }
            
            return new Object[0];
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }
    
    private class PropertiesTableLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        public String getColumnText(Object element, int columnIndex) {
            PropertyEntry entry = (PropertyEntry)element;
            
            switch(columnIndex) {
                case 0:
                    if(entry.isProperty()) {
                        return entry.getKey();
                    }
                    else if(entry.isComment()) {
                        return entry.getOriginalLine();
                    }
                    return ""; //$NON-NLS-1$

                case 1:
                    if(entry.isProperty()) {
                        return entry.getValue();
                    }
                    return ""; //$NON-NLS-1$

                default:
                    return ""; //$NON-NLS-1$
            }
        }
    }

    
    /**
     * Cell Modifier
     */
    private class PropertiesTableCellModifier implements ICellModifier {

        public boolean canModify(Object element, String property) {
            PropertyEntry entry = (PropertyEntry)element;
            return entry.isProperty();
        }

        public Object getValue(Object element, String property) {
            PropertyEntry entry = (PropertyEntry)element;
            return entry.getValue();
        }

        public void modify(Object element, String property, Object newValue) {
            Item item = (Item)element;
            PropertyEntry entry = (PropertyEntry)item.getData();
            
            String newText = (String)newValue;
            String oldText = entry.getValue();
            
            if(!newText.equals(oldText)) {
                entry.setValue(newText);
                
                update(entry, null);
                getTable().notifyListeners(SWT.Modify, null);
                
                // Move onto next row
                final List<?> list = (List<?>)getInput();
                final int index = list.indexOf(entry) + 1;
                if(index < list.size()) {
                    Display.getCurrent().asyncExec(new Runnable() {
                        public void run() {
                            editElement(list.get(index), 1);
                        }
                    });
                }
            }
        }
        
    }
}
