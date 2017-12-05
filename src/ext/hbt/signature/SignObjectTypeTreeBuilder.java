package ext.hbt.signature;

import wt.util.WTException;

import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ColumnConfig;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.TreeConfig;
import com.ptc.mvc.util.ClientMessageSource;

import ext.hbt.reviewobjecttype.ObjectTypeResource;

 
@ComponentBuilder(value = "hbt.signature.signObjectType")
public class SignObjectTypeTreeBuilder extends AbstractComponentBuilder{
      
    private final ClientMessageSource messageSource = getMessageSource(ObjectTypeResource.class.getName());
    private final ClientMessageSource messageSource2 = getMessageSource(SignatureResource.class.getName());

    public SignObjectTypeTreeBuilder()
    {
    }

	public ComponentConfig buildComponentConfig(ComponentParams componentparams) throws WTException
	{
		ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
		TreeConfig treeconfig = componentconfigfactory.newTreeConfig();
		treeconfig.setLabel(messageSource2.getMessage("TypeMaintainTree"));
		treeconfig.setId("hbt_signObjectType_Tree");
		treeconfig.setSelectable(true);
		treeconfig.setShowCount(true);
		treeconfig.setExpansionLevel("full");
		treeconfig.setActionModel("hbt_signObjectType_action_toolbar");
		
		ColumnConfig columnconfig = componentconfigfactory.newColumnConfig("hbt_sign_objTypeName", true);
		columnconfig.setLabel(messageSource.getMessage("TypeNameLabel"));
		columnconfig.setDataUtilityId("hbt_signature_DataU");
		treeconfig.addComponent(columnconfig);
		
		columnconfig = componentconfigfactory.newColumnConfig();
		columnconfig.setId("nmActions");
		columnconfig.setActionModel("hbt_signObjectType_row_action");
		treeconfig.addComponent(columnconfig);
		
		columnconfig = componentconfigfactory.newColumnConfig("fullTypeName", true);
		columnconfig.setDataUtilityId("Maintain_ObjectType_DataU");
		columnconfig.setLabel(messageSource.getMessage("TypeFullNameLabel"));
		treeconfig.addComponent(columnconfig); 
		
		columnconfig = componentconfigfactory.newColumnConfig("typecomment", true);
		columnconfig.setLabel(messageSource.getMessage("commentLabel"));
		treeconfig.addComponent(columnconfig);
		return treeconfig;
	}

    public  SignObjectTypeTreeHandler buildComponentData(ComponentConfig componentconfig, ComponentParams componentparams)
        throws WTException{
        return new  SignObjectTypeTreeHandler();
    }


}
