package ${package};

import java.beans.IntrospectionException;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

public class ${object}BeanChildFactory extends ChildFactory<${object}> {
    
    @Override
    protected boolean createKeys(List<${object}> toPopulate) {
        //connect to data source
        //and populate "toPopulate" list
        return true;
    }
    
    @Override
    protected Node createNodeForKey(${object} key) {
        ${object}BeanNode cbn = null;
        try {
            cbn = new ${object}BeanNode(key);
        } catch (IntrospectionException ex) {
            Exceptions.printStackTrace(ex);
        }
        return cbn;
    }
    
}