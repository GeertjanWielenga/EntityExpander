package ${package};

import java.beans.IntrospectionException;
import java.util.List;
import javax.swing.Action;
import org.openide.nodes.BeanNode;
import org.openide.util.Utilities;

public class ${object}BeanNode extends BeanNode {
    
    public ${object}BeanNode(${object} bean) throws IntrospectionException {
        super(bean);
        //setDisplayName();
        //setShortDescription();
    }

    @Override
    public Action[] getActions(boolean context) {
        List<? extends Action> ${object}Actions = 
            Utilities.actionsForPath("Actions/${object}");
        return ${object}Actions.toArray(new Action[${object}Actions.size()]);
    }

}