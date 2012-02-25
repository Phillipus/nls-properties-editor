package com.dadabeatnik.properties.editor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;


/**
 * Main Editor
 * 
 * @author Phillip Beauvoir
 */
public class PropertiesEditor extends EditorPart {
    
    private IFile fFile;
    
    private MainComposite fComposite;
    
    private List<PropertyEntry> fProperties;
    
    private boolean fIsDirty;
    
    private boolean fSaving;
    
    /*
     * Listen to other things changing this resource
     */
    IResourceChangeListener resourceChangeLister = new IResourceChangeListener() {
        public void resourceChanged(IResourceChangeEvent event) {
            IResourceDelta delta = event.getDelta();
            IResourceDelta member = delta.findMember(fFile.getFullPath());
            if(member != null && !fSaving) {
                switch(member.getKind()) {
                    // Something else changed it so reload the file
                    case IResourceDelta.CHANGED:
                        loadFile();
                        fComposite.setProperties(fProperties);
                        fIsDirty = false;
                        firePropertyChange(IEditorPart.PROP_DIRTY);
                        break;

                    // Something else deleted the file
                    case IResourceDelta.REMOVED:
                        Display.getDefault().asyncExec(new Runnable(){
                            @Override
                            public void run() {
                                getEditorSite().getPage().closeEditor(PropertiesEditor.this, false);
                            }
                        });
                        break;
                    
                    default:
                        break;
                }
            }
        }
    };
    
    public PropertiesEditor() {
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        
        if(input instanceof IFileEditorInput) {
            fFile = ((IFileEditorInput)input).getFile();
            loadFile();
            setPartName(fFile.getName());
        }
        
        ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeLister);
    }

    private void loadFile() {
        fProperties = new ArrayList<PropertyEntry>();
        
        try {
            FileReader fr = new FileReader(fFile.getLocation().toFile());
            BufferedReader reader = new BufferedReader(fr);
            String line = null;
            while((line = reader.readLine()) != null) {
                PropertyEntry entry = new PropertyEntry(line);
                fProperties.add(entry);
            }
            reader.close();
        }
        catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        fSaving = true;
        
        try {
            StringBuffer buf = new StringBuffer();
            
            for(int i = 0; i < fProperties.size(); i++) {
                PropertyEntry entry = fProperties.get(i);
                buf.append(entry.getEditedLine());
                if(i < fProperties.size() - 1) {
                    buf.append("\r\n"); //$NON-NLS-1$
                }
            }
            
            InputStream is = new ByteArrayInputStream(buf.toString().getBytes());
            fFile.setContents(is, IResource.KEEP_HISTORY, monitor);
            is.close();
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        finally {
            fSaving = false;
        }
        
        fIsDirty = false;
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    @Override
    public void doSaveAs() {
    }

    @Override
    public boolean isDirty() {
        return fIsDirty;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        fComposite = new MainComposite(parent, SWT.NULL);
        
        fComposite.getTableViewer().getTable().addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                fIsDirty = true;
                firePropertyChange(IEditorPart.PROP_DIRTY);
            }
        });
        
        fComposite.setProperties(fProperties);
    }

    @Override
    public void setFocus() {
        if(fComposite != null) {
            fComposite.setFocus();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(resourceChangeLister);
    }
}
