/*
 * PublicPrint.java  2006/11/17
 * 
 * Copyright 2005 Foxconn All rights reserved.
 */

package ext.yhzc.tools;

import java.util.Vector;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * @author XieHongbo
 * 
 * SIDC KMS PDM Team
 */
public class PublicLogger
{
	private boolean bPrint = false;
	private boolean blog = false;
	private String strAbsolutePath = null;
	public void println(String location, int object)
	{
		if (bPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}

		if (blog == true && strAbsolutePath != null && strAbsolutePath.length() > 0)
		{
			String content = (new Date(System.currentTimeMillis())).toLocaleString() + ": " + location + ": " + object + "\n";
			outputlog(strAbsolutePath, content);
		}
	}
	public void println(String location, boolean object)
	{
		if (bPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}

		if (blog == true && strAbsolutePath != null && strAbsolutePath.length() > 0)
		{
			String content = (new Date(System.currentTimeMillis())).toLocaleString() + ": " + location + ": " + object + "\n";
			outputlog(strAbsolutePath, content);
		}
	}
	public void println(String location, char object)
	{
		if (bPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}

		if (blog == true && strAbsolutePath != null && strAbsolutePath.length() > 0)
		{
			String content = (new Date(System.currentTimeMillis())).toLocaleString() + ": " + location + ": " + object + "\n";
			outputlog(strAbsolutePath, content);
		}
	}
	public void println(String location, char[] object)
	{
		if (bPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object.toString());
		}

		if (blog == true && strAbsolutePath != null && strAbsolutePath.length() > 0)
		{
			String content = (new Date(System.currentTimeMillis())).toLocaleString() + ": " + location + ": " + object + "\n";
			outputlog(strAbsolutePath, content);
		}
	}
	public void println(String location, double object)
	{
		if (bPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}

		if (blog == true && strAbsolutePath != null && strAbsolutePath.length() > 0)
		{
			String content = (new Date(System.currentTimeMillis())).toLocaleString() + ": " + location + ": " + object + "\n";
			outputlog(strAbsolutePath, content);
		}
	}

	public void println(String location, float object)
	{
		if (bPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}

		if (blog == true && strAbsolutePath != null && strAbsolutePath.length() > 0)
		{
			String content = (new Date(System.currentTimeMillis())).toLocaleString() + ": " + location + ": " + object + "\n";
			outputlog(strAbsolutePath, content);
		}
	}

	public void println(String location, long object)
	{
		if (bPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}

		if (blog == true && strAbsolutePath != null && strAbsolutePath.length() > 0)
		{
			String content = (new Date(System.currentTimeMillis())).toLocaleString() + ": " + location + ": " + object + "\n";
			outputlog(strAbsolutePath, content);
		}
	}

	public void println(String location, Object object)
	{
		if (bPrint == true)
		{
			if (object != null)
			{
				System.out.println(location + ":Print for test==>" + object);
			}
		}

		if (blog == true && strAbsolutePath != null && strAbsolutePath.length() > 0)
		{
			String content = (new Date(System.currentTimeMillis())).toLocaleString() + ": " + location + ": " + object + "\n";
			outputlog(strAbsolutePath, content);
		}
	}
	public void println(String location, String object)
	{
		if (bPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}

		if (blog == true && strAbsolutePath != null && strAbsolutePath.length() > 0)
		{
			String content = (new Date(System.currentTimeMillis())).toLocaleString() + ": " + location + ": " + object + "\n";
			outputlog(strAbsolutePath, content);
		}
	}
	//------------------------------------------------------------------------------------------------------
	public void println(String location, int object, boolean privateBPrint)
	{
		if (privateBPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}
	}
	public void println(String location, boolean object, boolean privateBPrint)
	{
		if (privateBPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}
	}
	public void println(String location, char object, boolean privateBPrint)
	{
		if (privateBPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}
	}
	public void println(String location, char[] object, boolean privateBPrint)
	{
		if (privateBPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object.toString());
		}
	}
	public void println(String location, double object, boolean privateBPrint)
	{
		if (privateBPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}
	}

	public void println(String location, float object, boolean privateBPrint)
	{
		if (privateBPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}
	}

	public void println(String location, long object, boolean privateBPrint)
	{
		if (privateBPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}
	}

	public void println(String location, Object object, boolean privateBPrint)
	{
		if (privateBPrint == true)
		{
			if (object != null)
			{
				System.out.println(location + ":Print for test==>" + object);
			}
		}
	}
	public void println(String location, String object, boolean privateBPrint)
	{
		if (privateBPrint == true)
		{
			System.out.println(location + ":Print for test==>" + object);
		}
	}
	//------------------------------------------------------------------------------------------------------
	//  鍙冩暩鍒嗘瀽:strAbsolutePath鐐篖og鐨勫叏璺緫鍖呮嫭logName鏂囦欢鍚�writedcontent鐐鸿瀵叆鐨凩og淇℃伅,姣忔淇℃伅浠�\n"绲愬熬,琛ㄧず鎻涜.
	//  鍔熻兘鎻忚堪:灏囨墦鍗颁俊鎭鍏ュ埌鐗瑰畾鐨勮矾寰戠殑鏂囦欢涓�
	//  瑾跨敤浣嶇疆:鏈湴鏂规硶PublicLogger.print();
	//  浣跨敤鑸変緥:String strAbsolutePath = "D:/test.log";
	//           outputlog(strAbsolutePath,"Just test!\n");
	public void outputlog(String strAbsolutePath, String writedcontent)
	{
		try
		{
			if (strAbsolutePath != null && strAbsolutePath.length() > 0)
			{
				int intPosition = strAbsolutePath.lastIndexOf("/");
				if(intPosition==-1)
				{
					intPosition = strAbsolutePath.lastIndexOf("\\");
				}
				String writedfilepath = strAbsolutePath.substring(0, intPosition + 1);
				String logName = strAbsolutePath.substring(intPosition + 1);
				File file1 = new File(writedfilepath);
				if (!file1.exists())
				{
					file1.mkdirs(); //濡傛灉涓嶅瓨鍦�鍓囪嚜鍕曠敓鎴�
				}
				String strwritedfile = writedfilepath + logName;
				//瑷偤true,鏂囦欢涓殑鍏у鐐虹疮鍔�false鍓囨槸姣忔娓呯┖,鍐嶅鍏�
				BufferedWriter out = new BufferedWriter(new FileWriter(strwritedfile, true));
				out.write(writedcontent);
				out.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("writelog Exception in PublicLogger!!");
		}

	}
	public boolean isBPrint()
	{
		return bPrint;
	}

	public void setBPrint(boolean b)
	{
		bPrint = b;
	}

	public void setBlog(boolean b)
	{
		blog = b;
	}

	public boolean isBlog()
	{
		return blog;
	}

	public String getStrAbsolutePath()
	{
		return strAbsolutePath;
	}

	public void setStrAbsolutePath(String string)
	{
		strAbsolutePath = string;
	}
	public void test()
	{
		PublicLogger Logger = new PublicLogger();
		Logger.setBPrint(false);
		Logger.println("test0", "test00000000000");
	}
	public void test1()
	{
		PublicLogger Logger = new PublicLogger();
		Logger.setBPrint(false);
		Logger.println("test1", "test111111111");
	}
	public static void main(String[] args)
	{
		PublicLogger Logger = new PublicLogger();
		Logger.setBPrint(true);
		Logger.setBlog(true);
		Logger.test();
		Logger.test1();
		String writedfile = "D:/test.log";
		Logger.setStrAbsolutePath(writedfile);
		Vector aa = new Vector();
		aa.add("xx");
		aa.add("cc");
		aa.add("ee");
		int cc = 9;
		Logger.println("int cc", cc);
		Logger.println("Vector aa", aa);

	}
}
