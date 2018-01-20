package com.yisi.stiku.db.gen;


import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.DefaultJavaFormatter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.JavaVisibility;
import org.mybatis.generator.api.dom.java.Method;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.Element;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.springframework.util.StringUtils;

import com.ujigu.secure.common.utils.BaseConfig;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Sean on 14-2-25.
 */
public class CodeGenPlug extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }
    
    /**
     * 该方法不执行，不知道怎么回事
     */
    @Override
    public boolean modelExampleClassGenerated(TopLevelClass topLevelClass,
    		IntrospectedTable introspectedTable) {
    	Field defaultSeriField = new Field("serialVersionUID", new FullyQualifiedJavaType("long"));
    	defaultSeriField.setVisibility(JavaVisibility.PRIVATE);
    	defaultSeriField.setStatic(true);
    	defaultSeriField.setFinal(true);
    	defaultSeriField.setInitializationString("1L");
    	
    	topLevelClass.addField(defaultSeriField);
    	
    	return true;
    }
    
    @Override
    public boolean sqlMapBaseColumnListElementGenerated(XmlElement element,
    		IntrospectedTable introspectedTable) {
    	element.getAttributes().set(0, new Attribute("id", "BaseColumnList"));
    	return true;
    }
    
    @Override
    public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element,
    		IntrospectedTable introspectedTable) {
        element.getAttributes().set(0, new Attribute("id", "findByPK"));
    	
    	XmlElement includeElem = (XmlElement)element.getElements().get(1);
    	includeElem.getAttributes().set(0, new Attribute("refid", "BaseColumnList"));
    	
    	return true;
    }
    
    @Override
    public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element,
    		IntrospectedTable introspectedTable) {
    	element.getAttributes().set(0, new Attribute("id", "deleteByPK"));
    	return true;
    }
    
    @Override
    public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element,
    		IntrospectedTable introspectedTable) {
    	return false;
    }
    
    @Override
    public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(
    		XmlElement element, IntrospectedTable introspectedTable) {
    	element.getAttributes().set(0, new Attribute("id", "updateByPK"));
    	return true;
    }
    
    @Override
    public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(
    		XmlElement element, IntrospectedTable introspectedTable) {
    	return false;
    }
    
    @Override
    public boolean sqlMapDocumentGenerated(Document document,
                                           IntrospectedTable introspectedTable) {
        Attribute namespace = new Attribute("namespace", document.getRootElement().getAttributes().get(0).getValue().replace("Mapper", "Dao"));
        document.getRootElement().getAttributes().remove(0);
        document.getRootElement().addAttribute(namespace);

        XmlElement insertRetPkElem = getInsertRetPkElem(document, introspectedTable);
        XmlElement insertBatchElem = getInsertBatchElem(introspectedTable);
        XmlElement findByPKInMasterElem = getFindByPKInMaster(introspectedTable);
        XmlElement pageConfitionElem = getPageConditionElem();
        XmlElement cntByPageElem = getCntByPageElem(introspectedTable);
        XmlElement findByPageElem = getFindByPageElem(introspectedTable, "findByPage", buildPageCondition());
        XmlElement findAllElem = getFindByPageElem(introspectedTable, "findAll", buildOrderBySql());
        XmlElement findByEntityElem = getFindByPageElem(introspectedTable, "findByEntity", buildEntityCondition(introspectedTable));
        
        
        document.getRootElement().addElement(insertRetPkElem);
        document.getRootElement().addElement(insertBatchElem);
        document.getRootElement().addElement(findByPKInMasterElem);
        document.getRootElement().addElement(cntByPageElem);
        document.getRootElement().addElement(findAllElem);
        document.getRootElement().addElement(findByEntityElem);
        document.getRootElement().addElement(findByPageElem);
        document.getRootElement().addElement(pageConfitionElem);
        
        return true;
    }
    
    private XmlElement getPageConditionElem(){
    	XmlElement pageConfitionElem = new XmlElement("sql");
    	pageConfitionElem.addAttribute(new Attribute("id", "pageCondition"));
    	
    	XmlElement whereElem = new XmlElement("where");
    	XmlElement trimElem = new XmlElement("trim");
    	trimElem.addAttribute(new Attribute("suffixOverrides", "and"));
    	
    	XmlElement ifElem = new XmlElement("if");
    	ifElem.addAttribute(new Attribute("test", "entity != null"));
    	
    	TextElement textElem = new TextElement("\t\t<!-- 写where的判断条件,下边是样例\n" +
	              "\t\t\t<if test=\"entity.nick != null\">\n" + 
	                 "\t\t\t\tand nick = #{entity.nick,jdbcType=VARCHAR}\n" +
	              "\t\t\t</if>\n" +
	              "\t\t\t<if test=\"entity.company != null\">\n" +
	                 "\t\t\t\tand company = '${entity.nick}'\n" +
	              "\t\t\t</if>\n" +
            "\t\t-->");
    	
    	TextElement dynamicSqlElem = new TextElement("<if test=\"dynamicSql != null\">\n"
    			+ "\t\t\tand ${dynamicSql}\n"
    			+ "\t\t</if>");
    	
    	ifElem.addElement(textElem);
    	trimElem.addElement(ifElem);
    	trimElem.addElement(dynamicSqlElem);
    	whereElem.addElement(trimElem);
    	pageConfitionElem.addElement(whereElem);
    	
    	return pageConfitionElem;
    }
    
    private XmlElement getCntByPageElem(IntrospectedTable introspectedTable){
    	XmlElement cntByPageElem = new XmlElement("select");
    	cntByPageElem.addAttribute(new Attribute("id", "getCntByPage"));
    	cntByPageElem.addAttribute(new Attribute("resultType", "java.lang.Integer"));
    	
    	TextElement textElem = new TextElement("select \n" +
    			                                "\t\t<if test=\"forceMaster\">\n" +
    			                                "\t\t\t<![CDATA[/*master*/]]>\n" +
    		                                    "\t\t</if>\n" +
											      "\t\tcount(1)\n" +
											    "\tfrom\n\t\t" + 
											      introspectedTable.getTableConfiguration().getTableName() +
											     "\n\t<include refid=\"pageCondition\" />");
    	
    	cntByPageElem.addElement(textElem);
    	
    	return cntByPageElem;
    }
    
    private XmlElement getInsertRetPkElem(Document document, IntrospectedTable introspectedTable){
    	XmlElement insertRetPkElem = new XmlElement("insert");
    	insertRetPkElem.addAttribute(new Attribute("id", "insertReturnPK"));
    	insertRetPkElem.addAttribute(new Attribute("useGeneratedKeys", "true"));
    	insertRetPkElem.addAttribute(new Attribute("keyProperty", getPrimaryKeyPropName(introspectedTable)));
    	insertRetPkElem.addAttribute(new Attribute("parameterType", introspectedTable.getBaseRecordType()));
    	
    	List<Element> elemList = document.getRootElement().getElements();
    	for(Element elem : elemList){
    		if(elem instanceof XmlElement){
    			XmlElement xmlElem = (XmlElement)elem;
    			if("insert".equals(xmlElem.getAttributes().get(0).getValue())){
    				for(Element textElem : xmlElem.getElements()){
    					insertRetPkElem.addElement(textElem);
    				}
    			}
    		}
    	}
    	
    	return insertRetPkElem;
    }
    
    private XmlElement getInsertBatchElem(IntrospectedTable introspectedTable){
    	XmlElement insertRetPkElem = new XmlElement("insert");
    	insertRetPkElem.addAttribute(new Attribute("id", "insertBatch"));
    	insertRetPkElem.addAttribute(new Attribute("parameterType", "java.util.List"));
    	
    	StringBuilder builder = new StringBuilder("insert into ");
    	builder.append(introspectedTable.getTableConfiguration().getTableName());
    	builder.append("\n\t\t(");
    	
    	StringBuilder valBuidler = new StringBuilder();
    	List<IntrospectedColumn> columnList = introspectedTable.getAllColumns();
    	for(IntrospectedColumn column : columnList){
    		builder.append(column.getActualColumnName());
    		builder.append(",");
    		
    		valBuidler.append("#{item." + column.getJavaProperty() + ",jdbcType=" + column.getJdbcTypeName() + "}");
    		valBuidler.append(",");
    	}
    	if(builder.toString().endsWith(",")){
    		builder.setLength(builder.length() - 1);
    		
    		valBuidler.setLength(valBuidler.length() - 1);
    	}
    	builder.append(")\n");
    	
    	builder.append("\tvalues \n");
    	
    	builder.append("\t<foreach collection=\"list\" item=\"item\" separator=\",\" index=\"index\">\n");
    	builder.append("\t\t(");
    	builder.append(valBuidler.toString());
    	builder.append(")\n");
    	builder.append("\t</foreach>");
    	
    	insertRetPkElem.addElement(new TextElement(builder.toString()));
    	
    	return insertRetPkElem;
    }
    
    private XmlElement getFindByPKInMaster(IntrospectedTable introspectedTable){
    	XmlElement findByPKInMasterElem = new XmlElement("select");
    	findByPKInMasterElem.addAttribute(new Attribute("id", "findByPK_inMaster"));
    	findByPKInMasterElem.addAttribute(new Attribute("resultMap", "BaseResultMap"));
    	findByPKInMasterElem.addAttribute(new Attribute("parameterType", "java.util.Map"));
    	
    	String content = "select\n"
    			+ "\t\t<if test=\"forceMaster\">\n"
    			+ "\t\t\t<![CDATA[/*master*/]]>\n"
    			+ "\t\t</if>\n"
    			+ "\t\t<include refid=\"BaseColumnList\"/>\n"
    			+ "\tfrom " + introspectedTable.getTableConfiguration().getTableName()
    			+ "\twhere " + getPrimaryKeyColumnName(introspectedTable) + " = #{pk, jdbcType=" + getPrimaryKeyColumnType(introspectedTable) + "}"; 
    	findByPKInMasterElem.addElement(new TextElement(content));
    	
    	return findByPKInMasterElem;
    }
    
    private XmlElement getFindByPageElem(IntrospectedTable introspectedTable, String elemId, String whereSql){
    	XmlElement findByPageElem = new XmlElement("select");
    	findByPageElem.addAttribute(new Attribute("id", elemId));
    	findByPageElem.addAttribute(new Attribute("resultMap", "BaseResultMap"));
    	
    	String content = "select\n"
    			+ "\t\t<if test=\"forceMaster\">\n"
    			+ "\t\t\t<![CDATA[/*master*/]]>\n"
    			+ "\t\t</if>\n"
    			+ "\t\t<include refid=\"BaseColumnList\"/>\n"
    			+ "\tfrom " + introspectedTable.getTableConfiguration().getTableName();
    	if(!StringUtils.isEmpty(whereSql)){
    		content += whereSql;
    	}
    	findByPageElem.addElement(new TextElement(content));
    	
    	return findByPageElem;
    }
    
    private String buildPageCondition(){
    	return "\n" +
  	          "\t\t<include refid=\"pageCondition\"/>\n" + 
  			  "\t\t<if test=\"pageInfo != null and pageInfo.orderBySql != null\">\n" +
			         "\t\t\t${pageInfo.orderBySql}\n" +
			      "\t\t</if>\n" +
			      "\t\t<if test=\"pageInfo !=null\">\n" +
			         "\t\t\tlimit ${pageInfo.startIndex}, ${pageInfo.pagesize}\n" +
			      "\t\t</if>";
    }
    
    private String buildOrderBySql(){
    	return "\n"
    			+ "\t\t<if test=\"orderBySql != null\">\n"
    			+ "\t\t\t${orderBySql}\n"
    			+ "\t\t</if>";
    }
    
    private String buildEntityCondition(IntrospectedTable introspectedTable){
    	StringBuilder builder = new StringBuilder("\n\t<where>\n");
    	builder.append("\t\t<trim suffixOverrides=\"and\" >\n");
    	
    	List<IntrospectedColumn> columns = introspectedTable.getAllColumns();
    	if(columns != null && !columns.isEmpty()){
    		for(IntrospectedColumn column : columns){
    			builder.append("\t\t\t<if test=\"entity."+column.getJavaProperty()+" != null\" >\n");
    			builder.append("\t\t\t\tand "+column.getActualColumnName()+" = #{entity."+column.getJavaProperty()+",jdbcType="+column.getJdbcTypeName()+"}\n");
    			builder.append("\t\t\t</if>\n");
    		}
    	}
        builder.append("\t\t</trim>\n");
        builder.append("\t</where>\n");
        builder.append("\t\t<if test=\"orderBySql != null\">\n");
        builder.append("\t\t\t${orderBySql}\n");
        builder.append("\t\t</if>");
        		
        return builder.toString();
    }

    @Override
    public boolean clientSelectByPrimaryKeyMethodGenerated(Method method,
    		Interface interfaze, IntrospectedTable introspectedTable) {
    	return false;
    }
    
    @Override
    public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method,
    		Interface interfaze, IntrospectedTable introspectedTable) {
    	return false;
    }
    
    @Override
    public boolean clientInsertSelectiveMethodGenerated(Method method,
    		Interface interfaze, IntrospectedTable introspectedTable) {
    	return false;
    }
    
    @Override
    public boolean clientInsertMethodGenerated(Method method,
    		Interface interfaze, IntrospectedTable introspectedTable) {
    	return false;
    }
    
    @Override
    public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(
    		Method method, Interface interfaze,
    		IntrospectedTable introspectedTable) {
    	return false;
    }
    
    @Override
    public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(
    		Method method, Interface interfaze,
    		IntrospectedTable introspectedTable) {
    	return false;
    }
    
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass,
    		IntrospectedTable introspectedTable) {
    	String baseEntityName = BaseConfig.getValue("base.entity.full.path");
    	topLevelClass.setSuperClass(new FullyQualifiedJavaType(baseEntityName + "<" + getPrimaryKeyType(introspectedTable) + ">"));
    	
    	Method method = new Method("getPK");
    	method.setVisibility(JavaVisibility.PUBLIC);
    	method.addAnnotation("@Override");
    	method.setReturnType(new FullyQualifiedJavaType(getPrimaryKeyType(introspectedTable)));
    	method.addBodyLine("return " + getPrimaryKeyPropName(introspectedTable) + ";");
    	topLevelClass.addMethod(method);
    	
    	topLevelClass.addImportedType(new FullyQualifiedJavaType(baseEntityName));
    	
    	return true;
    }
    
    @Override
    public boolean clientGenerated(Interface interfaze,
                                   TopLevelClass topLevelClass,
                                   IntrospectedTable introspectedTable) {
    	String baseDaoName = BaseConfig.getValue("base.dao.full.path");
    	interfaze.addAnnotation("@Repository");
    	interfaze.addSuperInterface(new FullyQualifiedJavaType(baseDaoName + "<" + getPrimaryKeyType(introspectedTable) + "," + introspectedTable.getBaseRecordType() + ">"));
    	interfaze.addImportedType(new FullyQualifiedJavaType(baseDaoName));
    	interfaze.addImportedType(new FullyQualifiedJavaType(introspectedTable.getBaseRecordType()));
    	interfaze.addImportedType(new FullyQualifiedJavaType("org.springframework.stereotype.Repository"));
    	
        return true;
    }
    
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(
    		IntrospectedTable introspectedTable) {
    	
    	String baseDaoName =  BaseConfig.getValue("base.dao.full.path");
    	String baseDaoImplName = BaseConfig.getValue("base.dao.impl.full.path");
    	String baseServiceImplName = BaseConfig.getValue("base.service.impl.full.path");
    	String pkType = getPrimaryKeyType(introspectedTable);
    	String entityName = introspectedTable.getBaseRecordType();
    	FullyQualifiedJavaType javaType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());
    	
    	TopLevelClass daoImplClass = buildOverideBody(javaType, baseDaoImplName, baseDaoName, "getProxyBaseDao", pkType, entityName, "Dao", "", "Repository");
    	
    	TopLevelClass serviceImplClass = buildOverideBody(javaType, baseServiceImplName, baseDaoImplName, "getBaseDaoImpl", pkType, entityName, "Service", "Impl", "Service");
    	
//    	TopLevelClass serviceImplClass = new TopLevelClass(javaRootPkg + ".service.impl." + javaType.getShortName() + "ServiceImpl");
//    	serviceImplClass.setVisibility(JavaVisibility.PUBLIC);
//    	serviceImplClass.addAnnotation("@Service");
//    	serviceImplClass.setSuperClass(baseServiceImplName + "<" + getPrimaryKeyType(introspectedTable) + "," + introspectedTable.getBaseRecordType() + ">");
//    	serviceImplClass.addImportedType("org.springframework.stereotype.Service");
//    	serviceImplClass.addImportedType(baseServiceImplName);
    	
    	GeneratedJavaFile daoImplFile = new GeneratedJavaFile(daoImplClass, BaseConfig.getValue("source.file.path"), new DefaultJavaFormatter());
    	GeneratedJavaFile serviceImplFile = new GeneratedJavaFile(serviceImplClass, BaseConfig.getValue("source.file.path"), new DefaultJavaFormatter());
    	
    	return Arrays.asList(daoImplFile, serviceImplFile);
    }

//    @Override
//    public boolean modelFieldGenerated(Field field,
//    		TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
//    		IntrospectedTable introspectedTable, ModelClassType modelClassType) {
//    	
//    	return true;
//    }
    
    private TopLevelClass buildOverideBody(FullyQualifiedJavaType javaType, String baseClzzImplName, String returnType, String methodName, String pkType, String entityName, String classSuffix, String secondSuffix, String annotationName){
    	String baseClzzImplNameWithGeneric = baseClzzImplName + "<" + pkType + "," + entityName + ">";
    	String javaRootPkg = BaseConfig.getValue("java.package.rootpath");
    	
    	TopLevelClass daoImplClass = new TopLevelClass(javaRootPkg + "."+classSuffix.toLowerCase()+".impl." + javaType.getShortName() + classSuffix + ("Dao".equals(classSuffix) ? "Impl" : ""));
    	daoImplClass.setVisibility(JavaVisibility.PUBLIC);
    	daoImplClass.addAnnotation("@" + annotationName);
    	
    	String daoName = javaRootPkg + ".dao." + (StringUtils.isEmpty(secondSuffix) ? "" : lowerFirstChar(secondSuffix) + ".") + javaType.getShortName() + "Dao" + secondSuffix;
    	String fieldName = lowerFirstChar(javaType.getShortName()) + "Dao" + secondSuffix;
    	Field field = new Field(fieldName, new FullyQualifiedJavaType(daoName));
    	field.setVisibility(JavaVisibility.PRIVATE);
    	field.addAnnotation("@Resource");
    	
    	Method method = new Method(methodName);
    	method.setVisibility(JavaVisibility.PROTECTED);
    	method.addAnnotation("@Override");
    	method.addBodyLine("return " + fieldName + ";");
    	
    	method.setReturnType(new FullyQualifiedJavaType(returnType +  "<" + pkType + "," + entityName + ">"));
    	
    	daoImplClass.addField(field);
    	daoImplClass.addMethod(method);
    	daoImplClass.setSuperClass(baseClzzImplNameWithGeneric);
    	daoImplClass.addImportedType("org.springframework.stereotype."+annotationName);
    	daoImplClass.addImportedType("javax.annotation.Resource");
    	daoImplClass.addImportedType(baseClzzImplName);
    	daoImplClass.addImportedType(returnType);
    	daoImplClass.addImportedType(daoName);
    	daoImplClass.addImportedType(entityName);
    	
    	return daoImplClass;
    }
    
    private String getPrimaryKeyType(IntrospectedTable introspectedTable){
    	List<IntrospectedColumn> columns = introspectedTable.getPrimaryKeyColumns();
    	String type = "";
    	if(columns != null && !columns.isEmpty()){
    		for(IntrospectedColumn column : columns){
    			type += column.getFullyQualifiedJavaType().getFullyQualifiedName();
    		}
    	}
    	
    	return type;
    }
    
    private String getPrimaryKeyColumnType(IntrospectedTable introspectedTable){
    	List<IntrospectedColumn> columns = introspectedTable.getPrimaryKeyColumns();
    	String columnType = "";
    	if(columns != null && !columns.isEmpty()){
    		for(IntrospectedColumn column : columns){
    			columnType += column.getJdbcTypeName();
    		}
    	}
    	
    	return columnType;
    }
    
    private String getPrimaryKeyColumnName(IntrospectedTable introspectedTable){
    	List<IntrospectedColumn> columns = introspectedTable.getPrimaryKeyColumns();
    	String columnName = "";
    	if(columns != null && !columns.isEmpty()){
    		for(IntrospectedColumn column : columns){
    			columnName += column.getActualColumnName();
    		}
    	}
    	
    	return columnName;
    }
    
    private String getPrimaryKeyPropName(IntrospectedTable introspectedTable){
    	List<IntrospectedColumn> columns = introspectedTable.getPrimaryKeyColumns();
    	String propName = "";
    	if(columns != null && !columns.isEmpty()){
    		for(IntrospectedColumn column : columns){
    			propName += column.getJavaProperty();
    		}
    	}
    	
    	return StringUtils.isEmpty(propName) ? "id" : propName;
    }
    
    private String lowerFirstChar(String str){
    	String firstChar = String.valueOf(str.charAt(0));
    	
    	return firstChar.toLowerCase() + str.substring(1);
    }
}
