package ext.hbt.signature;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import wt.fc.Persistable;
import wt.fc.ReferenceFactory;
import wt.util.WTException;

import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ColumnConfig;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.TableConfig;
import com.ptc.mvc.util.ClientMessageSource;
import com.ptc.netmarkets.util.beans.NmCommandBean;
import com.ptc.netmarkets.util.beans.NmHelperBean;

import ext.hbt.reviewobjecttype.model.TypeItem;

 
@ComponentBuilder(value = "hbt.signature.CoordinateItemTable")
public class CoordinateItemTableBuilder extends AbstractComponentBuilder{
      
    private final ClientMessageSource messageSource = getMessageSource(SignatureResource.class.getName());
   
    public CoordinateItemTableBuilder()
    {
    }

	public ComponentConfig buildComponentConfig(ComponentParams componentparams) throws WTException
	{
		ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
        TableConfig tableconfig = componentconfigfactory.newTableConfig();
        tableconfig.setConfigurable(false);
        tableconfig.setLabel(messageSource.getMessage("TableTitle"));
        tableconfig.setId("hbt_signature_CoordinateItem");
        tableconfig.setSelectable(true);
        tableconfig.setShowCount(true);
        tableconfig.setActionModel("hbt_signature_table_action_toolbar");

		ColumnConfig columnconfig= componentconfigfactory.newColumnConfig("hbt_sign_objTypeName", true);
		columnconfig.setLabel(messageSource.getMessage("ObjTypeName"));
		columnconfig.setDataUtilityId("hbt_signature_DataU");
		tableconfig.addComponent(columnconfig);
		columnconfig= componentconfigfactory.newColumnConfig("attrName", true);
		columnconfig.setLabel(messageSource.getMessage("AttrName"));
		columnconfig.setSortable(true);
		tableconfig.addComponent(columnconfig);
		columnconfig = componentconfigfactory.newColumnConfig();
		columnconfig.setId("nmActions");
		columnconfig.setActionModel("hbt_signature_table_row_action");
		tableconfig.addComponent(columnconfig);
		columnconfig= componentconfigfactory.newColumnConfig("attrType", true);
		columnconfig.setLabel(messageSource.getMessage("AttrType"));
		columnconfig.setSortable(true);
		tableconfig.addComponent(columnconfig);
		columnconfig= componentconfigfactory.newColumnConfig("xLocation", true);
		columnconfig.setLabel(messageSource.getMessage("LocationX"));
		columnconfig.setSortable(true);
		tableconfig.addComponent(columnconfig);
		columnconfig = componentconfigfactory.newColumnConfig("yLocation", true);
		columnconfig.setLabel(messageSource.getMessage("LocationY"));
		columnconfig.setSortable(true);
		tableconfig.addComponent(columnconfig);
		columnconfig = componentconfigfactory.newColumnConfig("signRange", true);
		columnconfig.setLabel(messageSource.getMessage("SignRange"));
		columnconfig.setSortable(true);
		tableconfig.addComponent(columnconfig);
		columnconfig= componentconfigfactory.newColumnConfig("fontSize", true);
		columnconfig.setLabel(messageSource.getMessage("FontSize"));
		columnconfig.setSortable(true);
		tableconfig.addComponent(columnconfig);
		columnconfig = componentconfigfactory.newColumnConfig("fontType", true);
		columnconfig.setLabel(messageSource.getMessage("FontType"));
		columnconfig.setSortable(true);
		tableconfig.addComponent(columnconfig);
		columnconfig= componentconfigfactory.newColumnConfig("rotation", true);
		columnconfig.setLabel(messageSource.getMessage("Rotation"));
		columnconfig.setSortable(true);
		tableconfig.addComponent(columnconfig);
		columnconfig= componentconfigfactory.newColumnConfig("contentType", true);
		columnconfig.setLabel(messageSource.getMessage("contentType"));
		columnconfig.setSortable(true);
		tableconfig.addComponent(columnconfig);
		columnconfig= componentconfigfactory.newColumnConfig("imageWidth", true);
		columnconfig.setLabel(messageSource.getMessage("ImageWidth"));
		columnconfig.setSortable(true);
		tableconfig.addComponent(columnconfig);
		columnconfig = componentconfigfactory.newColumnConfig("imageHeight", true);
		columnconfig.setLabel(messageSource.getMessage("ImageHeight"));
		columnconfig.setSortable(true);
		tableconfig.addComponent(columnconfig);
		return tableconfig;
	}

	public List buildComponentData(ComponentConfig componentconfig, ComponentParams componentparams) throws Exception
	{
		List list = new ArrayList();
		NmHelperBean nmhelperbean = ((JcaComponentParams) componentparams).getHelperBean();
		NmCommandBean nmcommandbean = nmhelperbean.getNmCommandBean();
		HttpServletRequest request=nmcommandbean.getRequest();
		String oid = request.getParameter("coid");
		ReferenceFactory referencefactory = new ReferenceFactory();
		Persistable ps =  referencefactory.getReference(oid).getObject();
        if(ps instanceof TypeItem)
        {
        	TypeItem  item =(TypeItem)ps;
        	list=SignatureDataUtil.getCoordinateItems(item);
		}
		return list;
	}


}
