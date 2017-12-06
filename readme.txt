
1、初次导入基础数据:
windchill wt.load.LoadFileSet -file %WT_HOME%/loadFiles/ext/loadSet.xml -Unattended -NoServerStop -u wcadmin -p wcadmin 
2、更新基础数据
windchill wt.load.LoadFromFile -d %WT_HOME%/loadFiles/ext/XX/XX.xml -u wcadmin -p wcadmin -CONT_PATH \"/wt.inf.container.OrgContainer=XXX"