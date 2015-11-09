package org.netbeans.modules.entityexpander;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.TreePathScanner;
import java.awt.event.ActionEvent;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

@ActionID(
        category = "Tools",
        id = "org.tc.customizer.ExpandEntityAction")
@ActionRegistration(
        displayName = "Generate...",
        lazy = false)
@ActionReferences({
    @ActionReference(path = "Projects/package/Actions", position = 0),
    @ActionReference(path = "Loaders/text/x-java/Actions", position = 150)
})
public final class ExpandEntityAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    private final DataObject dobj;
    private static Map args = new HashMap();

    public ExpandEntityAction() {
        this(Utilities.actionsGlobalContext());
    }

    public ExpandEntityAction(Lookup context) {
        super("Generate...");
        putValue("expansion", true);
        this.dobj = context.lookup(DataObject.class);
        //Enable the menu item only if we're dealing with a TopComponent
        FileObject fo = dobj.getPrimaryFile();
        if (fo != null) {
            JavaSource javaSource = JavaSource.forFileObject(fo);
            if (javaSource != null) {
                try {
                    javaSource.runUserActionTask(new ScanForObject(this), true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        //Hide the menu item if it isn't enabled:
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ExpandEntityAction(actionContext);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu templateMenu = new JMenu("Entity Expanders");
        final FileObject entityTemplatesFolder = FileUtil.getConfigFile("Templates/Entities");
        for (final FileObject oneEntityTemplate : FileUtil.getOrder(Arrays.asList(entityTemplatesFolder.getChildren()), false)) {
//            String displayName = oneEntityTemplate.getAttribute("displayName").toString();
            JMenuItem jmi = new JMenuItem(oneEntityTemplate.getName());
            jmi.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (dobj.getPrimaryFile().isFolder()) {
                            FileObject packageFo = dobj.getPrimaryFile();
                            for (FileObject fo : packageFo.getChildren()) {
                                if (fo.getMIMEType().equals("text/x-java")) {
                                    generateTemplate(DataObject.find(fo));
                                }
                            }
                        } else {
                            generateTemplate(dobj);
                        }
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                private void generateTemplate(DataObject d) throws DataObjectNotFoundException, IOException {
                    DataObject formDobj = DataObject.find(oneEntityTemplate);
                    DataFolder df = DataFolder.findFolder(d.getPrimaryFile().getParent());
                    String pojoName = d.getPrimaryFile().getName();
                    String targetName = oneEntityTemplate.getName();
                    JavaSource javaSource = JavaSource.forFileObject(d.getPrimaryFile());
                    if (javaSource != null) {
                        try {
                            javaSource.runUserActionTask(new ScanForObject(ExpandEntityAction.this), true);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    formDobj.createFromTemplate(df, pojoName + targetName, args);
                }
            });
            templateMenu.add(jmi);
        }
        templateMenu.add(new JSeparator());
        templateMenu.add(new AbstractAction("Create...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    NotifyDescriptor.InputLine inputLine = new NotifyDescriptor.InputLine(
                            "Name",
                            "Create Template");
                    Object notify = DialogDisplayer.getDefault().notify(inputLine);
                    if (notify == NotifyDescriptor.OK_OPTION) {
                        String inputText = inputLine.getInputText().trim().replaceAll(" ", "");
                        FileObject customTemplate = entityTemplatesFolder.createData(inputText, "java");
                        customTemplate.setAttribute("displayName", inputText);
                        customTemplate.setAttribute("template", true);
                        customTemplate.setAttribute("javax.script.ScriptEngine", "freemarker");
                        customTemplate.setAttribute("templateCategory", "java-classes");
                        writeTemplate(customTemplate);
                        DataObject customTemplateDobj = DataObject.find(customTemplate);
                        customTemplateDobj.getLookup().lookup(Openable.class).open();
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        if (this.isEnabled()) {
            return templateMenu;
        } else {
            return null;
        }
    }

    private void writeTemplate(FileObject obj) {
        FileLock fileLock = null;
        OutputStreamWriter osw;
        try {
            fileLock = obj.lock();
            OutputStream fout = obj.getOutputStream(fileLock);
            OutputStream bout = new BufferedOutputStream(fout);
            osw = new OutputStreamWriter(bout, "UTF-8");
            osw.write("package ${package};\n"
                    + "\n"
                    + "import javax.swing.JFrame;\n"
                    + "\n"
                    + "public class ${object}Frame extends JFrame {\n"
                    + "    \n"
                    + "  <#list fieldsAndModifiers as field>\n"
                    + "     ${field};\n"
                    + "  </#list>\n"
                    + "\n"
                    + "    public ${object}Frame() {\n"
                    + "    }\n"
                    + "    \n"
                    + "}");
            osw.flush();
            osw.close();
        } catch (IOException ex) {
        } finally {
            if (fileLock != null) {
                fileLock.releaseLock();
            }
        }
    }

    private static class ScanForObject implements Task<CompilationController> {

        private final ExpandEntityAction action;

        private ScanForObject(ExpandEntityAction action) {
            this.action = action;
        }

        @Override
        public void run(CompilationController compilationController) throws Exception {
            compilationController.toPhase(Phase.ELEMENTS_RESOLVED);
            CompilationUnitTree unit = compilationController.getCompilationUnit();
                new MemberVisitor(compilationController, action).scan(unit, null);
        }
    }

    private static class MemberVisitor extends TreePathScanner<Void, Void> {

        private final CompilationInfo info;
        private final AbstractAction action;

        public MemberVisitor(CompilationInfo info, AbstractAction action) {
            this.info = info;
            this.action = action;
        }

        @Override
        public Void visitClass(ClassTree t, Void v) {
            Element el = info.getTrees().getElement(getCurrentPath());
            if (el != null) {
                TypeElement te = (TypeElement) el;
                if (te.getKind() == ElementKind.CLASS && te.getModifiers().contains(Modifier.PUBLIC)) {
                    action.setEnabled(true);
                    int index = te.getQualifiedName().toString().lastIndexOf(".");
                    args.put("package", te.getQualifiedName().toString().substring(0, index));
                    args.put("object", te.getQualifiedName().toString().substring(index + 1));
                    List<String> fields = new ArrayList<String>();
                    List<Element> fieldElems = new ArrayList<Element>();
                    for (Element e : te.getEnclosedElements()) {
                        if (e.getKind() == ElementKind.FIELD
                                && !e.getModifiers().contains(Modifier.STATIC)) {
                            fields.add(e.getSimpleName().toString());
                            fieldElems.add(e);
                            List<? extends AnnotationMirror> annotationMirrors = e.getAnnotationMirrors();
                            for (AnnotationMirror annotationMirror : annotationMirrors) {
                                String str = annotationMirror.toString();
                                if (str.equals("@javax.persistence.Id")) {
                                    // In case its a JPA id field, special arg for that
                                    args.put("idfield", e);
                                }
                            }
                        }
                    }
                    args.put("fields", fields);
                    args.put("fieldElems", fieldElems);
                    List<String> fieldsAndModifiers = new ArrayList<String>();
                    for (Element e : te.getEnclosedElements()) {
                        if (e.getKind() == ElementKind.FIELD) {
                            Modifier next;
                            if (e.getModifiers().iterator().hasNext()) {
                                next = e.getModifiers().iterator().next();
                            } else {
                                next = Modifier.PRIVATE;
                            }
                            fieldsAndModifiers.add(
                                    next
                                    + " "
                                    + e.asType().toString()
                                    + " "
                                    + e.getSimpleName().toString()
                            );
                        }
                    }
                    args.put("fieldsAndModifiers", fieldsAndModifiers);
//                    List<String> methods = new ArrayList<String>();
//                    for (Element e : te.getEnclosedElements()) {
//                        if (e.getKind() == ElementKind.METHOD) {
//                            methods.add(e.getModifiers().iterator().next().toString() + " " + e.asType().toString() + " " + e.getSimpleName().toString() + "() {\n"
//                                    + "    }");
//                        }
//                    }
//                    args.put("methods", methods);
                } else {
                    action.setEnabled(false);
                }
            }
            return null;
        }
    }
}
