package ${package};

import java.awt.BorderLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

@TopComponent.Description(
        preferredID = "${object}ViewerTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
        mode = "explorer", 
        openAtStartup = true)
@ActionID(category = "Window", 
        id = "${package}.${object}ViewerTopComponent")
@ActionReference(
        path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_${object}ViewerAction",
        preferredID = "${object}ViewerTopComponent"
)
@Messages({
    "CTL_${object}ViewerAction=${object}Viewer",
    "CTL_${object}ViewerTopComponent=${object}Viewer Window",
    "HINT_${object}ViewerTopComponent=This is a ${object}Viewer window"
})
public class ${object}ViewerTopComponent extends TopComponent implements ExplorerManager.Provider {
    
    private ExplorerManager em = new ExplorerManager();

    public ${object}ViewerTopComponent() {
        setName(Bundle.CTL_${object}ViewerTopComponent());
        setToolTipText(Bundle.HINT_${object}ViewerTopComponent());
        setLayout(new BorderLayout());
        //somehow create your children:
        //Children.create(new MyChildFactory(), true);
        Node rootNode = new AbstractNode(Children.LEAF);
        em.setRootContext(rootNode);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

}