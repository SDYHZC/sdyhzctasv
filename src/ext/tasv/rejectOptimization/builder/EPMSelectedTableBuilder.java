package ext.tasv.rejectOptimization.builder;

import java.util.ArrayList;
import java.util.List;

import wt.change2.ChangeHelper2;
import wt.change2.WTChangeActivity2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.util.WTException;
import wt.workflow.work.WorkItem;

import com.ptc.jca.mvc.components.JcaComponentParams;
import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ColumnConfig;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.TableConfig;
import com.ptc.netmarkets.util.beans.NmCommandBean;

import ext.tasv.rejectOptimization.processor.TableBuilderHelper;

@ComponentBuilder("TZXZBHTable")
public class EPMSelectedTableBuilder extends AbstractComponentBuilder {

	@Override
	public Object buildComponentData(ComponentConfig arg0, ComponentParams arg1)
			throws Exception {
		JcaComponentParams jca_params = (JcaComponentParams) arg1;
		NmCommandBean cb = jca_params.getHelperBean().getNmCommandBean();
		Persistable pbo = (Persistable)cb.getPrimaryOid().getRefObject();
//		String ecn= TableBuilderHelper.getParam(cb, "oid");
		List<Object> results = new ArrayList<Object>();
		if(pbo instanceof WorkItem){
			WorkItem wo = (WorkItem) pbo;
			Persistable per = wo.getPrimaryBusinessObject().getObject();
			if(per instanceof WTChangeOrder2){
				WTChangeOrder2 o = (WTChangeOrder2) per;
				QueryResult qr = ChangeHelper2.service.getChangeActivities(o);
				while(qr.hasMoreElements()){
					WTChangeActivity2 eca = (WTChangeActivity2) qr.nextElement();
					if(eca.getName().contains(o.getNumber())) continue;
					QueryResult qr1 = ChangeHelper2.service.getChangeablesAfter(eca);
					while(qr1.hasMoreElements()){
						results.add(qr1.nextElement());
					}
				}
				return results;
			}else if(per instanceof WTChangeActivity2){
				QueryResult qr1 = ChangeHelper2.service.getChangeablesAfter((WTChangeActivity2)per);
				while(qr1.hasMoreElements()){
					results.add(qr1.nextElement());
				}
				return results;
			}
		}
		return null;
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams arg0)
			throws WTException {
		ComponentConfigFactory factory = getComponentConfigFactory();
		TableConfig tableConfig = factory.newTableConfig();
		tableConfig.setLabel("选择需要驳回的选项");
		tableConfig.setActionModel("rejectOptimizationTab row actions");
		tableConfig.setSelectable(true);
			
		ColumnConfig coluConfig = factory.newColumnConfig("name","名称", true);
		coluConfig.setSortable(true);
		tableConfig.addComponent(coluConfig);
		
		ColumnConfig coluConfig1 = factory.newColumnConfig("number","编号", true);
		coluConfig1.setSortable(true);
		tableConfig.addComponent(coluConfig1);
		
		ColumnConfig coluConfig12 = factory.newColumnConfig("primaryAttachmentProperties","下载", true);
		coluConfig12.setSortable(true);
		coluConfig12.setWidth(80);
		tableConfig.addComponent(coluConfig12);
		
		ColumnConfig coluConfig2 = factory.newColumnConfig("version","版本", true);
		coluConfig2.setSortable(true);
		tableConfig.addComponent(coluConfig2);
		
		ColumnConfig coluConfig3 = factory.newColumnConfig("state","状态", true);
		coluConfig3.setSortable(true);
		tableConfig.addComponent(coluConfig3);
		
		ColumnConfig coluConfig4 = factory.newColumnConfig("checkoutInfo","状况", true);
		coluConfig4.setSortable(true);
		tableConfig.addComponent(coluConfig4);
		return tableConfig;
	}

}
