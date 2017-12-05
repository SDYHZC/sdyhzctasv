package ext.tasv.preference;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import wt.change2.ChangeHelper2;
import wt.change2.WTChangeOrder2;
import wt.fc.Persistable;
import wt.fc.QueryResult;
import wt.fc.WTObject;
import wt.inf.library.WTLibrary;
import wt.part.Quantity;
import wt.part.WTPart;
import wt.part.WTPartConfigSpec;
import wt.part.WTPartHelper;
import wt.part.WTPartMaster;
import wt.part.WTPartStandardConfigSpec;
import wt.part.WTPartUsageLink;
import wt.pdmlink.PDMLinkProduct;
import wt.query.SearchCondition;
import wt.type.TypeDefinitionForeignKey;
import wt.type.TypeDefinitionReference;
import wt.util.WTException;
import wt.util.WTProperties;
import wt.vc.views.ViewHelper;



public class ECNUtil
{
	private static WTProperties properties;
	  private static String WT_HOME = null;
	  public static WTPartConfigSpec wtPartStandardConfigSpec;
	  public static String path;
	  public static String wthome;
	  static {
	    try {
	      Properties props = WTProperties.getLocalProperties();
	      WT_HOME = props.getProperty("wt.home");
	      WTProperties wtproperties = WTProperties.getLocalProperties();
		  wthome = wtproperties.getProperty("wt.home");
		  path = wthome + File.separator + "codebase/temp" + File.separator;
		  wtPartStandardConfigSpec = WTPartConfigSpec.newWTPartConfigSpec(WTPartStandardConfigSpec
				.newWTPartStandardConfigSpec(ViewHelper.service.getView("Design"), null));
	    } catch (Throwable throwable) {
	    	throw new ExceptionInInitializerError(throwable);
	    }
	  }
  /*
   * 判断是否存在非优选标准件
   * 判断标准是查找非优选标准件库中是否存在编号为部件编号的零件
	*/
  public static String hasNoPreferencePart(WTObject obj)
  {
    String str = "";
    try {
      if ((obj instanceof WTChangeOrder2)) {
        WTChangeOrder2 ecn = (WTChangeOrder2)obj;
        QueryResult qr = ChangeHelper2.service.getChangeablesAfter(ecn, true);
        while (qr.hasMoreElements()) {
          Persistable pobj = (Persistable)qr.nextElement();
          if ((pobj instanceof WTPart)) {
        	  WTPart pt = (WTPart) pobj;
        	  List<WTPart> ptList= Select.from(WTPart.class).andFrom(WTLibrary.class)
	                    .join(0,WTPart.CONTAINER_REFERENCE,1)
	                    .where(0,WTPart.NUMBER,SearchCondition.EQUAL,pt.getNumber())
	                    .where(1,WTLibrary.NAME,SearchCondition.EQUAL,"非优选标准件库")
	                    .onlyLatest()
	                    .list();
        	  System.out.println("zyj--ts--ptList:"+ptList);
        	  if(ptList!=null && ptList.size()>0){
        		  for(int i = 0 ; i < ptList.size() ; i++){
        			  WTPart part  = ptList.get(i);
        			  str += part.getNumber()+"、";
        		  }
        	  }
        	  str = getAllPart(pt,str);
          }
        }
        System.out.println("zyj--ts--str1:"+str);
        if (str!=null&&!"".equals(str)) 
		{
        	System.out.println("zyj--ts--come in str:"+str);
			str=str.substring(0, str.length()-1);
			return str;
			
		}
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    return str;
  }
  public static String getAllPart(WTPart topPart,String str) throws WTException
	{
		QueryResult subNodes = WTPartHelper.service.getUsesWTParts(topPart, wtPartStandardConfigSpec);
		while (subNodes.hasMoreElements())
		{
			Persistable aSubNodePair[] = (Persistable[]) subNodes.nextElement();
			if (aSubNodePair[1] instanceof WTPart) 
			{
				WTPart childpart = (WTPart) aSubNodePair[1];
				//物料编码
				String partNum = childpart.getNumber();
				
				WTPartUsageLink usagelink = (WTPartUsageLink) aSubNodePair[0];
				Quantity localQuantity = usagelink.getQuantity();
				//数量
				double count= localQuantity.getAmount();
				List<WTPart> ptList= Select.from(WTPart.class).andFrom(WTLibrary.class)
	                    .join(0,WTPart.CONTAINER_REFERENCE,1)
	                    .where(0,WTPart.NUMBER,SearchCondition.EQUAL,childpart.getNumber())
	                    .where(1,WTLibrary.NAME,SearchCondition.EQUAL,"非优选标准件库")
	                    .onlyLatest()
	                    .list();
				System.out.println("zyj--ts--childpartList.size1:"+ptList.size());
      	    if(ptList!=null && ptList.size()>0){
      		  for(int i = 0 ; i < ptList.size() ; i++){
      			  WTPart part  = ptList.get(i);
      			  str += part.getNumber()+"、";
      		  }
      	     }
      	  System.out.println("zyj--ts--str2:"+str);
				getAllPart(childpart,str);
			}
			else if (aSubNodePair[1] instanceof WTPartMaster) 
			{
				continue;
			}
		}
		return str;
	}
  /*
   * 判断是否是新数据
   * 首先与总成件相对比，-前面的部件与总成件-前面的相同，并且-后面的两位也被总成件包含，即为新数据，否则为旧数据
   * 
   * */
  public static void isNewData(WTPart topPart,WTPart part)
  {
    String str = "";
    try {
       String topName = topPart.getName();
       String partName = part.getName();
       String topGBStr = "";
       String topGAStr = "";
       if(topName.indexOf("-")>-1){
    	   topGBStr  = topName.substring(0,topName.indexOf("-"));
       }
      }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}