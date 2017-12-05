package ext.hbt.historydata;

/**
 * 处理excel
 * @author yaoy
 *
 */

public class ExcelData {
	
	private String epmnumber;	 //CAD文档编号
		
	public ExcelData(){
		
	}
	
	public ExcelData(String epmnumber)
	{		
		this.epmnumber = epmnumber;
	}

	public String getEpmnumber() {
		return epmnumber;
	}

	public void setEpmnumber(String epmnumber) {
		this.epmnumber = epmnumber;
	}
			
}
