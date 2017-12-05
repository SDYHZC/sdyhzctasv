/* bcwti
 *
 * Copyright (c) 2010 Parametric Technology Corporation (PTC). All Rights Reserved.
 *
 * This software is the confidential and proprietary information of PTC
 * and is subject to the terms of a software license agreement. You shall
 * not disclose such confidential information and shall use it only in accordance
 * with the terms of the license agreement.
 *
 * ecwti
 */
package ext.tan.partreport;

import wt.util.resource.RBEntry;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;

@RBUUID("ext.tan.partreport.ReportResource")
public final class ReportResource_zh_CN extends WTListResourceBundle 
{   
	@RBEntry("定制报表")
    public static final String PRIVATE_CONSTANT_1 = "tan.tanCustGroupGWT.description";

	@RBEntry("航天泰安特种车报表")
    public static final String PRIVATE_CONSTANT_2 = "tan.tanReportGWT.description";
    @RBEntry("航天泰安特种车报表")
    public static final String PRIVATE_CONSTANT_3 = "tan.tanReportGWT.tooltip";
    @RBEntry("export_32x32.png")
    public static final String PRIVATE_CONSTANT_4 = "tan.tanReportGWT.icon";

    //标准件汇总表
    @RBEntry("标准件汇总表")
    public static final String PRIVATE_CONSTANT_ACE1 = "tan.exportStandardPartSummary.description";
    @RBEntry("标准件汇总表")
    public static final String PRIVATE_CONSTANT_ACE2 = "tan.exportStandardPartSummary.title";
    @RBEntry("标准件汇总表")
    public static final String PRIVATE_CONSTANT_ACE3 = "tan.exportStandardPartSummary.tooltip";
    
    //整 车 明 细 表
    @RBEntry("整车明细表")
    public static final String PRIVATE_CONSTANT_PD1 = "tan.exportProductDetailsSummary.description";
    @RBEntry("整车明细表")
    public static final String PRIVATE_CONSTANT_PD2 = "tan.exportProductDetailsSummary.title";
    @RBEntry("整车明细表")
    public static final String PRIVATE_CONSTANT_PD3 = "tan.exportProductDetailsSummary.tooltip";
    
    //外购件汇总表
    @RBEntry("外购件汇总表")
    public static final String PRIVATE_CONSTANT_OSC1 = "tan.exportOutsourcingPartSummary.description";
    @RBEntry("外购件汇总表")
    public static final String PRIVATE_CONSTANT_OSC2 = "tan.exportOutsourcingPartSummary.title";
    @RBEntry("外购件汇总表")
    public static final String PRIVATE_CONSTANT_OSC3 = "tan.exportOutsourcingPartSummary.tooltip";

    //分组明细表
    @RBEntry("分组明细表")
    public static final String PRIVATE_CONSTANT_GD1 = "tan.exportGroupingDetailsSummary.description";
    @RBEntry("分组明细表")
    public static final String PRIVATE_CONSTANT_GD2 = "tan.exportGroupingDetailsSummary.title";
    @RBEntry("分组明细表")
    public static final String PRIVATE_CONSTANT_GD3 = "tan.exportGroupingDetailsSummary.tooltip";

    //分组明细表（三维）
    @RBEntry("分组明细表（三维）")
    public static final String PRIVATE_CONSTANT_GD1TW = "tan.exportGroupingDetailsSummaryTW.description";
    @RBEntry("分组明细表（三维）")
    public static final String PRIVATE_CONSTANT_GD2TW = "tan.exportGroupingDetailsSummaryTW.title";
    @RBEntry("分组明细表（三维）")
    public static final String PRIVATE_CONSTANT_GD3TW = "tan.exportGroupingDetailsSummaryTW.tooltip";
    
    //CAPP报表
    @RBEntry("CAPP报表")
    public static final String PRIVATE_CONSTANT_CAPP1 = "tan.psbReportMultiLevelCAPPBOM.description";
    @RBEntry("CAPP报表")
    public static final String PRIVATE_CONSTANT_CAPP2 = "tan.psbReportMultiLevelCAPPBOM.title";
    @RBEntry("CAPP报表")
    public static final String PRIVATE_CONSTANT_CAPP3 = "tan.psbReportMultiLevelCAPPBOM.tooltip"; 
    
    //分组明细表
    @RBEntry("分组明细表(特殊结构)")
    public static final String PRIVATE_CONSTANT_GDU1 = "tan.exportGroupingDetailsSpecialSummary.description";
    @RBEntry("分组明细表(特殊结构)")
    public static final String PRIVATE_CONSTANT_GDU2 = "tan.exportGroupingDetailsSpecialSummary.title";
    @RBEntry("分组明细表(特殊结构)")
    public static final String PRIVATE_CONSTANT_GDU3 = "tan.exportGroupingDetailsSpecialSummary.tooltip";
    
}
