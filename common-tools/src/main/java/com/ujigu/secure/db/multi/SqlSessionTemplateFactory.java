package com.ujigu.secure.db.multi;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.ujigu.secure.common.exception.BaseRuntimeException;

@Component
public class SqlSessionTemplateFactory implements ApplicationContextAware {

	private final Map<String, SqlSessionTemplate> templateMap = new HashMap<String, SqlSessionTemplate>();

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {

		Map<String, SqlSessionTemplate> tmplMap = applicationContext.getBeansOfType(SqlSessionTemplate.class);

		if (tmplMap != null) {
			templateMap.putAll(tmplMap);
		}
	}

	public SqlSessionTemplate getSqlSessionTemplate(String daoClzzName) {

		if (templateMap.isEmpty()) {
			throw new BaseRuntimeException("NO_BEAN_EXIST", "系统异常",
					"There should at least one SqlSessionTemplate defined in spring for mybatis");
		}

		Set<Entry<String, SqlSessionTemplate>> entries = templateMap.entrySet();
		if (templateMap.size() == 1) {
			return entries.iterator().next().getValue();
		}

		SqlSessionTemplate sqlSessionTemplate = templateMap.get(daoClzzName);
		if (sqlSessionTemplate == null) {
			for (Entry<String, SqlSessionTemplate> entry : entries) {
				String dsKey = DSKeyHolder.parseDsKey(daoClzzName);
				if (entry.getKey().equals(dsKey + "SqlSessionTemplate") || entry.getKey().equals(dsKey)
						|| daoClzzName.startsWith(entry.getKey())) {
					sqlSessionTemplate = entry.getValue();
					break;
				}
			}

			if (sqlSessionTemplate == null) {
				sqlSessionTemplate = templateMap.get("sqlSessionTemplate");
			}
		}

		return sqlSessionTemplate;
	}

}
