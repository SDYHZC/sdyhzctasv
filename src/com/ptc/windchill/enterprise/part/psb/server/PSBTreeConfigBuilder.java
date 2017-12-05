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
package com.ptc.windchill.enterprise.part.psb.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import wt.session.SessionHelper;
import wt.util.InstalledProperties;
import wt.util.WTException;
import wt.util.WTMessage;

import com.extjs.gxt.ui.client.Style.ButtonScale;
import com.google.gwt.gen2.logging.shared.Log;
import com.ptc.cat.assocnav.client.NavConstants;
import com.ptc.cat.assocnav.server.params.PartAlternatePartsParams;
import com.ptc.cat.assocnav.server.params.PartBuildCADDocsParams;
import com.ptc.cat.assocnav.server.params.PartCADDocsParams;
import com.ptc.cat.assocnav.server.params.PartDescribeCADDocsParams;
import com.ptc.cat.assocnav.server.params.PartDescribeDocsParams;
import com.ptc.cat.assocnav.server.params.PartDistributionTargetsParams;
import com.ptc.cat.assocnav.server.params.PartDownStreamAllocationParams;
import com.ptc.cat.assocnav.server.params.PartDownStreamTraceParams;
import com.ptc.cat.assocnav.server.params.PartReferenceDocsParams;
import com.ptc.cat.assocnav.server.params.PartSubstitutePartsParams;
import com.ptc.cat.assocnav.server.params.PartUpStreamAllocationParams;
import com.ptc.cat.assocnav.server.params.PartUpStreamTraceParams;
import com.ptc.cat.assocnav.server.params.groups.CADDocumentGroupParams;
import com.ptc.cat.assocnav.server.params.groups.DistributionTargetGroupParams;
import com.ptc.cat.assocnav.server.params.groups.DocumentGroupParams;
import com.ptc.cat.assocnav.server.params.groups.ReplacementPartGroupParams;
import com.ptc.cat.assocnav.server.params.groups.RequirementGroupParams;
import com.ptc.cat.assocnav.server.params.groups.RootReplacementPartGroupParams;
import com.ptc.cat.config.client.ActionConfig;
import com.ptc.cat.config.client.AttributeConfig;
import com.ptc.cat.config.client.DefaultActionConfig;
import com.ptc.cat.config.client.DefaultAttributeConfig;
import com.ptc.cat.config.client.DefaultColumnConfig;
import com.ptc.cat.config.client.DefaultRelatedObjsConfig;
import com.ptc.cat.config.client.DefaultTableConfig;
import com.ptc.cat.config.client.RelatedObjsConfig;
import com.ptc.cat.config.client.TableConfig;
import com.ptc.cat.config.client.ActionConfig.ButtonType;
import com.ptc.cat.config.server.AbstractComponentConfigBuilder;
import com.ptc.cat.config.server.AbstractPartStructureTableView;
import com.ptc.cat.config.server.columnLabelsRB;
import com.ptc.cat.config.server.relatedObjLabelsRB;
import com.ptc.cat.plm.client.PLMEntity;
import com.ptc.core.htmlcomp.jstable.JSPropertyDataConstants;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentBuilderType;


@ComponentBuilder(value="PSB.tree", type=ComponentBuilderType.CONFIG_ONLY)
public class PSBTreeConfigBuilder extends AbstractComponentConfigBuilder
{
    protected static final String RESOURCE = "com.ptc.cat.config.server.columnLabelsRB";

    protected String getId()
    {
        return "PSB.tree";
    }

    protected String getTableID()
    {
        return "PSBTree";
    }

    protected String getContextMenuActionModelID()
    {
        return "psbRelatedPartsTreeContextMenu";
    }

    protected String getToolBarActionModelID()
    {
        return "psbRelatedPartsTreeToolBar";
    }
    protected String getIndicatorActionModelID()
    {
        return "psbIndicatorActionModel";
    }

    protected TableConfig getTableConfig()
    {
        return null;
    }

    @Override
    protected List<String> getActiveNavigationTypes()
    {
        List<String> navigation_types = new ArrayList<String>();
        navigation_types.add(NavConstants.PART_USES_NAV_CONSTANT);
        return navigation_types;
    }

    @Override
    protected List<String> getInActiveNavigationTypes()
    {
        List<String> navigation_types = new ArrayList<String>();
        navigation_types.add(NavConstants.PART_USES_OCCURRENCE_NAV_CONSTANT);
        navigation_types.add(NavConstants.PART_PATH_OCCURRENCE_NAV_CONSTANT);
        return navigation_types;
    }

    @Override
    protected List<String> getOptionalNavigationTypes()
    {
        List<String> navigation_types = new ArrayList<String>();

        navigation_types.add(PartReferenceDocsParams.PART_REFERENCE_DOCS_NAV_CONSTANT);
        navigation_types.add(PartDescribeDocsParams.PART_DESCRIBE_DOCS_NAV_CONSTANT);

        navigation_types.add(DocumentGroupParams.GROUP_DOCUMENT_NAV_CONSTANT);

        //navigation_types.add(PartCADDocsParams.PART_CAD_DOCS_NAV_CONSTANT);
        navigation_types.add(NavConstants.FILTERED_PART_BUILD_HISTORY_EPMDOCS);
        navigation_types.add(PartDescribeCADDocsParams.PART_DESCRIBE_CAD_DOCS_NAV_CONSTANT);
        //navigation_types.add(PartBuildCADDocsParams.PART_BUILD_CAD_DOCS_NAV_CONSTANT);
        navigation_types.add(NavConstants.FILTERED_PART_BUILD_RULE_EPMDOCS);

        navigation_types.add(CADDocumentGroupParams.GROUP_CAD_DOCUMENT_NAV_CONSTANT);

        navigation_types.add(NavConstants.PART_ALTERNATE_PARTS_NAV_CONSTANT);
        navigation_types.add(NavConstants.PART_SUBSTITUTE_PARTS_NAV_CONSTANT);

        navigation_types.add(ReplacementPartGroupParams.GROUP_REPLACEMENT_PART_NAV_CONSTANT);
        navigation_types.add(RootReplacementPartGroupParams.GROUP_ROOT_REPLACEMENT_PART_NAV_CONSTANT);

        boolean bESIInstalled = InstalledProperties.isInstalled( InstalledProperties.ESI );
        if (bESIInstalled) {
            navigation_types.add(PartDistributionTargetsParams.PART_DISTRIBUTION_TARGETS_NAV_CONSTANT);

            navigation_types.add(DistributionTargetGroupParams.GROUP_DISTRIBUTION_TARGET_NAV_CONSTANT);
        } // end if bESI

        boolean bREQLInstalled = InstalledProperties.isInstalled( InstalledProperties.REQL);
        if (bREQLInstalled) {
            navigation_types.add(PartDownStreamTraceParams.PART_DOWN_TRACE_NAV_CONSTANT);
            navigation_types.add(PartUpStreamTraceParams.PART_UP_TRACE_NAV_CONSTANT);
            navigation_types.add(PartDownStreamAllocationParams.PART_DOWN_ALLOCATION_NAV_CONSTANT);
            navigation_types.add(PartUpStreamAllocationParams.PART_UP_ALLOCATION_NAV_CONSTANT);

            navigation_types.add(RequirementGroupParams.GROUP_REQUIREMENT_NAV_CONSTANT);
        } // end if bREQL
        return navigation_types;
    }

    @Override
    protected DefaultTableConfig getTableConfigOverride()
    {
        DefaultTableConfig table_config = new DefaultTableConfig();
        table_config.setAutoExpandColumn("Child:identityBuilder");
        table_config.setAutoExpandMin(100);
        table_config.setAutoExpandMax(1000);
        table_config.setHideHeaders(false);
        table_config.setEnableColumnResize(true);
        return table_config;
    }

    @Override
    protected Set<DefaultColumnConfig> getColumnConfigOverrides(Locale locale)
    {
        Set<DefaultColumnConfig> column_config_overrides = new HashSet<DefaultColumnConfig>();
        column_config_overrides.add(getNameColumnConfig());
        column_config_overrides.add(getNumberColumnConfig());
        column_config_overrides.add(getCheckOutStatusColumnConfig());
        column_config_overrides.add(getSharedStatusColumnConfig());
        //column_config_overrides.add(getNoteStatusColumnConfig());
        column_config_overrides.add(getReplacementStatusColumnConfig());
        column_config_overrides.add(getVersionColumnConfig());
        column_config_overrides.add(getReferenceDesignatorColumnConfig(locale));
        column_config_overrides.add(getUsageQuantityColumnConfig(locale));
        column_config_overrides.add(getOrganizationColumnConfig(locale));
        column_config_overrides.add(getChangeStatusColumnConfig());
        column_config_overrides.add(getUsedQuantityAmountColumnConfig());
        column_config_overrides.add(getUsedQuantityUnitColumnConfig());
        column_config_overrides.add(getIdentityColumnConfig(locale));
        column_config_overrides.add(getCADSynchronizedColumnConfig(locale));
        column_config_overrides.add(getBuildStatusColumnConfig(locale));
        column_config_overrides.add(getSourcingStatusColumnConfig(locale));
        column_config_overrides.add(getGeneralStatusColumnConfig());
        column_config_overrides.add(getCostStatusColumnConfig());
        List<String> complianceSpecAttributeIdList = getEnvironmentalComplianceSpecAttributeIdList();
    	for (String attributeId : complianceSpecAttributeIdList) {
    		column_config_overrides.add(getEnvironmentalComplianceStatusColumnConfig(attributeId));
    	}
        column_config_overrides.add(getAllocationStatusColumnConfig());
        return column_config_overrides;
    }

    private List<String> getEnvironmentalComplianceSpecAttributeIdList() {
    	List<String> list = new ArrayList<String>();
    	list.add("rohs_consumer");
    	list.add("rohs_infrastructure");
    	list.add("rohs_server");
    	list.add("jig_a");
    	list.add("jig_b");
    	list.add("jig_101_regulated");
    	list.add("jig_101_for_information_only");
    	list.add("reach_svhc_candidate");
    	list.add("reach_svhc_authorization");
    	list.add("reach_svhc_restricted");
    	list.add("reach_svhc_suspect");
    	list.add("gadsl_and_imds_app_prohibited");
    	list.add("elv_automotive");
    	return list;
    }
    
    private DefaultColumnConfig getNameColumnConfig()
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(JSPropertyDataConstants.NAME_JSID);
        column_config.setWidth(180);
        return column_config;
    }

     private DefaultColumnConfig getOrganizationColumnConfig(Locale locale)
     {
         DefaultColumnConfig column_config = new DefaultColumnConfig();
         column_config.setId(AbstractPartStructureTableView.ORGANIZATION);
         column_config.setLabel(WTMessage.getLocalizedMessage(RESOURCE, columnLabelsRB.ORGANIZATION, new String[0], locale));
         column_config.setWidth(100);
         return column_config;
     }

    private DefaultColumnConfig getCheckOutStatusColumnConfig()
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.CHECKOUT_STATUS);
        column_config.setLabel(" ");
        column_config.setRendererType("com.ptc.cat.ui.client.cellrenderer.IconCellRenderer");
        column_config.setWidth(15);
        return column_config;
    }

    private DefaultColumnConfig getSharedStatusColumnConfig()
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.SHARED_STATUS);
        column_config.setLabel(" ");
        column_config.setRendererType(JSONOBJECT_CELL_RENDERER);
        column_config.setWidth(15);
        return column_config;
    }

 /*private DefaultColumnConfig getNoTestatusColumnConfig()
   // {
     //   DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.NOTE_STATUS);
        column_config.setLabel(" ");
        column_config.setRendererType("com.ptc.cat.ui.client.cellrenderer.IconCellRenderer");
        column_config.setWidth(15);
        return column_config;
    }*/

    private DefaultColumnConfig getReplacementStatusColumnConfig()
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.REPLACEMENT_STATUS);
        column_config.setLabel(" ");
        column_config.setRendererType("com.ptc.cat.ui.client.cellrenderer.IconCellRenderer");
        column_config.setWidth(15);
        return column_config;
    }

    private DefaultColumnConfig getChangeStatusColumnConfig()
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.CHANGE_STATUS);
        column_config.setLabel(" ");
        column_config.setRendererType("com.ptc.cat.ui.client.cellrenderer.IconCellRenderer");
        column_config.setWidth(15);
        return column_config;
    }

    private DefaultColumnConfig getGeneralStatusColumnConfig() {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.SERVER_STATUS);
        column_config.setRendererType(JSONOBJECT_CELL_RENDERER);
        column_config.setLabel(" ");
        column_config.setWidth(15);
        return column_config;
    }

    private DefaultColumnConfig getCostStatusColumnConfig() {
    	DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId("CostVerdict");
        column_config.setRendererType(JSONOBJECT_CELL_RENDERER);
        column_config.setWidth(90);
        return column_config;
    }
    
    private DefaultColumnConfig getEnvironmentalComplianceStatusColumnConfig(String attributeId) {
    	DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(attributeId);
        column_config.setRendererType(JSONOBJECT_CELL_RENDERER);
        column_config.setWidth(90);
        return column_config;
    }

    private DefaultColumnConfig getAllocationStatusColumnConfig() {
    	DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId("AllocationStatus");
        column_config.setRendererType(JSONOBJECT_CELL_RENDERER);
        column_config.setWidth(90);
        return column_config;
    }
    
    private DefaultColumnConfig getNumberColumnConfig()
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(JSPropertyDataConstants.NUMBER_JSID);
        column_config.setWidth(200);
        return column_config;
    }

    private DefaultColumnConfig getVersionColumnConfig()
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.VERSION);
        column_config.setWidth(100);
        return column_config;
    }

    private DefaultColumnConfig getReferenceDesignatorColumnConfig(Locale locale)
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.REFERENCE_DESIGNATOR_RANGE);
        column_config.setLabel(WTMessage.getLocalizedMessage(RESOURCE, columnLabelsRB.REFERENCE_DESIGNATOR, new String[0],
                locale));
        column_config.setWidth(120);
        return column_config;
    }

    private DefaultColumnConfig getUsageQuantityColumnConfig(Locale locale) {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.USAGE_QUANTITY);
        column_config.setWidth(40);
        return column_config;
    }

    private DefaultColumnConfig getUsedQuantityAmountColumnConfig()
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.QUANTITY_AMOUNT);
        column_config.setWidth(40);
        return column_config;
    }

    private DefaultColumnConfig getUsedQuantityUnitColumnConfig()
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.QUANTITY_UNIT);
        column_config.setWidth(40);
        return column_config;
    }

    private DefaultColumnConfig getIdentityColumnConfig(Locale locale)
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.IDENTITY);
        column_config.setLabel(WTMessage.getLocalizedMessage(RESOURCE, columnLabelsRB.IDENTITY, null, locale));
        column_config.setWidth(350);
        return column_config;
    }

    private DefaultColumnConfig getSourcingStatusColumnConfig(Locale locale)
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId("oemPreference");
        column_config.setLabel(WTMessage.getLocalizedMessage(RESOURCE, columnLabelsRB.SOURCING_STATUS, null, locale));
        column_config.setRendererType("com.ptc.windchill.enterprise.part.psb.client.cellrenderer.SourcingStatusCellRenderer");
        column_config.setWidth(85);
        return column_config;
    }

    private DefaultColumnConfig getCADSynchronizedColumnConfig(Locale locale)
    {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId("cadSynchronized");
        column_config.setLabel(WTMessage.getLocalizedMessage(RESOURCE, columnLabelsRB.CAD_SYNCHRONIED, null, locale));
        column_config.setRendererType("com.ptc.cat.ui.client.cellrenderer.BuildStatusCellRenderer");
        column_config.setWidth(80);
        return column_config;
    }

    private DefaultColumnConfig getBuildStatusColumnConfig(Locale locale) {
        DefaultColumnConfig column_config = new DefaultColumnConfig();
        column_config.setId(AbstractPartStructureTableView.BUILD_STATUS);
        column_config.setLabel(WTMessage.getLocalizedMessage(RESOURCE, columnLabelsRB.CAD_SYNCHRONIED, null, locale));
        column_config.setRendererType("com.ptc.cat.ui.client.cellrenderer.BuildStatusCellRenderer");
        column_config.setWidth(80);
        return column_config;
    }

    @Override
    protected boolean useAdvancedToolbar() {
        return true;
    }

    @Override
    protected List<ActionConfig> getToolbarConfigOverrides(Locale locale) {
        DefaultActionConfig[] actionConfigs = {
                // Editing group
                new DefaultActionConfig("editingGroupGWT", ButtonType.GROUP, null, null, 2),
                new DefaultActionConfig("insertExistingPartStructureGWT", ButtonType.BUTTON, null, ButtonScale.SMALL),
                new DefaultActionConfig("removeGWT", ButtonType.BUTTON, null, ButtonScale.SMALL),
                new DefaultActionConfig("insertNewPartStructureSplitGWT", ButtonType.SPLIT, null, ButtonScale.SMALL),
                new DefaultActionConfig("insertNewPartStructureGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("insertMultiNewPartStructureGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("editSplitGWT", ButtonType.SPLIT, null, ButtonScale.SMALL),
                new DefaultActionConfig("editGWT", ButtonType.MENUITEM, null, ButtonScale.SMALL),
                new DefaultActionConfig("editCommonAttrsGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("editAttrValuesUsageLinksGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("editUsageLinkGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("renameGWT", ButtonType.MENUITEM, null, null),

                // Check Out/In group
                new DefaultActionConfig("checkInOutGroupGWT", ButtonType.GROUP, null, null, 2),
                new DefaultActionConfig("checkoutGWT", ButtonType.BUTTON, null, null),
                new DefaultActionConfig("undocheckoutGWT", ButtonType.BUTTON, null, ButtonScale.SMALL),
                new DefaultActionConfig("findCheckoutItemsGWT", ButtonType.BUTTON, null, null),
                new DefaultActionConfig("checkinGWT", ButtonType.BUTTON, null, ButtonScale.SMALL),

                // Clip board group
                new DefaultActionConfig("clipboardGroupGWT", ButtonType.GROUP, null, null, 2),
                new DefaultActionConfig("pasteFromClipboardToTreeWithLargeIconGWT", ButtonType.SPLIT, null,
                        ButtonScale.MEDIUM),
                new DefaultActionConfig("pasteFromClipboardToTreeGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("pasteSelectToTreeGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("copyToWindchillWithLargeIconGWT", ButtonType.BUTTON, null, ButtonScale.MEDIUM),

                // Viewing group
                new DefaultActionConfig("viewingGroupGWT", ButtonType.GROUP, null, null, 2),
                new DefaultActionConfig("relatedObjectsWithLargeIconGWT", ButtonType.BUTTON, null, ButtonScale.MEDIUM),
                new DefaultActionConfig("configurableTableViewGWT", ButtonType.MENU, null, ButtonScale.SMALL),
                new DefaultActionConfig("configurableTableViewListGWT", ButtonType.CHECK, "table_view", null),
                new DefaultActionConfig("configurableTableViewManagerGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("sourcingContextGWT", ButtonType.SUBMENU, null, null),
                new DefaultActionConfig("sourcingContextListGWT", ButtonType.SUBCHECK, "sourcing_context", null),
                new DefaultActionConfig("findSourcingContextGWT", ButtonType.SUBMENUITEM, null, null),
                new DefaultActionConfig("displayGWT", ButtonType.MENU, null, ButtonScale.SMALL),
                new DefaultActionConfig("displayOccurrencesGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("selectAllGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("expandAllLevelsGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("collapseAllLevelsGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("viewLayout2Panel", ButtonType.CHECK, "layout", null),
                new DefaultActionConfig("viewLayout3Panel", ButtonType.CHECK, "layout", null),
                new DefaultActionConfig("refreshGWT", ButtonType.MENUITEM, null, null),

                // New/Add To group
                new DefaultActionConfig("newAddToGroupGWT", ButtonType.GROUP, null, null, 3),
                new DefaultActionConfig("newActionsGWT", ButtonType.MENU, null, ButtonScale.MEDIUM),
                new DefaultActionConfig("saveAsGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig ("newPARGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("newPartConfigGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("newRepresentationGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("addToActionsGWT", ButtonType.MENU, null, ButtonScale.MEDIUM),
                new DefaultActionConfig("addToBaselineGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("addToPackageGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("SBAddToPrj", ButtonType.MENUITEM, null, null),

                // Filter group
                new DefaultActionConfig("filterGroupGWT", ButtonType.GROUP, null, null, 2),
                new DefaultActionConfig("editFilterWithLargeIconGWT", ButtonType.BUTTON, null, ButtonScale.MEDIUM),
                new DefaultActionConfig("filterPropertiesGWT", ButtonType.BUTTON, null, ButtonScale.SMALL),
                new DefaultActionConfig("savedFiltersGWT", ButtonType.CUSTOMMENU, null, ButtonScale.SMALL),

                // Tools group
                new DefaultActionConfig("toolsGroupGWT", ButtonType.GROUP, null, null, 2),
                new DefaultActionConfig("compareActionsGWT", ButtonType.MENU, null, ButtonScale.MEDIUM),
                new DefaultActionConfig("launchStructureCompareGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("launchCompareToCADGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("openActionsGWT", ButtonType.MENU, null, ButtonScale.MEDIUM),
                new DefaultActionConfig("openInProductViewGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("openInPSEGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("openInMPSEGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("openInSPSEGWT", ButtonType.MENUITEM, null, null),

                // Reports group
                new DefaultActionConfig("psbReportsGroupGWT", ButtonType.GROUP, null, null, 3),
                new DefaultActionConfig("psbReportsGWT", ButtonType.MENU, null, ButtonScale.MEDIUM),
                new DefaultActionConfig("psbReportMultiLevelCompListGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("psbReportSingleLevelConsolBOMGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("psbReportSingleLevelBOMGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("psbReportSingleLevelBOMWithNotesGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("psbReportMultiLevelBOMGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("psbReportMultiLevelBOMWithReplacementsGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("psbReportMultiLevelBOMWithAMLAVLGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("psbReportMultiLevelBOMWithAMLGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("singleSourceOEMPartsGWT", ButtonType.MENUITEM, null, null),
                new DefaultActionConfig("uniqueManufacturerPartsGWT", ButtonType.MENUITEM, null, null),
                
                
                
                //报表
                //by sq begin
                new DefaultActionConfig("customTZCReportsGWT", ButtonType.MENU, null, ButtonScale.MEDIUM),
                new DefaultActionConfig("psbReportMultiLevelBOMBZJMXB", ButtonType.MENUITEM,null,null),//////标准件明细表
                new DefaultActionConfig("psbReportMultiLevelBOMWGJMXB", ButtonType.MENUITEM,null,null),//////外购件明细表
                new DefaultActionConfig("psbReportMultiLevelBOMFZMX", ButtonType.MENUITEM,null,null),//////分装明细表
				new DefaultActionConfig("psbReportMultiLevelCAPPBOM", ButtonType.MENUITEM,null,null),//////CAPP报表
				
                //by lindw begin
                new DefaultActionConfig("tanCustGroupGWT", ButtonType.GROUP, null, null, 1),
                new DefaultActionConfig("tanReportGWT", ButtonType.MENU, null, ButtonScale.MEDIUM),
                new DefaultActionConfig("exportStandardPartSummary", ButtonType.MENUITEM,null,null),//////标准件明细表
                new DefaultActionConfig("exportProductDetailsSummary", ButtonType.MENUITEM,null,null),//////整车明细表
                new DefaultActionConfig("exportOutsourcingPartSummary", ButtonType.MENUITEM,null,null),//////外购件明细表
                new DefaultActionConfig("exportGroupingDetailsSummary", ButtonType.MENUITEM,null,null),//////分组明细表
                new DefaultActionConfig("exportGroupingDetailsSummaryTW", ButtonType.MENUITEM,null,null),//////分组明细表（三维）
                new DefaultActionConfig("psbReportMultiLevelCAPPBOM", ButtonType.MENUITEM,null,null),//////CAPP报表
                new DefaultActionConfig("exportGroupingDetailsSpecialSummary", ButtonType.MENUITEM,null,null),//////分组明细表(特殊结构)
        };

    	List<ActionConfig> column_config_overrides = new ArrayList<ActionConfig>(actionConfigs.length);
        for (int i = 0; i < actionConfigs.length; i++) {
            column_config_overrides.add(actionConfigs[i]);
        }
        return column_config_overrides;
    }

    public RelatedObjsConfig buildRelatedObjsConfig() {
        final String DISPLAY_CONSTANT_DOCUMENTS = "Documents";
        final String DISPLAY_CONSTANT_CAD_DOCUMENTS = "CAD Documents";
        final String DISPLAY_CONSTANT_REPLACEMENTS = "Replacement Parts";
        final String DISPLAY_CONSTANT_REQUIREMENTS = "Requirements";
        final String DISPLAY_CONSTANT_DISTRIBUTIONS = "Distribution Targets";

        boolean bESIInstalled = InstalledProperties.isInstalled( InstalledProperties.ESI );
        boolean bREQLInstalled = InstalledProperties.isInstalled( InstalledProperties.REQL);

        Locale locale = null;
        try {
            locale = SessionHelper.getLocale();
        } catch (WTException e) {
            Log.warning(getClass().getName() + ".buildComponentConfig() missing locale");
        }
        DefaultRelatedObjsConfig relatedConfig = new DefaultRelatedObjsConfig();

        relatedConfig.appendDisplayConstant(DISPLAY_CONSTANT_DOCUMENTS);
        relatedConfig.appendDisplayConstant(DISPLAY_CONSTANT_CAD_DOCUMENTS);
        relatedConfig.appendDisplayConstant(DISPLAY_CONSTANT_REPLACEMENTS);
        if (bREQLInstalled) {
        	// TODO turn off until related requirements is ready relatedConfig.appendDisplayConstant(DISPLAY_CONSTANT_REQUIREMENTS);
        }
        if (bESIInstalled) {
        	relatedConfig.appendDisplayConstant(DISPLAY_CONSTANT_DISTRIBUTIONS);
        }

        String relatedObjSource = relatedObjLabelsRB.class.getName();

        String title = WTMessage.getLocalizedMessage(relatedObjSource, relatedObjLabelsRB.TITLE, null, locale);
        relatedConfig.setDisplayLabel(RelatedObjsConfig.DISPLAY_CONSTANT_TITLE, title);

        String manage = WTMessage.getLocalizedMessage(relatedObjSource, relatedObjLabelsRB.MANAGE, null, locale);
        relatedConfig.setDisplayLabel(RelatedObjsConfig.DISPLAY_CONSTANT_MANAGE, manage);

        String instruction = WTMessage.getLocalizedMessage(relatedObjSource, relatedObjLabelsRB.INSTRUCTION, null, locale);
        relatedConfig.setDisplayLabel(RelatedObjsConfig.DISPLAY_CONSTANT_INSTRUCTION, instruction);

        String show = WTMessage.getLocalizedMessage(relatedObjSource, relatedObjLabelsRB.SHOW, null, locale);
        relatedConfig.setDisplayLabel(RelatedObjsConfig.DISPLAY_CONSTANT_SHOW, show);

        String information = WTMessage.getLocalizedMessage(relatedObjSource, relatedObjLabelsRB.INFORMATION, null, locale);
        relatedConfig.setDisplayLabel(RelatedObjsConfig.DISPLAY_CONSTANT_INFORMATION, information);

        String documents = WTMessage.getLocalizedMessage(relatedObjSource, relatedObjLabelsRB.DOCUMENTS, null, locale);
        relatedConfig.setDisplayLabel(DISPLAY_CONSTANT_DOCUMENTS, documents);

        String caddocs = WTMessage.getLocalizedMessage(relatedObjSource, relatedObjLabelsRB.CAD_DOCUMENTS, null, locale);
        relatedConfig.setDisplayLabel(DISPLAY_CONSTANT_CAD_DOCUMENTS, caddocs);

        String replacements = WTMessage.getLocalizedMessage(relatedObjSource, relatedObjLabelsRB.REPLACEMENTS, null, locale);
        relatedConfig.setDisplayLabel(DISPLAY_CONSTANT_REPLACEMENTS, replacements);

        String requirements = WTMessage.getLocalizedMessage(relatedObjSource, relatedObjLabelsRB.REQUIREMENTS, null, locale);
        relatedConfig.setDisplayLabel(DISPLAY_CONSTANT_REQUIREMENTS, requirements);

        String distributions = WTMessage.getLocalizedMessage(relatedObjSource, relatedObjLabelsRB.DESTRIBUTIONS, null, locale);
        relatedConfig.setDisplayLabel(DISPLAY_CONSTANT_DISTRIBUTIONS, distributions);

        // Documents
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_DOCUMENTS,
                PartReferenceDocsParams.PART_REFERENCE_DOCS_NAV_CONSTANT);
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_DOCUMENTS,
                PartDescribeDocsParams.PART_DESCRIBE_DOCS_NAV_CONSTANT);
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_DOCUMENTS,
                DocumentGroupParams.GROUP_DOCUMENT_NAV_CONSTANT);
        // CAD Documents
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_CAD_DOCUMENTS,
        		NavConstants.FILTERED_PART_BUILD_RULE_EPMDOCS);
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_CAD_DOCUMENTS,
                PartDescribeCADDocsParams.PART_DESCRIBE_CAD_DOCS_NAV_CONSTANT);
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_CAD_DOCUMENTS,
        		NavConstants.FILTERED_PART_BUILD_HISTORY_EPMDOCS);
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_CAD_DOCUMENTS,
                CADDocumentGroupParams.GROUP_CAD_DOCUMENT_NAV_CONSTANT);
        // Replacement Parts
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_REPLACEMENTS,
                NavConstants.PART_SUBSTITUTE_PARTS_NAV_CONSTANT);
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_REPLACEMENTS,
                NavConstants.PART_ALTERNATE_PARTS_NAV_CONSTANT);
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_REPLACEMENTS,
                ReplacementPartGroupParams.GROUP_REPLACEMENT_PART_NAV_CONSTANT);
        // Requirements
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_REQUIREMENTS,
                PartDownStreamTraceParams.PART_DOWN_TRACE_NAV_CONSTANT);
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_REQUIREMENTS,
                PartUpStreamTraceParams.PART_UP_TRACE_NAV_CONSTANT);
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_REQUIREMENTS,
                PartDownStreamAllocationParams.PART_DOWN_ALLOCATION_NAV_CONSTANT);
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_REQUIREMENTS,
                PartUpStreamAllocationParams.PART_UP_ALLOCATION_NAV_CONSTANT);
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_REQUIREMENTS,
                RequirementGroupParams.GROUP_REQUIREMENT_NAV_CONSTANT);
        // Distribution Targets
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_DISTRIBUTIONS,
                PartDistributionTargetsParams.PART_DISTRIBUTION_TARGETS_NAV_CONSTANT);
        relatedConfig.setDisplayToDefinition(DISPLAY_CONSTANT_DISTRIBUTIONS,
                DistributionTargetGroupParams.GROUP_DISTRIBUTION_TARGET_NAV_CONSTANT);

        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_DOCUMENTS,
                DocumentGroupParams.GROUP_DOCUMENT_NAV_CONSTANT);
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_DOCUMENTS,
                PartReferenceDocsParams.LINK_TYPE_ID.getTypename());
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_DOCUMENTS,
                PartDescribeDocsParams.LINK_TYPE_ID.getTypename());

        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_CAD_DOCUMENTS,
                CADDocumentGroupParams.GROUP_CAD_DOCUMENT_NAV_CONSTANT);
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_CAD_DOCUMENTS,
                PartBuildCADDocsParams.LINK_TYPE_ID.getTypename());
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_CAD_DOCUMENTS,
                PartDescribeCADDocsParams.LINK_TYPE_ID.getTypename());
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_CAD_DOCUMENTS,
                PartCADDocsParams.LINK_TYPE_ID.getTypename());

        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_REPLACEMENTS,
                ReplacementPartGroupParams.GROUP_REPLACEMENT_PART_NAV_CONSTANT);
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_REPLACEMENTS,
                RootReplacementPartGroupParams.GROUP_ROOT_REPLACEMENT_PART_NAV_CONSTANT);
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_REPLACEMENTS,
                PartAlternatePartsParams.LINK_TYPE_ID.getTypename());
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_REPLACEMENTS,
                PartSubstitutePartsParams.LINK_TYPE_ID.getTypename());


        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_REPLACEMENTS,
                RequirementGroupParams.GROUP_REQUIREMENT_NAV_CONSTANT);
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_REQUIREMENTS,
                PartDownStreamTraceParams.LINK_TYPE_ID.getTypename());
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_REQUIREMENTS,
                PartUpStreamTraceParams.LINK_TYPE_ID.getTypename());
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_REQUIREMENTS,
                PartDownStreamAllocationParams.LINK_TYPE_ID.getTypename());
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_REQUIREMENTS,
                PartUpStreamAllocationParams.LINK_TYPE_ID.getTypename());

        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_DISTRIBUTIONS,
                DistributionTargetGroupParams.GROUP_DISTRIBUTION_TARGET_NAV_CONSTANT);
        relatedConfig.setDisplayToLinkTypeName(DISPLAY_CONSTANT_DISTRIBUTIONS,
                PartDistributionTargetsParams.LINK_TYPE_ID.getTypename());

        return relatedConfig;
    }

    @Override
    protected Set<AttributeConfig> getAdditionalAttributeConfigsToGetMetaDataFor() {
        Set<AttributeConfig> attribute_config_set = new HashSet<AttributeConfig>();
        DefaultAttributeConfig ro_ac = new DefaultAttributeConfig();
        ro_ac.setAttribute(PLMEntity.OCCURRENCE_DESIGNATOR);
        ro_ac.setObjectType("wt.part.PartUsesOccurrence");
        attribute_config_set.add(ro_ac);
        DefaultAttributeConfig oq_ac = new DefaultAttributeConfig();
        oq_ac.setAttribute(PLMEntity.OCCURRENCE_QUANTITY_AMOUNT);
        oq_ac.setObjectType("wt.part.PartUsesOccurrence");
        attribute_config_set.add(oq_ac);
        DefaultAttributeConfig obs_ac = new DefaultAttributeConfig();
        obs_ac.setAttribute(PLMEntity.OCCURRENCE_BUILD_STATUS);
        obs_ac.setObjectType("wt.part.PartUsesOccurrence");
        attribute_config_set.add(obs_ac);
        return attribute_config_set;
    }

} // end class
